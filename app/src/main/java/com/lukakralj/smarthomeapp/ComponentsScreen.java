package com.lukakralj.smarthomeapp;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.lukakralj.smarthomeapp.backend.GpioComponent;
import com.lukakralj.smarthomeapp.backend.ServerConnection;
import com.lukakralj.smarthomeapp.backend.logger.Level;
import com.lukakralj.smarthomeapp.backend.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class ComponentsScreen extends ListActivity {

    private static List<GpioComponent> components = new ArrayList<>();
    private ComponentsAdapter curAdapter;
    private boolean itemsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_components_screen);
        itemsEnabled = true;

        components.add(new GpioComponent(0, 27, "out", "LED 1", "Description 1"));
        components.add(new GpioComponent(0, 28, "out", "LED 2", "Description 2"));
        components.add(new GpioComponent(0, 29, "out", "LED 3", "Description 3"));

        curAdapter = new ComponentsAdapter(this, components);
        setListAdapter(curAdapter);

        ServerConnection.getInstance().subscribeOnConnectEvent(this.getClass(), (data) -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::enableAll);
        });

        ServerConnection.getInstance().subscribeOnDisconnectEvent(this.getClass(), (data) -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::disableAll);
        });

        if (!ServerConnection.getInstance().isConnected()) {
            disableAll();
        }
    }

    @Override
    protected void onListItemClick(ListView list, View vi, int position, long id) {
        super.onListItemClick(list, vi, position, id);
    }

    public void addComponent(GpioComponent component) {
        components.add(component);
        curAdapter.notifyDataSetChanged();
    }

    public void removeComponent(int componentId) {
        for (int i = 0; i < components.size(); ++i) {
            if (components.get(i).getId() == componentId) {
                components.remove(i);
                break;
            }
        }
        curAdapter.notifyDataSetChanged();
    }

    private void disableAll() {
        itemsEnabled = false;
        curAdapter.notifyDataSetChanged();
    }

    private void enableAll() {
        itemsEnabled = true;
        curAdapter.notifyDataSetChanged();
    }

    private class ComponentsAdapter extends BaseAdapter {
        Context context;
        List<GpioComponent> data;
        private LayoutInflater inflater;

        private ComponentsAdapter(Context context, List<GpioComponent> data) {
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).getId();
        }

        @Override
        public boolean isEnabled(int position) {
            return itemsEnabled;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return itemsEnabled;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null) {
                vi = inflater.inflate(R.layout.component_toggle, null);
            }
            ((TextView) vi.findViewById(R.id.mainTitle)).setText(data.get(position).getName());
            ((TextView) vi.findViewById(R.id.subtitle)).setText(data.get(position).getDescription());

            if (data.get(position).getDirection().equals("out")) {
                RadioGroup toggle = (RadioGroup) vi.findViewById(R.id.toggle);
                RadioButton toggleOn = vi.findViewById(R.id.toggleOn);
                RadioButton toggleOff = vi.findViewById(R.id.toggleOff);

                toggle.setOnCheckedChangeListener(null);
                if (data.get(position).getIsOn()) {
                    toggle.check(toggleOn.getId());
                }
                else {
                    toggle.check(toggleOff.getId());
                }
                toggle.setOnCheckedChangeListener((radioGroup, checkedId) -> {
                    data.get(position).setIsOn(!data.get(position).getIsOn());
                    notifyDataSetChanged();
                });

                if (itemsEnabled) {
                    toggleOn.setEnabled(true);
                    toggleOff.setEnabled(true);
                }
                else {
                    toggleOn.setEnabled(false);
                    toggleOff.setEnabled(false);
                }
            }

            // needed for correct rendering
            if (ServerConnection.getInstance().isConnected()) {
                vi.setEnabled(true);
            }
            else {
                vi.setEnabled(false);
            }

            return vi;
        }
    }

    /*
    TODO: FOR REFERENCE ONLY

        ServerConnection.getInstance().scheduleRequest(RequestCode.LED_STATUS,true, data -> {
            boolean isOn = false;
            try {
                String msg = "LED is turned ";
                msg += data.getString("ledStatus") + ".";
                toggleLEDMsg.setText(msg);

                isOn = data.getString("ledStatus").equals("on");
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
                e.printStackTrace();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            final boolean isOnFinal = isOn;
            handler.post(() -> enableButtons(isOnFinal));
        });

    private void toggleLED(boolean turnOn) {
        String toSend = (turnOn) ? "on" : "off";
        JSONObject extra = new JSONObject();
        try {
            extra.put("ledStatus", toSend);
        }
        catch (JSONException e) {
            Logger.log(e.getMessage(), Level.ERROR);
            e.printStackTrace();
            return;
        }
        ServerConnection.getInstance().scheduleRequest(RequestCode.TOGGLE_LED, extra, true, data -> {
            boolean isOn = false;
            try {
                if (data.getString("status").equals("OK")) {
                    String msg = "success: LED is turned ";
                    msg += data.getString("ledstatus") + ".";
                    toggleLEDMsg.setText(msg);

                    isOn = data.getString("ledStatus").equals("on");
                }
                else {
                    Logger.log("Could not toggle LED.", Level.ERROR);
                    Logger.log(data.toString(), Level.DEBUG);
                    String msg = "error: " + data.getString("err_code") + ". LED is turned ";
                    msg += data.getString("ledStatus") + ".";
                    toggleLEDMsg.setText(msg);
                    isOn = data.getString("ledStatus").equals("on");
                }
            }
            catch (JSONException e) {
                Logger.log(e.getMessage(), Level.ERROR);
                e.printStackTrace();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            final boolean isOnFinal = isOn;
            handler.post(() -> enableButtons(isOnFinal));
        });
    }

     */

}
