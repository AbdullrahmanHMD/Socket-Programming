package user;

import utils.TCPPayload;

import java.util.Scanner;
import java.util.regex.Pattern;

import static utils.Utilities.*;

public class ClientMain {

    private static String accessToken;

    public static void main(String[] args) {
        if (!InitializeConnection())
            System.err.println("Failed to connect to server.");
        else {
            InitializeQuerying();
        }
    }

    private static boolean InitializeConnection() {

        TCPPayload serverResponse;
        byte[] clientResponse;
        String clientMessage;

        AuthenticatedConnection connectionToServer =
                new AuthenticatedConnection(DEFAULT_SERVER_ADDRESS, AUTH_PORT);

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
            connectionToServer.TerminateConnection();
            return true;
        }
        return false;
    }

    private static boolean InitializeQuerying(){
        TCPPayload serverResponse;
        byte[] clientResponse;
        String clientMessage;
        byte query;
        AuthenticatedConnection connectionToServer =
                new AuthenticatedConnection(DEFAULT_SERVER_ADDRESS, QUERY_PORT);

        connectionToServer.EstablishConnection();
        System.out.println(connectionToServer.readFromServer().getMessage());

        Scanner reader = new Scanner(System.in);
        clientMessage = reader.nextLine();

        while (!clientMessage.equals("QUIT")){
            query = getQuery(clientMessage);

            while(query == 0){
                System.err.println("Invalid query, try again");
                clientMessage = reader.nextLine();
                query = getQuery(clientMessage);
            }
            System.out.println(query);

            clientResponse = getRequestByteArray(Query_Phase, query, clientMessage.length(), clientMessage);
            serverResponse = connectionToServer.SendRequest(clientResponse);

            System.out.println("Enter a request:");
            clientMessage = reader.nextLine();


        }

        return false;
    }


    private static byte getQuery(String message) {
        if (Pattern.compile(dateRegex).matcher(message).matches())
            return Query_Image;
        else if (message.toLowerCase().equals("weather"))
            return Query_Weather;
        return 0;
    }
}
