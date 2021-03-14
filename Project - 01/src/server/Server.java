package server;

import user.Client;

import java.util.ArrayList;

import static utils.Utilities.*;

import java.io.*;
import java.net.*;

public class Server {

    private final String SERVER_TOKEN = "SERVER";

    private final ArrayList<Client> clients;
    private ServerSocket authenticationServerSocket, queryServerSocket;
    private DataInputStream reader;
    private DataOutputStream writer;
    private String clientUsername;

    public Server(int port) {

        clients = new ArrayList<Client>();
        FillClients();

        try {
            this.authenticationServerSocket = new ServerSocket(port);
            System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();

        }
        if (AuthenticateClient()) {
            System.out.println("AUTHENTICATION COMPLETE");

            try {
                this.queryServerSocket = new ServerSocket(port);
                System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean AuthenticateClient() { // Implement Auth_Fail when client is unresponsive for 10 secs.
        Socket authenticationSocket = null;
        String serverMessage = "";
        String clientResponse = "";


        String password;

        int authAttempts = 0;

        byte[] serverResponse;
        byte phase;
        byte type;
        int size;
        try {
            authenticationSocket = authenticationServerSocket.accept();
            reader = new DataInputStream(new DataInputStream(authenticationSocket.getInputStream()));
            writer = new DataOutputStream(new DataOutputStream(authenticationSocket.getOutputStream()));

            System.out.println("Client request accepted" + authenticationSocket.getRemoteSocketAddress());

            phase = reader.readByte();
            type = reader.readByte();
            size = reader.readInt();
            clientResponse = new String(reader.readNBytes(size));

            if (!AuthenticateUsername(clientResponse)) {
                serverMessage = "No such user. Authentication failed";
                serverResponse = getRequestByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                writer.write(serverResponse);
            } else {
                clientUsername = clientResponse;
                String failedMessage = "";
                while (authAttempts < 3) {
                    serverMessage = failedMessage + "Enter Your password:";
                    serverResponse = getRequestByteArray(Auth_Phase, Auth_Challenge, serverMessage.length(), serverMessage);
                    writer.write(serverResponse);

                    phase = reader.readByte();
                    type = reader.readByte();
                    size = reader.readInt();
                    clientResponse = new String(reader.readNBytes(size));

                    if (AuthenticatePassword(clientUsername, clientResponse)) {
                        serverMessage = generateToken(clientUsername, (int) (clientUsername.length() * AUTH_TOKEN_LENGTH));
                        serverResponse = getRequestByteArray(Auth_Phase, Auth_Success, serverMessage.length(), serverMessage);
                        writer.write(serverResponse);
                        return true;
                    } else {
                        authAttempts++;
                        failedMessage = String.format("Incorrect password | " + (3 - authAttempts) + " attempt%s left | ", authAttempts == 1 ? "s" : "");
                    }
                }
                serverMessage = "Authentication failed: Too many unsuccessful attempts to authenticate connection";
                serverResponse = getRequestByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                writer.write(serverResponse);

                return false;
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void QueryingPhase() {

        Socket querySocket = null;
        String serverMessage = "";
        String clientResponse = "";
        String token = "";

        byte[] serverResponse;
        byte phase;
        byte type;
        int size;
        try {

            querySocket = queryServerSocket.accept();
            reader = new DataInputStream(new DataInputStream(querySocket.getInputStream()));
            writer = new DataOutputStream(new DataOutputStream(querySocket.getOutputStream()));
            serverMessage = serverWelcomeMessage(clientUsername);
            serverResponse = getRequestByteArray(Query_Phase, Query_Success, serverMessage.length(), serverMessage);

            phase = reader.readByte();
            type = reader.readByte();
            size = reader.readInt();
            token = new String(reader.readNBytes(size));


        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void FillClients() {
        String[] username = {"Abdul", "Kuze", "Zeyd"};
        String[] passwords = {"1232abc", "1357", "12345"};

        for (int i = 0; i < username.length; i++) {
            this.clients.add(new Client(username[i], passwords[i]));
        }
    }

    private boolean AuthenticateUsername(String username) {
        for (Client c : this.clients) {
            if (c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private boolean AuthenticatePassword(String username, String password) {
        for (Client c : this.clients) {
            if (c.getUsername().equals(username) && c.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }
}



