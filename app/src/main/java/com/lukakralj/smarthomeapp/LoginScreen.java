package com.lukakralj.smarthomeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This is the entry point of the application. It requires the user to login
 * with predefined username and password.
 */
public class LoginScreen extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private TextView loginMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        usernameInput = (EditText) findViewById(R.id.usernameInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        loginMessage = (TextView) findViewById(R.id.loginMessage);

        final Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLoginButton(v);
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
     * Decide what happens when a Login button is clicked.
     *
     * @param v
     */
    private void handleLoginButton(View v) {
        String msg;
        if (usernameInput.getText().toString().trim().equals("")) {
            msg = getString(R.string.emptyUsernameMsg);
        }
        else if (passwordInput.getText().toString().trim().equals("")) {
            msg = getString(R.string.emptyPasswordMsg);
        }
        else {
            boolean validCredentials = verifyUser(usernameInput.getText().toString(), passwordInput.getText().toString());
            if (!validCredentials) {
                msg = getString(R.string.invalidCredentialsMsg);
            }
            else {
                msg = null;
            }
        }
        if (msg == null) {
            loginMessage.setText("");
            loginUser();
        }
        else {
            loginMessage.setText(msg);
        }
    }

    /**
     * This method verifies user. It send a login request.
     * TODO: this method should take care of storing access tokens and user info locally
     *
     * @param username Username the user entered.
     * @param password Password the user entered.
     * @return True if this user can login, false otherwise.
     */
    private boolean verifyUser(String username, String password) {
        // TODO: put in separate class and add actual verification
        return username.equals("admin") && password.equals("admin");
    }

    /**
     * This method opens a new activity upon successful login.
     */
    private void loginUser() {
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }
}
