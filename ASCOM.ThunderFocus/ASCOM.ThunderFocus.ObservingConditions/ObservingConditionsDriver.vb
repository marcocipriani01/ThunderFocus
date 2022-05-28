'tabs=4
' --------------------------------------------------------------------------------'
' ASCOM ObservingConditions driver for ThunderFocus
'
' Description:	ASCOM ObservingConditions driver for ThunderFocus
'
' Implements:	ASCOM ObservingConditions interface version: 1.0
' Author:		(MRC) Marco Cipriani <marco.cipriani.01@gmail.com>
'
' Edit Log:
'
' Date			Who	Vers	Description
' -----------	---	-----	-------------------------------------------------------
' 26-MAY-2022	MRC	1.0.0	First version
' ---------------------------------------------------------------------------------
'
' Your driver's ID is ASCOM.ThunderFocus.ObservingConditions
'
#Const Device = "ObservingConditions"

Option Strict On
Option Infer On

Imports System.Globalization
Imports ASCOM.DeviceInterface
Imports ASCOM.Utilities

<Guid("e44c825f-5946-49a6-8794-3f3a6af69187")>
<ClassInterface(ClassInterfaceType.None)>
Public Class ObservingConditions
    Implements IObservingConditions

    '
    ' Driver ID and descriptive string that shows in the Chooser
    '
    Friend Shared driverID As String = "ASCOM.ThunderFocus.ObservingConditions"
    Private Shared ReadOnly driverDescription As String = "ThunderFocus ambient"

    Friend Shared socketPort As Integer = 5001
    Friend Shared debug As Boolean = False

    Private ReadOnly helper As DriverHelper
    Private connectedState As Boolean = False
    Private TL As TraceLogger

    Private temp As Double = 0.0
    Private hum As Double = 0.0
    Private dataUpdateTime As DateTime = Nothing

    Private ReadOnly myProperties As String() = {"temperature", "humidity"}

    '
    ' Constructor - Must be public for COM registration!
    '
    Public Sub New()
        ReadProfile() ' Read device configuration from the ASCOM Profile store
        TL = New TraceLogger("", "ThunderFocus_ObservingConditions") With {
            .Enabled = debug
        }
        TL.LogMessage("ObservingConditions", "Starting initialisation")
        connectedState = False
        Application.EnableVisualStyles()
        helper = New DriverHelper()
        TL.LogMessage("ObservingConditions", "Completed initialisation")
    End Sub

    '
    ' PUBLIC COM INTERFACE IObservingConditions IMPLEMENTATION
    '

