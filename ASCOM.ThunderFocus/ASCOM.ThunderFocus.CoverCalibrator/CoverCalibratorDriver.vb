'tabs=4
' --------------------------------------------------------------------------------'
' ASCOM CoverCalibrator driver for ThunderFocus
'
' Description:	ASCOM CoverCalibrator driver for ThunderFocus
'
' Implements:	ASCOM CoverCalibrator interface version: 1.0
' Author:		(MRC) Marco Cipriani <marco.cipriani.01@gmail.com>
'
' Edit Log:
'
' Date			Who	Vers	Description
' -----------	---	-----	-------------------------------------------------------
' 26-MAY-2022	MRC	1.0.0	First version
' ---------------------------------------------------------------------------------
'
' Your driver's ID is ASCOM.ThunderFocus.CoverCalibrator
'
#Const Device = "CoverCalibrator"

Option Strict On
Option Infer On

Imports ASCOM.DeviceInterface
Imports ASCOM.Utilities

<Guid("8a4e1828-b0d7-413b-84d9-006da8c93be4")>
<ClassInterface(ClassInterfaceType.None)>
Public Class CoverCalibrator
    Implements ICoverCalibratorV1

    '
    ' Driver ID and descriptive string that shows in the Chooser
    '
    Friend Shared driverID As String = "ASCOM.ThunderFocus.CoverCalibrator"
    Private Shared ReadOnly driverDescription As String = "ThunderFocus CoverCalibrator"

    Friend Shared socketPort As Integer = 5001
    Friend Shared debug As Boolean = False

    Private ReadOnly helper As DriverHelper
    Private connectedState As Boolean = False
    Private TL As TraceLogger

    '
    ' Constructor - Must be public for COM registration!
    '
    Public Sub New()
        ReadProfile() ' Read device configuration from the ASCOM Profile store
        TL = New TraceLogger("", "ThunderFocus_CoverCalibrator") With {
            .Enabled = debug
        }
        TL.LogMessage("CoverCalibrator", "Starting initialisation")
        connectedState = False
        Application.EnableVisualStyles()
        helper = New DriverHelper()
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
            MessageBox.Show("ASCOM bridge running, use the control panel to configure the flat .", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Information)
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
            If value = connectedState Then
                Return
            End If
            If value Then
                TL.LogMessage("Connected Set", "Connecting to port " + socketPort.ToString())
                Try
                    SyncLock helper
                        connectedState = helper.Connect(socketPort, "HasFlat")
                    End SyncLock
                Catch ex As Exception
                    TL.LogIssue("Connected Set", "Connection exception! " + ex.Message)
                    connectedState = False
                    Throw New DriverException("Could not connect to ThunderFocus!")
                End Try
                If connectedState = False Then
                    Throw New DriverException("This ThunderFocus board doesn't have a flat panel!")
                End If
            Else
                SyncLock helper
                    helper.Disconnect()
                End SyncLock
                connectedState = False
            End If
        End Set
    End Property

    Public ReadOnly Property Description As String Implements ICoverCalibratorV1.Description
        Get
            Dim d As String = driverDescription
            TL.LogMessage("Description Get", d)
            Return d
        End Get
    End Property

    Public ReadOnly Property DriverInfo As String Implements ICoverCalibratorV1.DriverInfo
        Get
            Dim m_version As Version = Reflection.Assembly.GetExecutingAssembly().GetName().Version
            Dim s_driverInfo As String = "ThunderFocus flat bridge v" + m_version.Major.ToString() + "." + m_version.Minor.ToString()
            TL.LogMessage("DriverInfo Get", s_driverInfo)
            Return s_driverInfo
        End Get
    End Property

    Public ReadOnly Property DriverVersion() As String Implements ICoverCalibratorV1.DriverVersion
        Get
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
            Return "ThunderFocus flat panel"
        End Get
    End Property

    Public Sub Dispose() Implements ICoverCalibratorV1.Dispose
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

