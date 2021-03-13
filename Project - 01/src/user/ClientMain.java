package user;

import javax.xml.transform.sax.SAXSource;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import static utils.Utilities.*;

public class ClientMain {
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    public static final int DEFAULT_SERVER_PORT = 9999;

    public static String INIT_MESSAGE = "init";

    public static void main(String[] args) {
        if(!InitializeConnection())
            System.err.println("Failed to connect to server.");

    }


    private static boolean InitializeConnection() {

        AuthenticatedConnection connectionToServer =
                new AuthenticatedConnection(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT);

        connectionToServer.EstablishConnection();

        Scanner reader = new Scanner(System.in);
        System.out.println("Establishing network...");

        System.out.println("Enter your username:");

        String message =  reader.nextLine();;

        byte[] tcpPayload = protocolByteArray(Auth_Phase, Auth_Request, message.length(), message);

        String serverResponse = connectionToServer.SentRequest(tcpPayload);
        System.out.println("Response from server: " + serverResponse);

        connectionToServer.TerminateConnection();

        return false;
    }

}
