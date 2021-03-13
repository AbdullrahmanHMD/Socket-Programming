package server;

import user.Client;

import java.util.ArrayList;

import static utils.Utilities.*;

import java.io.*;
import java.net.*;

public class Server {

    private final ArrayList<Client> clients;
    private ServerSocket authenticationServerSocket, requestServerSocket;
    private DataInputStream reader;
    private DataOutputStream writer;


    public Server(int port) {

        clients = new ArrayList<Client>();
        FillClients();

        try {
            this.authenticationServerSocket = new ServerSocket(port);
            System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();

        }
        if(Accept()){
            System.out.println("AUTHENTICATION COMPLETE");
        }
    }

    private boolean Accept() {
        Socket authenticationSocket = null;
        String serverMessage = "";
        String clientResponse = "";

        String username;
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
                serverResponse = getTCPByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                writer.write(serverResponse);
            } else {
                username = clientResponse;

                while(authAttempts < 3){

                serverMessage = "Enter Your password";
                serverResponse = getTCPByteArray(Auth_Phase, Auth_Challenge, serverMessage.length(), serverMessage);
                writer.write(serverResponse);

                phase = reader.readByte();
                type = reader.readByte();
                size = reader.readInt();

                clientResponse = new String(reader.readNBytes(size));

                if(AuthenticatePassword(username, clientResponse)) {
                    password = clientResponse;

                    serverMessage = "Client authenticated. Welcome" + username + "!";
                    serverResponse = getTCPByteArray(Auth_Phase, Auth_Success, serverMessage.length(), serverMessage);
                    return true;
                }else{
                    authAttempts++;
                }
                }
                serverMessage = "Authentication failed: Too many unsuccessful attempts to authenticate connections";
                serverResponse = getTCPByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                return false;
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void AcceptClient() {
        BufferedReader reader = null;
        PrintWriter writer = null;

        Socket authenticationSocket = null;
        String username, password;

        try {
            authenticationSocket = authenticationServerSocket.accept();

            reader = new BufferedReader(new InputStreamReader(authenticationSocket.getInputStream()));
            writer = new PrintWriter(authenticationSocket.getOutputStream());

            String initMessage = reader.readLine();

            writer.println("Client request accepted" + authenticationSocket.getRemoteSocketAddress() +
                    "|| Enter your username:");
            writer.flush();
            username = reader.readLine();
            System.out.println("Username: " + username);

            while (!AuthenticateUsername(username)) {
                writer.println("Username not recognized, try again");
                writer.flush();
                username = reader.readLine();
            }
            writer.println("Enter your password:");
            writer.flush();

            password = reader.readLine();

            while (!AuthenticatePassword(username, password)) {
                writer.println("Server: Incorrect password, try again");
                writer.flush();
                password = reader.readLine();
            }
            System.out.println("Client " + username + " is now connected");

            writer.println("Connection authentication complete. Welcome " + username + "!");
            writer.flush();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void FillClients() {
        String[] username = {"Abdul", "Kuze", "Zeyd"};
        String[] passwords = {"2468", "1357", "12345"};

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



