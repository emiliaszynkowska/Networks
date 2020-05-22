import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Pinger implements Runnable {

    private Coordinator coordinator;

    public Pinger(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        while (true) {
            for (int participant : coordinator.getParticipantsList()) {
                int success = 0;
                try {
                    Thread.sleep(1000);
                    Socket socket = new Socket(InetAddress.getLocalHost(), participant);
                    success++;
                } catch (Exception e1) {
                }
                try {
                    Thread.sleep(1000);
                    Socket socket = new Socket(InetAddress.getLocalHost(), participant);
                    success++;
                } catch (Exception e2) {
                    try {
                        Thread.sleep(1000);
                        Socket socket = new Socket(InetAddress.getLocalHost(), participant);
                        success++;
                    } catch (Exception e3) {
                    }
                    if (success == 0) {
                        CoordinatorLogger.getLogger().participantCrashed(participant);
                        coordinator.removePart();
                        coordinator.getParticipantsList().remove(participant);
                    }
                }
            }
        }
    }

}
