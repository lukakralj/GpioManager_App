package com.lukakralj.GpioManager_App.backend;

import com.lukakralj.GpioManager_App.LoginScreenController;
import com.lukakralj.GpioManager_App.backend.logger.Level;
import com.lukakralj.GpioManager_App.backend.logger.Logger;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import io.socket.client.IO;
import io.socket.client.Socket;
import android.os.Process;
import static com.lukakralj.GpioManager_App.backend.RequestCode.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class enables communication with the main server. All the communication
 * happens on the background thread that is different from the UI thread.
 *
 *  @author Luka Kralj
 *  @version 1.0
 */
public class ServerConnection extends Thread {
    private static String url = ""; // Server URL.
    private static ServerConnection instance;

    /** Queue of all the request that need to be send to the server. */
    private static List<ServerEvent> events = Collections.synchronizedList(new LinkedList<>());

    private static String accessToken; // Cached user access token (used in all requests).

    /* Each activity can subscribe to these three events to update the UI/trigger actions accordingly.*/
    private static SubscriberEvent onConnectEvent = null;
    private static SubscriberEvent onDisconnectEvent = null;
    private static SubscriberEvent onComponentsChangeEvent = null;

    private Socket io;
    private boolean stop; // Control variable to stop the thread.
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
     * For the singleton pattern.
     *
     * @return Instance of ServerConnection.
     */
    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
            instance.start();
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
        if (instance != null) {
            instance.stopThread();
            try {
                instance.join();
            }
            catch (InterruptedException e) {
                Logger.log(e.getMessage(), Level.ERROR);
            }
            instance.io.disconnect();
            instance.io.close();
            instance = null;
        }
        url = newUrl;
        getInstance();
    }

    /**
     * Start executing the events.
     */
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        while (!stop) {
            if (events.size() > 0) { // check if there are any new events
                Logger.log("Processing event: " + events.get(0).requestCode);
                processEvent(events.remove(0));
            }
            try {
                sleep(1);
            }
            catch (InterruptedException e) {
                Logger.log("Sleep interrupted: " + e.getMessage(), Level.WARNING);
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

        // Include accessToken to every request appart the LOGIN request that does not need it.
        if (event.requestCode != LOGIN) {
            if (event.extraData == null) {
                event.extraData = new JSONObject();
            }
            try {
                event.extraData.put("accessToken", accessToken);
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
                Logger.log("Event not processed due to exception.", Level.ERROR);
                return;
            }
        }

        io.emit(code, event.extraData);

        io.once(code + "Res", args -> {
            try {
                JSONObject data = (JSONObject)args[0];
                if (event.requestCode != LOGIN && event.requestCode != LOGOUT && event.requestCode != REFRESH_TOKEN) {
                    // Need to verify if the authentication was still okay.
                    if (data.getString("status").equals("ERR")
                        && (data.getString("err_code").equals("BAD_AUTH")
                        || data.getString("err_code").equals("NO_AUTH"))) {
                        // Redirect back to login screen.
                        LoginScreenController.startLoginActivity();
                    }
                    else {
                        event.listener.processResponse(data);
                    }
                }
                else {
                    event.listener.processResponse(data);
                }
            }
            catch (Exception e) {
                Logger.log(e.getMessage(), Level.ERROR);
            }
        });
    }

    /**
     * Stop thread.
     */
    public void stopThread() {
        stop = true;
    }

    /**
     *
     * @return URL that the app is currently connected to.
     */
    public String getCurrentUrl() {
        return url;
    }

    /**
     * Schedule new request to be sent to the server. Requests are processed in
     * first-come-first-server manner.
     *
     * @param requestCode Request specific code.
     * @param extraData Specify additional information to be sent to the server. null if no
     *                  additional information needed.
     * @param listener Specifies what happens when the response is received.
     */
    public void scheduleRequest(RequestCode requestCode, JSONObject extraData, ResponseListener listener) {
        if (!connected) {
            // Prevent spamming.
            return;
        }
        events.add(new ServerEvent(requestCode, extraData, listener));
    }

    /**
     * Schedule new request to be sent to the server. Requests are processed in
     * first-come-first-server manner.
     * Use this method if the request does not require any extra data.
     *
     * @param requestCode Request specific code.
     * @param listener Specifies what happens when the response is received.
     */
    public void scheduleRequest(RequestCode requestCode, ResponseListener listener) {
        scheduleRequest(requestCode, null, listener);
    }

    /**
     * Set what happens when the connection is established.
     *
     * @param event Event to be triggered.
     */
    public void subscribeOnConnectEvent(SubscriberEvent event) {
        onConnectEvent = event;
    }

    /**
     * Set what happens when the connection is lost.
     *
     * @param event Event to be triggered.
     */
    public void subscribeOnDisconnectEvent(SubscriberEvent event) {
        onDisconnectEvent = event;
    }

    /**
     * Set what happens when the app is notified that the components have changed.
     *
     * @param event Event to be triggered.
     */
    public void subscribeComponentsChangeEvent(SubscriberEvent event) {
        onComponentsChangeEvent = event;
    }

    /**
     * Set the access token to be used in requests.
     *
     * @param newToken Token returned by the server.
     */
    public void setAccessToken(String newToken) {
        accessToken = newToken;
    }

    /**
     * @return True if the connection with the server is established, false when disconnected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Converts the request code constant into a string to be used with socket.IO.
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

    /**
     * Combines the details about each request (to be send to the server).
     */
    private class ServerEvent {

        private RequestCode requestCode;
        private ResponseListener listener;
        private JSONObject extraData;

        /**
         *
         * @param requestCode Request specific code.
         * @param extraData Specify additional information to be sent to the server. null if no
         *                  additional information needed.
         * @param listener Specifies what happens when the response is received.
         */
        private ServerEvent(RequestCode requestCode, JSONObject extraData, ResponseListener listener) {
            this.requestCode = requestCode;
            this.extraData = extraData;
            this.listener = listener;
        }
    }
}
