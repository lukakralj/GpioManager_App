package com.lukakralj.smarthomeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.lukakralj.smarthomeapp.backend.Crypto;
import com.lukakralj.smarthomeapp.backend.ServerConnection;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * This will be the main screen of the app once the user is logged in.
 * This should group different actions a user can take.
 * For now this screen only provides a simple toggle. This will be updated once the
 * whole structure is tested.
 */
public class HomeScreenController extends AppCompatActivity {

    private TextView toggleLEDMsg;
    private RadioGroup toggle;
    private EditText newUrlInput;

    private ServerConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        toggleLEDMsg = (TextView) findViewById(R.id.toggleLEDMsg);
        toggle = (RadioGroup) findViewById(R.id.toggle);
        newUrlInput = (EditText) findViewById(R.id.newUrlInput);

        toggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                handleToggle(group, checkedId);
            }
        });

        final Button reconnectBtn = (Button) findViewById(R.id.reconnectButton);
        reconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reconnect();
            }
        });

        connection = new ServerConnection();
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
                //msg = getString(R.string.ledOn);
            }
        }
        else {
            if (toggleLED(false)) {
                //msg = getString(R.string.ledOff);
            }
        }

        if (msg == null) {
            //toggleLEDMsg.setText(getString(R.string.toggleLEDfailed));
        }
        else {
            System.out.println("======= text set");
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

        Socket io = connection.getSocket();
        if (io == null) {
            toggleLEDMsg.setText("Couldn't connect.");
            return false;
        }
        String toSend = (turnOn) ? "Android: LED ON" : "Android: LED OFF";
        try {
            toSend = Crypto.getInstance().encrypt(toSend, ServerConnection.serverKey);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        io.emit("msg", toSend);
        io.once("res", resReceived);

        // TODO: add request sending
        return true;
    }

    private int count = 0;
    private Emitter.Listener resReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("======= resReceived called:" + args);
            System.out.println("Encoded: " + args[0]);
            String decoded = null;
            try {
                decoded = Crypto.getInstance().decrypt((String) args[0]);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            String res = decoded + (++count);
            toggleLEDMsg.setText(res);
        }
    };

    private void reconnect() {
        String newUrl = newUrlInput.getText().toString();
        if (newUrl.trim().length() == 0) {
            //toggleLEDMsg.setText("Empty url");
            //return;
            newUrl = ServerConnection.url;
        }

        ServerConnection.url = newUrl;

        if (connection.getSocket() != null) {
            connection.getSocket().disconnect();
        }
        connection = new ServerConnection();
    }
}
