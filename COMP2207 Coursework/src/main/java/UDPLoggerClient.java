import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPLoggerClient {
	
	private final int loggerServerPort;
	private final int processId;
	private final int timeout;
	private Boolean ack;
	private Thread receiveThread;
	private Thread sendThread;

	public int getLoggerServerPort() {
		return loggerServerPort;
	}
	public int getProcessId() {
		return processId;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setAck() { ack = true; }

	/*
	Create a new UDPLoggerClient
	Start a new receiveThread
	ReceiveThread constantly checks for acknowledgements
	 */
	public UDPLoggerClient(int loggerServerPort, int processId, int timeout) {
		this.loggerServerPort = loggerServerPort;
		this.processId = processId;
		this.timeout = timeout;
		receiveThread = new Thread(new UDPReceiver1(this, loggerServerPort));
		receiveThread.start();
	}

	/*
	Send messages to the UDPLoggerServer
	Resend up to 3 times, otherwise throw IOException
	 */
	public void logToServer(String message) throws IOException {
		ack = false;
		int counter = 0;
		while (counter < 3 && ack == false) {
			InetAddress address = InetAddress.getLocalHost();
			DatagramSocket socket = new DatagramSocket();
			byte[] buf = message.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, loggerServerPort);
			socket.send(packet);
			socket.close();
			counter++;
		}
	}

}
