package com.lukakralj.GpioManager_App;

import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lukakralj.GpioManager_App.backend.RequestCode;
import com.lukakralj.GpioManager_App.backend.ServerConnection;


/**
 * This will be the main screen of the app once the user is logged in.
 * This should group different actions a user can take.
 */
public class HomeScreenController extends AppCompatActivity {

    private Button componentsButton;
    private Button logoutButton;
    private TextView homeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.homeToolbar);
        setSupportActionBar(myToolbar);

        homeMessage = (TextView) findViewById(R.id.homeMessage);
        componentsButton = (Button) findViewById(R.id.componentsButton);
        componentsButton.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ComponentsScreenController.class);
            startActivity(intent);
        });

        logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener((view) -> {
            logoutUser();
        });

        ServerConnection.getInstance().subscribeOnConnectEvent(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::enableAll);
        });

        ServerConnection.getInstance().subscribeOnDisconnectEvent(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::disableAll);
        });
    }

    private void enableAll() {
        logoutButton.setEnabled(true);
        componentsButton.setEnabled(true);
        homeMessage.setText("");
    }

    private void disableAll() {
        logoutButton.setEnabled(false);
        componentsButton.setEnabled(false);
        homeMessage.setText(R.string.waitingConnection);
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
            // open url config activity
            showUrlDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void showUrlDialog() {
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

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        // Remove token from prefs.
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("accessToken");
        editor.apply();

        // Send request.
        ServerConnection.getInstance().scheduleRequest(RequestCode.LOGOUT, data -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(getApplicationContext(), R.string.logoutOK, duration);
                toast.show();
            });
        });

        Intent intent = new Intent(this, LoginScreenController.class);
        startActivity(intent);
    }

}
