/*
 * Helper Class to handle threads
 * Manages and deals with multi-threading
 * 
 * z5417124
 * 11/2023
 * 
 * */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class to handle server client interactions on each thread
 * 
 */
public class ClientThread extends Thread {
    private final Socket socket;
    private final int numFailures;
    private boolean clientAlive = false;
    private String username;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Server server;

    /**
     * Initialises a new ClientHandler to facilitate communication between the
     * server and a client.
     * @param server        the currently running server
     * @param socket        the socket connecting the server to a client to be handled
     * @param numFailures   the number of invalid login attempts before timeout
     */
    public ClientThread(Server server, Socket socket, int numFailures) {
        this.server = server;
        this.socket = socket;
        this.numFailures = numFailures;
        this.username = "";
        try {
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.inputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the logging in of a user
     */
    private boolean authUserLogin() {
        int loginAttempts = numFailures;
        boolean loggedIn = false;
        boolean validUser = false;
        String password = "";
        User user = server.findUser(username);

        // Request username
        while (!validUser) {
            sendMsg("Username: ");
            username = readMsg();
            user = server.findUser(username);
            if (user != null) {
                validUser = true;
                // Check if the valid username can be used to login
                if (server.getActiveUsers().contains(username)) {
                    sendMsg("=== Error: Account already logged in. ===");
                    validUser = false;
                } else if (user.getTimeout()) {
                    sendMsg("=== Error: Consecutive login failures. Please try again later.");
                    validUser = false;
                }
            } else {
                sendMsg("=== Error: Invalid Username ===");
            }
        }

        // Request password
        while (!loggedIn) {
            sendMsg("Password: ");
            password = readMsg();
            if (user.getTimeout()) {
                sendMsg("=== Error: Consecutive login failures. Please try again later.");
            } else if (user.validPassword(password)) {
                loggedIn = true;
                break;
            } else {
                loginAttempts--;
                if (loginAttempts == 0) {
                    user.setTimeout();
                }
            }
        }

        // Successful Login
        return true;
    }

    /**
     * Reads input stream
     * @return the incoming message sent from the user to the server
     */
    public String readMsg() {
        String message = "";
        try {
            message = inputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        return message;
    }

    /**
     * Sends a String message to the user
     * @param message 	the message to send to the user
     */
    public void sendMsg(String message) {
        try {
            outputStream.writeUTF(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    /**
     * Clean up the ClientHandler.
     * Closes socket and its stream threads.
     * Also removes the client and interrupts the thread
     */
    public void close() {
        try {
            server.removeClient(this, username);
            inputStream.close();
            outputStream.close();
            socket.close();
            Thread.currentThread().interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        // get client Internet Address and port number
        String clientAddress = socket.getInetAddress().getHostAddress();
        int clientPort = socket.getPort();
        String clientID = "("+ clientAddress + ", " + clientPort + ")";

        System.out.println("===== New connection created for user - " + clientID);
        clientAlive = true;

        // define the dataInputStream to get message (input) from client
        // DataInputStream - used to acquire input from client
        // DataOutputStream - used to send data to client
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            dataInputStream = new DataInputStream(this.socket.getInputStream());
            dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (clientAlive) {
            try {
                // Authentication
                if (authUserLogin()) {
                    server.addClient(this, username);
                } else {
                    close();
                    return;
                }

                // while (!Thread.currentThread().isInterrupted()) {
                //     String data = readMsg();
                //     if (data.isEmpty()) continue;
                //     String[] splitData = data.split(" ");
                //     switch (splitData[0]) {
                //         case "logout":
                //             if (data.equals("logout")) {
                //                 logoutUser();
                //             } else {
                //                 sendMsg("Invalid command. Use format: logout");
                //             }
                //             break;
                //         case "whoelse":
                //             if (data.equals("whoelse")) {
                //                 displayActiveUsersSince(-1);
                //             } else {
                //                 sendMsg("Invalid command. Use format: whoelse");
                //             }
                //             break;
                //         case "whoelsesince":
                //             // length 2 for whoelsesince and the time
                //             if (splitData.length == 2 && isNumeric(splitData[1])) {
                //                 displayActiveUsersSince(Integer.parseInt(splitData[1]));
                //             } else {
                //                 sendMsg("Invalid command. Use format: whoelsesince <time>");
                //             }
                //             break;
                //         case "broadcast":
                //             // length 2 for broadcast and at least 1 word message
                //             if (splitData.length >= 2) {
                //                 String message = username + ":" + data.substring(splitData[0].length());
                //                 if (!sendBroadcast(message, username)) {
                //                     sendMsg("Your message could not be delivered to some recipients");
                //                 }
                //             } else {
                //                 sendMsg("Invalid command. Use format: broadcast <message>");
                //             }
                //             break;
                //         case "message":
                //             // length 3 for message, user and at least 1 word message
                //             if (splitData.length >= 3) {
                //                 String user = splitData[1];
                //                 if (user.equals(username)) {
                //                     sendMsg("Error: You can't message yourself");
                //                 } else if (server.getUser().containsKey(user)) {
                //                     List<String> messageData = Arrays.asList(splitData).subList(2, splitData.length);
                //                     String message = username + ": " + String.join(" ", messageData);
                //                     if (!sendMsgToUser(message, username, user)) {
                //                         sendMsg("Your message could not be delivered as the recipient has blocked you");
                //                     }
                //                 } else {
                //                     sendMsg("Error: Invalid user");
                //                 }
                //             } else {
                //                 sendMsg("Invalid command. User format: message <user> <message>");
                //             }
                //             break;
                //         case "block":
                //             // length 2 for block and the user's name
                //             if (splitData.length == 2) {
                //                 String user = splitData[1];
                //                 User currUser = server.getUser().get(username);
                //                 if (user.equals(username)) {
                //                     sendMsg("Error: Cannot block yourself");
                //                 } else if (!server.getUser().containsKey(user)) {
                //                     sendMsg("Error: invalid user");
                //                 } else if (currUser.hasBlacklisted(user)) {
                //                     sendMsg(user + " is already blocked");
                //                 } else {
                //                     currUser.addToBlacklist(user);
                //                     sendMsg(user + " is blocked");
                //                 }
                //             } else {
                //                 sendMsg("Invalid command. User format: block <user>");
                //             }
                //             break;
                //         case "unblock":
                //             // length 2 for unblock and the user's name
                //             if (splitData.length == 2) {
                //                 String user = splitData[1];
                //                 User currUser = server.getUser().get(username);
                //                 if (user.equals(username)) {
                //                     sendMsg("Error: You are not blocked");
                //                 } else if (!server.getUser().containsKey(user)) {
                //                     sendMsg("Error: invalid user");
                //                 } else if (!currUser.hasBlacklisted(user)) {
                //                     sendMsg(user + " was not blocked");
                //                 } else {
                //                     currUser.removeFromBlacklist(user);
                //                     sendMsg(user + " is unblocked");
                //                 }
                //             } else {
                //                 sendMsg("Invalid command. User format: unblock <user>");
                //             }
                //             break;
                //         case "setupprivate":
                //             // length 2 for setupprivate and the user's name
                //             if (splitData.length == 2) {
                //                 String user = splitData[1];
                //                 if (user.equals(username)) {
                //                     // notify user they are trying to start P2P with self
                //                     sendMsg("startprivate,self");
                //                 } else if (server.getUser().containsKey(user)) {
                //                     User userData = server.getUser().get(user);
                //                     if (userData.hasBlacklisted(username)) {
                //                         sendMsg("startprivate,blocked");
                //                     } else if (userData.isOnline()) {
                //                         // target client's listening socket for P2P will have the same
                //                         // address and port as the socket used for communication with
                //                         // this server. This is allowed since TCP sockets are defined
                //                         // but source address/port as well as destination
                //                         String ipAddress = userData.getSocket().getInetAddress().getHostAddress();
                //                         int port = userData.getSocket().getPort();
                //                         // send port and address data back to the user, so they can
                //                         // start a P2P session
                //                         String[] message = {"startprivate", user, ipAddress, Integer.toString(port)};
                //                         sendMsg(String.join(",", message));
                //                     } else {
                //                         // notify that target user is offline
                //                         sendMsg("startprivate,offline");
                //                     }
                //                 } else {
                //                     // notify that the target user doesn't exist
                //                     sendMsg("startprivate,invalid");
                //                 }
                //             }
                //             break;
                //         default:
                //             sendMsg("That is not a valid command.");
                //     }
                // }
            } catch (Exception e) {
                System.out.println("===== the user disconnected, user - " + clientID);
                clientAlive = false;
                e.printStackTrace();
            }
        }
    }
}
