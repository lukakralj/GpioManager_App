package com.lukakralj.smarthomeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

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

    @Override
    public void onBackPressed() {
        // do nothing...
    }

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

    private boolean toggleLED(boolean turnOn) {
        // TODO: add request sending
        return true;
    }
}
