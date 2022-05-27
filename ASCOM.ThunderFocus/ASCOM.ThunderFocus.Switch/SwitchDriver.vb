'tabs=4
' --------------------------------------------------------------------------------'
' ASCOM Switch driver for ThunderFocus
'
' Description:	ASCOM Switch driver for ThunderFocus
'
' Implements:	ASCOM Switch interface version: 2.0
' Author:		(MRC) Marco Cipriani <marco.cipriani.01@gmail.com>
'
' Edit Log:
'
' Date			Who	Vers	Description
' -----------	---	-----	-------------------------------------------------------
' 26-MAY-2022	MRC	1.0.0	First version
' ---------------------------------------------------------------------------------
'
' Your driver's ID is ASCOM.ThunderFocus.Switch
'
#Const Device = "Switch"

Option Strict On
Option Infer On

Imports ASCOM.DeviceInterface
Imports ASCOM.Utilities
Imports ThunderFocus

<Guid("ff584723-1c09-49b8-a0e5-e263b1be2332")>
<ClassInterface(ClassInterfaceType.None)>
Public Class Switch
    Implements ISwitchV2

    '
    ' Driver ID and descriptive string that shows in the Chooser
    '
    Friend Shared driverID As String = "ASCOM.ThunderFocus.Switch"
    Private Shared ReadOnly driverDescription As String = "ThunderFocus Powerbox"

    Friend Shared socketPort As Integer = 5001
    Friend Shared debug As Boolean = False

    Private ReadOnly helper As DriverHelper
    Private connectedState As Boolean = False
    Private TL As TraceLogger

    Private maxSwitchVal As Short = 0

    '
    ' Constructor - Must be public for COM registration!
    '
    Public Sub New()
        ReadProfile() ' Read device configuration from the ASCOM Profile store
        TL = New TraceLogger("", "ThunderFocus_Switch") With {
            .Enabled = debug
        }
        TL.LogMessage("Switch", "Starting initialisation")
        connectedState = False
        Application.EnableVisualStyles()
        helper = New DriverHelper()
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
        Application.EnableVisualStyles()
        If IsConnected Then
            MessageBox.Show("ASCOM bridge running, use the control panel to configure the switches.", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Information)
        Else
            Using F As New SetupDialogForm()
                Dim result As DialogResult = F.ShowDialog()
                If result = DialogResult.OK Then
                    WriteProfile()
                End If
            End Using
        End If
    End Sub

    Public ReadOnly Property SupportedActions() As ArrayList Implements ISwitchV2.SupportedActions
        Get
            TL.LogMessage("SupportedActions Get", "Returning empty arraylist")
            Return New ArrayList()
        End Get
    End Property

    Public Function Action(ActionName As String, ActionParameters As String) As String Implements ISwitchV2.Action
        Throw New ActionNotImplementedException("Action " & ActionName & " is not supported by this driver")
    End Function

    Public Sub CommandBlind(Command As String, Optional Raw As Boolean = False) Implements ISwitchV2.CommandBlind
        CheckConnected("CommandBlind")
        Throw New MethodNotImplementedException("CommandBlind")
    End Sub

    Public Function CommandBool(Command As String, Optional Raw As Boolean = False) As Boolean Implements ISwitchV2.CommandBool
        CheckConnected("CommandBool")
        Throw New MethodNotImplementedException("CommandBool")
    End Function

    Public Function CommandString(Command As String, Optional Raw As Boolean = False) As String Implements ISwitchV2.CommandString
        CheckConnected("CommandString")
        Throw New MethodNotImplementedException("CommandString")
    End Function

    Public Property Connected() As Boolean Implements ISwitchV2.Connected
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
                    connectedState = helper.Connect(socketPort, "HasPowerBox")
                    helper.SocketSend("MaxSwitch")
                    Dim rcv As String = helper.SocketRead()
                    If String.IsNullOrEmpty(rcv) Then
                        helper.Disconnect()
                        connectedState = False
                        Throw New DriverException("Trouble reading information from ThunderFocus!")
                    End If
                    maxSwitchVal = Short.Parse(rcv)
                    TL.LogMessage("Connected Set", "Number of switches = " + CStr(MaxSwitch))
                Catch ex As Exception
                    TL.LogIssue("Connected Set", "Connection exception! " + ex.Message)
                    connectedState = False
                    Throw New DriverException("Could not connect to ThunderFocus!")
                End Try
                If connectedState = False Then
                    Throw New DriverException("This ThunderFocus board doesn't have a powerbox!")
                End If
            Else
                helper.Disconnect()
                connectedState = False
            End If
        End Set
    End Property

    Public ReadOnly Property Description As String Implements ISwitchV2.Description
        Get
            Dim d As String = driverDescription
            TL.LogMessage("Description Get", d)
            Return d
        End Get
    End Property

    Public ReadOnly Property DriverInfo As String Implements ISwitchV2.DriverInfo
        Get
            Dim m_version As Version = Reflection.Assembly.GetExecutingAssembly().GetName().Version
            Dim s_driverInfo As String = "ThunderFocus powerbox bridge v" + m_version.Major.ToString() + "." + m_version.Minor.ToString()
            TL.LogMessage("DriverInfo Get", s_driverInfo)
            Return s_driverInfo
        End Get
    End Property

    Public ReadOnly Property DriverVersion() As String Implements ISwitchV2.DriverVersion
        Get
            TL.LogMessage("DriverVersion Get", Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2))
            Return Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2)
        End Get
    End Property

    Public ReadOnly Property InterfaceVersion() As Short Implements ISwitchV2.InterfaceVersion
        Get
            TL.LogMessage("InterfaceVersion Get", "3")
            Return 3
        End Get
    End Property

    Public ReadOnly Property Name As String Implements ISwitchV2.Name
        Get
            Return "ThunderFocus powerbox"
        End Get
    End Property

    Public Sub Dispose() Implements ISwitchV2.Dispose
        TL.LogMessage("Dispose", "Disposing...")
        Try
            helper.Disconnect()
            connectedState = False
        Catch ex As Exception
            TL.LogIssue("Dispose", "Exception while disconnecting: " + ex.Message)
        End Try
        TL.Enabled = False
        TL.Dispose()
        TL = Nothing
    End Sub

