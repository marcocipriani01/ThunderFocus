package marcocipriani.openfocuser.manager.io;

import com.jcraft.jsch.*;
import marcocipriani.openfocuser.manager.Main;

import java.io.*;
import java.net.NoRouteToHostException;

/**
 * Upload file to SSH server.
 *
 * @author JCraft
 * @author marcocipriani01
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ScpUploader {

    /**
     * Sends a file to a remote SSH server.
     *
     * @param localFile        the local file to send.
     * @param remoteUser       the name of the remote user.
     * @param remoteHost       the remote host.
     * @param remoteFile       where to save in the server.
     * @param userInfoProvider user information (passwords) provider.
     * @throws ConnectionException in case of error.
     */
    public static void send(File localFile, String remoteUser, String remoteHost, String remoteFile, UserInfo userInfoProvider) {
        FileInputStream fis = null;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(remoteUser, remoteHost, 22);

            // Username and password will be given via UserInfo interface.
            session.setUserInfo(userInfoProvider);
            session.connect();

            // Exec 'scp -t remoteFile' remotely
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("scp -t \"" + remoteFile + "\"");

            // Get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            channel.connect();
            if (!check(in)) {
                throw new ConnectionException("Unknown error in SCP!", ConnectionException.Type.UNKNOWN);
            }

            // Send "C0644 fileSize filename", where filename should not include file separators
            out.write(("C0644 " + localFile.length() + " " + localFile.getName() + "\n").getBytes());
            out.flush();
            if (!check(in)) {
                throw new ConnectionException("Remote directory not found or unexpected IO error!", ConnectionException.Type.REMOTE_FILE_NOT_FOUND);
            }

            // Send a content of localFile
            fis = new FileInputStream(localFile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len);
            }
            fis.close();
            fis = null;
            // Send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (!check(in)) {
                throw new ConnectionException("Unknown error in SCP!", ConnectionException.Type.UNKNOWN);
            }
            out.close();

            channel.disconnect();
            session.disconnect();

        } catch (JSchException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoRouteToHostException) {
                throw new ConnectionException(cause.getMessage(), cause, ConnectionException.Type.HOST_NOT_FOUND);
            }
            if (e.getMessage().equals("Auth cancel")) {
                throw new ConnectionException("Operation cancelled by user", ConnectionException.Type.CANCELLED_BY_USER);
            }
            throw new ConnectionException(e.getMessage(), e, ConnectionException.Type.UNKNOWN);

        } catch (IOException e) {
            try {
                if (fis != null) {
                    fis.close();
                }

            } catch (IOException ignored) {

            }
            throw new ConnectionException(e.getMessage(), e, ConnectionException.Type.IO);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean check(InputStream in) throws IOException {
        int result = in.read();
        if (result == 0) {
            return true;
        }
        StringBuilder sb = new StringBuilder();
        int c;
        do {
            c = in.read();
            sb.append((char) c);

        } while (c != '\n');
        Main.err(sb.toString(), true);
        return false;
    }
}