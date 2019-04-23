package com.lukakralj.smarthomeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

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
            public void onClick(View v) {
                handleLoginButton(v);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // do nothing...
    }

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

    private boolean verifyUser(String username, String password) {
        // TODO: put in separate class and add actual verification
        return username.equals("admin") && password.equals("admin");
    }

    private void loginUser() {
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }
}
