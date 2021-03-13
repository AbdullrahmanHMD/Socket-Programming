package user;

import utils.TCPPayload;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class AuthenticatedConnection {

    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private DataInputStream reader;
    private DataOutputStream writer;

    public AuthenticatedConnection(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void EstablishConnection() {
        try {
            socket = new Socket(serverAddress, serverPort);

            reader = new DataInputStream(new DataInputStream(socket.getInputStream()));
            writer = new DataOutputStream(new DataOutputStream(socket.getOutputStream()));

        } catch (IOException | NullPointerException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public TCPPayload SendRequest(byte[] message) {
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


    public void TerminateConnection() {
        try {
            reader.close();
            writer.close();
            socket.close();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        System.out.println("Disconnected from server.");
    }
}
