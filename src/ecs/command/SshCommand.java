package ecs.command;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
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
        JSch shell = new JSch();
        try {
            shell.setKnownHosts("C:\\Users\\daniel\\.ssh\\known_hosts");
        } catch (JSchException e) {
            e.printStackTrace();
            return;
        }
        try {
            String userName = "";
            String host = "";
            shell.addIdentity(Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa").toString());
//            shell.setKnownHosts(Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString());
            Session session = shell.getSession(userName, host, 22);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(1000);
            Channel channel = session.openChannel("shell");
            channel.setOutputStream(System.out);
//            channel.setInputStream(InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8)););
            System.out.println(session.isConnected());
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }
}
