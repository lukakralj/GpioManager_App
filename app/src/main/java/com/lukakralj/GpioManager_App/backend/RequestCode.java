package com.lukakralj.GpioManager_App.backend;

/**
 * All the possible request codes that the server can interpret.
 *
 *  @author Luka Kralj
 *  @version 1.0
 */
public enum RequestCode {
    LOGIN,
    LOGOUT,
    JOIN_COMPONENTS_ROOM,
    LEAVE_COMPONENTS_ROOM,
    COMPONENTS,
    TOGGLE_COMPONENT,
    UPDATE_COMPONENT,
    ADD_COMPONENT,
    REMOVE_COMPONENT,
    COMPONENTS_CHANGE,
    REFRESH_TOKEN
}
