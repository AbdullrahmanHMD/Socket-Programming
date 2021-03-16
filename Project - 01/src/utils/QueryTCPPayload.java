package utils;

/**
 * An object to hold the query information,
 */
public class QueryTCPPayload extends TCPPayload {

    private int tSize;
    private String token;

    public QueryTCPPayload(byte phase, byte type, int mSize, int tSize, String message, String token) {
        super(phase, type, mSize, message);
        this.tSize = tSize ;
        this.token = token;
    }

    public int gettSize() {
        return this.tSize;
    }

    public String getToken() {
        return this.token;
    }

    @Override
    public String toString() {
        return super.toString() +
                " QueryTCPPayload{" +
                "tSize=" + tSize +
                ", token='" + token + '\'' +
                '}';
    }
}
