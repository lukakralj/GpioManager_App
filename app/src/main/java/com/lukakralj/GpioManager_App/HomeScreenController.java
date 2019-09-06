package com.lukakralj.GpioManager_App;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.lukakralj.GpioManager_App.backend.RequestCode;
import com.lukakralj.GpioManager_App.backend.ServerConnection;


/**
 * This is the main screen of the app.
 * It groups different actions the user can take.
 *
 *  @author Luka Kralj
 *  @version 1.0
 */
public class HomeScreenController extends AppCompatActivity {

    private Button componentsButton;
    private Button logoutButton;
    private View loadingScreen;
    private TextView loadingScreenMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.homeToolbar);
        setSupportActionBar(myToolbar);

        componentsButton = (Button) findViewById(R.id.componentsButton);
        componentsButton.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ComponentsScreenController.class);
            startActivity(intent);
        });
        loadingScreen = (View) findViewById(R.id.loadingScreen);
        loadingScreenMsg = (TextView) findViewById(R.id.loadingScreenMsg);

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

    /**
     * Disable back button.
     */
    @Override
    public void onBackPressed() {
        // do nothing...
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
        logoutButton.setEnabled(true);
        componentsButton.setEnabled(true);
    }

    /**
     * Disable all elements and display the loading screen.
     */
    private void disableAll() {
        loadingScreenON(getText(R.string.waitingConnection));
        logoutButton.setEnabled(false);
        componentsButton.setEnabled(false);
    }

    /**
     * Clear tokens and send logout request.
     */
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
