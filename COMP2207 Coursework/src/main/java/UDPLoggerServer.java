import java.io.*;
import java.net.*;

public class UDPLoggerServer {

    private int port;
    private final PrintStream ps;
    private Thread receiveThread;
    private Thread sendThread;

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        new UDPLoggerServer(Integer.valueOf(args[0]));
    }

    /*
    Create a new UDPLoggerServer
    Initialise a PrintStream and receiveThread
     */
    public UDPLoggerServer(int port) throws FileNotFoundException, InterruptedException {
        this.port = port;
        ps = new PrintStream("logfile.txt");
        receiveThread = new Thread(new UDPReceiver2(this, port));
        receiveThread.start();
        while(true) {
            Thread.sleep(100);
        }
    }

    /*
     Get a message from the receiveThread
     Log message to logfile.txt
     */
    public void getMessage(String message) {
        ps.println(port + " " + System.currentTimeMillis() + " " + message);
    }

    /*
    Send an acknowledgement "ACK" to the sender
     */
    public void sendAcknowledgement(int port) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = "ACK".getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {}
    }

    public void connectionAccepted() {}
    public void participantCrashed() {}

}
