'tabs=4
' --------------------------------------------------------------------------------'
' ASCOM CoverCalibrator driver for ThunderFocus
'
' Description:	ASCOM CoverCalibrator driver for ThunderFocus
'
' Implements:	ASCOM Focuser interface version: 1.0
' Author:		(MRC) Marco Cipriani <marco.cipriani.01@gmail.com>
'
' Edit Log:
'
' Date			Who	Vers	Description
' -----------	---	-----	-------------------------------------------------------
' 07-DEC-2020	MRC	1.0.0	Initial edit
' ---------------------------------------------------------------------------------
'
' Your driver's ID is ASCOM.ThunderFocus.CoverCalibrator
'
#Const Device = "CoverCalibrator"

Imports System.Net
Imports System.Net.Sockets
Imports System.Threading
Imports ASCOM.DeviceInterface
Imports ASCOM.Utilities

<Guid("79e4943f-dc9c-4336-8a82-be3056198549")>
<ClassInterface(ClassInterfaceType.None)>
Public Class CoverCalibrator
    Implements ICoverCalibratorV1

    '
    ' Driver ID and descriptive string that shows in the Chooser
    '
    Friend Shared driverID As String = "ASCOM.ThunderFocus.CoverCalibrator"
    Private Shared ReadOnly driverDescription As String = "ThunderFocus CoverCalibrator"

    Friend Shared socketPortProfileName As String = "Socket port"
    Friend Shared socketPortDefault As Integer = 5001

    Friend Shared debugProfileName As String = "Debug"
    Friend Shared debugDefault As String = "False"

    Friend Shared socketPort As Integer
    Friend Shared debug As Boolean

    Private connectedState As Boolean
    Private TL As TraceLogger

    Private ReadOnly socket As New Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp) With {
            .NoDelay = True,
            .ReceiveTimeout = 500,
            .SendTimeout = 1000
        }
    Private ReadOnly ipAddress As IPAddress

    Private Function ReadSocket() As String
        If Connected = True Then
            Try
                Dim socketBuffer As Byte() = New Byte(1023) {}
                Dim bytesRec As Integer = socket.Receive(socketBuffer)
                Dim rcv As String = Encoding.ASCII.GetString(socketBuffer, 0, bytesRec)
                TL.LogMessage("ReadSocket", rcv)
                Return rcv
            Catch ex As Exception
                TL.LogMessage("ReadSocket", ex.Message)
                Connected = False
            End Try
        Else
            TL.LogMessage("ReadSocket", "Ignoring read attempt.")
        End If
        Return String.Empty
    End Function

    Private Sub SendSocket(msg As String)
        If Connected = True Then
            Try
                TL.LogMessage("Connected Set", "Sending " + msg)
                Dim bytesToSend As Byte() = Encoding.UTF8.GetBytes(msg + Environment.NewLine)
                socket.SendBufferSize = bytesToSend.Length
                socket.Send(bytesToSend)
            Catch ex As Exception
                TL.LogMessage("SendSocket", ex.Message)
                Connected = False
            End Try
        Else
            TL.LogMessage("ReadSocket", "Ignoring send attempt (" + msg + ")")
        End If
    End Sub

    '
    ' Constructor - Must be public for COM registration!
    '
    Public Sub New()
        ReadProfile() ' Read device configuration from the ASCOM Profile store
        TL = New TraceLogger("", "ThunderFocus") With {
            .Enabled = debug
        }
        TL.LogMessage("CoverCalibrator", "Starting initialisation")
        connectedState = False
        Application.EnableVisualStyles()
        Dim ipHostInfo As IPHostEntry = Dns.GetHostEntry(Dns.GetHostName())
        ipAddress = ipHostInfo.AddressList(0)
        TL.LogMessage("CoverCalibrator", "Completed initialisation")
    End Sub

    '
    ' PUBLIC COM INTERFACE ICoverCalibratorV1 IMPLEMENTATION
    '

