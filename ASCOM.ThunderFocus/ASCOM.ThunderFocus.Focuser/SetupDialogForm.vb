Option Strict On
Option Infer On
Imports MetroFramework

<ComVisible(False)>
Public Class SetupDialogForm

    Private Sub OK_Button_Click(sender As Object, e As EventArgs) Handles OK_Button.Click ' OK button event handler
        Focuser.socketPort = CInt(SocketPortSpinner.Value)
        Focuser.debug = DebugToggle.Checked
        DialogResult = DialogResult.OK
        Close()
    End Sub

    Private Sub Cancel_Button_Click(sender As Object, e As EventArgs) Handles Cancel_Button.Click 'Cancel button event handler
        DialogResult = DialogResult.Cancel
        Close()
    End Sub

    Private Sub ShowAscomWebPage(sender As Object, e As EventArgs) Handles PictureBox1.DoubleClick, PictureBox1.Click
        Try
            Process.Start("https://ascom-standards.org/")
        Catch other As Exception
            MetroMessageBox.Show(Me, "Could not open the browser.", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Warning, 100)
        End Try
    End Sub

    Private Sub ShowMyWebsite(sender As Object, e As EventArgs) Handles PictureBox2.DoubleClick, PictureBox2.Click
        Try
            Process.Start("https://marcocipriani01.github.io/")
        Catch other As Exception
            MetroMessageBox.Show(Me, "Could not open the browser.", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Warning, 100)
        End Try
    End Sub

    Private Sub SetupDialogForm_Load(sender As Object, e As EventArgs) Handles MyBase.Load ' Form load event handler
        SocketPortSpinner.Value = Focuser.socketPort
        DebugToggle.Checked = Focuser.debug
    End Sub
End Class