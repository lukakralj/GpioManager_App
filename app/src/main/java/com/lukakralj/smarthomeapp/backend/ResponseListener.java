package com.lukakralj.smarthomeapp.backend;

import org.json.JSONObject;

public interface ResponseListener {
    void processResponse(JSONObject data);
}
