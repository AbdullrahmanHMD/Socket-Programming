package server;

import user.Client;

import java.util.ArrayList;

import static utils.Utilities.*;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.regex.Pattern;

public class Server {

    private final ArrayList<Client> clients;
    private ServerSocket authenticationServerSocket, queryServerSocket;
    private DataInputStream reader;
    private DataOutputStream writer;
    private String clientUsername;

    public Server(int port) {

        clients = new ArrayList<Client>();
        FillClients();

        try {
            this.authenticationServerSocket = new ServerSocket(port);
            System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        if (AuthenticateClient()) {
            System.out.println("AUTHENTICATION COMPLETE");
            try {
                this.queryServerSocket = new ServerSocket(QUERY_PORT);
                System.out.println("Server socket successfully opened at: " + Inet4Address.getLocalHost());
                QueryingPhase();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean AuthenticateClient() {
        Socket authenticationSocket = null;
        String serverMessage = "";
        String clientResponse = "";
        String password;

        int authAttempts = 0;
        byte[] serverResponse;
        byte phase;
        byte type;
        int size;

        try {
            authenticationSocket = authenticationServerSocket.accept();
            reader = new DataInputStream(new DataInputStream(authenticationSocket.getInputStream()));
            writer = new DataOutputStream(new DataOutputStream(authenticationSocket.getOutputStream()));

            System.out.println("Client request accepted" + authenticationSocket.getRemoteSocketAddress());

            phase = reader.readByte();
            type = reader.readByte();
            size = reader.readInt();
            clientResponse = new String(reader.readNBytes(size));

            if (!AuthenticateUsername(clientResponse)) {
                serverMessage = "No such user. Authentication failed";
                serverResponse = getRequestByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                writer.write(serverResponse);
            } else {
                clientUsername = clientResponse;
                String failedMessage = "";
                while (authAttempts < 3) {

                    serverMessage = failedMessage + "Enter Your password:";
                    serverResponse = getRequestByteArray(Auth_Phase, Auth_Challenge, serverMessage.length(),
                            serverMessage);
                    writer.write(serverResponse);

                    authenticationSocket.setSoTimeout(PASSWORD_TIMEOUT);

                    try{
                        phase = reader.readByte();
                        type = reader.readByte();
                        size = reader.readInt();
                        clientResponse = new String(reader.readNBytes(size));
                    }
                    catch (SocketTimeoutException e){
                        serverMessage = failedMessage + "Disconnected: Password timeout";
                        serverResponse = getRequestByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                        writer.write(serverResponse);

                        phase = reader.readByte();
                        type = reader.readByte();
                        size = reader.readInt();
                        clientResponse = new String(reader.readNBytes(size));

                        return false;
                    }

                    if (AuthenticatePassword(clientUsername, clientResponse)) {
                        serverMessage = generateToken(clientUsername, (int) (clientUsername.length() * AUTH_TOKEN_LENGTH));
                        serverResponse = getRequestByteArray(Auth_Phase, Auth_Success, serverMessage.length(), serverMessage);
                        writer.write(serverResponse);
                        return true;
                    } else {
                        authAttempts++;
                        failedMessage = String.format("Incorrect password | " + (3 - authAttempts) + " attempt%s left | ", authAttempts == 1 ? "s" : "");
                    }
                }
                serverMessage = "Authentication failed: Too many unsuccessful attempts to authenticate connection";
                serverResponse = getRequestByteArray(Auth_Phase, Auth_Fail, serverMessage.length(), serverMessage);
                writer.write(serverResponse);

                return false;
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void QueryingPhase() {

        URL weatherURL;
        URL apodURL;

        HttpURLConnection apiConnection;

        Socket querySocket = null;
        String serverMessage = "";
        String clientResponse = "";
        String token = "";

        byte[] serverResponse;
        byte phase;
        byte type;
        int size;
        try {
            querySocket = queryServerSocket.accept();

            reader = new DataInputStream(new DataInputStream(querySocket.getInputStream()));
            writer = new DataOutputStream(new DataOutputStream(querySocket.getOutputStream()));

            serverMessage = serverWelcomeMessage(clientUsername);
            serverResponse = getRequestByteArray(Query_Phase, Query_Success, serverMessage.length(), serverMessage);

            writer.write(serverResponse);

            while(true){

                phase = reader.readByte();
                type = reader.readByte();
                size = reader.readInt();
                token = new String(reader.readNBytes(size));

                if(type == Query_Image){

                    apodURL = new URL(APOD_BASE_URL + token);
                    apiConnection = (HttpURLConnection) apodURL.openConnection();
                    apiConnection.setRequestMethod("GET");
                    int status = apiConnection.getResponseCode();

                    String line = "";
                    StringBuilder lines = new StringBuilder();

                    BufferedReader buffredReader = getStreamReader(status, apiConnection);

                    while((line = buffredReader.readLine()) != null){
                            lines.append(line);
                    }
                    String imageURL = getUrlFromText(lines.toString().split("\""));
                    System.out.println(imageURL);
                    buffredReader.close();

                    serverMessage = imageToByteArray(new URL(imageURL)).toString();
                    serverResponse = getRequestByteArray(Query_Phase, Query_Success, serverMessage.length(),
                            serverMessage);

                    writer.write(serverResponse);
                }
                else if(type == Query_Weather){
                    weatherURL = new URL(INSIGHT_BASE_URL);

                    apiConnection = (HttpURLConnection) weatherURL.openConnection();
                    apiConnection.setRequestMethod("GET");
                    int status = apiConnection.getResponseCode();

                    String line = "";
                    StringBuilder lines = new StringBuilder();

                    BufferedReader buffredReader = getStreamReader(status, apiConnection);

                    while((line = buffredReader.readLine()) != null){
                        lines.append(line);
                    }
                    buffredReader.close();
                    serverMessage = filteredWeather(lines.toString());
                    serverResponse = getRequestByteArray(Query_Phase, Query_Success,serverMessage.length(),
                            serverMessage);

                    writer.write(serverResponse);
                }
                else if(type == Query_Exit){
                    serverMessage = "Disconnected from the server.";
                    serverResponse = getRequestByteArray(Query_Phase, Query_Exit,serverMessage.length(),
                            serverMessage);
                    return;
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void FillClients() {
        String[] username = {"Abdul", "Kuze", "Zeyd"};
        String[] passwords = {"1232abc", "1357", "12345"};

        for (int i = 0; i < username.length; i++) {
            this.clients.add(new Client(username[i], passwords[i]));
        }
    }

    private boolean AuthenticateUsername(String username) {
        for (Client c : this.clients) {
            if (c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private boolean AuthenticatePassword(String username, String password) {
        for (Client c : this.clients) {
            if (c.getUsername().equals(username) && c.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    private BufferedReader getStreamReader(int status, HttpURLConnection connection){
        try {
            if(status > 299){
                return new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            else
                return new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }catch (IOException | NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    private static String getUrlFromText(String[] lines) {
        for(String str : lines) {
            if(Pattern.matches(urlRegex, str))
                return str;
        }
        return null;
    }

    private static String filteredWeather(String weather){
        ArrayList<String> weatherList = new ArrayList<>();
        Random rand = new Random();

        String[] weatherText = weather.split("},");
        for(String str : weatherText) {
            if(str.contains("\"PRE\": {") && !str.contains("["))
                weatherList.add(str.substring(str.indexOf("\"PRE\"")).replaceAll("\"PRE\": \\{\\s{5}",
                        "").replaceAll("\"", ""));
        }

        int randomIndex = rand.nextInt(weatherList.size());
        return weatherList.get(randomIndex);
    }
    private static byte[] imageToByteArray(URL url) {
        try {
            InputStream inputStream = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] byteArray = new byte[1024];
            int n = 0;

            while ((n = inputStream.read(byteArray)) != -1){
                outputStream.write(byteArray, 0, n);
            }
            outputStream.close();
            inputStream.close();
            return outputStream.toByteArray();}
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String serverWelcomeMessage(String username){
        return "\nHello " + username + ", welcome to the StratoNet server" +
                "\nYou have access to following queries:" +
                "\n1) Weather on Mars: Type \"Weather\"" +
                "\n2) For the image of the day: Type the date of an image as follows: yyyy-mm-dd\n";
    }
}



