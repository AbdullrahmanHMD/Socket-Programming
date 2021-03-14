package user;

import utils.TCPPayload;

import java.util.Scanner;
import static utils.Utilities.*;

public class ClientMain {

    private static String accessToken;

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
                new AuthenticatedConnection(DEFAULT_SERVER_ADDRESS, DEFAULT_PORT);

        connectionToServer.EstablishConnection();
        Scanner reader = new Scanner(System.in);

        System.out.println("Establishing network...");
        System.out.println("Enter your username:");

        clientMessage = reader.nextLine();

        clientResponse = getRequestByteArray(Auth_Phase, Auth_Request, clientMessage.length(), clientMessage);
        serverResponse = connectionToServer.SendRequest(clientResponse);

        if (serverResponse.getType() == Auth_Fail) {
            System.err.println(serverResponse.getMessage());
            connectionToServer.TerminateConnection();
            return false;
        }

        while (serverResponse.getType() == Auth_Challenge) {
            System.out.println(serverResponse.getMessage());
            clientMessage = reader.nextLine();
            clientResponse = getRequestByteArray(Auth_Phase, Auth_Request, clientMessage.length(), clientMessage);
            serverResponse = connectionToServer.SendRequest(clientResponse);
        }
        if (serverResponse.getType() == Auth_Fail) {
            System.err.println(serverResponse.getMessage());
            connectionToServer.TerminateConnection();
            return false;
        } else if (serverResponse.getType() == Auth_Success){
            System.out.println("Authentication complete!");
            accessToken = serverResponse.getMessage();
            System.out.println("Access Token Generated | Your access token is: " + accessToken);
            return true;
        }
        return false;
    }

    private static boolean InitializeQuerying(){
        TCPPayload serverResponse;
        byte[] clientResponse;
        String clientMessage;

        AuthenticatedConnection connectionToServer =
                new AuthenticatedConnection(DEFAULT_SERVER_ADDRESS, DEFAULT_PORT);

        System.out.println(connectionToServer.readFromServer().getMessage());
        connectionToServer.EstablishConnection();

        Scanner reader = new Scanner(System.in);
        clientMessage = reader.nextLine();

        while (!clientMessage.equals("QUIT")){
//
//            clientResponse = getRequestByteArray();
//            serverResponse = connectionToServer.SendRequest();
//

        }


        return false;
    }
}