#Region "ICoverCalibrator Implementation"

    Private coverVal As CoverStatus = CoverStatus.Unknown
    Private brightnessVal As Integer = 0
    Private calibratorVal As CalibratorStatus = CalibratorStatus.Unknown

    ''' <summary>
    ''' Returns the state of the device cover, if present, otherwise returns "NotPresent"
    ''' </summary>
    Public ReadOnly Property CoverState() As CoverStatus Implements ICoverCalibratorV1.CoverState
        Get
            CheckConnected("Attemped CoverState while disconnected!")
            Try
                SyncLock helper
                    helper.SocketSend("CoverState")
                    Dim rcv As String = helper.SocketRead()
                    If Not String.IsNullOrEmpty(rcv) Then
                        Select Case rcv
                            Case "Closed"
                                coverVal = CoverStatus.Closed
                            Case "Open"
                                coverVal = CoverStatus.Open
                            Case "NotPresent"
                                coverVal = CoverStatus.NotPresent
                            Case Else
                                TL.LogIssue("CoverState", "Unknown CoverStatus")
                        End Select
                    End If
                    TL.LogMessage("CoverState Get", coverVal.ToString())
                End SyncLock
            Catch ex As Exception
                TL.LogIssue("CoverState Get", "Exception: " + ex.Message)
            End Try
            Return coverVal
        End Get
    End Property

    ''' <summary>
    ''' Initiates cover opening if a cover is present
    ''' </summary>
    Public Sub OpenCover() Implements ICoverCalibratorV1.OpenCover
        CheckConnected("Attemped OpenCover while disconnected!")
        Try
            SyncLock helper
                helper.SocketSend("OpenCover")
                Dim rcv As String = helper.SocketRead()
                If Not String.IsNullOrEmpty(rcv) And rcv.Equals("Error") Then
                    Throw New MethodNotImplementedException("OpenCover")
                End If
            End SyncLock
        Catch ex As Exception
            TL.LogIssue("OpenCover", "Exception: " + ex.Message)
        End Try
    End Sub

    ''' <summary>
    ''' Initiates cover closing if a cover is present
    ''' </summary>
    Public Sub CloseCover() Implements ICoverCalibratorV1.CloseCover
        CheckConnected("Attemped CloseCover while disconnected!")
        Try
            SyncLock helper
                helper.SocketSend("CloseCover")
                Dim rcv As String = helper.SocketRead()
                If Not String.IsNullOrEmpty(rcv) And rcv.Equals("Error") Then
                    Throw New MethodNotImplementedException("CloseCover")
                End If
            End SyncLock
        Catch ex As Exception
            TL.LogIssue("CloseCover", "Exception: " + ex.Message)
        End Try
    End Sub

    ''' <summary>
    ''' Stops any cover movement that may be in progress if a cover is present and cover movement can be interrupted.
    ''' </summary>
    Public Sub HaltCover() Implements ICoverCalibratorV1.HaltCover
        Throw New MethodNotImplementedException("HaltCover")
    End Sub

    ''' <summary>
    ''' Returns the state of the calibration device, if present, otherwise returns "NotPresent"
    ''' </summary>
    Public ReadOnly Property CalibratorState() As CalibratorStatus Implements ICoverCalibratorV1.CalibratorState
        Get
            CheckConnected("Attemped CalibratorState while disconnected!")
            Try
                SyncLock helper
                    helper.SocketSend("CalibratorState")
                    Dim rcv As String = helper.SocketRead()
                    If Not String.IsNullOrEmpty(rcv) Then
                        Select Case rcv
                            Case "Ready"
                                calibratorVal = CalibratorStatus.Ready
                            Case "Off"
                                calibratorVal = CalibratorStatus.Off
                            Case Else
                                TL.LogIssue("CoverState", "Unknown CalibratorStatus")
                        End Select
                    End If
                    TL.LogMessage("CalibratorState Get", calibratorVal.ToString())
                End SyncLock
            Catch ex As Exception
                TL.LogIssue("CalibratorState Get", "Exception: " + ex.Message)
            End Try
            Return calibratorVal
        End Get
    End Property

    ''' <summary>
    ''' Returns the current calibrator brightness in the range 0 (completely off) to <see cref="MaxBrightness"/> (fully on)
    ''' </summary>
    Public ReadOnly Property Brightness As Integer Implements ICoverCalibratorV1.Brightness
        Get
            CheckConnected("Attemped Brightness while disconnected!")
            Try
                SyncLock helper
                    helper.SocketSend("Brightness")
                    Dim rcv As String = helper.SocketRead()
                    If Not String.IsNullOrEmpty(rcv) Then
                        brightnessVal = CInt(rcv)
                    End If
                    TL.LogMessage("Brightness Get", brightnessVal.ToString())
                End SyncLock
            Catch ex As Exception
                TL.LogIssue("Brightness Get", "Exception: " + ex.Message)
            End Try
            Return brightnessVal
        End Get
    End Property

    ''' <summary>
    ''' The Brightness value that makes the calibrator deliver its maximum illumination.
    ''' </summary>
    Public ReadOnly Property MaxBrightness As Integer Implements ICoverCalibratorV1.MaxBrightness
        Get
            Return 255
        End Get
    End Property

    ''' <summary>
    ''' Turns the calibrator on at the specified brightness if the device has calibration capability
    ''' </summary>
    ''' <param name="Brightness"></param>
    Public Sub CalibratorOn(Brightness As Integer) Implements ICoverCalibratorV1.CalibratorOn
        CheckConnected("Attemped CalibratorOn while disconnected!")
        Try
            SyncLock helper
                helper.SocketSend("CalibratorOn=" + Brightness.ToString())
                Dim rcv As String = helper.SocketRead()
                If Not String.IsNullOrEmpty(rcv) And rcv.Equals("Error") Then
                    Throw New MethodNotImplementedException("CalibratorOn")
                End If
            End SyncLock
        Catch ex As Exception
            TL.LogIssue("CalibratorOn", "Exception: " + ex.Message)
        End Try
    End Sub

    ''' <summary>
    ''' Turns the calibrator off if the device has calibration capability
    ''' </summary>
    Public Sub CalibratorOff() Implements ICoverCalibratorV1.CalibratorOff
        CheckConnected("Attemped CalibratorOff while disconnected!")
        Try
            SyncLock helper
                helper.SocketSend("CalibratorOff")
                Dim rcv As String = helper.SocketRead()
                If Not String.IsNullOrEmpty(rcv) And rcv.Equals("Error") Then
                    Throw New MethodNotImplementedException("CalibratorOff")
                End If
            End SyncLock
        Catch ex As Exception
            TL.LogIssue("CalibratorOff", "Exception: " + ex.Message)
        End Try
    End Sub

#End Region

#Region "Private properties and methods"

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
            driverProfile.DeviceType = "CoverCalibrator"
            debug = Convert.ToBoolean(driverProfile.GetValue(driverID, DriverHelper.debugProfileName, String.Empty, DriverHelper.debugDefault))
            socketPort = Integer.Parse(driverProfile.GetValue(driverID, DriverHelper.socketPortProfileName, String.Empty, DriverHelper.socketPortDefault))
        End Using
    End Sub

    ''' <summary>
    ''' Write the device configuration to the  ASCOM  Profile store
    ''' </summary>
    Friend Sub WriteProfile()
        Using driverProfile As New Profile()
            driverProfile.DeviceType = "CoverCalibrator"
            driverProfile.WriteValue(driverID, DriverHelper.debugProfileName, debug.ToString())
            driverProfile.WriteValue(driverID, DriverHelper.socketPortProfileName, socketPort.ToString())
        End Using

    End Sub

#End Region

End Class
