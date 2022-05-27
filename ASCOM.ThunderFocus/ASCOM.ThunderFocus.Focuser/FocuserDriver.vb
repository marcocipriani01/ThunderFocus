'tabs=4
' --------------------------------------------------------------------------------'
' ASCOM Focuser driver for ThunderFocus
'
' Description:	ASCOM Focuser driver for ThunderFocus
'
' Implements:	ASCOM Focuser interface version: 3.0
' Author:		(MRC) Marco Cipriani <marco.cipriani.01@gmail.com>
'
' Edit Log:
'
' Date			Who	Vers	Description
' -----------	---	-----	-------------------------------------------------------
' 07-DEC-2020	MRC	1.0.0	Initial edit, from Focuser template
' 26-MAY-2022	MRC	2.0.0	Update
' ---------------------------------------------------------------------------------
'
' Your driver's ID is ASCOM.ThunderFocus.Focuser
'
#Const Device = "Focuser"

Option Strict On
Option Infer On

Imports System.Net
Imports System.Net.Sockets
Imports System.Threading
Imports ASCOM.DeviceInterface
Imports ASCOM.Utilities

<Guid("863670cf-a7a6-4314-9479-c99e1c6fce06")>
<ClassInterface(ClassInterfaceType.None)>
Public Class Focuser
    Implements IFocuserV3

    '
    ' Driver ID and descriptive string that shows in the Chooser
    '
    Friend Shared driverID As String = "ASCOM.ThunderFocus.Focuser"
    Private Shared ReadOnly driverDescription As String = "ThunderFocus Focuser"

    Friend Shared socketPortProfileName As String = "Socket port"
    Friend Shared socketPortDefault As String = "5001"

    Friend Shared debugProfileName As String = "Debug"
    Friend Shared debugDefault As String = "False"

    Friend Shared socketPort As Integer = 5001
    Friend Shared debug As Boolean = False

    Private connectedState As Boolean = False
    Private TL As TraceLogger

    Private ReadOnly socket As Socket
    Private ReadOnly ipAddress As IPAddress

    Private Function SocketRead() As String
        If Connected = False Then
            Throw New DriverException("Not connected!")
        End If
        Try
            Dim socketBuffer As Byte() = New Byte(1023) {}
            Dim bytesRec As Integer = socket.Receive(socketBuffer)
            Dim rcv As String = Encoding.ASCII.GetString(socketBuffer, 0, bytesRec).Replace("\n", "").Replace("\r", "").Trim()
            TL.LogMessage("ReadSocket", rcv)
            Return rcv
        Catch ex As Exception
            TL.LogIssue("ReadSocket", ex.Message)
            Disconnect()
        End Try
        Return String.Empty
    End Function

    Private Sub SocketSend(msg As String)
        If Connected = False Then
            Throw New DriverException("Not connected!")
        End If
        Try
            TL.LogMessage("SocketSend", "Sending " + msg)
            Dim bytesToSend As Byte() = Encoding.UTF8.GetBytes(msg + Environment.NewLine)
            socket.SendBufferSize = bytesToSend.Length
            socket.Send(bytesToSend)
        Catch ex As Exception
            TL.LogIssue("SendSocket", ex.Message)
            Disconnect()
        End Try
    End Sub

    Private Sub Disconnect()
        TL.LogMessage("Disconnect", "Disconnecting from port " + socketPort.ToString())
        Try
            socket.Shutdown(SocketShutdown.Both)
            socket.Close()
            connectedState = False
        Catch ex As Exception
            TL.LogIssue("Connected Set", "Disconnection exception! " + ex.Message)
            Throw New DriverException("Disconnection error!")
        End Try
    End Sub

    '
    ' Constructor - Must be public for COM registration!
    '
    Public Sub New()
        ReadProfile() ' Read device configuration from the ASCOM Profile store
        TL = New TraceLogger("", "ThunderFocus") With {
            .Enabled = debug
        }
        TL.LogMessage("Focuser", "Starting initialisation")
        connectedState = False
        Application.EnableVisualStyles()
        Dim ipHostInfo As IPHostEntry = Dns.GetHostEntry(Dns.GetHostName())
        ipAddress = ipHostInfo.AddressList(0)
        socket = New Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp) With {
            .NoDelay = True,
            .ReceiveTimeout = 1000,
            .SendTimeout = 1000
        }
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
            MessageBox.Show("ASCOM bridge running, use the control panel to configure the focuser.", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Information)
        Else
            Using F As New FocuserSetupDialog()
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

    Public Function Action(ActionName As String, ActionParameters As String) As String Implements IFocuserV3.Action
        Throw New ActionNotImplementedException("Action " & ActionName & " is not supported by this driver")
    End Function

    Public Sub CommandBlind(Command As String, Optional Raw As Boolean = False) Implements IFocuserV3.CommandBlind
        CheckConnected("CommandBlind")
        Throw New MethodNotImplementedException("CommandBlind")
    End Sub

    Public Function CommandBool(Command As String, Optional Raw As Boolean = False) As Boolean Implements IFocuserV3.CommandBool
        CheckConnected("CommandBool")
        Throw New MethodNotImplementedException("CommandBool")
    End Function

    Public Function CommandString(Command As String, Optional Raw As Boolean = False) As String Implements IFocuserV3.CommandString
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
            If value = connectedState Then
                Return
            End If
            If value Then
                TL.LogMessage("Connected Set", "Connecting to port " + socketPort.ToString())
                Try
                    Dim remoteEP As New IPEndPoint(ipAddress, socketPort)
                    socket.Connect(remoteEP)
                    Dim bytesToSend As Byte() = Encoding.UTF8.GetBytes("HasFocuser" + Environment.NewLine)
                    socket.SendBufferSize = bytesToSend.Length
                    socket.Send(bytesToSend)
                    Thread.Sleep(200)
                    Dim socketBuffer As Byte() = New Byte(1023) {}
                    Dim bytesRec As Integer = socket.Receive(socketBuffer)
                    connectedState = Encoding.ASCII.GetString(socketBuffer, 0, bytesRec).Contains("true")
                    If connectedState = False Then
                        Throw New DriverException("This ThunderFocus board doesn't have a focuser!")
                    End If
                Catch ex As Exception
                    TL.LogIssue("Connected Set", "Connection exception! " + ex.Message)
                    connectedState = False
                    Throw New DriverException("Could not connect to ThunderFocus!")
                End Try
            Else
                Disconnect()
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
            Dim m_version As Version = Reflection.Assembly.GetExecutingAssembly().GetName().Version
            Dim s_driverInfo As String = "ThunderFocus focuser bridge v" + m_version.Major.ToString() + "." + m_version.Minor.ToString()
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
            Return "ThunderFocus"
        End Get
    End Property

    Public Sub Dispose() Implements IFocuserV3.Dispose
        TL.LogMessage("Dispose", "Disposing...")
        Try
            Disconnect()
        Catch ex As Exception
            TL.LogIssue("Dispose", "Exception disconnecting: " + ex.Message)
        End Try
        TL.Enabled = False
        TL.Dispose()
        TL = Nothing
    End Sub

