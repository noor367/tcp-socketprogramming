/*
 * Java multi-threading server with TCP
 * 
 * 11/2023
 * 
 * Adapted from CSE Multi-threaded code file TCPServer.java by Wei Song
 * https://webcms3.cse.unsw.edu.au/COMP3331/23T3/resources/90204
 * */

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.logging.*;

public class Server {

    // Server constants
    private static ServerSocket serverSocket;
    private static Integer serverPort;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    public static int numFailures;

    private List<String> activeUsers;
    private List<User> users;

    public Server() {
        activeUsers = new ArrayList<>();
        users = new ArrayList<User>();

        // Initialise users with data in credential.txt file
        File credentialFile = new File("credentials.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(credentialFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split("\\s+");
                String username = credentials[0];
                String password = credentials[1];
                users.add(new User(username, password));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adding a client entry to current collection of active users
     * @param client        the ClientThread to be stored
     * @param username      the name of the user being handled by ClientThread
     */
    public synchronized void addClient(ClientThread client, String username) {
        activeUsers.add(username);
        User newClient = findUser(username);
        newClient.setThread(client);
        newClient.setActive(true);
    }

    public synchronized void removeClient(ClientThread client, String username) {
        activeUsers.remove(username);
        User newClient = findUser(username);
        newClient.setThread(null);
        newClient.setActive(false);
    }

    /**
     * Getter for list of active users usernames
     * @return
     */
    public List<String> getActiveUsers() {
        return activeUsers;
    } 

    /**
     * Helper function for returning a User object
     * @param username
     * @return
     */
    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername() == username) {
                return user;
            }
        }
        return null;
    }

    private static void errorMsgs(String error) {
        switch (error) {
            case "initial usage":
                System.out.println("=== Error usage: java Server server_port number_of_consecutive_failed_attempts ===");
                break;
            case "num failures":
                System.out.println("=== Error usage: Invalid number of allowed failed consecutive attempt. The valid value of argument number is an integer between 1 and 5 ===");
                break;
            default:
                System.out.println("=== Error ===");
        }
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            Server.errorMsgs("initial usage");
            return;
        }

        Server server = new Server();

        // acquire port number from command line parameter
        serverPort = Integer.parseInt(args[0]);

        // acquire number_of_consecutive_failed_attempts allowed
        try {
            numFailures = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Server.errorMsgs("num failures");
        }
        if (numFailures < 1 || numFailures > 5) {
            Server.errorMsgs("num failures");
        }

        // define server socket with the input port number, by default the host would be localhost i.e., 127.0.0.1
        serverSocket = new ServerSocket(serverPort);
        // make serverSocket listen connection request from clients
        System.out.println("===== Server is running =====");
        System.out.println("===== Waiting for connection request from clients...=====");

        while (true) {
            // when new connection request reaches the server, then server socket establishes connection
            Socket clientSocket = serverSocket.accept();
            // for each user there would be one thread, all the request/response for that user would be processed in that thread
            // different users will be working in different thread which is multi-threading (i.e., concurrent)
            ClientThread clientThread = new ClientThread(server, clientSocket, numFailures);
            clientThread.run();
        }
    }

}
