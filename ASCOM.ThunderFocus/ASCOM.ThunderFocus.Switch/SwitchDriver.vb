'tabs=4
' --------------------------------------------------------------------------------'
' ASCOM CoverCalibrator driver for ThunderFocus
'
' Description:	ASCOM Switch driver for ThunderFocus
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
' Your driver's ID is ASCOM.ThunderFocus.Switch
'
#Const Device = "Switch"

Imports System.Net
Imports System.Net.Sockets
Imports System.Threading
Imports ASCOM.DeviceInterface
Imports ASCOM.Utilities

<Guid("d3ad7299-1f00-4943-8432-cd5cf33afb73")>
<ClassInterface(ClassInterfaceType.None)>
Public Class Switch
    Implements ISwitchV2

    '
    ' Driver ID and descriptive string that shows in the Chooser
    '
    Friend Shared driverID As String = "ASCOM.ThunderFocus.Switch"
    Private Shared ReadOnly driverDescription As String = "ThunderFocus"

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
        TL.LogMessage("Switch", "Starting initialisation")
        connectedState = False
        Application.EnableVisualStyles()
        Dim ipHostInfo As IPHostEntry = Dns.GetHostEntry(Dns.GetHostName())
        ipAddress = ipHostInfo.AddressList(0)
        TL.LogMessage("Switch", "Completed initialisation")
    End Sub

    '
    ' PUBLIC COM INTERFACE ISwitchV2 IMPLEMENTATION
    '

#Region "Common properties and methods"
    ''' <summary>
    ''' Displays the Setup Dialog form.
    ''' If the user clicks the OK button to dismiss the form, then
    ''' the new settings are saved, otherwise the old values are reloaded.
    ''' THIS IS THE ONLY PLACE WHERE SHOWING USER INTERFACE IS ALLOWED!
    ''' </summary>
    Public Sub SetupDialog() Implements ISwitchV2.SetupDialog
        ' consider only showing the setup dialog if not connected
        ' or call a different dialog if connected
        If IsConnected Then
            System.Windows.Forms.MessageBox.Show("Already connected, just press OK")
        End If

        Using F As SetupDialogForm = New SetupDialogForm()
            Dim result As System.Windows.Forms.DialogResult = F.ShowDialog()
            If result = DialogResult.OK Then
                WriteProfile() ' Persist device configuration values to the ASCOM Profile store
            End If
        End Using
    End Sub

    Public ReadOnly Property SupportedActions() As ArrayList Implements ISwitchV2.SupportedActions
        Get
            TL.LogMessage("SupportedActions Get", "Returning empty arraylist")
            Return New ArrayList()
        End Get
    End Property

    Public Function Action(ByVal ActionName As String, ByVal ActionParameters As String) As String Implements ISwitchV2.Action
        Throw New ActionNotImplementedException("Action " & ActionName & " is not supported by this driver")
    End Function

    Public Sub CommandBlind(ByVal Command As String, Optional ByVal Raw As Boolean = False) Implements ISwitchV2.CommandBlind
        CheckConnected("CommandBlind")
        ' TODO The optional CommandBlind method should either be implemented OR throw a MethodNotImplementedException
        ' If implemented, CommandBlind must send the supplied command to the mount And return immediately without waiting for a response

        Throw New MethodNotImplementedException("CommandBlind")
    End Sub

    Public Function CommandBool(ByVal Command As String, Optional ByVal Raw As Boolean = False) As Boolean _
        Implements ISwitchV2.CommandBool
        CheckConnected("CommandBool")
        ' TODO The optional CommandBool method should either be implemented OR throw a MethodNotImplementedException
        ' If implemented, CommandBool must send the supplied command to the mount, wait for a response and parse this to return a True Or False value

        ' Dim retString as String = CommandString(command, raw) ' Send the command And wait for the response
        ' Dim retBool as Boolean = XXXXXXXXXXXXX ' Parse the returned string And create a boolean True / False value
        ' Return retBool ' Return the boolean value to the client

        Throw New MethodNotImplementedException("CommandBool")
    End Function

    Public Function CommandString(ByVal Command As String, Optional ByVal Raw As Boolean = False) As String _
        Implements ISwitchV2.CommandString
        CheckConnected("CommandString")
        ' TODO The optional CommandString method should either be implemented OR throw a MethodNotImplementedException
        ' If implemented, CommandString must send the supplied command to the mount and wait for a response before returning this to the client

        Throw New MethodNotImplementedException("CommandString")
    End Function

    Public Property Connected() As Boolean Implements ISwitchV2.Connected
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

    Public ReadOnly Property Description As String Implements ISwitchV2.Description
        Get
            ' this pattern seems to be needed to allow a public property to return a private field
            Dim d As String = driverDescription
            TL.LogMessage("Description Get", d)
            Return d
        End Get
    End Property

    Public ReadOnly Property DriverInfo As String Implements ISwitchV2.DriverInfo
        Get
            Dim m_version As Version = System.Reflection.Assembly.GetExecutingAssembly().GetName().Version
            ' TODO customise this driver description
            Dim s_driverInfo As String = "Information about the driver itself. Version: " + m_version.Major.ToString() + "." + m_version.Minor.ToString()
            TL.LogMessage("DriverInfo Get", s_driverInfo)
            Return s_driverInfo
        End Get
    End Property

    Public ReadOnly Property DriverVersion() As String Implements ISwitchV2.DriverVersion
        Get
            ' Get our own assembly and report its version number
            TL.LogMessage("DriverVersion Get", Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2))
            Return Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2)
        End Get
    End Property

    Public ReadOnly Property InterfaceVersion() As Short Implements ISwitchV2.InterfaceVersion
        Get
            TL.LogMessage("InterfaceVersion Get", "2")
            Return 2
        End Get
    End Property

    Public ReadOnly Property Name As String Implements ISwitchV2.Name
        Get
            Dim s_name As String = "Short driver name - please customise"
            TL.LogMessage("Name Get", s_name)
            Return s_name
        End Get
    End Property

    Public Sub Dispose() Implements ISwitchV2.Dispose
        ' Clean up the trace logger and util objects
        TL.Enabled = False
        TL.Dispose()
        TL = Nothing
    End Sub

