import java.io.BufferedReader;
import java.io.IOException;
import java.net.*;

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
        try {
            DatagramSocket socket = new DatagramSocket(port);
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while(true) {
                // Receive a datagram packet
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength()).trim();
                // Relay the message to the UDPLoggerClient
                if(!message.contains("ACK"))
                    parent.getMessage(message);
                if(message.contains("accepted"))
                    parent.connectionAccepted();
            }
        } catch (IOException e) {}
    }
}
