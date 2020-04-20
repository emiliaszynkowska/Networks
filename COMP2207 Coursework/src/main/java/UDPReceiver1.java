import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class UDPReceiver1 implements Runnable {

    private UDPLoggerClient parent;
    private int port;

    /*
    Create a new UDPReceiver1
    UDPReceiver1 is used by UDPLoggerClient
    Receives acknowledgements from the UDPLoggerServer
    Relays messages to the UDPLoggerClient
     */
    public UDPReceiver1(UDPLoggerClient parent, int port) {
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
                // If acknowledgement, notify the UDPLoggerClient
                if (message.contains("ACK"))
                    parent.setAck();
                socket.close();
                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {
            }
        }
    }
}