#Region "Common properties and methods"
    ''' <summary>
    ''' Displays the Setup Dialog form.
    ''' If the user clicks the OK button to dismiss the form, then
    ''' the new settings are saved, otherwise the old values are reloaded.
    ''' THIS IS THE ONLY PLACE WHERE SHOWING USER INTERFACE IS ALLOWED!
    ''' </summary>
    Public Sub SetupDialog() Implements ICoverCalibratorV1.SetupDialog
        Application.EnableVisualStyles()
        If IsConnected Then
            MessageBox.Show("ASCOM bridge running, use the control panel to configure the flat panel.", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Information)
        Else
            Using F As New SetupDialogForm()
                Dim result As DialogResult = F.ShowDialog()
                If result = DialogResult.OK Then
                    WriteProfile()
                End If
            End Using
        End If
    End Sub

    Public ReadOnly Property SupportedActions() As ArrayList Implements ICoverCalibratorV1.SupportedActions
        Get
            TL.LogMessage("SupportedActions Get", "Returning empty arraylist")
            Return New ArrayList()
        End Get
    End Property

    Public Function Action(ActionName As String, ActionParameters As String) As String Implements ICoverCalibratorV1.Action
        Throw New ActionNotImplementedException("Action " & ActionName & " is not supported by this driver")
    End Function

    Public Sub CommandBlind(Command As String, Optional Raw As Boolean = False) Implements ICoverCalibratorV1.CommandBlind
        CheckConnected("CommandBlind")
        Throw New MethodNotImplementedException("CommandBlind")
    End Sub

    Public Function CommandBool(Command As String, Optional Raw As Boolean = False) As Boolean _
        Implements ICoverCalibratorV1.CommandBool
        CheckConnected("CommandBool")
        Throw New MethodNotImplementedException("CommandBool")
    End Function

    Public Function CommandString(Command As String, Optional Raw As Boolean = False) As String _
        Implements ICoverCalibratorV1.CommandString
        CheckConnected("CommandString")
        Throw New MethodNotImplementedException("CommandString")
    End Function

    Public Property Connected() As Boolean Implements ICoverCalibratorV1.Connected
        Get
            TL.LogMessage("Connected Get", IsConnected.ToString())
            Return IsConnected
        End Get
        Set(value As Boolean)
            TL.LogMessage("Connected Set", value.ToString())
            If value = IsConnected Then
                Return
            End If

            If value Then
                connectedState = True
                TL.LogMessage("Connected Set", "Connecting to port " + comPort)
                ' TODO connect to the device
            Else
                connectedState = False
                TL.LogMessage("Connected Set", "Disconnecting from port " + comPort)
                ' TODO disconnect from the device
            End If
        End Set
    End Property

    Public ReadOnly Property Description As String Implements ICoverCalibratorV1.Description
        Get
            ' this pattern seems to be needed to allow a public property to return a private field
            Dim d As String = driverDescription
            TL.LogMessage("Description Get", d)
            Return d
        End Get
    End Property

    Public ReadOnly Property DriverInfo As String Implements ICoverCalibratorV1.DriverInfo
        Get
            Dim m_version As Version = System.Reflection.Assembly.GetExecutingAssembly().GetName().Version
            ' TODO customise this driver description
            Dim s_driverInfo As String = "Information about the driver itself. Version: " + m_version.Major.ToString() + "." + m_version.Minor.ToString()
            TL.LogMessage("DriverInfo Get", s_driverInfo)
            Return s_driverInfo
        End Get
    End Property

    Public ReadOnly Property DriverVersion() As String Implements ICoverCalibratorV1.DriverVersion
        Get
            ' Get our own assembly and report its version number
            TL.LogMessage("DriverVersion Get", Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2))
            Return Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2)
        End Get
    End Property

    Public ReadOnly Property InterfaceVersion() As Short Implements ICoverCalibratorV1.InterfaceVersion
        Get
            TL.LogMessage("InterfaceVersion Get", "1")
            Return 1
        End Get
    End Property

    Public ReadOnly Property Name As String Implements ICoverCalibratorV1.Name
        Get
            Dim s_name As String = "Short driver name - please customise"
            TL.LogMessage("Name Get", s_name)
            Return s_name
        End Get
    End Property

    Public Sub Dispose() Implements ICoverCalibratorV1.Dispose
        ' Clean up the trace logger and util objects
        TL.Enabled = False
        TL.Dispose()
        TL = Nothing
        utilities.Dispose()
        utilities = Nothing
        astroUtilities.Dispose()
        astroUtilities = Nothing
    End Sub

#End Region