#End Region

#Region "ISwitchV2 Implementation"

    Dim numSwitches As Short = 0

    ''' <summary>
    ''' The number of switches managed by this driver
    ''' </summary>
    Public ReadOnly Property MaxSwitch As Short Implements ISwitchV2.MaxSwitch
        Get
            TL.LogMessage("MaxSwitch Get", numSwitches.ToString())
            Return numSwitches
        End Get
    End Property

    ''' <summary>
    ''' Return the name of switch n
    ''' </summary>
    ''' <param name="id">The switch number to return</param>
    ''' <returns>The name of the switch</returns>
    Public Function GetSwitchName(id As Short) As String Implements ISwitchV2.GetSwitchName
        Validate("GetSwitchName", id)
        TL.LogMessage("GetSwitchName", "Not Implemented")
        Throw New MethodNotImplementedException("GetSwitchName")
    End Function

    ''' <summary>
    ''' Sets a switch name to a specified value
    ''' </summary>
    ''' <param name="id">The number of the switch whose name is to be set</param>
    ''' <param name="name">The name of the switch</param>
    Sub SetSwitchName(id As Short, name As String) Implements ISwitchV2.SetSwitchName
        Validate("SetSwitchName", id)
        TL.LogMessage("SetSwitchName", "Not Implemented")
        Throw New MethodNotImplementedException("SetSwitchName")
    End Sub

    ''' <summary>
    ''' Gets the description of the specified switch. This is to allow a fuller description of the switch to be returned, for example for a tool tip.
    ''' </summary>
    ''' <param name="id">The number of the switch whose description is to be returned</param><returns></returns>
    ''' <exception cref="InvalidValueException">If id is outside the range 0 to MaxSwitch - 1</exception>
    Public Function GetSwitchDescription(id As Short) As String Implements ISwitchV2.GetSwitchDescription
        Validate("GetSwitchDescription", id)
        TL.LogMessage("GetSwitchDescription", "Not Implemented")
        Throw New MethodNotImplementedException("GetSwitchDescription")
    End Function

    ''' <summary>
    ''' Reports if the specified switch can be written to.
    ''' This is false if the switch cannot be written to, for example a limit switch or a sensor.
    ''' </summary>
    ''' <param name="id">The number of the switch whose write state is to be returned</param>
    ''' <returns>
    ''' <c>true</c> if the switch can be set, otherwise <c>false</c>.
    ''' </returns>
    ''' <exception cref="MethodNotImplementedException">If the method is not implemented</exception>
    ''' <exception cref="InvalidValueException">If id is outside the range 0 to MaxSwitch - 1</exception>
    Public Function CanWrite(id As Short) As Boolean Implements ISwitchV2.CanWrite
        Validate("CanWrite", id)
        TL.LogMessage("CanWrite", "Default true")
        Return True
    End Function

