package ecs.command;

import com.jcraft.jsch.*;
import ecs.State;
import server.kv.CacheType;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
            System.out.format("ssh is connected: %b\n", session.isConnected());

//            copyFile(session);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            InputStream in = channel.getInputStream();
//            channel.setOutputStream(System.out);
//
            String cachingType = " --cache-type " + CacheType.LFU.name();
            String cachingSize = " --cache-size " + 10;
            String port = " --port " + 40000;
            String command = "java -jar server.jar" + cachingType + cachingSize + port + " &";

            channel.setCommand(command);
            channel.connect();

            channel.disconnect();
            session.disconnect();

        } catch (JSchException e) {
            e.printStackTrace();
//        } catch (SftpException e) {
//            System.out.println("sftp exception");
        } catch (IOException e) {
            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
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
