import java.io.*;
import java.net.*;

public class UDPLoggerServer {

    private int port;
    private final PrintStream ps;
    private final Thread receiveThread;
    DatagramSocket socket;
    DatagramPacket packet;
    byte[] buf;

    public static void main(String[] args) throws IOException, InterruptedException {
        new UDPLoggerServer(Integer.valueOf(args[0]));
    }

    /*
    Create a new UDPLoggerServer
    Initialise a PrintStream and receiveThread
     */
    public UDPLoggerServer(int port) throws FileNotFoundException, InterruptedException, IOException {
        this.port = port;
        File file = new File("logfile.txt");
        file.delete();
        ps = new PrintStream("logfile.txt");
        receiveThread = new Thread(new UDPReceiver2(this, port));
        receiveThread.start();
        socket = new DatagramSocket();
        buf = "ACK".getBytes();
        packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), port);
        Thread.sleep(100);
    }

    /*
     Get a message from the receiveThread
     Log message to logfile.txt
     */
    public void getMessage(String message) {
        ps.println(port + " " + System.currentTimeMillis() + " " + message);
        sendAcknowledgement(port);
    }

    /*
    Send an acknowledgement "ACK" to the sender
     */
    public void sendAcknowledgement(int port) {
        try {
            socket.send(packet);
        } catch (IOException e) {}
    }

    public void connectionAccepted() {}
    public void participantCrashed() {}

}
