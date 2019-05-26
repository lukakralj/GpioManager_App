package com.lukakralj.iotControlApp.backend;

import org.json.JSONObject;

public interface ResponseListener {
    void processResponse(JSONObject data);
}
