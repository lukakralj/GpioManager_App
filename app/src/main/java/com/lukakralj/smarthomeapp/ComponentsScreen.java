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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lukakralj.smarthomeapp.backend.GpioComponent;
import com.lukakralj.smarthomeapp.backend.logger.Level;
import com.lukakralj.smarthomeapp.backend.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class ComponentsScreen extends ListActivity {

    private RelativeLayout mainPanel;
    GpioComponent[] testing;
    CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_components_screen);

        testing = new GpioComponent[] {
                new GpioComponent(0, 27, "out", "LED 1", "Description 1", false, 0),
                new GpioComponent(0, 28, "out", "LED 2", "Description 2", false, 0),
                new GpioComponent(0, 29, "out", "LED 3", "Description 3", true, 0),
        };

        adapter = new CustomAdapter(this, testing);
        setListAdapter(adapter);

    }

    // when an item of the list is clicked
    @Override
    protected void onListItemClick(ListView list, View vi, int position, long id) {
        super.onListItemClick(list, vi, position, id);

        GpioComponent selectedItem = (GpioComponent) getListView().getItemAtPosition(position);
        ((RadioGroup) vi.findViewById(R.id.toggle)).setOnCheckedChangeListener(null);
        if (testing[position].getIsOn()) {
            ((RadioGroup) vi.findViewById(R.id.toggle)).check(vi.findViewById(R.id.toggleOn).getId());
        }
        else {
            ((RadioGroup) vi.findViewById(R.id.toggle)).check(vi.findViewById(R.id.toggleOff).getId());
        }
        ((RadioGroup) vi.findViewById(R.id.toggle)).setOnCheckedChangeListener((radioGroup, checkedId) -> {
            testing[position].setIsOn(!testing[position].getIsOn());
            adapter.notifyDataSetChanged();
        });


        Logger.log("You clicked " + selectedItem + " at position " + position + ", id: " + id, Level.DEBUG);
    }

    private class CustomAdapter extends BaseAdapter {

        Context context;
        GpioComponent[] data;
        private LayoutInflater inflater = null;

        public CustomAdapter(Context context, GpioComponent[] data) {
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int position) {
            return data[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null) {
                vi = inflater.inflate(R.layout.component_toggle, null);
            }
            ((TextView) vi.findViewById(R.id.mainTitle)).setText(data[position].getName());
            ((TextView) vi.findViewById(R.id.subtitle)).setText(data[position].getDescription());
            return vi;
        }
    }

}
