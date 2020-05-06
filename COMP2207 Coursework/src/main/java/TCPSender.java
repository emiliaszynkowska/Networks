import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Random;

public class TCPSender implements Runnable {
    private Participant parent;
    private ArrayList<Integer> ports;
    private ArrayList<Vote> votesToSend;
    private Socket sendSocket;
    PrintWriter out;
    BufferedReader in;

    /*
    Create a new TCPSender
    TCPSender is used by Participants to exchange votes
    Implements Runnable and can be run concurrently with TCPReceiver
     */
    public TCPSender(Participant parent, ArrayList<Integer> ports) {
        this.parent = parent;
        this.ports = ports;
    }

    @Override
    public synchronized void run() {
        while(true) {
            while(true) {
                try {
                    // Update the HashMap of votesToSend
                    this.votesToSend = (ArrayList<Vote>) parent.getVotesToSend().clone();
                    break;
                } catch (ConcurrentModificationException c) {}
            }

            // Create the vote message
            for (Integer port : ports) {
                String message = "VOTE ";
                for (Vote vote : votesToSend) {
                    message += vote.getParticipantPort() + " " + vote.getVote() + " ";
                }

                // Send a vote to the other participants
                Boolean sent = false;
                //int timeout = 0;
                while (!sent) {
                    try {
//                        Random random = new Random();
//                        Thread.sleep(random.nextInt(3)*1000);
//                        if (timeout > 5) {
//                            parent.timeout(port);
//                        }
//                        timeout += 1;
                        ServerSocket sendServerSocket = new ServerSocket(port);
                        sendSocket = sendServerSocket.accept();
                        out = new PrintWriter(sendSocket.getOutputStream(), true);
                        out.println(message);
                        sent = true;
                        out.close();
                        sendSocket.close();
                        parent.getVotesSent().addAll(votesToSend);
                        ParticipantLogger.getLogger().votesSent(port,votesToSend);
                    } catch (IOException e) {}
                }
            }
        }
    }
}