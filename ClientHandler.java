/*
 * Helper Class to handle clients
 * Manages and deals with multi-threading
 * 
 * z5417124
 * 11/2023
 * 
 * */

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler extends Thread {
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;
    protected Socket socket;
    protected Client client;

    /**
     * Constructor
     * @param client    
     * @param inputStream       
     * @param outputStream       
     * @param socket
     */
    public ClientHandler(Client client, DataInputStream inputStream, DataOutputStream outputStream, Socket socket) {
        this.client = client;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (!Thread.currentThread().isInterrupted()) {
                assert this.inputStream != null;
                assert this.outputStream != null;
                System.out.println(inputStream.readUTF());
                String userInput = reader.readLine();
                outputStream.writeUTF(userInput);
                outputStream.flush();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            close();
            System.exit(0);
        }

    }

    /**
     * Closes the input/output streams, socket, and thread
     */
    public void close() {
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}