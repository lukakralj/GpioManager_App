package com.lukakralj.GpioManager_App.backend;

import com.lukakralj.GpioManager_App.backend.logger.Level;
import com.lukakralj.GpioManager_App.backend.logger.Logger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.socket.client.IO;
import io.socket.client.Socket;
import android.os.Process;
import static com.lukakralj.GpioManager_App.backend.RequestCode.*;
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
    private static SubscriberEvent onConnectEvent = null;
    private static SubscriberEvent onDisconnectEvent = null;
    private static SubscriberEvent onComponentsChangeEvent = null;

    private Socket io;
    private boolean stop;
    private boolean connected;

    private ServerConnection() {
        try {
            Logger.log("Connecting to: " + url);
            io = IO.socket(url);

            io.on(Socket.EVENT_CONNECT, (data) -> {
                connected = true;
                if (onConnectEvent != null) {
                    onConnectEvent.triggerEvent();
                }
            });

            io.on(Socket.EVENT_DISCONNECT, (data) -> {
                connected = false;
                if (onDisconnectEvent != null) {
                    onDisconnectEvent.triggerEvent();
                }
            });

            io.on(getCodeString(COMPONENTS_CHANGE), (data) -> {
                if (onComponentsChangeEvent != null) {
                    onComponentsChangeEvent.triggerEvent();
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
        Logger.log("Reconnecting with: " + newUrl);
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
        Logger.log("ServerConnection thread started", Level.DEBUG);

        while (!stop) {
            if (currentEvent != events.size() - 1) { // check if there are any new events
                currentEvent++;
                Logger.log("Processing event: " + events.get(currentEvent).requestCode);
                processEvent(events.get(currentEvent));
            }
            try {
                sleep(1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
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
        // only login doesn't need the token
        if (event.requestCode != LOGIN) {
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

        io.emit(code, event.extraData);

        io.once(code + "Res", args -> {
            try {
                JSONObject data = (JSONObject)args[0];
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
     */
    public void scheduleRequest(RequestCode requestCode, JSONObject extraData, ResponseListener listener) {
        if (!connected) {
            // Prevent spamming.
            return;
        }
        events.add(new ServerEvent(requestCode, extraData, listener));
    }

    /**
     * first-come-first-server manner.
     * Schedule new request to be sent to the server. Requests are processed in
     *
     * @param requestCode Request specific code.
     * @param listener Specifies what happens when the response is received.
     */
    public void scheduleRequest(RequestCode requestCode, ResponseListener listener) {
        scheduleRequest(requestCode, null, listener);
    }

    public void subscribeOnConnectEvent(SubscriberEvent event) {
        onConnectEvent = event;
    }

    public void subscribeOnDisconnectEvent(SubscriberEvent event) {
        onDisconnectEvent = event;
    }

    public void subscribeComponentsChangeEvent(SubscriberEvent event) {
        onComponentsChangeEvent = event;
    }

    public void setAccessToken(String newToken) {
        accessToken = newToken;
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

        /**
         *
         * @param requestCode Request specific code.
         * @param listener Specifies what happens when the response is received.
         */
        private ServerEvent(RequestCode requestCode, JSONObject extraData, ResponseListener listener) {
            this.requestCode = requestCode;
            this.extraData = extraData;
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
            case LOGIN: return "login";
            case LOGOUT: return "logout";
            case JOIN_COMPONENTS_ROOM: return "joinComponentsRoom";
            case LEAVE_COMPONENTS_ROOM: return "leaveComponentsRoom";
            case COMPONENTS: return "components";
            case TOGGLE_COMPONENT: return "toggleComponent";
            case UPDATE_COMPONENT: return "updateComponent";
            case ADD_COMPONENT: return "addComponent";
            case REMOVE_COMPONENT: return "removeComponent";
            case COMPONENTS_CHANGE: return "componentsChange";
            case REFRESH_TOKEN: return "refreshToken";
            default: throw new RuntimeException("Invalid server code: " + code);
        }
    }
}
