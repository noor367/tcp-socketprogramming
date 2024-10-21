/*
 * Java multi-threading client with TCP
 * 
 * 11/2023
 * 
 * Adapted from CSE Multi-threaded code file TCPClient.java by Wei Song
 * https://webcms3.cse.unsw.edu.au/COMP3331/23T3/resources/90204
 * */

import java.net.*;
import java.io.*;

public class Client {
    // server host and port number, which would be acquired from command line parameter
    private static String serverHost;
    private static Integer serverPort;
    private Integer udpPort;
    private String username;

    public Client(Integer udpPort) {
        this.udpPort = udpPort;
        this.username = "";
    } 

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("===== Error usage: java Client server_IP server_port client_udp_server_port =====");
            return;
        }

        serverHost = args[0];
        serverPort = Integer.parseInt(args[1]);
        Client client = new Client(Integer.parseInt(args[2]));

        // define socket for client
        Socket clientSocket = new Socket(serverHost, serverPort);

        // define DataInputStream instance which would be used to receive response from the server
        // define DataOutputStream instance which would be used to send message to the server
        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        while (true) {
            ClientHandler clientHandler = new ClientHandler(client, dataInputStream, dataOutputStream, clientSocket);
            clientHandler.run();
            if (true) {
                System.out.println("Good bye");
                clientSocket.close();
                dataOutputStream.close();
                dataInputStream.close();
                break;
            }
        }
    }
}
