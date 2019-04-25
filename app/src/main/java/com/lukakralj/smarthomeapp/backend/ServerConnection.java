package com.lukakralj.smarthomeapp.backend;

import android.app.Application;
import com.lukakralj.smarthomeapp.R;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * This class enables communication with the main server.
 */
public class ServerConnection {

    private Socket io;
    public static String url = "http://9fa8f89c.ngrok.io";
    public static String serverKey;

    public ServerConnection() {
        try {
            System.out.println("Connecting to: " + url);
            io = IO.socket(url);

            io.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        io.emit("key", Crypto.getInstance().getPublicKey());
        io.once("keyRes", resReceived);
    }

    private Emitter.Listener resReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("======= resReceived called in connect:" + args);
            System.out.println("==== res type: " + args[0].getClass());
            String res = (String) args[0];

            //res = res.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");

            serverKey = res;
            System.out.println("server key:" + serverKey);
        }
    };

    public Socket getSocket() {
        if (!io.connected()) {
            return null;
        }
        return io;
    }
}
