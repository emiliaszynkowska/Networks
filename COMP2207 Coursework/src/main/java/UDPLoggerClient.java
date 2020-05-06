import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPLoggerClient {

	private final int loggerServerPort;
	private final int processId;
	private final int timeout;
	private Boolean ack;
	DatagramSocket sendSocket;
	DatagramSocket receiveSocket;
	private final Thread receiveThread;

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
		boolean success = false;
		while (!success) {
			try {
				sendSocket = new DatagramSocket();
				//receiveSocket = new DatagramSocket(loggerServerPort);
				success = true;
			} catch (IOException e) {}
		}
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
			byte[] bufSend = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(bufSend, bufSend.length, InetAddress.getLocalHost(), loggerServerPort);
			sendSocket.send(sendPacket);
			counter++;
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {}
		}
	}

}
