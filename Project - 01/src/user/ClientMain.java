package user;

import utils.TCPPayload;

import java.util.Scanner;

import static utils.Utilities.*;

public class ClientMain {
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    public static final int DEFAULT_SERVER_PORT = 9999;

    public static String INIT_MESSAGE = "init";

    public static void main(String[] args) {
        if (!InitializeConnection())
            System.err.println("Failed to connect to server.");
        else {


        }
    }

    private static boolean InitializeConnection() {

        TCPPayload serverResponse;
        byte[] clientResponse;
        String clientMessage;

        AuthenticatedConnection connectionToServer =
                new AuthenticatedConnection(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT);

        connectionToServer.EstablishConnection();
        Scanner reader = new Scanner(System.in);

        System.out.println("Establishing network...");
        System.out.println("Enter your username:");

        clientMessage = reader.nextLine();

        clientResponse = getTCPByteArray(Auth_Phase, Auth_Request, clientMessage.length(), clientMessage);
        serverResponse = connectionToServer.SendRequest(clientResponse);

        if (serverResponse.getType() == Auth_Fail) {
            connectionToServer.TerminateConnection();
            return false;
        }

        while (serverResponse.getType() == Auth_Challenge) {
            System.out.println("Enter your password:");
            clientMessage = reader.nextLine();
            clientResponse = getTCPByteArray(Auth_Phase, Auth_Request, clientMessage.length(), clientMessage);
            serverResponse = connectionToServer.SendRequest(clientResponse);
        }
        if (serverResponse.getType() == Auth_Fail) {
            connectionToServer.TerminateConnection();
            return false;
        } else
            return serverResponse.getType() == Auth_Success;
    }

}
