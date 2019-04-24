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
    public static String url = "http://2ab92dee.ngrok.io";

    public ServerConnection() {
        try {
            io = IO.socket(url);
            io.connect();
            System.out.println("======IO connected:" + io.connected());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public Socket getSocket() {
        if (!io.connected()) {
            return null;
        }
        return io;
    }
}
