package com.lukakralj.smarthomeapp.backend;

public class GpioComponent {

    private int id;
    private int physicalPin;
    private String direction;
    private String name;
    private String description;
    private boolean isOn;
    private int curValue;

    public GpioComponent(int id, int physicalPin, String direction, String name, String description, boolean isOn, int curValue) {
        this.id = id;
        this.physicalPin = physicalPin;
        this.direction = direction;
        this.name = name;
        this.description = description;
        this.isOn = isOn;
        this.curValue = curValue;
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
}