#Region "boolean members"
    ''' <summary>
    ''' Return the state of switch n as a boolean.
    ''' A multi-value switch must throw a MethodNotImplementedException.
    ''' </summary>
    ''' <param name="id">The switch number to return</param>
    ''' <returns>True or false</returns>
    Function GetSwitch(id As Short) As Boolean Implements ISwitchV2.GetSwitch
        Validate("GetSwitch", id, True)
        TL.LogMessage("GetSwitch", "Not Implemented")
        Throw New ASCOM.MethodNotImplementedException("GetSwitch")
    End Function

    ''' <summary>
    ''' Sets a switch to the specified state, true or false.
    ''' If the switch cannot be set then throws a MethodNotImplementedException.
    ''' </summary>
    ''' <param name="ID">The number of the switch to set</param>
    ''' <param name="State">The required switch state</param>
    Sub SetSwitch(id As Short, state As Boolean) Implements ISwitchV2.SetSwitch
        Validate("SetSwitch", id, True)
        TL.LogMessage("SetSwitch", "Not Implemented")
        Throw New ASCOM.MethodNotImplementedException("SetSwitch")
    End Sub

#End Region

#Region "Analogue members"
    ''' <summary>
    ''' Returns the maximum analogue value for this switch
    ''' Boolean switches must return 1.0
    ''' </summary>
    ''' <param name="id"></param>
    ''' <returns></returns>
    Function MaxSwitchValue(id As Short) As Double Implements ISwitchV2.MaxSwitchValue
        Validate("MaxSwitchValue", id)
        TL.LogMessage("MaxSwitchValue", "Not Implemented")
        Throw New MethodNotImplementedException("MaxSwitchValue")
    End Function

    ''' <summary>
    ''' Returns the minimum analogue value for this switch
    ''' Boolean switches must return 0.0
    ''' </summary>
    ''' <param name="id"></param>
    ''' <returns></returns>
    Function MinSwitchValue(id As Short) As Double Implements ISwitchV2.MinSwitchValue
        Validate("MinSwitchValue", id)
        TL.LogMessage("MinSwitchValue", "Not Implemented")
        Throw New MethodNotImplementedException("MinSwitchValue")
    End Function

    ''' <summary>
    ''' returns the step size that this switch supports. This gives the difference between successive values of the switch.
    ''' The number of values is ((MaxSwitchValue - MinSwitchValue) / SwitchStep) + 1
    ''' boolean switches must return 1.0, giving two states.
    ''' </summary>
    ''' <param name="id"></param>
    ''' <returns></returns>
    Function SwitchStep(id As Short) As Double Implements ISwitchV2.SwitchStep
        Validate("SwitchStep", id)
        TL.LogMessage("SwitchStep", "Not Implemented")
        Throw New MethodNotImplementedException("SwitchStep")
    End Function

    ''' <summary>
    ''' Returns the analogue switch value for switch id
    ''' Boolean switches must throw a MethodNotImplementedException
    ''' </summary>
    ''' <param name="id"></param>
    ''' <returns></returns>
    Function GetSwitchValue(id As Short) As Double Implements ISwitchV2.GetSwitchValue
        Validate("GetSwitchValue", id, False)
        TL.LogMessage("GetSwitchValue", "Not Implemented")
        Throw New MethodNotImplementedException("GetSwitchValue")
    End Function

    ''' <summary>
    ''' Set the analogue value for this switch.
    ''' A MethodNotImplementedException should be thrown if CanWrite returns False
    ''' If the value is not between the maximum and minimum then throws an InvalidValueException
    ''' boolean switches must throw a MethodNotImplementedException
    ''' </summary>
    ''' <param name="id"></param>
    ''' <param name="value"></param>
    Sub SetSwitchValue(id As Short, value As Double) Implements ISwitchV2.SetSwitchValue
        Validate("SetSwitchValue", id, value)
        If value < MinSwitchValue(id) Or value > MaxSwitchValue(id) Then
            Throw New InvalidValueException("", value.ToString(), String.Format("{0} to {1}", MinSwitchValue(id), MaxSwitchValue(id)))
        End If
        TL.LogMessage("SetSwitchValue", "Not Implemented")
        Throw New MethodNotImplementedException("SetSwitchValue")
    End Sub

#End Region

#End Region

#Region "Private methods"

    ''' <summary>
    ''' Checks that the switch id is in range and throws an InvalidValueException if it isn't
    ''' </summary>
    ''' <param name="message">The message.</param>
    ''' <param name="id">The id.</param>
    Private Sub Validate(message As String, id As Short)
        If (id < 0 Or id >= numSwitches) Then
            Throw New InvalidValueException(message, id.ToString(), String.Format("0 to {0}", numSwitches - 1))
        End If
    End Sub

    ''' <summary>
    ''' Checks that the number of states for the switch is correct and throws a methodNotImplemented exception if not.
    ''' Boolean switches must have 2 states and multi-value switches more than 2.
    ''' </summary>
    ''' <param name="message"></param>
    ''' <param name="id"></param>
    ''' <param name="expectBoolean"></param>
    Private Sub Validate(message As String, id As Short, expectBoolean As Boolean)
        Validate(message, id)
        Dim ns As Integer = (((MaxSwitchValue(id) - MinSwitchValue(id)) / SwitchStep(id)) + 1)
        If (expectBoolean And ns <> 2) Or (Not expectBoolean And ns <= 2) Then
            TL.LogMessage(message, String.Format("Switch {0} has the wrong number of states", id, ns))
            Throw New MethodNotImplementedException(String.Format("{0}({1})", message, id))
        End If
    End Sub

    ''' <summary>
    ''' Checks that the switch id and value are in range and throws an
    ''' InvalidValueException if they are not.
    ''' </summary>
    ''' <param name="message">The message.</param>
    ''' <param name="id">The id.</param>
    ''' <param name="value">The value.</param>
    Private Sub Validate(message As String, id As Short, value As Double)
        Validate(message, id, False)
        Dim min = MinSwitchValue(id)
        Dim max = MaxSwitchValue(id)
        If (value < min Or value > max) Then
            TL.LogMessage(message, String.Format("Value {1} for Switch {0} is out of the allowed range {2} to {3}", id, value, min, max))
            Throw New InvalidValueException(message, value.ToString(), String.Format("Switch({0}) range {1} to {2}", id, min, max))
        End If
    End Sub
#End Region

#Region "Private properties and methods"
    ' here are some useful properties and methods that can be used as required
    ' to help with

#Region "ASCOM Registration"

    Private Shared Sub RegUnregASCOM(ByVal bRegister As Boolean)

        Using P As New Profile() With {.DeviceType = "Switch"}
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
            driverProfile.DeviceType = "Switch"
            debug = Convert.ToBoolean(driverProfile.GetValue(driverID, debugProfileName, String.Empty, debugDefault))
            socketPort = Integer.Parse(driverProfile.GetValue(driverID, socketPortProfileName, String.Empty, socketPortDefault))
        End Using
    End Sub

    ''' <summary>
    ''' Write the device configuration to the  ASCOM  Profile store
    ''' </summary>
    Friend Sub WriteProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "Switch"
            driverProfile.WriteValue(driverID, debugProfileName, debug.ToString())
            driverProfile.WriteValue(driverID, socketPortProfileName, socketPort.ToString())
        End Using

    End Sub

#End Region

End Class
