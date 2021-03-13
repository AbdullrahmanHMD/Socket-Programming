package server;

import user.Client;

import javax.xml.transform.sax.SAXSource;
import java.util.ArrayList;

import java.io.*;
import java.net.*;

public class Server {

    private final Byte Auth_Challenge = 1;
    private final Byte Auth_Fail = 2;
    private final Byte Auth_Success = 3;

    private final ServerSocket authenticationServerSocket, requestServerSocket;
    private final ArrayList<Client> clients;

    public Server(int port) {

        clients = new ArrayList<Client>();
        FillClients();

        try {
            this.authenticationServerSocket = new ServerSocket(port);
            System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            AcceptClient();
        }
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



