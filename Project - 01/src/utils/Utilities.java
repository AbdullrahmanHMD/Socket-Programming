package utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Utilities {

    public static final byte Auth_Request = 0;
    public static final byte Auth_Challenge = 1;
    public static final byte Auth_Fail = 2;
    public static final byte Auth_Success = 3;

    public static final byte Auth_Phase = 0;
    

    public static byte[] protocolByteArray(byte phase, byte type, int size, String payload) {

        byte[] stringToByte = payload.getBytes(StandardCharsets.UTF_8);

        int tcpPayloadSize = phase + type + 4 + stringToByte.length + 1;
        byte[] tcpPayload = new byte[tcpPayloadSize];

        tcpPayload[0] = phase;
        tcpPayload[1] = type;

        byte[] byteToInt = ByteBuffer.allocate(4).putInt(size).array();

        for (int i = 0; i < 4; i++) {
            tcpPayload[i + 2] = byteToInt[i];
        }

        for (int i = 0; i < stringToByte.length; i++) {
            tcpPayload[i + 6] = stringToByte[i];
        }

        return tcpPayload;
    }




}
