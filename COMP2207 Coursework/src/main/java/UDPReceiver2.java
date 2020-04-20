import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class UDPReceiver2 implements Runnable {

    private UDPLoggerServer parent;
    private int port;

    /*
    Create a new UDPReceiver2
    UDPReceiver2 is used by UDPLoggerServer
    Receives messages and relays to the UDPLoggerServer
     */
    public UDPReceiver2(UDPLoggerServer parent, int port) {
        this.parent = parent;
        this.port = port;
    }

    @Override
    public void run() {
        while(true) {
            try {
                // Receive a datagram packet
                DatagramSocket socket = new DatagramSocket(port);
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength()).trim();
                // Relay the message to the UDPLoggerClient
                parent.getMessage(message);
                socket.close();
                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {}
        }
    }
}
