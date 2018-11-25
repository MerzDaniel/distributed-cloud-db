package ecs.service;

import com.jcraft.jsch.*;
import lib.metadata.ServerData;
import server.kv.CacheType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public final class SshService {
    public static void startKvServer(ServerData sd) throws JSchException, IOException {
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
    }
    private static Session connect(String user, String host) throws JSchException {
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

    private void copyFile(Session session) throws JSchException, SftpException {
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        sftpChannel.put("I:/demo/myOutFile.txt", "/tmp/QA_Auto/myOutFile.zip");
    }


}
