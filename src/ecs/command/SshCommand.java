package ecs.command;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import ecs.State;

public class SshCommand implements ecs.Command {
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
            Session session = shell.getSession(userName, host, 22);
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
