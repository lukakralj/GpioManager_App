package com.lukakralj.GpioManager_App.backend;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

/**
 * This class represents a component that is connected to a certain GPIO pin
 * on the DragonBoard.
 * It is serializable so that it can be passed in Intents between activities.
 */
public class GpioComponent implements Serializable {

    private int id;
    private int physicalPin;
    private String direction;
    private String name;
    private String description;
    private boolean isOn;
    private int curValue;

    /**
     * Initialises the object with component information. To set the isOn and curValue use
     * setters after the object is created.
     */
    public GpioComponent(int id, int physicalPin, String direction, String name, String description) {
        this.id = id;
        this.physicalPin = physicalPin;
        this.direction = direction;
        this.name = name;
        this.description = description;
        this.isOn = false;
        this.curValue = -1;
    }

    /**
     * Initialises the object with component information that is parsed from a JSON object.
     * This object also needs to contain values for isOn or curValue - depending on the
     * type of the component.
     */
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
