package server;

import user.Client;
import utils.QueryTCPPayload;

import javax.management.Query;
import java.util.*;

import static utils.Utilities.*;

import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

public class Server extends Thread {

    private final ArrayList<Client> clients;
    private final HashMap<String, String[]> tokenMap;
    private final Socket commandSocket;
    private final Socket fileSocket;
    private ServerSocket commandServerSocket;
    private ServerSocket fileServerSocket;
    private DataInputStream commandReader;
    private DataOutputStream commandWriter;
    private DataInputStream fileReader;
    private DataOutputStream fileWriter;
    private String clientUsername;
    private InetAddress clientIP;
    private int clientPort;
    private String clientToken;
    private int port;

    public Server(Socket commandSocket, Socket fileSocket) {
        clients = new ArrayList<Client>();
        tokenMap = new HashMap<>();

        this.commandSocket = commandSocket;
        this.fileSocket = fileSocket;

    }

    /**
     * Given a String array, finds the url is this string array and returns it.
     *
     * @param lines the String array to be searched for a url.
     * @return a url retrieved from a String array.
     */
    private static String getUrlFromText(String[] lines) {
        for (String str : lines) {
            if (Pattern.matches(urlRegex, str))
                return str;
        }
        return null;
    }

    /**
     * Given a parsed JSON object, finds the weather information related to the pressure and adds them to an ArrayList,
     * then pick a random entry from the constructed ArrayList and returns it.
     *
     * @param weather a parsed JSON string containing information about the weather on Mars.
     * @return a randomly picked information about the weather on Mars.
     */
    private static String filteredWeather(String weather) {
        ArrayList<String> weatherList = new ArrayList<>();
        Random rand = new Random();

        String[] weatherText = weather.split("},");
        for (String str : weatherText) {
            if (str.contains("\"PRE\": {") && !str.contains("["))
                weatherList.add(str.substring(str.indexOf("\"PRE\"")).replaceAll("\"PRE\": \\{\\s{5}",
                        "").replaceAll("\"", ""));
        }

        int randomIndex = rand.nextInt(weatherList.size());
        return weatherList.get(randomIndex);
    }

