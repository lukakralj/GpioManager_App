package com.lukakralj.smarthomeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ConfigureURLController extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_url);
        this.setFinishOnTouchOutside(false);
    }
}