#Region "Common properties and methods"
    ''' <summary>
    ''' Displays the Setup Dialog form.
    ''' If the user clicks the OK button to dismiss the form, then
    ''' the new settings are saved, otherwise the old values are reloaded.
    ''' THIS IS THE ONLY PLACE WHERE SHOWING USER INTERFACE IS ALLOWED!
    ''' </summary>
    Public Sub SetupDialog() Implements IObservingConditions.SetupDialog
        Application.EnableVisualStyles()
        If connectedState Then
            MessageBox.Show("ASCOM bridge running, use the control panel to configure the powerbox.", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Information)
        Else
            Using F As New SetupDialogForm()
                Dim result As DialogResult = F.ShowDialog()
                If result = DialogResult.OK Then
                    WriteProfile()
                End If
            End Using
        End If
    End Sub

    Public ReadOnly Property SupportedActions() As ArrayList Implements IObservingConditions.SupportedActions
        Get
            Return New ArrayList()
        End Get
    End Property

    Public Function Action(ActionName As String, ActionParameters As String) As String Implements IObservingConditions.Action
        Throw New ActionNotImplementedException("Action " & ActionName & " is not supported by this driver")
    End Function

    Public Sub CommandBlind(Command As String, Optional Raw As Boolean = False) Implements IObservingConditions.CommandBlind
        CheckConnected("CommandBlind")
        Throw New MethodNotImplementedException("CommandBlind")
    End Sub

    Public Function CommandBool(Command As String, Optional Raw As Boolean = False) As Boolean _
        Implements IObservingConditions.CommandBool
        CheckConnected("CommandBool")
        Throw New MethodNotImplementedException("CommandBool")
    End Function

    Public Function CommandString(Command As String, Optional Raw As Boolean = False) As String _
        Implements IObservingConditions.CommandString
        CheckConnected("CommandString")
        Throw New MethodNotImplementedException("CommandString")
    End Function

    Public Property Connected() As Boolean Implements IObservingConditions.Connected
        Get
            TL.LogMessage("Connected Get", connectedState.ToString())
            Return connectedState
        End Get
        Set(value As Boolean)
            TL.LogMessage("Connected Set", value.ToString())
            If value = connectedState Then
                Return
            End If
            If value Then
                TL.LogMessage("Connected Set", "Connecting to port " + socketPort.ToString())
                Try
                    SyncLock helper
                        connectedState = helper.Connect(socketPort, "HasAmbientSensors")
                        helper.SocketSend("GetTemperature")
                        Dim rcv As String = helper.SocketRead()
                        If String.IsNullOrEmpty(rcv) Then
                            helper.Disconnect()
                            connectedState = False
                            Throw New DriverException("Trouble reading information from ThunderFocus!")
                        End If
                        temp = Double.Parse(rcv, CultureInfo.InvariantCulture)
                        If temp < -20.0 Then
                            helper.Disconnect()
                            connectedState = False
                            Throw New DriverException("Sensors data not yet available, wait and retry!")
                        End If
                        TL.LogMessage("Connected Set", "Temperature = " + temp.ToString())
                        helper.SocketSend("GetHumidity")
                        rcv = helper.SocketRead()
                        If String.IsNullOrEmpty(rcv) Then
                            helper.Disconnect()
                            connectedState = False
                            Throw New DriverException("Trouble reading information from ThunderFocus!")
                        End If
                        hum = Double.Parse(rcv, CultureInfo.InvariantCulture)
                        If hum < 0.0 Then
                            helper.Disconnect()
                            connectedState = False
                            Throw New DriverException("Sensors data not yet available, wait and retry!")
                        End If
                        TL.LogMessage("Connected Set", "Humidity = " + hum.ToString())
                    End SyncLock
                    dataUpdateTime = Date.Now
                Catch ex As Exception
                    TL.LogIssue("Connected Set", "Connection exception! " + ex.Message)
                    connectedState = False
                    Throw New DriverException("Could not connect to ThunderFocus!")
                End Try
                If connectedState = False Then
                    Throw New DriverException("This ThunderFocus board doesn't have a temperature sensor!")
                End If
            Else
                SyncLock helper
                    helper.Disconnect()
                End SyncLock
                dataUpdateTime = Nothing
                connectedState = False
            End If
        End Set
    End Property

    Public ReadOnly Property Description As String Implements IObservingConditions.Description
        Get
            Dim d As String = driverDescription
            TL.LogMessage("Description Get", d)
            Return d
        End Get
    End Property

    Public ReadOnly Property DriverInfo As String Implements IObservingConditions.DriverInfo
        Get
            Dim m_version As Version = Reflection.Assembly.GetExecutingAssembly().GetName().Version
            Dim s_driverInfo As String = "ThunderFocus focuser bridge v" + m_version.Major.ToString() + "." + m_version.Minor.ToString()
            TL.LogMessage("DriverInfo Get", s_driverInfo)
            Return s_driverInfo
        End Get
    End Property

    Public ReadOnly Property DriverVersion() As String Implements IObservingConditions.DriverVersion
        Get
            TL.LogMessage("DriverVersion Get", Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2))
            Return Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2)
        End Get
    End Property

    Public ReadOnly Property InterfaceVersion() As Short Implements IObservingConditions.InterfaceVersion
        Get
            TL.LogMessage("InterfaceVersion Get", "1")
            Return 1
        End Get
    End Property

    Public ReadOnly Property Name As String Implements IObservingConditions.Name
        Get
            Return "ThunderFocus weather"
        End Get
    End Property

    Public Sub Dispose() Implements IObservingConditions.Dispose
        TL.LogMessage("Dispose", "Disposing...")
        Try
            SyncLock helper
                helper.Disconnect()
            End SyncLock
            connectedState = False
        Catch ex As Exception
            TL.LogIssue("Dispose", "Exception while disconnecting: " + ex.Message)
        End Try
        TL.Enabled = False
        TL.Dispose()
        TL = Nothing
    End Sub

