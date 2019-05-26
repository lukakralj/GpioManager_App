package com.lukakralj.smarthomeapp.backend;

import org.json.JSONException;
import org.json.JSONObject;

public class GpioComponent {

    private int id;
    private int physicalPin;
    private String direction;
    private String name;
    private String description;
    private boolean isOn;
    private int curValue;

    public GpioComponent(int id, int physicalPin, String direction, String name, String description) {
        this.id = id;
        this.physicalPin = physicalPin;
        this.direction = direction;
        this.name = name;
        this.description = description;
        this.isOn = false;
        this.curValue = -1;
    }

    public GpioComponent(JSONObject data) throws JSONException {
        this.id = data.getInt("id");
        this.physicalPin = data.getInt("physicalPin");
        this.direction = data.getString("direction");
        this.name = data.getString("name");
        this.description = data.getString("description");
        this.isOn = false;
        this.curValue = -1;

        if (this.direction.equals("out")) {
            this.isOn = data.getBoolean("isOn");
        }
        else {
            this.curValue = data.getInt("curValue");
        }
    }

    public int getId() {
        return id;
    }

    public int getPhysicalPin() {
        return physicalPin;
    }

    public String getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(boolean isOn) {
        this.isOn = isOn;
    }

    public int getCurValue() {
        return curValue;
    }

    public void setCurValue(int curValue) {
        this.curValue = curValue;
    }
}
