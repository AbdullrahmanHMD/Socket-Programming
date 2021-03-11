package user;

import java.util.Scanner;

public class ClientMain {
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    public static final int DEFAULT_SERVER_PORT = 9999;

    public static void main(String[] args){

        AuthenticatedConnection connectionToServer =
                new AuthenticatedConnection(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT);

        connectionToServer.EstablishConnection();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Establishing network...");
        String message = scanner.nextLine(); // Replace with default request to initialize authentication.

        while (!message.equals("QUIT")){
            System.out.println("Response from server: " + connectionToServer.SentRequest(message));
            message = scanner.nextLine();
        }
        connectionToServer.TerminateConnection();
    }
}
