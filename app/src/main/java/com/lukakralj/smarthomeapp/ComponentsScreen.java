package com.lukakralj.smarthomeapp;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.lukakralj.smarthomeapp.backend.logger.Level;
import com.lukakralj.smarthomeapp.backend.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class ComponentsScreen extends ListActivity {

    private RelativeLayout mainPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_components_screen);

        List<String> testing = new ArrayList<>();
        testing.add("one");
        testing.add("two");
        testing.add("three");
        //initMainPanel(testing);
        // initiate the listadapter
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.component_toggle, R.id.mainTitle, testing);

        // assign the list adapter
        setListAdapter(myAdapter);
    }

    // when an item of the list is clicked
    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        String selectedItem = (String) getListView().getItemAtPosition(position);
        //String selectedItem = (String) getListAdapter().getItem(position);

        Logger.log("You clicked " + selectedItem + " at position " + position, Level.DEBUG);
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