#End Region

#Region "IObservingConditions Implementation"

    Public Property AveragePeriod() As Double Implements IObservingConditions.AveragePeriod
        Get
            CheckConnected("AveragePeriod")
            Return 0.0
        End Get
        Set(value As Double)
            CheckConnected("AveragePeriod")
            ' Do nothing
            ' Check if value is negative for ASCOM Conformance
            If value < 0.0 Then
                Throw New InvalidValueException("Negative average period!")
            End If
        End Set
    End Property

    Public ReadOnly Property CloudCover() As Double Implements IObservingConditions.CloudCover
        Get
            Throw New PropertyNotImplementedException("CloudCover", False)
        End Get
    End Property

    Public ReadOnly Property DewPoint() As Double Implements IObservingConditions.DewPoint
        Get
            Throw New PropertyNotImplementedException("DewPoint", False)
        End Get
    End Property

    Public ReadOnly Property Humidity() As Double Implements IObservingConditions.Humidity
        Get
            CheckConnected("Attemped Humidity while disconnected!")
            Try
                SyncLock helper
                    helper.SocketSend("GetHumidity")
                    Dim rcv As String = helper.SocketRead()
                    If Not String.IsNullOrEmpty(rcv) Then
                        hum = Double.Parse(rcv, CultureInfo.InvariantCulture)
                    End If
                    TL.LogMessage("Humidity Get", hum.ToString())
                End SyncLock
            Catch ex As Exception
                TL.LogIssue("Humidity Get", "Exception: " + ex.Message)
            End Try
            Return hum
        End Get
    End Property

    Public ReadOnly Property Pressure() As Double Implements IObservingConditions.Pressure
        Get
            Throw New PropertyNotImplementedException("Pressure", False)
        End Get
    End Property

    Public ReadOnly Property RainRate() As Double Implements IObservingConditions.RainRate
        Get
            Throw New PropertyNotImplementedException("RainRate", False)
        End Get
    End Property

    Public ReadOnly Property SkyBrightness() As Double Implements IObservingConditions.SkyBrightness
        Get
            Throw New PropertyNotImplementedException("SkyBrightness", False)
        End Get
    End Property

    Public ReadOnly Property SkyQuality() As Double Implements IObservingConditions.SkyQuality
        Get
            Throw New PropertyNotImplementedException("SkyQuality", False)
        End Get
    End Property

    Public ReadOnly Property StarFWHM() As Double Implements IObservingConditions.StarFWHM
        Get
            Throw New PropertyNotImplementedException("StarFWHM", False)
        End Get
    End Property

    Public ReadOnly Property SkyTemperature() As Double Implements IObservingConditions.SkyTemperature
        Get
            Throw New PropertyNotImplementedException("SkyTemperature", False)
        End Get
    End Property

    Public ReadOnly Property Temperature() As Double Implements IObservingConditions.Temperature
        Get
            CheckConnected("Attemped Temperature while disconnected!")
            Try
                SyncLock helper
                    helper.SocketSend("GetTemperature")
                    Dim rcv As String = helper.SocketRead()
                    If Not String.IsNullOrEmpty(rcv) Then
                        temp = Double.Parse(rcv, CultureInfo.InvariantCulture)
                    End If
                    TL.LogMessage("Temperature Get", temp.ToString())
                End SyncLock
            Catch ex As Exception
                TL.LogIssue("Temperature Get", "Exception: " + ex.Message)
            End Try
            Return temp
        End Get
    End Property

    Public ReadOnly Property WindDirection() As Double Implements IObservingConditions.WindDirection
        Get
            Throw New PropertyNotImplementedException("WindDirection", False)
        End Get
    End Property

    Public ReadOnly Property WindGust() As Double Implements IObservingConditions.WindGust
        Get
            Throw New PropertyNotImplementedException("WindGust", False)
        End Get
    End Property

    Public ReadOnly Property WindSpeed() As Double Implements IObservingConditions.WindSpeed
        Get
            Throw New PropertyNotImplementedException("WindSpeed", False)
        End Get
    End Property

    Private Function HasProperty(name As String) As Boolean
        name = name.Trim().ToLowerInvariant()
        For Each p As String In myProperties
            If p.Equals(name) Then
                Return True
            End If
        Next
        Return False
    End Function

    Public Function TimeSinceLastUpdate(PropertyName As String) As Double Implements IObservingConditions.TimeSinceLastUpdate
        CheckConnected("TimeSinceLastUpdate")
        If String.IsNullOrEmpty(PropertyName) Then
            If IsNothing(dataUpdateTime) Then
                Throw New DriverException("dataUpdateTime = null")
            End If
            Return (Date.Now - dataUpdateTime).TotalSeconds
        ElseIf HasProperty(PropertyName) Then
            If IsNothing(dataUpdateTime) Then
                Throw New DriverException("dataUpdateTime = null")
            End If
            Return (Date.Now - dataUpdateTime).TotalSeconds
        Else
            Throw New MethodNotImplementedException("Property not implemented")
        End If
    End Function

    Public Function SensorDescription(PropertyName As String) As String Implements IObservingConditions.SensorDescription
        Select Case PropertyName.Trim.ToLowerInvariant()
            Case "averageperiod"
                Return "Not implemented, data is instantaneous."
            Case "temperature"
                Return "Outside temperature in °C."
            Case "humidity"
                Return "Relative humidity in %."
            Case Else
                Throw New MethodNotImplementedException("Property not implemented")
        End Select
    End Function

    Public Sub Refresh() Implements IObservingConditions.Refresh
        Throw New MethodNotImplementedException("Refresh")
    End Sub

