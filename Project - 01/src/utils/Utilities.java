package utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Utilities {

    public static final String API_KEY = "MPI3vpMK3K6dxIoNtMSrBSNbcffs5hb4KzCYx0hY";
    public static final String APOD_BASE_URL = "https://api.nasa.gov/planetary/apod?api_key=" + API_KEY +"&date=";
    public static final String INSIGHT_BASE_URL = "https://api.nasa.gov/insight_weather/?api_key="
            + API_KEY + "&feedtype=json&ver=1.0/";

    public static final double AUTH_TOKEN_LENGTH = 0.5;

    public static final int PASSWORD_TIMEOUT = 1000;
    public static final int AUTH_PORT = 9999;
    public static final int QUERY_PORT = 9998;

    public static final String DEFAULT_SERVER_ADDRESS = "localhost";

    public static final byte Auth_Request = 0;
    public static final byte Auth_Challenge = 1;
    public static final byte Auth_Fail = 2;
    public static final byte Auth_Success = 3;

    public static final byte Query_Image = 4;
    public static final byte Query_Weather = 5;
    public static final byte Query_Fail = 6;
    public static final byte Query_Success = 7;

    public static final byte Auth_Phase = 0;
    public static final byte Query_Phase = 1;

    public static final String dateRegex = "[0-9]{4}-[0-1][0-9]-[0-3][0-9]";
    public static final String urlRegex = "https://\\w+\\.\\w+\\.\\w+/apod/image/[0-9]+/\\w+\\.\\w+";

    private static final String TOKEN_SUFFIX = "87";

    public static byte[] getRequestByteArray(byte phase, byte type, int size, String payload) {

        byte[] stringToByte = payload.getBytes(StandardCharsets.UTF_8);

        int tcpPayloadSize = 1 + 1 + 4 + stringToByte.length;
        byte[] tcpPayload = new byte[tcpPayloadSize];

        tcpPayload[0] = phase;
        tcpPayload[1] = type;

        byte[] byteToInt = ByteBuffer.allocate(4).putInt(size).array();

        for (int i = 0; i < byteToInt.length; i++) {
            tcpPayload[i + 2] = byteToInt[i];
        }

        for (int i = 0; i < stringToByte.length; i++) {
            tcpPayload[i + 6] = stringToByte[i];
        }
        return tcpPayload;
    }


    /**
     * Given a username and an endIndex, returns a string that consists of the username concatenated with the
     * a pre-specified suffix. This returned token will be substring whose end is specified by the endIndex variable.
     * @param endIndex Specifies the last index of the token.
     * @return returns a string that consists of the username concatenated with the a pre-specified suffix
     *      This returned token will be substring whose end is specified by the endIndex variable.
     */
    public static String generateToken(String username, int endIndex){
        String token = username.concat(TOKEN_SUFFIX);
        return token.substring(0, endIndex).replaceAll("\\s", "");
    }

    public static String serverWelcomeMessage(String username){
        return "\nHello " + username + ", welcome to the StratoNet server" +
                "\nYou have access to following queries:" +
                "\n1) Weather on Mars: Type Weather" +
                "\n2) For the image of the day: Type the date of the image as follows yyyy-mm-dd\n";
    }
}
