Option Strict On
Option Infer On

Imports System.Net
Imports System.Net.Sockets
Imports System.Text
Imports System.Threading
Imports ASCOM

Public Class DriverHelper

    Public Shared ReadOnly socketPortProfileName As String = "Socket port"
    Public Shared ReadOnly socketPortDefault As String = "5001"
    Public Shared ReadOnly debugProfileName As String = "Debug"
    Public Shared ReadOnly debugDefault As String = "False"

    Private socket As Socket = Nothing
    Private ReadOnly ipAddress As IPAddress

    Public Sub New()
        Dim ipHostInfo As IPHostEntry = Dns.GetHostEntry(Dns.GetHostName())
        ipAddress = ipHostInfo.AddressList(0)
    End Sub

    Public Function Connect(port As Integer, request As String) As Boolean
        Dim remoteEP As New IPEndPoint(ipAddress, port)
        socket = New Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp) With {
                .NoDelay = True,
                .ReceiveTimeout = 1000,
                .SendTimeout = 1000
            }
        socket.Connect(remoteEP)
        Dim bytesToSend As Byte() = Encoding.UTF8.GetBytes(request + Environment.NewLine)
        socket.SendBufferSize = bytesToSend.Length
        socket.Send(bytesToSend)
        Thread.Sleep(250)
        Dim socketBuffer As Byte() = New Byte(1023) {}
        Dim bytesRec As Integer = socket.Receive(socketBuffer)
        Dim success As Boolean = Encoding.ASCII.GetString(socketBuffer, 0, bytesRec).Contains("true")
        If success = False Then
            Disconnect()
        End If
        Return success
    End Function

    Public Sub Disconnect()
        If IsNothing(socket) Then
            Try
                socket.Shutdown(SocketShutdown.Both)
                socket.Close()
                socket = Nothing
            Catch ex As Exception
                Throw New DriverException("Disconnection error!")
            End Try
        End If
    End Sub

    Public Function SocketRead() As String
        Dim socketBuffer As Byte() = New Byte(1023) {}
        Dim bytesRec As Integer = socket.Receive(socketBuffer)
        Dim rcv As String = Encoding.ASCII.GetString(socketBuffer, 0, bytesRec).Replace("\n", "").Replace("\r", "").Trim()
        Return rcv
    End Function

    Public Sub SocketSend(msg As String)
        Dim bytesToSend As Byte() = Encoding.UTF8.GetBytes(msg + Environment.NewLine)
        socket.SendBufferSize = bytesToSend.Length
        socket.Send(bytesToSend)
    End Sub
End Class