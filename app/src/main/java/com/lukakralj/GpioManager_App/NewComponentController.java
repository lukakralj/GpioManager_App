package com.lukakralj.GpioManager_App;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.lukakralj.GpioManager_App.backend.RequestCode;
import com.lukakralj.GpioManager_App.backend.ServerConnection;
import com.lukakralj.GpioManager_App.backend.logger.Level;
import com.lukakralj.GpioManager_App.backend.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This activity enables the user to add a new component.
 *
 *  @author Luka Kralj
 *  @version 1.0
 */
public class NewComponentController extends AppCompatActivity {

    private EditText compNameInput;
    private EditText compPinInput;
    private Switch compTypeInput;
    private EditText compDescriptionInput;
    private Button cancelButton;
    private Button addButton;
    private TextView newCompMessage;
    private View loadingScreen;
    private TextView loadingScreenMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_component);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.newComponentToolbar);
        setSupportActionBar(myToolbar);

        compNameInput = (EditText) findViewById(R.id.compNameInput);
        compPinInput = (EditText) findViewById(R.id.compPinInput);
        compTypeInput = (Switch) findViewById(R.id.compTypeInput);
        compDescriptionInput = (EditText) findViewById(R.id.compDescriptionInput);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        addButton = (Button) findViewById(R.id.addButton);
        newCompMessage = (TextView) findViewById(R.id.newCompMessage);
        newCompMessage.setText("");
        loadingScreen = (View) findViewById(R.id.loadingScreen);
        loadingScreenMsg = (TextView) findViewById(R.id.loadingScreenMsg);

        cancelButton.setOnClickListener(v -> onBackPressed());
        addButton.setOnClickListener(this::addComponent);

        compTypeInput.setChecked(false);
        compTypeInput.setText(R.string.outPin);
        compTypeInput.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                compTypeInput.setText(R.string.inPin);
            }
            else {
                compTypeInput.setText(R.string.outPin);
            }
        });

        ServerConnection.getInstance().subscribeOnConnectEvent(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::enableAll);
        });

        ServerConnection.getInstance().subscribeOnDisconnectEvent(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::disableAll);
        });

        if (ServerConnection.getInstance().isConnected()) {
            enableAll();
        }
        else {
            disableAll();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.configureURLlogin) {
            // Open URL configuration activity.
            showUrlDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showUrlDialog() {
        Intent intent = new Intent(this, ConfigureURLController.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ComponentsScreenController.class);
        startActivity(intent);
    }

    /**
     * Display the loading screen with the given message.
     *
     * @param msg Loading screen message.
     */
    private void loadingScreenON(CharSequence msg) {
        loadingScreen.setVisibility(View.VISIBLE);
        loadingScreenMsg.setText(msg);
    }

    /**
     * Hide the loading screen.
     */
    private void loadingScreenOFF() {
        loadingScreen.setVisibility(View.GONE);
        loadingScreenMsg.setText("");
    }

    /**
     * Enable all elements and hide the loading screen.
     */
    private void enableAll() {
        loadingScreenOFF();
        compNameInput.setEnabled(true);
        compPinInput.setEnabled(true);
        compTypeInput.setEnabled(true);
        compDescriptionInput.setEnabled(true);
        cancelButton.setEnabled(true);
        addButton.setEnabled(true);
    }

    /**
     * Disable all elements and display the loading screen.
     */
    private void disableAll() {
        loadingScreenON(getText(R.string.waitingConnection));
        compNameInput.setEnabled(false);
        compPinInput.setEnabled(false);
        compTypeInput.setEnabled(false);
        compDescriptionInput.setEnabled(false);
        cancelButton.setEnabled(false);
        addButton.setEnabled(false);
    }

    private void creationUnsuccessful() {
        newCompMessage.setText(R.string.addingFailed);
    }

    /**
     * Display a success message as a Toast.
     */
    private void creationSuccessful() {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(getApplicationContext(), R.string.addingOK, duration);
        toast.show();

        Intent intent = new Intent(this, ComponentsScreenController.class);
        startActivity(intent);
    }

    /**
     * Performs input checks and notifies user about the missing or invalid entries.
     *
     * @return True if all inputs are correct, false otherwise.
     */
    private boolean verifyData() {
        if (compNameInput.getText().toString().length() == 0) {
            newCompMessage.setText(R.string.emptyName);
            return false;
        }

        if (compPinInput.getText().toString().length() == 0) {
            newCompMessage.setText(R.string.emptyPinNo);
            return false;
        }

        try {
            int pinNo = Integer.parseInt(compPinInput.getText().toString());
            // valid range: 24-34 inclusive
            if (pinNo < 24 || pinNo > 34) {
                throw new NumberFormatException("Pin number out of range.");
            }
        }
        catch (NumberFormatException e) {
            Logger.log(e.getMessage(), Level.WARNING);
            newCompMessage.setText(R.string.invalidPinNo);
            return false;
        }
        newCompMessage.setText("");
        return true;
    }

    /**
     * Sends the request to add a new component.
     *
     * @param v
     */
    private void addComponent(View v) {
        disableAll();
        loadingScreenON(getText(R.string.addingComponent));
        if (!verifyData()) {
            enableAll();
            return;
        }
        JSONObject extra = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("name", compNameInput.getText().toString());
            data.put("physicalPin", Integer.parseInt(compPinInput.getText().toString()));
            data.put("direction", compTypeInput.getText().toString().equals(getText(R.string.outPin).toString()) ? "out" : "in");
            data.put("description", compDescriptionInput.getText().toString());
            extra.put("data", data);
        }
        catch (JSONException e) {
            Logger.log(e.getMessage(), Level.ERROR);
            enableAll();
            return;
        }
        ServerConnection.getInstance().scheduleRequest(RequestCode.ADD_COMPONENT, extra, resData -> {
            try {
                Logger.log("data: " + resData.toString(), Level.DEBUG);
                if (resData.getString("status").equals("OK")) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        enableAll();
                        creationSuccessful();
                    });
                }
                else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        enableAll();
                        creationUnsuccessful();
                    });
                }
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
            }
        });
    }
}