    /**
     * Creates a byte array that consists of an image retrieved from a given url.
     *
     * @param url the url to get the image from.
     * @return return a byte array containing the bytes that make up an image retrieved from a url.
     */
    private static byte[] imageToByteArray(URL url) {
        try {
            InputStream inputStream = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] byteArray = new byte[1024];
            int n = 0;

            while ((n = inputStream.read(byteArray)) != -1) {
                outputStream.write(byteArray, 0, n);
            }
            outputStream.close();
            inputStream.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a String as a welcoming message and an instruction menu.
     *
     * @param username the username of the client to include in the welcoming message.
     * @return returns a String as a welcoming message and an instruction menu.
     */
    public static String serverWelcomeMessage(String username) {
        return "\n-------------------------------------------------------------------------------------------------" +
                "\n|\t\t\t=== Hello " + username + ", welcome to the StratoNet server! ===" +
                "\n-------------------------------------------------------------------------------------------------" +
                "\n| You have access to following queries:" +
                "\n| 1) To get the weather on Mars type \"Weather\"" +
                "\n| 2) To get the image of the day type the date of an image as follows: yyyy-mm-dd" +
                "\n| 3) To disconnect from the server simply type \"disconnect\"" +
                "\n-------------------------------------------------------------------------------------------------";
    }

    public void run() {
        FillClients();
        try {
            System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        if (AuthenticateClient()) {
            System.out.println("Authentication Complete!");
            try {
                System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
                QueryingPhase();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the authentication phase.
     *
     * @return true if the authentication is successful and false otherwise.
     */
    private boolean AuthenticateClient() {
        String serverMessage = "";
        String clientResponse = "";
        String password;

        int authAttempts = 0;
        byte[] serverResponse;
        byte phase;
        byte type;
        int size;

        try {

            commandReader = new DataInputStream(new DataInputStream(commandSocket.getInputStream()));
            commandWriter = new DataOutputStream(new DataOutputStream(commandSocket.getOutputStream()));

            System.out.println("Client request accepted" + commandSocket.getRemoteSocketAddress());

            phase = commandReader.readByte();
            type = commandReader.readByte();
            size = commandReader.readInt();
            clientResponse = new String(commandReader.readNBytes(size));

            if (!AuthenticateUsername(clientResponse)) {
                serverMessage = "No such user. Authentication failed";
                serverResponse = getAuthRequestByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                commandWriter.write(serverResponse);
                printDisconnectionMessage(Integer.toString(commandSocket.getPort()),
                        commandSocket.getInetAddress().toString(), "No such user. Authentication failed",
                        true);
            } else {
                clientUsername = clientResponse;
                String failedMessage = "";
                while (authAttempts < 3) {

                    serverMessage = failedMessage + "Enter Your password:";
                    serverResponse = getAuthRequestByteArray(Auth_Phase, Auth_Challenge, serverMessage.length(),
                            serverMessage);
                    commandWriter.write(serverResponse);

                    commandSocket.setSoTimeout(PASSWORD_TIMEOUT);

                    try {
                        phase = commandReader.readByte();
                        type = commandReader.readByte();
                        size = commandReader.readInt();
                        clientResponse = new String(commandReader.readNBytes(size));
                    } catch (SocketTimeoutException e) {

                        commandSocket.setSoTimeout(0);

                        phase = commandReader.readByte();
                        type = commandReader.readByte();
                        size = commandReader.readInt();
                        clientResponse = new String(commandReader.readNBytes(size));

                        serverMessage = "Disconnected: Password timeout";
                        serverResponse = getAuthRequestByteArray(Auth_Phase, Auth_Fail, serverMessage.length(),
                                serverMessage);
                        commandWriter.write(serverResponse);

                        printDisconnectionMessage(Integer.toString(commandSocket.getPort()),
                                commandSocket.getInetAddress().toString(), "Password timeout", true);
                        return false;
                    }
                    commandSocket.setSoTimeout(0);

                    if (AuthenticatePassword(clientUsername, clientResponse)) {

                        serverMessage = generateToken(clientUsername,
                                (int) (clientUsername.length() * AUTH_TOKEN_LENGTH));

                        serverResponse = getAuthRequestByteArray(Auth_Phase, Auth_Success, serverMessage.length(),
                                serverMessage);

                        clientToken = serverMessage;
                        clientPort = commandSocket.getPort();
                        clientIP = commandSocket.getInetAddress();

                        String[] clientInfo = {Integer.toString(clientPort), clientIP.toString()};
                        tokenMap.put(clientToken, clientInfo);

                        commandWriter.write(serverResponse);

                        return true;
                    } else {
                        authAttempts++;
                        failedMessage = String.format("Incorrect password | " + (3 - authAttempts)
                                + " attempt%s left | ", authAttempts == 1 ? "s" : "");
                    }
                }
                serverMessage = "Authentication failed: Too many unsuccessful attempts to authenticate connection";
                serverResponse = getAuthRequestByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                commandWriter.write(serverResponse);

                printDisconnectionMessage(Integer.toString(commandSocket.getPort()),
                        commandSocket.getInetAddress().toString(), "Too many failed attempt to connect",
                        true);

                return false;
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Initializes the querying phase.
     */
    private void QueryingPhase() {

        URL weatherURL;
        URL apodURL;

        HttpURLConnection apiConnection;

        String serverMessage = "";

        byte[] serverResponse;

        QueryTCPPayload clientResponse;

        try {

            fileReader = new DataInputStream(new DataInputStream(fileSocket.getInputStream()));
            fileWriter = new DataOutputStream(new DataOutputStream(fileSocket.getOutputStream()));

            serverMessage = serverWelcomeMessage(clientUsername);
            serverResponse = getAuthRequestByteArray(Query_Phase, Query_Request, serverMessage.length(), serverMessage);

            commandWriter.write(serverResponse);

            while (true) {
                commandSocket.setSoTimeout(QUERY_TIMEOUT);

                //Getting response from the client.
                try {
                    byte phase = commandReader.readByte();
                    byte type = commandReader.readByte();
                    int mSize = commandReader.readInt();
                    int tSize = commandReader.readInt();
                    String message = new String(commandReader.readNBytes(mSize));
                    String token = new String(commandReader.readNBytes(tSize));
                    clientResponse = new QueryTCPPayload(phase, type, mSize, tSize, message, token);
                } catch (SocketTimeoutException e) {
                    commandSocket.setSoTimeout(0);

                    clientResponse = clientQueryResponse(commandReader);

                    serverMessage = "Query timeout";
                    serverResponse = getAuthRequestByteArray(Query_Phase, Query_Exit, serverMessage.length(),
                            serverMessage);

                    commandWriter.write(serverResponse);
                    printDisconnectionMessage(tokenMap.get(clientResponse.getToken())[0],
                            tokenMap.get(clientResponse.getToken())[1], "query timeout",
                            true);
                    return;
                }

                //Checks if the message is from the query phase or not, if not disconnect client.
                if (clientResponse.getPhase() == Auth_Phase) {

                    serverMessage = "INVALID REQUEST PHASE | current phase: querying phase, given: auth phase";
                    serverResponse = getAuthRequestByteArray(Query_Phase, Query_Exit, serverMessage.length(),
                            serverMessage);

                    commandWriter.write(serverResponse);
                    printDisconnectionMessage(tokenMap.get(clientResponse.getToken())[0],
                            tokenMap.get(clientResponse.getToken())[1], "Invalid request phase | current phase: " +
                                    "querying phase, given: auth phase",
                            true);
                    return;
                }
                // Verifying the client token, if not valid, disconnect client.
                if (!verifyToken(clientResponse.getToken(), commandSocket)) {
                    serverMessage = "INVALID TOKEN, Disconnecting from server...";
                    serverResponse = getAuthRequestByteArray(Query_Phase, Query_Exit, serverMessage.length(),
                            serverMessage);

                    commandWriter.write(serverResponse);
                    printDisconnectionMessage(tokenMap.get(clientResponse.getToken())[0],
                            tokenMap.get(clientResponse.getToken())[1], "Invalid token",
                            true);
                    return;
                }
                // Checks if the request is for the Image of the Day.
                if (clientResponse.getType() == Query_Image) {
                    commandSocket.setSoTimeout(0);
                    apodURL = new URL(APOD_BASE_URL + clientResponse.getMessage());

                    apiConnection = (HttpURLConnection) apodURL.openConnection();
                    apiConnection.setRequestMethod("GET");

                    int status = apiConnection.getResponseCode();

                    String line = "";
                    StringBuilder lines = new StringBuilder();

                    BufferedReader buffredReader = getStreamReader(status, apiConnection);

                    while ((line = buffredReader.readLine()) != null) {
                        // Gets everything that could be retrieved from the API response.
                        lines.append(line);
                    }
                    // Gets the image url from the returned String from the API
                    String imageURL = getUrlFromText(lines.toString().split("\""));
                    buffredReader.close();
                    // Converting the image into a byte array.
                    byte[] imageByteArray = imageToByteArray(new URL(imageURL));

                    serverMessage = Integer.toString(Arrays.hashCode(imageByteArray));
                    serverResponse = getAuthRequestByteArray(Query_Phase, Query_Request, serverMessage.length(),
                            serverMessage);
                    commandWriter.write(serverResponse);

                    serverResponse = queryMessage(Query_Phase, Query_Request, imageByteArray.length,
                            imageByteArray);
                    fileWriter.write(serverResponse);

                    clientResponse = clientQueryResponse(commandReader);
                    //Checks the integrity of the sent image, if the image is corrupted sends a message and Query_Exit
                    // to the client. If the image is valid, sends a message and Query_Success to the client.
                    if (clientResponse.getType() == Query_Image_Valid) {
                        serverMessage = "Image validated";
                        serverResponse = getAuthRequestByteArray(Query_Phase, Query_Success, serverMessage.length(),
                                serverMessage);
                    } else {
                        serverMessage = "Image is corrupted | Try another image";
                        serverResponse = getAuthRequestByteArray(Query_Phase, Query_Request, serverMessage.length(),
                                serverMessage);
                    }
                    commandWriter.write(serverResponse);

                }
                // Checks if the request is Weather on Mars.
                else if (clientResponse.getType() == Query_Weather) {
                    commandSocket.setSoTimeout(0);
                    weatherURL = new URL(INSIGHT_BASE_URL);

                    apiConnection = (HttpURLConnection) weatherURL.openConnection();
                    apiConnection.setRequestMethod("GET");
                    int status = apiConnection.getResponseCode();

                    String line = "";
                    StringBuilder lines = new StringBuilder();

                    BufferedReader buffredReader = getStreamReader(status, apiConnection);

                    while ((line = buffredReader.readLine()) != null) {
                        lines.append(line);
                    }
                    buffredReader.close();
                    serverMessage = filteredWeather(lines.toString());
                    serverResponse = getAuthRequestByteArray(Query_Phase, Query_Success, serverMessage.length(),
                            serverMessage);

                    commandWriter.write(serverResponse);

                } else if (clientResponse.getType() == Query_Exit) {
                    serverMessage = "Disconnected from the server.";
                    serverResponse = getAuthRequestByteArray(Query_Phase, Query_Exit, serverMessage.length(),
                            serverMessage);
                    commandWriter.write(serverResponse);

                    printDisconnectionMessage(tokenMap.get(clientResponse.getToken())[0],
                            tokenMap.get(clientResponse.getToken())[1], "Client request",
                            false);

                    return;
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fills in the ArrayList of clients from the clients.txt file located in the Project - 01 folder.
     */
    private void FillClients() {
        try {
            File clients = new File("clients.txt");
            Scanner reader = new Scanner(clients);
            while (reader.hasNextLine()) {
                String username = reader.nextLine();
                String password = reader.nextLine();
                this.clients.add(new Client(username, password));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Given a username, returns true if the username is valid and false otherwise.
     *
     * @param username the username to be validated.
     * @return returns true if the username is valid and false otherwise.
     */
    private boolean AuthenticateUsername(String username) {
        for (Client c : this.clients) {
            if (c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a username and a password, returns true if the password is valid and false otherwise.
     *
     * @param username the username corresponding to the password to check.
     * @param password the password to be checked.
     * @return returns true if the password is valid and false otherwise.
     */
    private boolean AuthenticatePassword(String username, String password) {
        for (Client c : this.clients) {
            if (c.getUsername().equals(username) && c.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given the status of given connection, returns an InputStream if the status is OK and an ErrorStream otherwise.
     *
     * @param status     the status of a given connection.
     * @param connection the connection that would return the InputStream of the ErrorStream.
     * @return returns an InputStream if the status is OK and an ErrorStream otherwise.
     */
    private BufferedReader getStreamReader(int status, HttpURLConnection connection) {
        try {
            if (status > 299) {
                return new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else
                return new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validates a given token.
     *
     * @param token  the token to be verified.
     * @param socket the socket of the user whose token is to be verified.
     * @return true if the token is verified and false otherwise.
     */
    private boolean verifyToken(String token, Socket socket) {
        String clientPort = tokenMap.get(token)[0];
        String clientIP = tokenMap.get(token)[1];

        String port = Integer.toString(socket.getPort());
        String IP = socket.getInetAddress().toString();

        return clientPort.equals(port) && clientIP.equals(IP);
    }

    /**
     * Prints a detailed message about the client whose connection has been terminated.
     *
     * @param port    the port number corresponding to a certain client.
     * @param IP      the IP address corresponding to a certain client.
     * @param reason  a String indicating the reason of the disconnection.
     * @param isError a flag to print the detailed message as an error message or a regular message.
     */
    private void printDisconnectionMessage(String port, String IP, String reason, boolean isError) {
        if (isError) {
            System.err.println("Client with port number: " + port + " and IP: "
                    + IP + " has disconnected from the server\nReason: " + reason + ".");
        } else {
            System.out.println("Client with port number: " + port + " and IP: "
                    + IP + " has disconnected from the server\nReason: " + reason + ".");
        }
    }

    /**
     * Given a DataInputStream object, returns a QueryTCPPayload object that contains the elements the the
     * DataInputStream object has read.
     *
     * @param reader The DataInputStream object
     * @return returns a QueryTCPPayload object that contains the elements the the DataInputStream object has read.
     */
    private QueryTCPPayload clientQueryResponse(DataInputStream reader) {
        try {
            byte phase = reader.readByte();
            byte type = reader.readByte();
            int mSize = reader.readInt();
            int tSize = reader.readInt();
            String message = new String(reader.readNBytes(mSize));
            String token = new String(reader.readNBytes(tSize));

            return new QueryTCPPayload(phase, type, mSize, tSize, message, token);

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();

            return null;
        }}

        /**
         * Adds a new client to the the client list
         *
         * @param username the clients username
         * @param password the clients password.
         */
        private void addClient (String username, String password){
            this.clients.add(new Client(username, password));
        }
    }