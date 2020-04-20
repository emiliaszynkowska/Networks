import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Coordinator {

    private int port;
    private int lport;
    private int parts;
    private int timeout;
    private ArrayList<String> optionsList;
    private ArrayList<Integer> participantsList;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    PrintWriter out;
    BufferedReader in;

    /*
    Get arguments <port> <lport> <parts> <timeout> [<option>]
    Create a new Coordinator
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<String> optionsInput = new ArrayList<>();
        for (int i=4; i<args.length; i++) {
            optionsInput.add(args[i]);
        }
        new Coordinator(Integer.valueOf(args[0]),Integer.valueOf(args[1]),Integer.valueOf(args[2]),Integer.valueOf(args[3]),optionsInput);
    }

    /*
    Create a new coordinator using the given arguments
    Initialise global variables
    Initialise the CoordinatorLogger class
     */
    public Coordinator(int port, int lport, int parts, int timeout, ArrayList<String> optionsList) throws IOException, InterruptedException {
        this.port = port;
        this.lport = lport;
        this.parts = parts;
        this.timeout = timeout;
        this.optionsList = optionsList;
        participantsList = new ArrayList<>();
        long longTime = System.currentTimeMillis();
        int intTime = (int) longTime;
        CoordinatorLogger.initLogger(lport,port,timeout);
        run();
    }

    /*
    Method encapsulating all main functions
    Receive join messages of the form JOIN <port>
    Send details to the participants
    Send options to the participants
    Receive the outcome from participants
     */
    public void run() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(port);
        addParticipants();
        sendDetails();
        sendOptions();
        getOutcome();
    }

    /*
    Get participants as the byte stream JOIN <port>
     */
    public void addParticipants() throws IOException, InterruptedException {
        int count = 0;
        while(count < parts) {
            try {
                clientSocket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                CoordinatorLogger.getLogger().startedListening(port);
                String message = in.readLine();
                if (message != null && message.contains("JOIN")) {
                    String[] messageSplit = message.split(" ");
                    Integer participant = Integer.valueOf(messageSplit[1]);
                    if (!(participantsList.contains(participant))) {
                        participantsList.add(participant);
                        CoordinatorLogger.getLogger().joinReceived(participant);
                        count++;
                    }
                }
                in.close();
                clientSocket.close();
            } catch (IOException e) {}
        }
        serverSocket.close();
        Thread.sleep(1000);
    }

    /*
    Send participant details as the byte stream DETAILS [<port>]
     */
    public void sendDetails() throws InterruptedException {
        String details = "";
        for (Integer participant : participantsList) {
            details += participant;
            details += " ";
        }
        for (Integer participant : participantsList) {
            Boolean sent = false;
            while (!sent) {
                try {
                    serverSocket = new ServerSocket(participant);
                    clientSocket = serverSocket.accept();
                    CoordinatorLogger.getLogger().connectionAccepted(participant);
                    out = new PrintWriter(clientSocket.getOutputStream());
                    String message = "DETAILS " + details;
                    out.println(message);
                    CoordinatorLogger.getLogger().messageSent(participant,message);
                    out.close();
                    clientSocket.close();
                    serverSocket.close();
                    sent = true;
                } catch (IOException e){}
            }
        }
        CoordinatorLogger.getLogger().detailsSent(port,participantsList);
        Thread.sleep(1000);
    }

    /*
    Send vote options to all participants as the byte stream VOTE_OPTIONS [<option>]
     */
    public void sendOptions() throws InterruptedException {
        String options = "";
        for (String option : optionsList) {
            options += option;
            options += " ";
        }
        for (Integer participant : participantsList) {
            Boolean sent = false;
            while (!sent) {
                try {
                    serverSocket = new ServerSocket(participant);
                    clientSocket = serverSocket.accept();
                    CoordinatorLogger.getLogger().connectionAccepted(participant);
                    out = new PrintWriter(clientSocket.getOutputStream());
                    String message = "VOTE_OPTIONS " + options;
                    out.println(message);
                    CoordinatorLogger.getLogger().messageSent(participant,message);
                    out.close();
                    clientSocket.close();
                    serverSocket.close();
                    sent = true;
                } catch (IOException e){continue;}
            }
        }
        CoordinatorLogger.getLogger().voteOptionsSent(port,optionsList);
        Thread.sleep(1000);
    }

    /*
    Receive outcome from participants as the byte stream OUTCOME <outcome> [<port>]
     */
    public void getOutcome() throws InterruptedException {
        while(true) {
            try {
                serverSocket = new ServerSocket(port);
                clientSocket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                CoordinatorLogger.getLogger().startedListening(port);
                String message = in.readLine();
                if (message.contains("OUTCOME")) {
                    String[] messageSplit = message.split(" ");
                    String outcome = messageSplit[1];
                    ArrayList<String> messageParticipants = new ArrayList<>();
                    Integer sender = null;
                    for (int i = 2; i < messageSplit.length; i++) {
                        messageParticipants.add(messageSplit[i]);
                    }
                    for (Integer participant : participantsList) {
                        if (!(messageParticipants.contains(participant)))
                            sender = participant;
                    }
                    CoordinatorLogger.getLogger().outcomeReceived(sender, outcome);
                    break;
                }
                in.close();
                clientSocket.close();
            } catch (IOException e) {}
        }
    }

}
