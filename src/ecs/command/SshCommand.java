package ecs.command;

import com.jcraft.jsch.*;
import ecs.State;

import java.nio.file.Paths;

/**
 * This class represent the command for SSH to {@link server.KVServer} instances
 */
public class SshCommand implements ecs.Command {

    /**
     * Execute the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        try {
            String userName = "clouddb";
            String host = "192.168.178.37";

            Session session = connect(userName, host);

//            copyFile(session);

            Channel channel = session.openChannel("shell");
//            channel.setInputStream(new I);
            channel.setOutputStream(System.out);

//            channel.setInputStream(InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8)););
            System.out.println(session.isConnected());
        } catch (JSchException e) {
            e.printStackTrace();
//        } catch (SftpException e) {
//            System.out.println("sftp exception");
        }
    }

    private void copyFile(Session session) throws JSchException, SftpException {
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        sftpChannel.put("I:/demo/myOutFile.txt", "/tmp/QA_Auto/myOutFile.zip");
    }

    private Session connect(String user, String host) throws JSchException {
        JSch shell = new JSch();
        shell.setKnownHosts("C:\\Users\\daniel\\.ssh\\known_hosts");
        shell.addIdentity(Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa").toString());
//            shell.setKnownHosts(Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString());
        Session session = shell.getSession(user, host, 22);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect(1000);
        return session;
    }
}
