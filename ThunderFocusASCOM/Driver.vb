'tabs=4
' --------------------------------------------------------------------------------'
' ASCOM Focuser driver for ThunderFocus
'
' Description:	ThunderFocus ASCOM socket-based driver.
'
' Implements:	ASCOM Focuser interface version: 1.0
' Author:		(MRC) Marco Cipriani <marco.cipriani.01@gmail.com>
'
' Edit Log:
'
' Date			Who	Vers	Description
' -----------	---	-----	-------------------------------------------------------
' 07-DEC-2020	MRC	1.0.0	Initial edit, from Focuser template
' ---------------------------------------------------------------------------------
'
'
' Your driver's ID is ASCOM.ThunderFocus.Focuser
'
' The Guid attribute sets the CLSID for ASCOM.DeviceName.Focuser
' The ClassInterface/None attribute prevents an empty interface called
' _Focuser from being created and used as the [default] interface
'

' This definition is used to select code that's only applicable for one device type
#Const Device = "Focuser"

Imports System.Net
Imports System.Net.Sockets
Imports System.Threading
Imports ASCOM.Astrometry
Imports ASCOM.Astrometry.AstroUtils
Imports ASCOM.DeviceInterface
Imports ASCOM.Utilities

<Guid("863670cf-a7a6-4314-9479-c99e1c6fce06")>
<ClassInterface(ClassInterfaceType.None)>
Public Class Focuser

    ' The Guid attribute sets the CLSID for ASCOM.ThunderFocus.Focuser
    ' The ClassInterface/None attribute prevents an empty interface called
    ' _ThunderFocus from being created and used as the [default] interface

    ' TODO Replace the not implemented exceptions with code to implement the function or
    ' throw the appropriate ASCOM exception.
    '
    Implements IFocuserV3

    '
    ' Driver ID and descriptive string that shows in the Chooser
    '
    Friend Shared driverID As String = "ASCOM.ThunderFocus.Focuser"
    Private Shared driverDescription As String = "ThunderFocus"

    Friend Shared socketPortProfileName As String = "Net Port"
    Friend Shared traceStateProfileName As String = "Trace Level"
    Friend Shared socketPortDefault As Integer = 5001
    Friend Shared traceStateDefault As String = "False"

    Friend Shared socketPort As Integer
    Friend Shared traceState As Boolean

    Private connectedState As Boolean
    Private utilities As Util
    Private astroUtilities As AstroUtils
    Private TL As TraceLogger

    Private socket As Socket
    Private ipAddress As IPAddress


    Private Function readSocket() As String
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

    Private Sub sendSocket(msg As String)
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
            .Enabled = traceState
        }
        TL.LogMessage("Focuser", "Starting initialisation")

        connectedState = False ' Initialise connected to false
        utilities = New Util() ' Initialise util object
        astroUtilities = New AstroUtils 'Initialise new astro utilities object

        Application.EnableVisualStyles()
        Dim ipHostInfo As IPHostEntry = Dns.GetHostEntry(Dns.GetHostName())
        ipAddress = ipHostInfo.AddressList(0)
        socket = New Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp)
        socket.NoDelay = True
        socket.ReceiveTimeout = 500
        socket.SendTimeout = 1000

        TL.LogMessage("Focuser", "Completed initialisation")
    End Sub

    '
    ' PUBLIC COM INTERFACE IFocuserV3 IMPLEMENTATION
    '

