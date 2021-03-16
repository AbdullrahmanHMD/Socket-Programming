package utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Utilities {

    // URL and connection-related constants.
//    public static final String API_KEY = "MPI3vpMK3K6dxIoNtMSrBSNbcffs5hb4KzCYx0hY";
    public static final String API_KEY = "DEMO_KEY";
    public static final String APOD_BASE_URL = "https://api.nasa.gov/planetary/apod?api_key=" + API_KEY +"&date=";
    public static final String INSIGHT_BASE_URL = "https://api.nasa.gov/insight_weather/?api_key="
            + API_KEY + "&feedtype=json&ver=1.0/";

    public static final int COMMAND_PORT = 9999;
    public static final int FILE_PORT = 9998;

    public static final String DEFAULT_SERVER_ADDRESS = "localhost";

    // Authentication constants.
    public static final byte Auth_Phase = 0;

    public static final byte Auth_Request = 0;
    public static final byte Auth_Challenge = 1;
    public static final byte Auth_Fail = 2;
    public static final byte Auth_Success = 3;

    public static final double AUTH_TOKEN_LENGTH = 0.5;
    public static final int PASSWORD_TIMEOUT = 7000;

    // Query constants.
    public static final byte Query_Phase = 1;

    public static final byte Query_Image = 4;
    public static final byte Query_Weather = 5;
    public static final byte Query_Success = 6; // Was 7.
    public static final byte Query_Request = 7;
    public static final byte Query_Image_Valid = 8;
    public static final byte Query_Image_Invalid = 9;

    public static final byte Query_Exit = -1;

    // Regular expressions.
    public static final String dateRegex = "[0-9]{4}-[0-1][0-9]-[0-3][0-9]";
    public static final String urlRegex = "https://\\w+\\.\\w+\\.\\w+/apod/image/[0-9]+/\\w+\\.\\w+";

    private static final String TOKEN_SUFFIX = "87";

    // Image-related constants.
    public static final String DEFAULT_IMAGE_PATH = "image_of_the_day.";
    public static final String IMAGE_FORMAT = "jpg";

    /**
     * Given certain parameters, creates a TCPPayload.
     * @param phase     the phase of the request.
     * @param type      the type of the request.
     * @param size      the size of the message.
     * @param payload   the message.
     * @return          a byte array that contains the given parameters.
     */
    public static byte[] getAuthRequestByteArray(byte phase, byte type, int size, String payload) {

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
     * Given certain parameters, creates a TCPPayload.
     * @param phase     the phase of the request.
     * @param type      the type of the request.
     * @param size      the size of the message.
     * @param payload   the message.
     * @return          a byte array that contains the given parameters.
     */
    public static byte[] queryMessage(byte phase, byte type, int size, byte[] payload) {

        int tcpPayloadSize = 1 + 1 + 4 + payload.length;
        byte[] tcpPayload = new byte[tcpPayloadSize];

        tcpPayload[0] = phase;
        tcpPayload[1] = type;

        byte[] byteToInt = ByteBuffer.allocate(4).putInt(size).array();

        for (int i = 0; i < byteToInt.length; i++) {
            tcpPayload[i + 2] = byteToInt[i];
        }

        for (int i = 0; i < payload.length; i++) {
            tcpPayload[i + 6] = payload[i];
        }
        return tcpPayload;
    }

    /**
     * Given certain parameters, creates a TCPPayload.
     * @param phase     the phase of the request.
     * @param type      the type of the request.
     * @param mSize     the size of the message.
     * @param tSize     the size of the token.
     * @param message   the message.
     * @param token     the token.
     * @return          a byte array that contains the given parameters.
     */
    public static byte[] getQueryRequestByteArray(byte phase, byte type, int mSize, int tSize, String message, String token) {

        byte[] messageToByte = message.getBytes(StandardCharsets.UTF_8);
        byte[] tokenToByte = token.getBytes(StandardCharsets.UTF_8);

        int tcpPayloadSize = 1 + 1 + 4 + 4 + messageToByte.length + tokenToByte.length;
        byte[] tcpPayload = new byte[tcpPayloadSize];

        tcpPayload[0] = phase;
        tcpPayload[1] = type;

        byte[] messageSize = ByteBuffer.allocate(4).putInt(mSize).array();
        byte[] tokenSize = ByteBuffer.allocate(4).putInt(tSize).array();

        for (int i = 0; i < messageSize.length; i++) {
            tcpPayload[i + 2] = messageSize[i];
        }

        for (int i = 0; i < tokenSize.length; i++) {
            tcpPayload[i + 6] = tokenSize[i];
        }

        for (int i = 0; i < messageToByte.length; i++) {
            tcpPayload[i + 10] = messageToByte[i];
        }

        for (int i = 0; i < tokenToByte.length; i++) {
            tcpPayload[i + tcpPayloadSize - token.length()] = tokenToByte[i];
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
}
