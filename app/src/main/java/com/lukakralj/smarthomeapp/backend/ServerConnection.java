package com.lukakralj.smarthomeapp.backend;

import android.app.Application;
import com.lukakralj.smarthomeapp.R;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * This class enables communication with the main server.
 */
public class ServerConnection {

    private Socket io;

    public ServerConnection() {
        try {
            io = IO.socket("http://10.171.17.204:3265");
            io.connect();
            System.out.println("======IO connected:" + io.connected());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public Socket getSocket() {
        return io;
    }
}
