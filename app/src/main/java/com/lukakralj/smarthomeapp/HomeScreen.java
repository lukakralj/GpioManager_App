package com.lukakralj.smarthomeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * This will be the main screen of the app once the user is logged in.
 * This should group different actions a user can take.
 * For now this screen only provides a simple toggle. This will be updated once the
 * whole structure is tested.
 */
public class HomeScreen extends AppCompatActivity {

    private TextView toggleLEDMsg;
    private RadioGroup toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        toggleLEDMsg = (TextView) findViewById(R.id.toggleLEDMsg);
        toggle = (RadioGroup) findViewById(R.id.toggle);

        toggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                handleToggle(group, checkedId);
            }
        });
    }

    /**
     * Disable back button.
     */
    @Override
    public void onBackPressed() {
        // do nothing...
    }

    /**
     * Decide what happens when a toggle is flipped.
     *
     * @param group The radio button group that represents a custom toggle.
     * @param checkedId Resource ID of the radio button that is currently selected.
     */
    private void handleToggle(RadioGroup group, int checkedId) {
        String msg = null;
        if (checkedId == R.id.toggleOn) {
            if (toggleLED(true)) {
                msg = getString(R.string.ledOn);
            }
        }
        else {
            if (toggleLED(false)) {
                msg = getString(R.string.ledOff);
            }
        }

        if (msg == null) {
            toggleLEDMsg.setText(getString(R.string.toggleLEDfailed));
        }
        else {
            toggleLEDMsg.setText(msg);
        }
    }

    /**
     * Send a request to physically toggle the LED.
     *
     * @param turnOn True if turning the LED on, false otherwise.
     * @return True if an LED was successfully turned on. False otherwise.
     */
    private boolean toggleLED(boolean turnOn) {
        // TODO: add request sending
        return true;
    }
}
