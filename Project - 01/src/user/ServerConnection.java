package user;

import utils.QueryTCPPayload;
import utils.TCPPayload;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import static utils.Utilities.*;

public class ServerConnection {

    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private DataInputStream reader;
    private DataOutputStream writer;

    public ServerConnection(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Initializes the socket and the DataInputStream and the DataOutputStream.
     */
    public void EstablishConnection() {
        try {
            socket = new Socket(serverAddress, serverPort);

            reader = new DataInputStream(new DataInputStream(socket.getInputStream()));
            writer = new DataOutputStream(new DataOutputStream(socket.getOutputStream()));

        } catch (IOException | NullPointerException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Writes the given message into an output stream and then reads from an input stream and finally, returns an
     * object containing the data read from the input stream.
     * @param message   the given message to be written into the output stream.
     * @return          returns an object containing the data read from the input stream.
     */
    public TCPPayload sendRequest(byte[] message) {
        TCPPayload response = null;
        byte phase;
        byte type;
        int size;
        String msg;

        try {
            writer.write(message);

            phase = reader.readByte();
            type = reader.readByte();
            size = reader.readInt();
            msg = new String(reader.readNBytes(size));

            response = new TCPPayload(phase, type, size, msg);

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Writes the given message into an output stream and then reads from an input stream and finally, returns an
     * object containing the data read from the input stream.
     * @param message   the given message to be written into the output stream.
     * @return          returns an object containing the data read from the input stream.
     */
    public QueryTCPPayload sendQueryRequest(byte[] message) {
        QueryTCPPayload response = null;
        byte phase;
        byte type;
        int mSize;
        int tSize;
        String token;
        String msg;

        try {
            writer.write(message);

            phase = reader.readByte();
            type = reader.readByte();
            mSize = reader.readInt();
            tSize = reader.readInt();
            msg = new String(reader.readNBytes(mSize));
            token = new String(reader.readNBytes(tSize));

            response = new QueryTCPPayload(phase, type, mSize, tSize, msg, token);

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Writes the given message into an output stream and then reads from an input stream and finally, returns an
     * object containing the data read from the input stream.
     * @param message   the given message to be written into the output stream.
     * @return          returns an object containing the data read from the input stream.
     */
    public TCPPayload sendImageRequest(byte[] message) {
        TCPPayload response = null;
        byte phase;
        byte type;
        int size;
        byte[] msg;

        try {
            writer.write(message);

            phase = reader.readByte();
            type = reader.readByte();
            size = reader.readInt();
            msg = reader.readNBytes(size);

            response = new TCPPayload(phase, type, size, msg);

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Reads the input written into the input stream and returns an object containing the data read from the input
     * stream.
     * @return  an object containing the data read from the input stream.
     *
     */
    public TCPPayload readFromServer() {
        TCPPayload response = null;
        byte phase;
        byte type;
        int size;
        String msg;
        try {
            phase = reader.readByte();
            type = reader.readByte();
            size = reader.readInt();
            msg = new String(reader.readNBytes(size));
            response = new TCPPayload(phase, type, size, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Closes the input and output streams and the socket.
     */
    public void TerminateConnection() {
        try {
            reader.close();
            writer.close();
            socket.close();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        System.out.println("Disconnected from socket.");
    }
}
