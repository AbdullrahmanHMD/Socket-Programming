package server;

import user.Client;
import java.util.ArrayList;

import java.io.*;
import java.net.*;

public class Server {

    private final ServerSocket serverSocket;
    private final ArrayList<Client> clients;


    public Server(int port) {

        clients = new ArrayList<>();
        FillClients();

        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            AcceptClient();
        }
    }

    private void AcceptClient() {
        BufferedReader reader;
        PrintWriter writer;

        Socket authenticationSocket;
        String username, password;

        try {
            authenticationSocket = serverSocket.accept();

            reader = new BufferedReader(new InputStreamReader(authenticationSocket.getInputStream()));
            writer = new PrintWriter(authenticationSocket.getOutputStream());

            writer.println("Client request accepted" + authenticationSocket.getRemoteSocketAddress());
            writer.println("Enter your username:");
            username = reader.readLine();
            writer.flush();
            while(!AuthenticateUsername(username)) {
                writer.println("Server: Username not recognized, try again");
                username = reader.readLine();
                writer.flush();
            }

            writer.println("Enter your password:");
            password = reader.readLine();

            writer.flush();
            while(!AuthenticatePassword(username, password)) {
                writer.println("Server: Incorrect password, try again");
                username = reader.readLine();
                writer.flush();
            }

            System.out.println("Client request accepted" + authenticationSocket.getRemoteSocketAddress());
            System.out.println("Your username: " + username);

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void FillClients(){
        String[] username = {"Abdul", "Kuze", "Zeyd", "Sarieh", "Noor", "Mwaffak"};
        String[] passwords = {"2468", "1357", "12345", "556677", "12345", "54321"};

        for (int i = 0; i < username.length; i++){
            this.clients.add(new Client(username[i], passwords[i]));
        }
    }

    private boolean AuthenticateUsername(String username) {
        for(Client c : this.clients){
            if(c.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }

    private boolean AuthenticatePassword(String username, String password){
        for(Client c : this.clients){
            if(c.getUsername().equals(username) && c.getPassword().equals(password)){
                return true;
            }
        }
        return false;
    }
}



