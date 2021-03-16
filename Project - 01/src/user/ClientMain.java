package user;

import utils.TCPPayload;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

import static utils.Utilities.*;

public class ClientMain {


    private static ServerConnection commandConnection;
    private static ServerConnection fileConnection;
    private static String accessToken;

    public static void main(String[] args) {
        if (!InitializeAuthentication())
            System.err.println("Failed to connect to server.");
        else {
            InitializeQuerying();
        }
    }

    /**
     * Initializes the authentication phase.
     * @return true if the authentication is successful and false otherwise.
     */
    private static boolean InitializeAuthentication() {

        TCPPayload serverResponse;
        byte[] clientResponse;
        String clientMessage;

        commandConnection =
                new ServerConnection(DEFAULT_SERVER_ADDRESS, COMMAND_PORT);

        fileConnection =
                new ServerConnection(DEFAULT_SERVER_ADDRESS, FILE_PORT);

        commandConnection.EstablishConnection();
        fileConnection.EstablishConnection();

        Scanner reader = new Scanner(System.in);

        System.out.println("Establishing network...");
        System.out.println("Enter your username:");

        clientMessage = reader.nextLine();

        clientResponse = getAuthRequestByteArray(Auth_Phase, Auth_Request, clientMessage.length(), clientMessage);
        serverResponse = commandConnection.sendRequest(clientResponse);
        // Checks if an Auth_Fail was sent after entering the username.
        if (serverResponse.getType() == Auth_Fail) {
            System.err.println(serverResponse.getMessage());
            commandConnection.TerminateConnection();
            return false;
        }

        while (serverResponse.getType() == Auth_Challenge) {
            System.out.println(serverResponse.getMessage());
            clientMessage = reader.nextLine();
            clientResponse = getAuthRequestByteArray(Auth_Phase, Auth_Request, clientMessage.length(), clientMessage);
            serverResponse = commandConnection.sendRequest(clientResponse);
        }
        // Checks if an Auth_Fail was sent after entering the password.
        if (serverResponse.getType() == Auth_Fail) {
            System.err.println(serverResponse.getMessage());
            commandConnection.TerminateConnection();
            return false;

        } else if (serverResponse.getType() == Auth_Success) {
            System.out.println("Authentication complete!");
            accessToken = serverResponse.getMessage();
            System.out.println("Access Token Generated | Your access token is: " + accessToken);
            return true;
        }
        return false;
    }

    /**
     * Initializes the querying phase.
     */
    private static void InitializeQuerying() {
        TCPPayload serverCommandResponse = null;
        byte[] clientResponse = null;
        String clientMessage = null;
        byte query = 0;

        System.out.println(commandConnection.readFromServer().getMessage());

        Scanner reader = new Scanner(System.in);
        clientMessage = reader.nextLine();
        query = getQuery(clientMessage);

        while (true) {
            while (query == 0) {
                System.err.println("Invalid query, try again");

                clientMessage = reader.nextLine();
                query = getQuery(clientMessage);
            }
            if (query == Query_Image) {
                clientResponse = getQueryRequestByteArray(Query_Phase, query, clientMessage.length(),
                        accessToken.length(), clientMessage, accessToken);

                System.out.println("Fetching image...");
                serverCommandResponse = commandConnection.sendRequest(clientResponse);
                // Checks if a Query_Fail was sent from the server, if so disconnect the client.
                if (serverCommandResponse.getType() == Query_Exit) {
                    System.err.println(serverCommandResponse.getMessage());
                    commandConnection.TerminateConnection();
                    return;
                }
                // Get the hashcode of the image from the server.
                String imageHash = serverCommandResponse.getMessage();

                serverCommandResponse = fileConnection.sendImageRequest(clientResponse);

                boolean imageIsValid = verifyImage(imageHash, serverCommandResponse.getByteMessage());

                if (imageIsValid) {
                    clientResponse = getQueryRequestByteArray(Query_Phase, Query_Image_Valid, clientMessage.length(),
                            accessToken.length(), clientMessage, accessToken);
                    createImage(serverCommandResponse.getByteMessage());
                    showImage(serverCommandResponse.getByteMessage());
                    System.out.println("Image downloaded!");
                } else {
                    clientResponse = getQueryRequestByteArray(Query_Phase, Query_Image_Invalid, clientMessage.length(),
                            accessToken.length(), clientMessage, accessToken);
                }
                serverCommandResponse = commandConnection.sendRequest(clientResponse);
                System.out.println(serverCommandResponse.getMessage());


            } else if (query == Query_Weather) {
                System.out.println("Fetching weather state...");

                clientResponse = getQueryRequestByteArray(Query_Phase, query, clientMessage.length(),
                        accessToken.length(), clientMessage, accessToken);
                serverCommandResponse = commandConnection.sendRequest(clientResponse);

                if (serverCommandResponse.getType() == Query_Exit) {
                    System.err.println(serverCommandResponse.getMessage());
                    commandConnection.TerminateConnection();
                    return;
                }

                System.out.println(serverCommandResponse.getMessage());
            } else if (query == Query_Exit) {
                clientResponse = getQueryRequestByteArray(Query_Phase, query, clientMessage.length(),
                        accessToken.length(), clientMessage, accessToken);
                serverCommandResponse = commandConnection.sendRequest(clientResponse);

                System.err.println(serverCommandResponse.getMessage());
                commandConnection.TerminateConnection();
                return;
            }
            System.out.println("Enter a request:");
            clientMessage = reader.nextLine();
            query = getQuery(clientMessage);
        }
    }

    /**
     * Given a String message, returns the corresponding query type.
     * @param message   the given message.
     * @return          returns the corresponding query type.
     */
    private static byte getQuery(String message) {
        if (Pattern.compile(dateRegex).matcher(message).matches())
            return Query_Image;
        else if (message.toLowerCase().equals("weather"))
            return Query_Weather;
        else if (message.toLowerCase().equals("disconnect"))
            return Query_Exit;
        return 0;
    }

    /**
     * Creates an image from a byte array and places it the programs file.
     * @param byteArray the byte array to create the image from.
     */
    private static void createImage(byte[] byteArray) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            BufferedImage byteImage = ImageIO.read(inputStream);

            ImageIO.write(byteImage, IMAGE_FORMAT, new File(DEFAULT_IMAGE_PATH + IMAGE_FORMAT));

        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifies the image given according to its hashcode.
     * @param receivedImageHashcode the hashcode sent from the server.
     * @param serverImage           the byte array sent from the server.
     * @return                      true if the hashcode provided from the server matches the hashcode of the
     *                              byte array
     */
    private static boolean verifyImage(String receivedImageHashcode, byte[] serverImage) {
        int imageHashcode = Integer.parseInt(receivedImageHashcode);
        return imageHashcode == Arrays.hashCode(serverImage);
    }

    /**
     * Displays an image that is constructed from a byte array.
     * @param imageByteArray    the byte array to construct the image from.
     */
    private static void showImage(byte[] imageByteArray) {
        JFrame frame = new JFrame("Image of the Day");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        ImageIcon image = new ImageIcon(imageByteArray);
        JLabel label = new JLabel();
        label.setIcon(image);

        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();

        frame.setSize(500,500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
