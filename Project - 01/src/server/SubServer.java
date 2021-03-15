package server;

import java.net.Socket;

public class SubServer extends Thread {
    protected Socket socket;

    public SubServer(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {



    }

}
