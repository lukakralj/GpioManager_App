package com.lukakralj.iotControlApp;

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
import com.lukakralj.iotControlApp.backend.GpioComponent;
import com.lukakralj.iotControlApp.backend.RequestCode;
import com.lukakralj.iotControlApp.backend.ServerConnection;
import com.lukakralj.iotControlApp.backend.logger.Level;
import com.lukakralj.iotControlApp.backend.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ComponentsScreen extends ListActivity {

    private static List<GpioComponent> components = new ArrayList<>();
    private ComponentsAdapter curAdapter;
    private boolean itemsEnabled;
    private TextView componentsMsg;
    private boolean joinedRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_components_screen);
        itemsEnabled = true;

        curAdapter = new ComponentsAdapter(this, components);
        setListAdapter(curAdapter);

        componentsMsg = (TextView) findViewById(R.id.componentsMsg);

        ServerConnection.getInstance().subscribeOnConnectEvent(this.getClass(), () -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::enableAll);
            retrieveData();
        });

        ServerConnection.getInstance().subscribeOnDisconnectEvent(this.getClass(), () -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::disableAll);
        });

        ServerConnection.getInstance().subscribeComponentsChangeEvent(this.getClass(), this::retrieveData);

        if (!ServerConnection.getInstance().isConnected()) {
            disableAll();
        }
        else {
            retrieveData();
        }
    }

    @Override
    protected void onListItemClick(ListView list, View vi, int position, long id) {
        super.onListItemClick(list, vi, position, id);
    }

    private void disableAll() {
        componentsMsg.setText(R.string.waitingConnection);
        itemsEnabled = false;
        curAdapter.notifyDataSetChanged();
    }

    private void enableAll() {
        itemsEnabled = true;
        retrieveData();
    }

    private void retrieveData() {
        componentsMsg.setText(R.string.retrievingData);
        if (!joinedRoom) {
            ServerConnection.getInstance().scheduleRequest(RequestCode.JOIN_COMPONENTS_ROOM, false, data -> {
                Logger.log("Joined components room.");
                joinedRoom = true;
            });
        }

        ServerConnection.getInstance().scheduleRequest(RequestCode.COMPONENTS, true, data -> {
            int msgId;
            try {
                if (data.getString("status").equals("OK")) {
                    JSONArray comps = data.getJSONArray("components");
                    components.clear();
                    for (int i = 0; i < comps.length(); ++i) {
                        Logger.log(comps.getJSONObject(i).toString(), Level.DEBUG);
                        components.add(new GpioComponent(comps.getJSONObject(i)));
                    }
                    msgId = R.string.upToDate;
                }
                else {
                    msgId = R.string.sthWentWrong;
                }
            }
            catch (JSONException e) {
                Logger.log(e.getLocalizedMessage(), Level.ERROR);
                e.printStackTrace();
                msgId = R.string.sthWentWrong;
            }
            Handler handler = new Handler(Looper.getMainLooper());
            final int id = msgId;
            handler.post(() -> {
                curAdapter.notifyDataSetChanged();
                componentsMsg.setText(id);
            });
        });
    }

    @Override
    public void onBackPressed() {
        ServerConnection.getInstance().scheduleRequest(RequestCode.LEAVE_COMPONENTS_ROOM, false, data -> {
            Logger.log("Left components room.");
        });
        joinedRoom = false;

        super.onBackPressed();
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
                    JSONObject extra;
                    try {
                        extra = new JSONObject();
                        extra.put("id", data.get(position).getId());
                        extra.put("status", (data.get(position).getIsOn()) ? "off" : "on");
                    }
                    catch (JSONException e) {
                        Logger.log(e.getCause().toString(), Level.ERROR);
                        extra = null;
                    }
                    ServerConnection.getInstance().scheduleRequest(RequestCode.TOGGLE_COMPONENT, extra, true, (serverData) -> {
                        try {
                            if (serverData.getString("status").equals("OK")) {
                                Logger.log("Toggle successful for component id: " + data.get(position).getId() + ".");
                            }
                        }
                        catch (JSONException e) {
                            Logger.log(e.getCause().toString(), Level.ERROR);
                        }
                    });
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
}
