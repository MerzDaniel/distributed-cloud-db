package ecs.service;

import com.jcraft.jsch.*;
import lib.metadata.ServerData;
import lib.server.CacheType;

import java.io.IOException;
import java.nio.file.Paths;

public final class SshService {
    public static void startKvServer(ServerData sd, String userName) throws JSchException, IOException, SftpException {
        String host = sd.getHost();

        Session session = connect(userName, host);

        copyFile(session);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
//        InputStream in = channel.getInputStream();

        String cachingType = " --cache-type " + sd.getCacheType();
        String cachingSize = " --cache-size " + sd.getCacheSize();
        String port = " --port " + sd.getPort();
        String logging = " --log-level ERROR";
        String command = "java -jar server.jar" + cachingType + cachingSize + port + logging + " &";

        channel.setCommand(command);
        channel.connect();

        channel.disconnect();
        session.disconnect();
    }
    private static Session connect(String user, String host) throws JSchException {
        JSch shell = new JSch();
        shell.setKnownHosts(Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString());
        shell.addIdentity(Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa").toString());
//            shell.setKnownHosts(Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString());
        Session session = shell.getSession(user, host, 22);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect(1000);
        return session;
    }

    private static void copyFile(Session session) throws JSchException, SftpException {
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        sftpChannel.put("server.jar", Paths.get(System.getProperty("user.home")).toString());
    }


}
