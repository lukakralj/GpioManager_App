package com.lukakralj.GpioManager_App.backend;

import org.json.JSONObject;

/**
 * Listener for the response sent by the server.
 * Interface was declared so tha lambdas can be used in place.
 *
 *  @author Luka Kralj
 *  @version 1.0
 */
public interface ResponseListener {
    void processResponse(JSONObject data);
}