#End Region

#Region "Private properties and methods"

#Region "ASCOM Registration"

    Private Shared Sub RegUnregASCOM(bRegister As Boolean)
        Using P As New Profile() With {.DeviceType = "ObservingConditions"}
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
    ''' Use this function to throw an exception if we aren't connected to the hardware
    ''' </summary>
    ''' <param name="message"></param>
    Private Sub CheckConnected(message As String)
        If Not connectedState Then
            Throw New NotConnectedException(message)
        End If
    End Sub

    ''' <summary>
    ''' Read the device configuration from the ASCOM Profile store
    ''' </summary>
    Friend Sub ReadProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "ObservingConditions"
            debug = Convert.ToBoolean(driverProfile.GetValue(driverID, DriverHelper.debugProfileName, String.Empty, DriverHelper.debugDefault))
            socketPort = Integer.Parse(driverProfile.GetValue(driverID, DriverHelper.socketPortProfileName, String.Empty, DriverHelper.socketPortDefault))
        End Using
    End Sub

    ''' <summary>
    ''' Write the device configuration to the  ASCOM  Profile store
    ''' </summary>
    Friend Sub WriteProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "ObservingConditions"
            driverProfile.WriteValue(driverID, DriverHelper.debugProfileName, debug.ToString())
            driverProfile.WriteValue(driverID, DriverHelper.socketPortProfileName, socketPort.ToString())
        End Using

    End Sub

#End Region

End Class