#Region "Common properties and methods"
    ''' <summary>
    ''' Displays the Setup Dialog form.
    ''' If the user clicks the OK button to dismiss the form, then
    ''' the new settings are saved, otherwise the old values are reloaded.
    ''' THIS IS THE ONLY PLACE WHERE SHOWING USER INTERFACE IS ALLOWED!
    ''' </summary>
    Public Sub SetupDialog() Implements IFocuserV3.SetupDialog
        Application.EnableVisualStyles()
        If IsConnected Then
            MessageBox.Show("Ponte in esecuzione, utilizzare il pannello di controllo per la configurazione.", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Information)
        Else
            Using F As SetupDialogForm = New SetupDialogForm()
                Dim result As DialogResult = F.ShowDialog()
                If result = DialogResult.OK Then
                    WriteProfile()
                End If
            End Using
        End If
    End Sub

    Public ReadOnly Property SupportedActions() As ArrayList Implements IFocuserV3.SupportedActions
        Get
            TL.LogMessage("SupportedActions Get", "Returning empty arraylist")
            Return New ArrayList()
        End Get
    End Property

    Public Function Action(ByVal ActionName As String, ByVal ActionParameters As String) As String Implements IFocuserV3.Action
        Throw New ActionNotImplementedException("Action " & ActionName & " is not supported by this driver")
    End Function

    Public Sub CommandBlind(ByVal Command As String, Optional ByVal Raw As Boolean = False) Implements IFocuserV3.CommandBlind
        CheckConnected("CommandBlind")
        Throw New MethodNotImplementedException("CommandBlind")
    End Sub

    Public Function CommandBool(ByVal Command As String, Optional ByVal Raw As Boolean = False) As Boolean _
        Implements IFocuserV3.CommandBool
        CheckConnected("CommandBool")
        Throw New MethodNotImplementedException("CommandBool")
    End Function

    Public Function CommandString(ByVal Command As String, Optional ByVal Raw As Boolean = False) As String _
        Implements IFocuserV3.CommandString
        CheckConnected("CommandString")
        Throw New MethodNotImplementedException("CommandString")
    End Function

    Public Property Connected() As Boolean Implements IFocuserV3.Connected
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
                TL.LogMessage("Connected Set", "Connecting to port " + socketPort.ToString())
                Try
                    Dim remoteEP As IPEndPoint = New IPEndPoint(ipAddress, socketPort)
                    socket.Connect(remoteEP)
                    Dim bytesToSend As Byte() = Encoding.UTF8.GetBytes("ThunderFocusPing" + Environment.NewLine)
                    socket.SendBufferSize = bytesToSend.Length
                    socket.Send(bytesToSend)
                    Thread.Sleep(200)
                    Dim socketBuffer As Byte() = New Byte(1023) {}
                    Dim bytesRec As Integer = socket.Receive(socketBuffer)
                    Dim rcv As String = Encoding.ASCII.GetString(socketBuffer, 0, bytesRec)
                    If rcv.Contains("ThunderFocusPingOK") Then
                        connectedState = True
                        TL.LogMessage("Connected Set", "Ping OK.")
                    Else
                        connectedState = False
                        TL.LogMessage("Connected Set", "Ping not OK!")
                    End If
                Catch ex As Exception
                    TL.LogMessage("Connected Set", "Connection exception! " + ex.Message)
                    connectedState = False
                End Try
            Else
                TL.LogMessage("Connected Set", "Disconnecting from port " + socketPort.ToString())
                Try
                    socket.Shutdown(SocketShutdown.Both)
                    socket.Close()
                    connectedState = False
                Catch ex As Exception
                    TL.LogMessage("Connected Set", "Disconnection exception! " + ex.Message)
                End Try
            End If
        End Set
    End Property

    Public ReadOnly Property Description As String Implements IFocuserV3.Description
        Get
            Dim d As String = driverDescription
            TL.LogMessage("Description Get", d)
            Return d
        End Get
    End Property

    Public ReadOnly Property DriverInfo As String Implements IFocuserV3.DriverInfo
        Get
            Dim m_version As Version = System.Reflection.Assembly.GetExecutingAssembly().GetName().Version
            Dim s_driverInfo As String = "ThunderFocus ASCOM bridge v" + m_version.Major.ToString() + "." + m_version.Minor.ToString()
            TL.LogMessage("DriverInfo Get", s_driverInfo)
            Return s_driverInfo
        End Get
    End Property

    Public ReadOnly Property DriverVersion() As String Implements IFocuserV3.DriverVersion
        Get
            TL.LogMessage("DriverVersion Get", Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2))
            Return Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2)
        End Get
    End Property

    Public ReadOnly Property InterfaceVersion() As Short Implements IFocuserV3.InterfaceVersion
        Get
            TL.LogMessage("InterfaceVersion Get", "3")
            Return 3
        End Get
    End Property

    Public ReadOnly Property Name As String Implements IFocuserV3.Name
        Get
            Dim s_name As String = "ThunderFocus"
            TL.LogMessage("Name Get", s_name)
            Return s_name
        End Get
    End Property

    Public Sub Dispose() Implements IFocuserV3.Dispose
        TL.LogMessage("Dispose", "Disposing...")
        Connected = False
        TL.Enabled = False
        TL.Dispose()
        TL = Nothing
        utilities.Dispose()
        utilities = Nothing
        astroUtilities.Dispose()
        astroUtilities = Nothing
    End Sub

#End Region

