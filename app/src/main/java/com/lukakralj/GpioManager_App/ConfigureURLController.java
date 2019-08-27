package com.lukakralj.GpioManager_App;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lukakralj.GpioManager_App.backend.ServerConnection;

public class ConfigureURLController extends AppCompatActivity {

    private EditText newUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_url);

        newUrl = (EditText) findViewById(R.id.newUrlInput);
        newUrl.setText(ServerConnection.getInstance().getCurrentUrl());

        final Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> onBackPressed());

        final Button reconnectButton = (Button) findViewById(R.id.reconnectButton);
        reconnectButton.setOnClickListener(this::handleReconnect);

    }

    private void handleReconnect(View v) {
        String url = newUrl.getText().toString();
        if (url.length() < 10) {
            newUrl.setBackgroundColor(Color.RED);
            return;
        }

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("url", url);
        editor.apply();

        ServerConnection.reconnect(url);
        onBackPressed();
    }
}
