package utils;
/**
 * An object to hold the query information,
 */
public class TCPPayload {

    private byte phase, type;
    private int size;
    private String message;
    private byte[] byteMessage;

    public TCPPayload(byte phase, byte type, int size, String message){
        this.phase = phase;
        this. type = type;
        this.size = size;
        this.message = message;
    }

    public TCPPayload(byte phase, byte type, int size, byte[] byteMessage){
        this.phase = phase;
        this. type = type;
        this.size = size;
        this.byteMessage = byteMessage;
    }


    public String getMessage() {
        return message;
    }

    public byte getPhase() {
        return phase;
    }

    public byte getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public byte[] getByteMessage(){
        return this.byteMessage;
    }

    @Override
    public String toString() {
        return "TCPPayload{" +
                "phase=" + phase +
                ", type=" + type +
                ", size=" + size +
                ", message='" + message + '\'' +
                '}';
    }
}
