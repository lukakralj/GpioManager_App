package com.lukakralj.smarthomeapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.lukakralj.smarthomeapp.backend.RequestCode;
import com.lukakralj.smarthomeapp.backend.ServerConnection;
import com.lukakralj.smarthomeapp.backend.logger.Level;
import com.lukakralj.smarthomeapp.backend.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * This will be the main screen of the app once the user is logged in.
 * This should group different actions a user can take.
 * For now this screen only provides a simple toggle. This will be updated once the
 * whole structure is tested.
 */
public class HomeScreenController extends AppCompatActivity {

    private TextView toggleLEDMsg;
    private RadioButton toggleOn;
    private RadioButton toggleOff;
    private RadioGroup toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        toggleLEDMsg = (TextView) findViewById(R.id.toggleLEDMsg);
        toggleOn = (RadioButton) findViewById(R.id.toggleOn);
        toggleOff = (RadioButton) findViewById(R.id.toggleOff);
        toggle = (RadioGroup) findViewById(R.id.toggle);

        toggle.setOnCheckedChangeListener(this::handleToggle);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.homeToolbar);
        setSupportActionBar(myToolbar);

        disableButtons();
        ServerConnection.getInstance().scheduleRequest(RequestCode.LED_STATUS,true, data -> {
            boolean isOn = false;
            try {
                String msg = "LED is turned ";
                msg += data.getString("ledStatus") + ".";
                toggleLEDMsg.setText(msg);

                isOn = data.getString("ledStatus").equals("on");
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
                e.printStackTrace();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            final boolean isOnFinal = isOn;
            handler.post(() -> enableButtons(isOnFinal));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.configureURLhome) {
            // open url config activity
            showDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void showDialog() {
        Intent intent = new Intent(this, ConfigureURLController.class);
        startActivity(intent);
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
        disableButtons();
        if (checkedId == R.id.toggleOn) {
            toggleLED(true);
        }
        else {
            toggleLED(false);
        }
    }

    /**
     * Send a request to physically toggle the LED.
     *
     * @param turnOn True if turning the LED on, false otherwise.
     */
    private void toggleLED(boolean turnOn) {
        String toSend = (turnOn) ? "on" : "off";
        JSONObject extra = new JSONObject();
        try {
            extra.put("ledStatus", toSend);
        }
        catch (JSONException e) {
            Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
            return;
        }
        ServerConnection.getInstance().scheduleRequest(RequestCode.TOGGLE_LED, extra, true, data -> {
            boolean isOn = false;
            try {
                if (data.getString("status").equals("OK")) {
                    String msg = "success: LED is turned ";
                    msg += data.getString("ledstatus") + ".";
                    toggleLEDMsg.setText(msg);

                    isOn = data.getString("ledStatus").equals("on");
                }
                else {
                    Logger.log("Could not toggle LED.", Level.ERROR);
                    Logger.log(data.toString(), Level.DEBUG);
                    String msg = "error: " + data.getString("err_code") + ". LED is turned ";
                    msg += data.getString("ledStatus") + ".";
                    toggleLEDMsg.setText(msg);
                    isOn = data.getString("ledStatus").equals("on");
                }
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            final boolean isOnFinal = isOn;
            handler.post(() -> enableButtons(isOnFinal));
        });
    }

    private void disableButtons() {
        toggleLEDMsg.setText(R.string.waitingServer);
        toggleOn.setEnabled(false);
        toggleOff.setEnabled(false);
    }

    /**
     * Enables buttons and sets the selected button.
     *
     * @param isOn True if LED is on. False if LED is off.
     */
    private void enableButtons(boolean isOn) {
        toggleOn.setEnabled(true);
        toggleOff.setEnabled(true);
        toggle.setOnCheckedChangeListener(null);
        if (isOn) {
            toggle.check(R.id.toggleOn);
        }
        else {
            toggle.check(R.id.toggleOff);
        }
        toggle.setOnCheckedChangeListener(this::handleToggle);
    }

}
