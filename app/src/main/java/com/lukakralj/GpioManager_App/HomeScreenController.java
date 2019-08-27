package com.lukakralj.GpioManager_App;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;


/**
 * This will be the main screen of the app once the user is logged in.
 * This should group different actions a user can take.
 */
public class HomeScreenController extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.homeToolbar);
        setSupportActionBar(myToolbar);

        ((Button) findViewById(R.id.componentsButton)).setOnClickListener((view) -> {
            Intent intent = new Intent(this, ComponentsScreenController.class);
            startActivity(intent);
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
        else if (id == R.id.logoutHome) {
            logoutUser();
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

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("accessToken");
        editor.apply();

        // TODO: send to logout request to server

        Intent intent = new Intent(this, LoginScreenController.class);
        startActivity(intent);
    }

}
