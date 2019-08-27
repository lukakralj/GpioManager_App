package com.lukakralj.GpioManager_App.backend;

import org.json.JSONObject;

public interface ResponseListener {
    void processResponse(JSONObject data);
}