#End Region

#Region "ISwitchV2 Implementation"

    ''' <summary>
    ''' The number of switches managed by this driver
    ''' </summary>
    Public ReadOnly Property MaxSwitch As Short Implements ISwitchV2.MaxSwitch
        Get
            CheckConnected("Attemped MaxSwitch while disconnected!")
            Return maxSwitchVal
        End Get
    End Property

    ''' <summary>
    ''' Return the name of switch n
    ''' </summary>
    ''' <param name="id">The switch number to return</param>
    ''' <returns>The name of the switch</returns>
    Public Function GetSwitchName(id As Short) As String Implements ISwitchV2.GetSwitchName
        CheckConnected("Attemped GetSwitchName while disconnected!")
        ValidateId("GetSwitchName", id)
        Try
            helper.SocketSend("GetSwitchName=" + id.ToString())
            Dim rcv As String = helper.SocketRead()
            If Not String.IsNullOrEmpty(rcv) Then
                TL.LogMessage("GetSwitchName", rcv)
                Return rcv
            End If
        Catch ex As Exception
            TL.LogIssue("GetSwitchName", "Exception while getting switch name: " + ex.Message)
        End Try
        Throw New DriverException("No response from ThunderFocus!")
    End Function

    ''' <summary>
    ''' Sets a switch name to a specified value
    ''' </summary>
    ''' <param name="id">The number of the switch whose name is to be set</param>
    ''' <param name="name">The name of the switch</param>
    Sub SetSwitchName(id As Short, name As String) Implements ISwitchV2.SetSwitchName
        CheckConnected("Attemped SetSwitchName while disconnected!")
        ValidateId("SetSwitchName", id)
        Try
            helper.SocketSend("SetSwitchName=" + id.ToString() + "," + name)
        Catch ex As Exception
            TL.LogIssue("SetSwitchName", "Exception while setting switch name: " + ex.Message)
        End Try
    End Sub

    ''' <summary>
    ''' Gets the description of the specified switch. This is to allow a fuller description of the switch to be returned, for example for a tool tip.
    ''' </summary>
    ''' <param name="id">The number of the switch whose description is to be returned</param><returns></returns>
    ''' <exception cref="InvalidValueException">If id is outside the range 0 to MaxSwitch - 1</exception>
    Public Function GetSwitchDescription(id As Short) As String Implements ISwitchV2.GetSwitchDescription
        Return GetSwitchName(id)
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
        CheckConnected("Attemped CanWrite while disconnected!")
        ValidateId("CanWrite", id)
        Try
            helper.SocketSend("CanWrite=" + id.ToString())
            Dim rcv As String = helper.SocketRead()
            If Not String.IsNullOrEmpty(rcv) Then
                TL.LogMessage("CanWrite", rcv)
                Return rcv.Contains("true")
            End If
        Catch ex As Exception
            TL.LogIssue("CanWrite", "Exception while getting switch write state: " + ex.Message)
        End Try
        Throw New DriverException("No response from ThunderFocus!")
    End Function

