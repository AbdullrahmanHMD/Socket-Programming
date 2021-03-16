package server;

import static utils.Utilities.*;
public class ServerMain {

    public static void main(String[] args){
        StratoNet server = new StratoNet(COMMAND_PORT, FILE_PORT);
        server.initialize();
    }
}