#Region "IFocuser Implementation"

    Private focuserPosition As Integer = 0
    Private focuserSteps As Integer = 32767
    Private moving As Boolean = False

    Public ReadOnly Property Absolute() As Boolean Implements IFocuserV3.Absolute
        Get
            TL.LogMessage("Absolute Get", True.ToString())
            Return True ' This is an absolute focuser
        End Get
    End Property

    Public Sub Halt() Implements IFocuserV3.Halt
        CheckConnected("Attemped halt while disconnected!")
        sendSocket("Halt")
    End Sub

    Public ReadOnly Property IsMoving() As Boolean Implements IFocuserV3.IsMoving
        Get
            CheckConnected("Attemped get while disconnected!")
            sendSocket("IsMoving")
            Dim rcv As String = readSocket()
            If Not String.IsNullOrEmpty(rcv) Then
                moving = rcv.Contains("true")
            End If
            TL.LogMessage("IsMoving Get", moving.ToString())
            Return moving
        End Get
    End Property

    Public Property Link() As Boolean Implements IFocuserV3.Link
        Get
            TL.LogMessage("Link Get", Me.Connected.ToString())
            Return Connected
        End Get
        Set(value As Boolean)
            TL.LogMessage("Link Set", value.ToString())
            Connected = value
        End Set
    End Property

    Public ReadOnly Property MaxIncrement() As Integer Implements IFocuserV3.MaxIncrement
        Get
            Return MaxStep()
        End Get
    End Property

    Public ReadOnly Property MaxStep() As Integer Implements IFocuserV3.MaxStep
        Get
            CheckConnected("Attemped get while disconnected!")
            sendSocket("MaxStep")
            Dim rcv As String = readSocket()
            If Not String.IsNullOrEmpty(rcv) Then
                focuserSteps = Integer.Parse(rcv)
            End If
            TL.LogMessage("MaxStep Get", focuserSteps.ToString())
            Return focuserSteps
        End Get
    End Property

    Public Sub Move(Position As Integer) Implements IFocuserV3.Move
        CheckConnected("Attemped Move while disconnected!")
        TL.LogMessage("Move", Position.ToString())
        focuserPosition = Position ' Set the focuser position
        sendSocket("Move=" + Position.ToString())
    End Sub

    Public ReadOnly Property Position() As Integer Implements IFocuserV3.Position
        Get
            CheckConnected("Attemped get while disconnected!")
            sendSocket("Position")
            Dim rcv As String = readSocket()
            If Not String.IsNullOrEmpty(rcv) Then
                focuserPosition = Integer.Parse(rcv)
            End If
            Return focuserPosition
        End Get
    End Property

    Public ReadOnly Property StepSize() As Double Implements IFocuserV3.StepSize
        Get
            TL.LogMessage("StepSize Get", "Not implemented")
            Throw New PropertyNotImplementedException("StepSize", False)
        End Get
    End Property

    Public Property TempComp() As Boolean Implements IFocuserV3.TempComp
        Get
            TL.LogMessage("TempComp Get", False.ToString())
            Return False
        End Get
        Set(value As Boolean)
            TL.LogMessage("TempComp Set", "Not implemented")
            Throw New PropertyNotImplementedException("TempComp", True)
        End Set
    End Property

    Public ReadOnly Property TempCompAvailable() As Boolean Implements IFocuserV3.TempCompAvailable
        Get
            TL.LogMessage("TempCompAvailable Get", False.ToString())
            Return False ' Temperature compensation is not available in this driver
        End Get
    End Property

    Public ReadOnly Property Temperature() As Double Implements IFocuserV3.Temperature
        Get
            TL.LogMessage("Temperature Get", "Not implemented")
            Throw New PropertyNotImplementedException("Temperature", False)
        End Get
    End Property

#End Region

#Region "Private properties and methods"
    ' here are some useful properties and methods that can be used as required
    ' to help with

#Region "ASCOM Registration"

    Private Shared Sub RegUnregASCOM(ByVal bRegister As Boolean)
        Using P As New Profile() With {.DeviceType = "Focuser"}
            If bRegister Then
                P.Register(driverID, driverDescription)
            Else
                P.Unregister(driverID)
            End If
        End Using
    End Sub

    <ComRegisterFunction()>
    Public Shared Sub RegisterASCOM(T As Type)
        RegUnregASCOM(True)
    End Sub

    <ComUnregisterFunction()>
    Public Shared Sub UnregisterASCOM(T As Type)
        RegUnregASCOM(False)
    End Sub

#End Region

    ''' <summary>
    ''' Returns true if there is a valid connection to the driver hardware
    ''' </summary>
    Private ReadOnly Property IsConnected As Boolean
        Get
            Return connectedState
        End Get
    End Property

    ''' <summary>
    ''' Use this function to throw an exception if we aren't connected to the hardware
    ''' </summary>
    ''' <param name="message"></param>
    Private Sub CheckConnected(message As String)
        If Not IsConnected Then
            Throw New NotConnectedException(message)
        End If
    End Sub

    ''' <summary>
    ''' Read the device configuration from the ASCOM Profile store
    ''' </summary>
    Friend Sub ReadProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "Focuser"
            traceState = Convert.ToBoolean(driverProfile.GetValue(driverID, traceStateProfileName, String.Empty, traceStateDefault))
            socketPort = Integer.Parse(driverProfile.GetValue(driverID, socketPortProfileName, String.Empty, socketPortDefault))
        End Using
    End Sub

    ''' <summary>
    ''' Write the device configuration to the  ASCOM  Profile store
    ''' </summary>
    Friend Sub WriteProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "Focuser"
            driverProfile.WriteValue(driverID, traceStateProfileName, traceState.ToString())
            driverProfile.WriteValue(driverID, socketPortProfileName, socketPort.ToString())
        End Using

    End Sub

#End Region

End Class