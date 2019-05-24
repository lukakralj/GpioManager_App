package com.lukakralj.smarthomeapp;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

public class ComponentsScreen extends AppCompatActivity {

    private RelativeLayout mainPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_components_screen);

        initMainPanel(new ArrayList<>());
    }

    private void initMainPanel(List<String> contents) { // TODO: to be changed to JSON
        mainPanel = new RelativeLayout(this);
        RelativeLayout.LayoutParams mainParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        mainPanel.setLayoutParams(mainParams);

        int curId = 1;
        for (int i = 0; i < contents.size(); i++) {
            RelativeLayout component = (RelativeLayout) View.inflate(this, R.layout.component_toggle, mainPanel);
            component.setId(curId);

            RelativeLayout.LayoutParams compParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            compParams.setMargins(0,0,0, dpToPx(10));

            if (curId > 1) {
                // add align_below
                compParams.addRule(RelativeLayout.BELOW, curId-1);
            }

            component.setLayoutParams(compParams);
            mainPanel.addView(component);
            curId++;
        }

        // add main to the scroll view
        ((ScrollView) findViewById(R.id.componentsScroll)).addView(mainPanel);
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }


}
