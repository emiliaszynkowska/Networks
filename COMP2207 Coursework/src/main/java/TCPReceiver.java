import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class TCPReceiver implements Runnable {
    private Participant parent;
    private Integer port;
    private Socket receiveSocket;
    PrintWriter out;
    BufferedReader in;

    /*
    Create a new TCPReceiver
    TCPReceiver is used by Participants to exchange votes
    Implements Runnable and can be run concurrently with TCPSender
     */
    public TCPReceiver(Participant parent, Integer port) {
        this.parent = parent;
        this.port = port;
    }

    @Override
    public synchronized void run() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
        while(true) {
            try {
                // Receive a vote
                receiveSocket = new Socket(InetAddress.getLocalHost(), port);
                in = new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
                String message = in.readLine().trim();

                // Receive a vote as a string
                // Add the vote to the votes HashMap if not added already
                if (message != null && message.contains("VOTE") && !(parent.getVotesReceived().contains(message))) {
                String[] messageSplit = message.split(" ");
                ArrayList<Vote> votes = new ArrayList<>();
                    for (int i=1; i<messageSplit.length; i+=2) {
                        while(true) {
                            try {
                                Vote voteReceived = new Vote(Integer.valueOf(messageSplit[1]),messageSplit[i+1]);
                                votes.add(voteReceived);
                                parent.getVotesReceived().add(voteReceived);
                                break;
                            } catch (ConcurrentModificationException c) {}
                        }
                    }
                    parent.setNumberOfVotesReceived();
                    ParticipantLogger.getLogger().votesReceived(Integer.valueOf(messageSplit[1]),votes);
                }
                in.close();
                receiveSocket.close();
            } catch (IOException e) {}
        }
    }
}