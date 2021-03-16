package server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This class simulates the StratoNet server. It can serve multiple clients at the same time by creating a new Thread
 * for each client.
 */
public class StratoNet {

    private ServerSocket commandServerSocket;
    private ServerSocket fileServerSocket;

    public StratoNet(int commandPort, int filePort) {
        try {
            commandServerSocket = new ServerSocket(commandPort);
            fileServerSocket = new ServerSocket(filePort);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the server by creating a new server thread each time a client wants to connect.
     * Creating a new server is held by the accept() from both server sockets at the initialization of a new server.
     * i.e. the while loop would iterate unless there is a user waiting to connect.
     */
    public void initialize(){
        while (true) {
            try {
                new Server(commandServerSocket.accept(), fileServerSocket.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the server sockets.
     */
    public void terminate(){
        try {
            commandServerSocket.close();
            fileServerSocket.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
