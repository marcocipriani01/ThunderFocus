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

Imports System.Net
Imports System.Net.Sockets
Imports System.Threading
Imports ASCOM.DeviceInterface
Imports ASCOM.Utilities

<Guid("85c45996-e7eb-4ff2-bd4a-4e7385acca5b")>
<ClassInterface(ClassInterfaceType.None)>
Public Class ObservingConditions
    Implements IObservingConditions

    '
    ' Driver ID and descriptive string that shows in the Chooser
    '
    Friend Shared driverID As String = "ASCOM.SimpleSQM.ObservingConditions"
    Private Shared ReadOnly driverDescription As String = "SimpleSQM"

    Friend Shared comPortProfileName As String = "COM Port"
    Friend Shared comPort As String

    Friend Shared debugProfileName As String = "Debug"
    Friend Shared debug As Boolean

    Friend Shared limitMagProfileName As String = "Limit magnitude"
    Friend Shared limitMag As Double = 19.0

    Friend WithEvents Serial As New IO.Ports.SerialPort()
    Private WithEvents UpdateTimer As New Threading.Timer(AddressOf updateTimer_Tick, Nothing, Timeout.Infinite, Timeout.Infinite)

    Private sqmValue As Double = -1.0
    Private sqmUpdateTime As DateTime = Nothing

    Private connectedState As Boolean
    Private TL As TraceLogger

    '
    ' Constructor - Must be public for COM registration!
    '
    Public Sub New()
        ReadProfile()
        TL = New TraceLogger("", "SimpleSQM") With {
            .Enabled = debug
        }
        Application.EnableVisualStyles()
        connectedState = False
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
        If IsConnected Then
            MessageBox.Show("Gi� connesso.", "SimpleSQM", MessageBoxButtons.OK, MessageBoxIcon.Information)
        End If
        Using dialog As SimpleSQM = New SimpleSQM()
            Dim result As DialogResult = dialog.ShowDialog()
            If result = DialogResult.OK Then
                TL.Enabled = debug
                WriteProfile()
            End If
        End Using
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
            TL.LogMessage("Connected Get", IsConnected.ToString())
            Return IsConnected
        End Get
        Set(value As Boolean)
            TL.LogMessage("Connected Set", value.ToString())
            If value = connectedState Then
                Return
            End If
            If value Then
                If Serial.IsOpen Then
                    Throw New DriverException("Already connected!")
                End If
                If comPort = "" Then
                    Throw New DriverException("Set the serial port first.")
                End If
                TL.LogMessage("Connected Set", "Connecting to port " + comPort)
                Serial.PortName = comPort
                Serial.NewLine = vbCr
                'serial.DtrEnable = True
                'serial.RtsEnable = True
                Serial.ReadTimeout = 8000
                Serial.BaudRate = 115200
                Serial.Open()
                Thread.Sleep(200)
                Serial.WriteLine(">")
                Thread.Sleep(800)
                Dim msg As String
                Try
                    msg = Serial.ReadLine().Replace(vbCr, "").Trim()
                Catch tex1 As TimeoutException
                    Serial.WriteLine(">")
                    Thread.Sleep(2000)
                    Try
                        msg = Serial.ReadLine().Replace(vbCr, "").Trim()
                    Catch tex2 As TimeoutException
                        Serial.Close()
                        Throw New DriverException("No SimpleSQM device detected on the selected port!")
                        Return
                    End Try
                End Try
                TL.LogMessage("SerialPort", msg)
                If msg.StartsWith("<") Then
                    sqmValue = Double.Parse(msg.Substring(1), CultureInfo.InvariantCulture) - 19.0 + limitMag
                    TL.LogMessage("SQM", sqmValue.ToString())
                    sqmUpdateTime = Date.Now
                    UpdateTimer.Change(15000, 15000)
                    connectedState = True
                Else
                    Serial.Close()
                    Throw New DriverException("No SimpleSQM device detected on the selected port!")
                    Return
                End If
            Else
                TL.LogMessage("Connected Set", "Disconnecting from port " + comPort)
                If Serial.IsOpen Then
                    Serial.Close()
                End If
                UpdateTimer.Change(Timeout.Infinite, Timeout.Infinite)
                sqmValue = -1.0
                sqmUpdateTime = Nothing
                connectedState = False
            End If
        End Set
    End Property

    Private Sub serialPort_DataReceived(sender As Object, e As IO.Ports.SerialDataReceivedEventArgs) Handles Serial.DataReceived
        If connectedState Then
            Thread.Sleep(100)
            Try
                While Serial.BytesToRead > 0
                    Dim msg As String = Serial.ReadExisting()
                    TL.LogMessage("SerialPort", msg)
                    If msg.StartsWith("<") Then
                        sqmValue = Double.Parse(msg.Substring(1), CultureInfo.InvariantCulture) - 19.0 + limitMag
                        TL.LogMessage("SQM", sqmValue.ToString())
                        sqmUpdateTime = Date.Now
                    End If
                End While
            Catch ex As Exception
                TL.LogMessage("SerialPort", ex.Message)
            End Try
        End If
    End Sub

    Private Sub updateTimer_Tick(state As Object)
        If Serial.IsOpen Then
            Serial.WriteLine(">")
        End If
    End Sub

    Public ReadOnly Property Description As String Implements IObservingConditions.Description
        Get
            Return driverDescription
        End Get
    End Property

    Public ReadOnly Property DriverInfo As String Implements IObservingConditions.DriverInfo
        Get
            Return "SimpleSQM ASCOM driver"
        End Get
    End Property

    Public ReadOnly Property DriverVersion() As String Implements IObservingConditions.DriverVersion
        Get
            Return Reflection.Assembly.GetExecutingAssembly.GetName.Version.ToString(2)
        End Get
    End Property

    Public ReadOnly Property InterfaceVersion() As Short Implements IObservingConditions.InterfaceVersion
        Get
            Return 1
        End Get
    End Property

    Public ReadOnly Property Name As String Implements IObservingConditions.Name
        Get
            Return "SimpleSQM"
        End Get
    End Property

    Public Sub Dispose() Implements IObservingConditions.Dispose
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
            Throw New PropertyNotImplementedException("Humidity", False)
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
            CheckConnected("SkyQuality")
            If sqmValue < 0.0 Or sqmValue >= 30.0 Then
                TL.LogMessage("SkyQuality", "Rcv: " + sqmValue.ToString())
                Throw New DriverException("Invalid SQM value received.")
            End If
            Return sqmValue
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
            Throw New PropertyNotImplementedException("Temperature", False)
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

    Public Function TimeSinceLastUpdate(PropertyName As String) As Double Implements IObservingConditions.TimeSinceLastUpdate
        CheckConnected("TimeSinceLastUpdate")
        If String.IsNullOrEmpty(PropertyName) Then
            If IsNothing(sqmUpdateTime) Then
                Throw New DriverException("sqmUpdateTime = null")
            End If
            Return (Date.Now - sqmUpdateTime).TotalSeconds
        ElseIf PropertyName.Trim().ToLowerInvariant().Equals("skyquality") Then
            If IsNothing(sqmUpdateTime) Then
                Throw New DriverException("sqmUpdateTime = null")
            End If
            Return (Date.Now - sqmUpdateTime).TotalSeconds
        Else
            Throw New MethodNotImplementedException("Property not implemented")
        End If
    End Function

    Public Function SensorDescription(PropertyName As String) As String Implements IObservingConditions.SensorDescription
        Select Case PropertyName.Trim.ToLowerInvariant
            Case "averageperiod"
                Return "Not implemented, data is instantaneous."
            Case "skyquality"
                Return "Sky quality measured in magnitudes per square arc second."
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

    Private Shared Sub RegUnregASCOM(ByVal bRegister As Boolean)
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
            driverProfile.DeviceType = "ObservingConditions"
            comPort = driverProfile.GetValue(driverID, comPortProfileName, String.Empty, "")
            debug = Convert.ToBoolean(driverProfile.GetValue(driverID, debugProfileName, String.Empty, False))
            limitMag = Convert.ToDouble(driverProfile.GetValue(driverID, limitMagProfileName, String.Empty, "19.0"))
        End Using
    End Sub

    ''' <summary>
    ''' Write the device configuration to the  ASCOM  Profile store
    ''' </summary>
    Friend Sub WriteProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "ObservingConditions"
            driverProfile.WriteValue(driverID, comPortProfileName, comPort.ToString())
            driverProfile.WriteValue(driverID, debugProfileName, debug.ToString())
            driverProfile.WriteValue(driverID, limitMagProfileName, limitMag.ToString())
        End Using

    End Sub

#End Region

End Class