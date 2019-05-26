package com.lukakralj.smarthomeapp.backend;

import com.lukakralj.smarthomeapp.backend.logger.Level;
import com.lukakralj.smarthomeapp.backend.logger.Logger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import android.os.Process;

import static com.lukakralj.smarthomeapp.backend.RequestCode.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class enables communication with the main server.
 */
public class ServerConnection extends Thread {
    private static String url = "http://04478faf.ngrok.io";
    private static ServerConnection instance;
    private static List<ServerEvent> events = new ArrayList<>();
    private static int currentEvent = -1;
    private static String accessToken;

    private Socket io;
    private boolean stop;
    private boolean connected;
    private Map<Class, OnConnectListener> onConnectListeners;
    private Map<Class, OnDisconnectListener> onDisconnectListeners;

    private ServerConnection() {
        try {
            Logger.log("Connecting to: " + url);
            io = IO.socket(url);

            io.on(Socket.EVENT_CONNECT, (data) -> {
                connected = true;
                for (OnConnectListener l : onConnectListeners.values()) {
                    l.connected(data);
                }
            });

            io.on(Socket.EVENT_DISCONNECT, (data) -> {
                connected = false;
                for (OnDisconnectListener l : onDisconnectListeners.values()) {
                    l.disconnected(data);
                }
            });

            io.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        stop = false;
    }

    /**
     *
     * @return Instance of ServerConnection.
     */
    public static ServerConnection getInstance() {
        if (instance == null) {
            try {
                instance = new ServerConnection();
                instance.start();
            }
            catch (Exception e) {
                Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
                throw new RuntimeException(e.getCause());
            }
        }
        return instance;
    }

    /**
     * Reconnect to the server using a new URL. Previously scheduled events will be preserved.
     *
     * @param newUrl New url of the server.
     */
    public static void reconnect(String newUrl) {
        instance.stopThread();
        try {
            instance.join();
        }
        catch (InterruptedException e) {
            Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
        }
        instance.io.disconnect();
        instance.io.close();
        instance = null;
        url = newUrl;
        getInstance();
    }

    /**
     * Start executing the events.
     */
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Logger.log("Run started", Level.DEBUG);
        // TODO: uncomment if using encryption
        /*scheduleRequest(SERVER_KEY, false, data -> {
            Logger.log("Data:" + data.toString(), Level.DEBUG);
            try {
                Crypto.getInstance().setServerPublicKey(data.getString("serverKey"));
                Logger.log("Received server key: {" + data.getString("serverKey") + "}", Level.DEBUG);
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
            }
        });*/
        while (!stop) {
            try {
                sleep(1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (currentEvent != events.size() - 1) { // check if there are any new events
                currentEvent++;
                Logger.log("Processing event: " + events.get(currentEvent).requestCode);
                processEvent(events.get(currentEvent));
            }
        }
    }

    /**
     * Executes the request specified by the event and threats the response.
     *
     * @param event Event to be executed.
     */
    private void processEvent(ServerEvent event) {
        String code = getCodeString(event.requestCode);

        // include accessToken to every request that needs it
        // only serverKey and login don't need the token
        if (event.requestCode != SERVER_KEY && event.requestCode != LOGIN) {
            if (event.extraData == null) {
                event.extraData = new JSONObject();
            }
            try {
                event.extraData.put("accessToken", accessToken);
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
                Logger.log("Event not processed due to exception.", Level.ERROR);
                return;
            }
        }

        // TODO: uncommendt if using encryption
        /*String toSend = null;
        if (event.requestCode != SERVER_KEY) {
            String encoded = Crypto.getInstance().rsaEncrypt(event.extraData);
            if (encoded == null) {
                Logger.log("Couldn't encode the message: " + event.extraData.toString());
                return;
            }
            toSend = encoded;
        }// else we don't have a key so we cannot rsaEncrypt
        */
        io.emit(code, event.extraData);

        io.once(code + "Res", args -> {
            try {
                JSONObject data;
                // TODO: uncomment if using encryption
                /*if (event.expectEncryptedResponse) {
                    data = Crypto.getInstance().rsaDecrypt((String) args[0]);
                    if (data == null) {
                        Logger.log("Couldn't decode the message for: " + code + "Res");
                        return;
                    }
                }
                else {*/
                    data = (JSONObject)args[0];
                //}
                if (event.requestCode == LOGIN && data.getString("status").equals("OK")) {
                    accessToken = data.getString("accessToken");
                }
                event.listener.processResponse(data);
            }
            catch (Exception e) {
                Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
            }
        });
    }

    /**
     * Stop thread.
     */
    public void stopThread() {
        stop = true;
    }

    public String getCurrentUrl() {
        return url;
    }
    /**
     * Schedule new request to be sent to the server. Requests are processed in
     * first-come-first-server manner.
     *
     * @param requestCode Request specific code.
     * @param listener Specifies what happens when the response is received.
     * @param extraData Specify additional information to be sent to the server. null if no
     *                  additional information needed.
     * @param expectEncryptedResponse True if the expected response is encrypted or not.
     *                                Usually, this should be true, unless for initial key exchange.
     */
    public boolean scheduleRequest(RequestCode requestCode, JSONObject extraData, boolean expectEncryptedResponse, ResponseListener listener) {
        if (!connected) {
            // Prevent spamming.
            return false;
        }
        events.add(new ServerEvent(requestCode, extraData, expectEncryptedResponse, listener));
        return true;
    }

    /**
     * Schedule new request to be sent to the server. Requests are processed in
     * first-come-first-server manner.
     *
     * @param requestCode Request specific code.
     * @param listener Specifies what happens when the response is received.
     * @param expectEncryptedResponse True if the expected response is encrypted or not.
     *                                Usually, this should be true, unless for initial key exchange.
     */
    public boolean scheduleRequest(RequestCode requestCode, boolean expectEncryptedResponse, ResponseListener listener) {
        return scheduleRequest(requestCode, null, expectEncryptedResponse, listener);
    }

    public void subscribeOnConnectEvent(Class subscriber, OnConnectListener listener) {
        onConnectListeners.put(subscriber, listener);
    }

    public void subscribeOnDisconnectEvent(Class subscriber, OnDisconnectListener listener) {
        onDisconnectListeners.put(subscriber, listener);
    }

    public boolean isConnected() {
        return connected;
    }
    /**
     * Combines the details about each request to be send to the server.
     */
    private class ServerEvent {

        private RequestCode requestCode;
        private ResponseListener listener;
        private JSONObject extraData;
        private boolean expectEncryptedResponse;

        /**
         *
         * @param requestCode Request specific code.
         * @param listener Specifies what happens when the response is received.
         */
        private ServerEvent(RequestCode requestCode, JSONObject extraData, boolean expectEncryptedResponse, ResponseListener listener) {
            this.requestCode = requestCode;
            this.extraData = extraData;
            this.expectEncryptedResponse = expectEncryptedResponse;
            this.listener = listener;
        }
    }

    /**
     *
     * @param code Request code.
     * @return String associated with the request code.
     */
    private String getCodeString(RequestCode code) {
        switch (code) {
            case SERVER_KEY: return "serverKey";
            case LOGIN: return "login";
            case LED_STATUS: return "ledStatus";
            case TOGGLE_LED: return "toggleLed";
            default: throw new RuntimeException("Invalid server code: " + code);
        }
    }
}
