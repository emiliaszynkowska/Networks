import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Participant {

    private int cport;
    private int lport;
    private int pport;
    private int timeout;
    private ArrayList<Integer> participants;
    private ArrayList<String> options;
    private int numberOfVotesReceived;
    private ArrayList<Vote> votesReceived;
    private ArrayList<Vote> votesSent;
    private ArrayList<Vote> votesToSend;
    private Map.Entry<ArrayList<Vote>, Long> outcome;
    private Socket clientSocket;
    PrintWriter out;
    BufferedReader in;

    public synchronized int getNumberOfVotesReceived() { return numberOfVotesReceived; }
    public synchronized void setNumberOfVotesReceived() { numberOfVotesReceived++; }
    public synchronized ArrayList<Vote> getVotesReceived() { return votesReceived; }
    public synchronized ArrayList<Vote> getVotesSent() { return votesSent; }
    public synchronized ArrayList<Vote> getVotesToSend() { return votesToSend; }
    public synchronized void setOutcome(Map.Entry<ArrayList<Vote>, Long> outcome) { this.outcome = outcome; }

    /*
    Get arguments <cport> <lport> <pport> <timeout>
    Create a new Participant
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        new Participant(Integer.valueOf(args[0]),Integer.valueOf(args[1]),Integer.valueOf(args[2]),Integer.valueOf(args[3]));
    }

    /*
    Create a new participant using the given arguments
    Initialise global variables
    Initialise the ParticipantLogger class
     */
    public Participant(int cport, int lport, int pport, int timeout) throws IOException {
        this.cport = cport;
        this.lport = lport;
        this.pport = pport;
        this.timeout= timeout;
        participants = new ArrayList<>();
        options = new ArrayList<>();
        votesReceived = new ArrayList<>();
        votesSent = new ArrayList<>();
        votesToSend = new ArrayList<>();
        long longTime = System.currentTimeMillis();
        int intTime = (int) longTime;
        ParticipantLogger.initLogger(lport,pport,timeout);
        run();
    }

    /*
    Method encapsulating all main functions
    Send a join message of the form JOIN <port>
    Get details from the coordinator
    Get options from the coordinator
    Vote with other participants
    Send the outcome to the coordinator
     */
    public void run() {
        Boolean success = false;
        while(!success) {
            try {
                newSocket(cport);
                out = new PrintWriter(clientSocket.getOutputStream());
                String message = "JOIN " + pport;
                out.println(message);
                ParticipantLogger.getLogger().joinSent(cport);
                out.close();
                clientSocket.close();
                success = true;
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Connection failed. Retrying...");
            }
        }
            getDetails();
            getOptions();
            vote();
            outcome();
        }

    /*
    Get details of other participants as DETAILS [<port>]
     */
    private void getDetails() {
        Boolean received = false;
        while (!received) {
            try {
                newSocket(pport);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ParticipantLogger.getLogger().startedListening();
                String message = in.readLine();
                if (message != null && message.contains("DETAILS")) {
                    String[] messageSplit = message.split(" ");
                    for (int i=1; i<messageSplit.length; i++) {
                        participants.add(Integer.valueOf(messageSplit[i]));
                    }
                    received = true;
                }
                in.close();
                clientSocket.close();
                Thread.sleep(1000);
            } catch (Exception e) {}
        }
        ParticipantLogger.getLogger().detailsReceived(participants);
    }

    /*
    Get vote options from the coordinator as VOTE_OPTIONS [<option>]
     */
    public void getOptions() {
        Boolean received = false;
        while (!received) {
            try {
                newSocket(pport);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ParticipantLogger.getLogger().startedListening();
                String message = in.readLine();
                if (message != null && message.contains("VOTE_OPTIONS")) {
                    String[] messageSplit = message.split(" ");
                    for (int i=1; i<messageSplit.length; i++) {
                        options.add(messageSplit[i].trim());
                    }
                    received = true;
                }
                in.close();
                clientSocket.close();
                Thread.sleep(1000);
            } catch (Exception e) {}
        }
        ParticipantLogger.getLogger().voteOptionsReceived(options);
    }

    /*
    Decide on a vote from the options received
     */
    public String decideInitialVote() {
        Random random = new Random();
        String vote = options.get(random.nextInt(options.size()-1));
        votesToSend.add(new Vote(pport,vote));
        return vote;
    }

    /*
    Send votes to other participants as the byte stream VOTE <port> <vote>
     */
    public void vote() {
        decideInitialVote();

        ArrayList<Integer> ports = new ArrayList<>();
        for (Integer participant : participants) {
            if (participant != pport)
                ports.add(participant);
        }

        // ReceiveThread constantly receives messages via TCP
        Thread receiveThread = new Thread(new TCPReceiver(this, pport));
        receiveThread.start();
        // SendThread sends votes via TCP
        Thread sendThread = new Thread(new TCPSender(this, ports));
        sendThread.start();

        // Start the next round
        int round = participants.size();
        for (int i=0; i<=round; i++) {
            ParticipantLogger.getLogger().beginRound(i+1);

            // Add unsent votes to votesToSend
            while(true) {
                try {
                    for (Vote receivedVote : votesReceived) {
                        if (!(votesToSend.contains(receivedVote))) {
                            votesToSend.add(receivedVote);
                        }
                    }
                    break;
                } catch (ConcurrentModificationException c) {}
            }

            // Clear votes for the next round
            numberOfVotesReceived = 0;

            // Receive votes until the correct number of votes have been received
            while (numberOfVotesReceived < participants.size()-1) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {}
            }

            ParticipantLogger.getLogger().endRound(i+1);
        }
        receiveThread.stop();
        sendThread.stop();
    }

    /*
    Send the outcome to the coordinator as the byte stream OUTCOME <outcome> [<port>]
     */
    public void outcome() {
        Stream.of(votesReceived)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .ifPresent(this::setOutcome);
        String output = outcome.getKey().get(0).getVote();
        int sender = outcome.getKey().get(0).getParticipantPort();
        ParticipantLogger.getLogger().outcomeDecided(output);

        // Send the outcome to the coordinator
        String message = "OUTCOME " + outcome + " " + sender;
        try {
            newSocket(cport);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.write(output, 0, output.length());
            ParticipantLogger.getLogger().outcomeNotified(output);
            out.close();
            clientSocket.close();
        } catch (IOException e) {}
    }

    public void newSocket(int port) {
        try {
            clientSocket = new Socket(InetAddress.getLocalHost(), port);
            ParticipantLogger.getLogger().connectionEstablished(port);
        } catch (IOException e) {}
    }

}