#Region "boolean members"
    ''' <summary>
    ''' Return the state of switch n as a boolean.
    ''' A multi-value switch must throw a MethodNotImplementedException.
    ''' </summary>
    ''' <param name="id">The switch number to return</param>
    ''' <returns>True or false</returns>
    Function GetSwitch(id As Short) As Boolean Implements ISwitchV2.GetSwitch
        CheckConnected("Attemped GetSwitch while disconnected!")
        ValidateStates("GetSwitch", id, True)
        Try
            helper.SocketSend("GetSwitch=" + id.ToString())
            Dim rcv As String = helper.SocketRead()
            If Not String.IsNullOrEmpty(rcv) Then
                TL.LogMessage("GetSwitch", rcv)
                If rcv.Contains("PWM") Then
                    Throw New MethodNotImplementedException("GetSwitch is not implemented for multi-value switches")
                End If
                Return rcv.Contains("true")
            End If
        Catch ex As Exception
            TL.LogIssue("GetSwitch", "Exception while getting switch state: " + ex.Message)
        End Try
        Throw New DriverException("No response from ThunderFocus!")
    End Function

    ''' <summary>
    ''' Sets a switch to the specified state, true or false.
    ''' If the switch cannot be set then throws a MethodNotImplementedException.
    ''' </summary>
    ''' <param name="ID">The number of the switch to set</param>
    ''' <param name="State">The required switch state</param>
    Sub SetSwitch(id As Short, state As Boolean) Implements ISwitchV2.SetSwitch
        CheckConnected("Attemped SetSwitch while disconnected!")
        ValidateStates("SetSwitch", id, True)
        Dim stateStr As String
        If state Then
            stateStr = "true"
        Else
            stateStr = "false"
        End If
        Dim rcv As String = ""
        Try
            helper.SocketSend("SetSwitch=" + id.ToString() + "," + stateStr)
            rcv = helper.SocketRead()
        Catch ex As Exception
            TL.LogIssue("SetSwitch", "Exception while setting switch state: " + ex.Message)
        End Try
        If String.IsNullOrEmpty(rcv) Then
            Throw New DriverException("No response from ThunderFocus!")
        End If
        TL.LogMessage("SetSwitch", rcv)
        If rcv.Contains("CannotWrite") Then
            Throw New MethodNotImplementedException("Cannot write to this switch!")
        End If
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
        CheckConnected("Attemped MaxSwitchValue while disconnected!")
        ValidateId("MaxSwitchValue", id)
        Dim rcv As String = ""
        Try
            helper.SocketSend("MaxSwitchValue=" + id.ToString())
            rcv = helper.SocketRead()
        Catch ex As Exception
            TL.LogIssue("MaxSwitchValue", "Exception while getting switch max value: " + ex.Message)
        End Try
        If String.IsNullOrEmpty(rcv) Then
            Throw New DriverException("No response from ThunderFocus!")
        End If
        TL.LogMessage("MaxSwitchValue", rcv)
        If rcv.Contains("NonExistent") Then
            Return 1.0
        End If
        Return CDbl(rcv)
    End Function

    ''' <summary>
    ''' Returns the minimum analogue value for this switch
    ''' Boolean switches must return 0.0
    ''' </summary>
    ''' <param name="id"></param>
    ''' <returns></returns>
    Function MinSwitchValue(id As Short) As Double Implements ISwitchV2.MinSwitchValue
        CheckConnected("Attemped MinSwitchValue while disconnected!")
        ValidateId("MinSwitchValue", id)
        Return 0.0
    End Function

    ''' <summary>
    ''' returns the step size that this switch supports. This gives the difference between successive values of the switch.
    ''' The number of values is ((MaxSwitchValue - MinSwitchValue) / SwitchStep) + 1
    ''' boolean switches must return 1.0, giving two states.
    ''' </summary>
    ''' <param name="id"></param>
    ''' <returns></returns>
    Function SwitchStep(id As Short) As Double Implements ISwitchV2.SwitchStep
        CheckConnected("Attemped SwitchStep while disconnected!")
        ValidateId("SwitchStep", id)
        Return 1.0
    End Function

    ''' <summary>
    ''' Returns the analogue switch value for switch id
    ''' Boolean switches must throw a MethodNotImplementedException
    ''' </summary>
    ''' <param name="id"></param>
    ''' <returns></returns>
    Function GetSwitchValue(id As Short) As Double Implements ISwitchV2.GetSwitchValue
        CheckConnected("Attemped GetSwitchValue while disconnected!")
        ValidateStates("GetSwitchValue", id, False)
        Dim rcv As String = ""
        Try
            helper.SocketSend("GetSwitchValue=" + id.ToString())
            rcv = helper.SocketRead()
        Catch ex As Exception
            TL.LogIssue("GetSwitchValue", "Exception while getting switch value: " + ex.Message)
        End Try
        If String.IsNullOrEmpty(rcv) Then
            Throw New DriverException("No response from ThunderFocus!")
        End If
        TL.LogMessage("GetSwitchValue", rcv)
        If rcv.Contains("Boolean") Then
            Throw New MethodNotImplementedException("GetSwitchValue is not implemented for boolean switches")
        End If
        Return CDbl(rcv)
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
        CheckConnected("Attemped SetSwitchValue while disconnected!")
        ValidateRange("SetSwitchValue", id, value)
        Dim rcv As String = ""
        Try
            helper.SocketSend("SetSwitchValue=" + id.ToString() + "," + value.ToString())
            rcv = helper.SocketRead()
        Catch ex As Exception
            TL.LogIssue("SetSwitchValue", "Exception while setting switch value: " + ex.Message)
        End Try
        If String.IsNullOrEmpty(rcv) Then
            Throw New DriverException("No response from ThunderFocus!")
        End If
        TL.LogMessage("SetSwitchValue", rcv)
        If Not rcv.Contains("OK") Then
            Throw New MethodNotImplementedException("SetSwitchValue is not implemented for boolean switches")
        End If
    End Sub

#End Region

#End Region

#Region "Private methods"

    ''' <summary>
    ''' Checks that the switch id is in range and throws an InvalidValueException if it isn't
    ''' </summary>
    ''' <param name="message">The message.</param>
    ''' <param name="id">The id.</param>
    Private Sub ValidateId(message As String, id As Short)
        If (id < 0 Or id >= maxSwitchVal) Then
            Throw New InvalidValueException(message, id.ToString(), String.Format("0 to {0}", maxSwitchVal - 1))
        End If
    End Sub

    ''' <summary>
    ''' Checks that the number of states for the switch is correct and throws a methodNotImplemented exception if not.
    ''' Boolean switches must have 2 states and multi-value switches more than 2.
    ''' </summary>
    ''' <param name="message"></param>
    ''' <param name="id"></param>
    ''' <param name="expectBoolean"></param>
    Private Sub ValidateStates(message As String, id As Short, expectBoolean As Boolean)
        ValidateId(message, id)
        Dim ns As Integer = CInt(MaxSwitchValue(id) + 1)
        If (expectBoolean And ns <> 2) Or (Not expectBoolean And ns <= 2) Then
            TL.LogIssue(message, String.Format("Switch {0} has the wrong number of states", id, ns))
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
    Private Sub ValidateRange(message As String, id As Short, value As Double)
        ValidateStates(message, id, False)
        Dim max = MaxSwitchValue(id)
        If value < 0.0 Or value > max Then
            TL.LogMessage(message, String.Format("Value {1} for Switch {0} is out of the allowed range {2} to {3}", id, value, 0.0, max))
            Throw New InvalidValueException(message, value.ToString(), String.Format("Switch({0}) range {1} to {2}", id, 0.0, max))
        End If
    End Sub
#End Region

#Region "Private properties and methods"

#Region "ASCOM Registration"

    Private Shared Sub RegUnregASCOM(bRegister As Boolean)
        Using P As New Profile() With {.DeviceType = "Switch"}
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
            ' TODO check that the driver hardware connection exists and is connected to the hardware
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
            driverProfile.DeviceType = "Switch"
            debug = Convert.ToBoolean(driverProfile.GetValue(driverID, DriverHelper.debugProfileName, String.Empty, DriverHelper.debugDefault))
            socketPort = Integer.Parse(driverProfile.GetValue(driverID, DriverHelper.socketPortProfileName, String.Empty, DriverHelper.socketPortDefault))
        End Using
    End Sub

    ''' <summary>
    ''' Write the device configuration to the  ASCOM  Profile store
    ''' </summary>
    Friend Sub WriteProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "Switch"
            driverProfile.WriteValue(driverID, DriverHelper.debugProfileName, debug.ToString())
            driverProfile.WriteValue(driverID, DriverHelper.socketPortProfileName, socketPort.ToString())
        End Using

    End Sub

#End Region

End Class
