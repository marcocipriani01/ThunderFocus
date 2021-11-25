<ComVisible(False)>
Public Class SetupDialogForm

    Private Sub OK_Button_Click(sender As Object, e As EventArgs) Handles OK_Button.Click ' OK button event handler
        Focuser.socketPort = Int(SocketPortSpinner.Value)
        Focuser.traceState = DebugCheckBox.Checked
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
            MessageBox.Show(Me, "Could not open the browser.", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Error)
        End Try
    End Sub

    Private Sub SetupDialogForm_Load(sender As Object, e As EventArgs) Handles MyBase.Load ' Form load event handler
        SocketPortSpinner.Value = Focuser.socketPort
        DebugCheckBox.Checked = Focuser.traceState
    End Sub

    Private Sub StartThunderFocusGUIButton_Click(sender As Object, e As EventArgs) Handles StartThunderFocusGUIButton.Click
        Try
            Dim dir As String = Environment.GetEnvironmentVariable("thunderfok")
            Process.Start(dir + "\bin\javaw.exe", """-jar"" """ + dir + "\ThunderFocus.jar""")
        Catch ex As Exception
            MessageBox.Show(Me, "Could not start ThunderFocus!", "ThunderFocus", MessageBoxButtons.OK, MessageBoxIcon.Error)
        End Try
    End Sub
End Class