#Region "ICoverCalibrator Implementation"

    ''' <summary>
    ''' Returns the state of the device cover, if present, otherwise returns "NotPresent"
    ''' </summary>
    Public ReadOnly Property CoverState() As CoverStatus Implements ICoverCalibratorV1.CoverState
        Get
            TL.LogMessage("CoverState Get", "Not implemented")
            Throw New ASCOM.PropertyNotImplementedException("CoverState", False)
        End Get
    End Property

    ''' <summary>
    ''' Initiates cover opening if a cover is present
    ''' </summary>
    Public Sub OpenCover() Implements ICoverCalibratorV1.OpenCover
        TL.LogMessage("OpenCover", "Not implemented")
        Throw New ASCOM.MethodNotImplementedException("OpenCover")
    End Sub

    ''' <summary>
    ''' Initiates cover closing if a cover is present
    ''' </summary>
    Public Sub CloseCover() Implements ICoverCalibratorV1.CloseCover
        TL.LogMessage("CloseCover", "Not implemented")
        Throw New ASCOM.MethodNotImplementedException("CloseCover")
    End Sub

    ''' <summary>
    ''' Stops any cover movement that may be in progress if a cover is present and cover movement can be interrupted.
    ''' </summary>
    Public Sub HaltCover() Implements ICoverCalibratorV1.HaltCover
        TL.LogMessage("HaltCover", "Not implemented")
        Throw New ASCOM.MethodNotImplementedException("HaltCover")
    End Sub

    ''' <summary>
    ''' Returns the state of the calibration device, if present, otherwise returns "NotPresent"
    ''' </summary>
    Public ReadOnly Property CalibratorState() As CalibratorStatus Implements ICoverCalibratorV1.CalibratorState
        Get
            TL.LogMessage("CalibratorState Get", "Not implemented")
            Throw New ASCOM.PropertyNotImplementedException("CalibratorState", False)
        End Get
    End Property

    ''' <summary>
    ''' Returns the current calibrator brightness in the range 0 (completely off) to <see cref="MaxBrightness"/> (fully on)
    ''' </summary>
    Public ReadOnly Property Brightness As Integer Implements ICoverCalibratorV1.Brightness
        Get
            TL.LogMessage("Brightness Get", "Not implemented")
            Throw New ASCOM.PropertyNotImplementedException("Brightness", False)
        End Get
    End Property

    ''' <summary>
    ''' The Brightness value that makes the calibrator deliver its maximum illumination.
    ''' </summary>
    Public ReadOnly Property MaxBrightness As Integer Implements ICoverCalibratorV1.MaxBrightness
        Get
            TL.LogMessage("MaxBrightness Get", "Not implemented")
            Throw New ASCOM.PropertyNotImplementedException("MaxBrightness", False)
        End Get
    End Property

    ''' <summary>
    ''' Turns the calibrator on at the specified brightness if the device has calibration capability
    ''' </summary>
    ''' <param name="Brightness"></param>
    Public Sub CalibratorOn(Brightness As Integer) Implements ICoverCalibratorV1.CalibratorOn
        TL.LogMessage("CalibratorOn", $"Not implemented. Value set: {Brightness}")
        Throw New ASCOM.MethodNotImplementedException("CalibratorOn")
    End Sub

    ''' <summary>
    ''' Turns the calibrator off if the device has calibration capability
    ''' </summary>
    Public Sub CalibratorOff() Implements ICoverCalibratorV1.CalibratorOff
        TL.LogMessage("CalibratorOff", "Not implemented")
        Throw New ASCOM.MethodNotImplementedException("CalibratorOff")
    End Sub

#End Region

#Region "Private properties and methods"
    ' here are some useful properties and methods that can be used as required
    ' to help with

#Region "ASCOM Registration"

    Private Shared Sub RegUnregASCOM(ByVal bRegister As Boolean)

        Using P As New Profile() With {.DeviceType = "CoverCalibrator"}
            If bRegister Then
                P.Register(driverID, driverDescription)
            Else
                P.Unregister(driverID)
            End If
        End Using

    End Sub

    <ComRegisterFunction()>
    Public Shared Sub RegisterASCOM(ByVal T As Type)

        RegUnregASCOM(True)

    End Sub

    <ComUnregisterFunction()>
    Public Shared Sub UnregisterASCOM(ByVal T As Type)

        RegUnregASCOM(False)

    End Sub

#End Region

    ''' <summary>
    ''' Returns true if there is a valid connection to the driver hardware
    ''' </summary>
    Private ReadOnly Property IsConnected As Boolean
        Get
            ' TODO check that the driver hardware connection exists and is connected to the hardware
            Return connectedState
        End Get
    End Property

    ''' <summary>
    ''' Use this function to throw an exception if we aren't connected to the hardware
    ''' </summary>
    ''' <param name="message"></param>
    Private Sub CheckConnected(ByVal message As String)
        If Not IsConnected Then
            Throw New NotConnectedException(message)
        End If
    End Sub

    ''' <summary>
    ''' Read the device configuration from the ASCOM Profile store
    ''' </summary>
    Friend Sub ReadProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "CoverCalibrator"
            traceState = Convert.ToBoolean(driverProfile.GetValue(driverID, traceStateProfileName, String.Empty, traceStateDefault))
            comPort = driverProfile.GetValue(driverID, comPortProfileName, String.Empty, comPortDefault)
        End Using
    End Sub

    ''' <summary>
    ''' Write the device configuration to the  ASCOM  Profile store
    ''' </summary>
    Friend Sub WriteProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "CoverCalibrator"
            driverProfile.WriteValue(driverID, traceStateProfileName, traceState.ToString())
            driverProfile.WriteValue(driverID, comPortProfileName, comPort.ToString())
        End Using

    End Sub

#End Region

End Class
