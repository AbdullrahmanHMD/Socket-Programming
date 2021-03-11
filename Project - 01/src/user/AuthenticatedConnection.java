package user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class AuthenticatedConnection {

    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public AuthenticatedConnection(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void EstablishConnection() {
        try {
            socket = new Socket(serverAddress, serverPort);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer = new PrintWriter(socket.getOutputStream());

        } catch (IOException | NullPointerException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public String SentRequest(String message) {
        String response = "";

        try {
            writer.println(message);
            writer.flush();

            response = reader.readLine();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return response;
    }


    public void TerminateConnection() {
        try {
            reader.close();
            writer.close();
            socket.close();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        System.out.println("Disconnected from server.");
    }
}
