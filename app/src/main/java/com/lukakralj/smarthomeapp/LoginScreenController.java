package com.lukakralj.smarthomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import com.lukakralj.smarthomeapp.backend.Crypto;
import com.lukakralj.smarthomeapp.backend.RequestCode;
import com.lukakralj.smarthomeapp.backend.ServerConnection;
import com.lukakralj.smarthomeapp.backend.logger.Level;
import com.lukakralj.smarthomeapp.backend.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the entry point of the application. It requires the user to login
 * with predefined username and password.
 */
public class LoginScreenController extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private TextView loginMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.startLogger();
        ServerConnection.getInstance();
        Crypto.getInstance();

        setContentView(R.layout.activity_login_screen);

        usernameInput = (EditText) findViewById(R.id.usernameInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        loginMessage = (TextView) findViewById(R.id.loginMessage);
        resetAll();

        final Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this::handleLoginButton);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.configureURLlogin) {
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
     * Decide what happens when a Login button is clicked.
     *
     * @param v
     */
    private void handleLoginButton(View v) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.equals("")) {
            loginMessage.setText(getString(R.string.emptyUsernameMsg));
        }
        else if (password.equals("")) {
            loginMessage.setText(getString(R.string.emptyPasswordMsg));
        }
        else {
            verifyUser(username, password);
        }
    }

    /**
     * Defines what happens when credentials are invalid.
     */
    private void invalidCredentials() {
        resetAll();
        loginMessage.setText(getString(R.string.invalidCredentialsMsg));
    }

    /**
     * This method verifies user. It send a login request.
     *
     * @param username Username the user entered.
     * @param password Password the user entered.
     */
    private void verifyUser(String username, String password) {
        JSONObject extraData = new JSONObject();
        try {
            extraData.put("username", username);
            extraData.put("password", password);
            extraData.put("clientKey", Crypto.getInstance().getPublicKey());
        }
        catch (JSONException e) {
            Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
            invalidCredentials();
            return;
        }
        ServerConnection.getInstance().scheduleRequest(RequestCode.LOGIN, extraData, true, data -> {
            try {
                Logger.log("data: " + data.toString(), Level.DEBUG);
                if (data.getString("status").equals("OK")) {
                    startHomeActivity();
                }
                else {
                    invalidCredentials();
                }
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
            }
        });
    }

    /**
     * Clears all input boxes and messages.
     */
    private void resetAll() {
        usernameInput.setText("");
        passwordInput.setText("");
        loginMessage.setText("");
    }

    /**
     * This method opens a new activity upon successful login.
     */
    private void startHomeActivity() {
        resetAll();
        Intent intent = new Intent(this, HomeScreenController.class);
        startActivity(intent);
    }
}