#End Region

#Region "IFocuser Implementation"

    Private focuserPosition As Integer = 0
    Private focuserMaxTravel As Integer = 32767
    Private moving As Boolean = False

    Public ReadOnly Property Absolute() As Boolean Implements IFocuserV3.Absolute
        Get
            Return True
        End Get
    End Property

    Public Sub Halt() Implements IFocuserV3.Halt
        CheckConnected("Attemped Halt while disconnected!")
        SocketSend("Halt")
    End Sub

    Public ReadOnly Property IsMoving() As Boolean Implements IFocuserV3.IsMoving
        Get
            CheckConnected("Attemped IsMoving while disconnected!")
            SocketSend("IsMoving")
            Dim rcv As String = SocketRead()
            If Not String.IsNullOrEmpty(rcv) Then
                moving = rcv.Contains("true")
            End If
            TL.LogMessage("IsMoving Get", moving.ToString())
            Return moving
        End Get
    End Property

    Public Property Link() As Boolean Implements IFocuserV3.Link
        Get
            Return Connected
        End Get
        Set(value As Boolean)
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
            CheckConnected("Attemped MaxStep while disconnected!")
            SocketSend("MaxStep")
            Dim rcv As String = SocketRead()
            If Not String.IsNullOrEmpty(rcv) Then
                focuserMaxTravel = Integer.Parse(rcv)
            End If
            TL.LogMessage("MaxStep Get", focuserMaxTravel.ToString())
            Return focuserMaxTravel
        End Get
    End Property

    Public Sub Move(Position As Integer) Implements IFocuserV3.Move
        CheckConnected("Attemped Move while disconnected!")
        If Position < 0 Or Position > focuserMaxTravel Then
            Throw New DriverException("Position " + Position.ToString() + " is outside the allowed boundaries!")
        End If
        If focuserPosition <> Position Then
            moving = True
            'TODO: check if this is OK
            'focuserPosition = Position
        End If
        SocketSend("Move=" + Position.ToString())
        TL.LogMessage("Move", Position.ToString())
    End Sub

    Public ReadOnly Property Position() As Integer Implements IFocuserV3.Position
        Get
            CheckConnected("Attemped Position while disconnected!")
            SocketSend("Position")
            Dim rcv As String = SocketRead()
            If Not String.IsNullOrEmpty(rcv) Then
                focuserPosition = Integer.Parse(rcv)
            End If
            Return focuserPosition
        End Get
    End Property

    Public ReadOnly Property StepSize() As Double Implements IFocuserV3.StepSize
        Get
            CheckConnected("Attemped StepSize while disconnected!")
            Throw New PropertyNotImplementedException("StepSize", False)
        End Get
    End Property

    Public Property TempComp() As Boolean Implements IFocuserV3.TempComp
        Get
            CheckConnected("Attemped TempComp while disconnected!")
            Return False
        End Get
        Set(value As Boolean)
            CheckConnected("Attemped TempComp while disconnected!")
            Throw New PropertyNotImplementedException("TempComp", True)
        End Set
    End Property

    Public ReadOnly Property TempCompAvailable() As Boolean Implements IFocuserV3.TempCompAvailable
        Get
            CheckConnected("Attemped TempCompAvailable while disconnected!")
            Return False
        End Get
    End Property

    Public ReadOnly Property Temperature() As Double Implements IFocuserV3.Temperature
        Get
            CheckConnected("Attemped Temperature while disconnected!")
            Throw New PropertyNotImplementedException("Temperature", False)
        End Get
    End Property

#End Region

#Region "Private properties and methods"

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
            debug = Convert.ToBoolean(driverProfile.GetValue(driverID, debugProfileName, String.Empty, debugDefault))
            socketPort = Integer.Parse(driverProfile.GetValue(driverID, socketPortProfileName, String.Empty, socketPortDefault))
        End Using
    End Sub

    ''' <summary>
    ''' Write the device configuration to the  ASCOM  Profile store
    ''' </summary>
    Friend Sub WriteProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "Focuser"
            driverProfile.WriteValue(driverID, debugProfileName, debug.ToString())
            driverProfile.WriteValue(driverID, socketPortProfileName, socketPort.ToString())
        End Using

    End Sub

#End Region

End Class