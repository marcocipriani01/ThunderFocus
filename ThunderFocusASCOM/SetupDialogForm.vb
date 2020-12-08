<ComVisible(False)>
Public Class SetupDialogForm

    Private Sub OK_Button_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles OK_Button.Click ' OK button event handler
        Focuser.socketPort = Int(SocketPortSpinner.Value)
        Focuser.traceState = DebugCheckBox.Checked
        DialogResult = DialogResult.OK
        Close()
    End Sub

    Private Sub Cancel_Button_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles Cancel_Button.Click 'Cancel button event handler
        DialogResult = DialogResult.Cancel
        Close()
    End Sub

    Private Sub ShowAscomWebPage(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles PictureBox1.DoubleClick, PictureBox1.Click
        Try
            Process.Start("https://ascom-standards.org/")
        Catch noBrowser As System.ComponentModel.Win32Exception
            If noBrowser.ErrorCode = -2147467259 Then
                MessageBox.Show(noBrowser.Message)
            End If
        Catch other As System.Exception
            MessageBox.Show(other.Message)
        End Try
    End Sub

    Private Sub SetupDialogForm_Load(sender As System.Object, e As System.EventArgs) Handles MyBase.Load ' Form load event handler
        SocketPortSpinner.Value = Focuser.socketPort
        DebugCheckBox.Checked = Focuser.traceState
    End Sub

    Private Sub StartThunderFocusGUIButton_Click(sender As Object, e As EventArgs) Handles StartThunderFocusGUIButton.Click
        Process.Start("C:\Program Files (x86)\ThunderFocus\bin\javaw.exe", """-jar"" ""C:\Program Files (x86)\ThunderFocus\ThunderFocus.jar""")
    End Sub
End Class