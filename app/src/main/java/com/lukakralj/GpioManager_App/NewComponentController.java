package com.lukakralj.GpioManager_App;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class NewComponentController extends AppCompatActivity {

    private EditText compNameInput;
    private EditText compPinInput;
    private Switch compTypeInput;
    private EditText compDescriptionInput;
    private Button cancelButton;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_component);

        compNameInput = (EditText) findViewById(R.id.compNameInput);
        compPinInput = (EditText) findViewById(R.id.compPinInput);
        compTypeInput = (Switch) findViewById(R.id.compTypeInput);
        compDescriptionInput = (EditText) findViewById(R.id.compDescriptionInput);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        createButton = (Button) findViewById(R.id.createButton);

        cancelButton.setOnClickListener(v -> onBackPressed());
        createButton.setOnClickListener(this::createComponent);

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

    private void createComponent(View v) {
        // TODO: send request and redirect to components screen
    }
}
