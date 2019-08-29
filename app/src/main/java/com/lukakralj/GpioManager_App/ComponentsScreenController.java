package com.lukakralj.GpioManager_App;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.lukakralj.GpioManager_App.backend.GpioComponent;
import com.lukakralj.GpioManager_App.backend.RequestCode;
import com.lukakralj.GpioManager_App.backend.ServerConnection;
import com.lukakralj.GpioManager_App.backend.logger.Level;
import com.lukakralj.GpioManager_App.backend.logger.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ComponentsScreenController extends ListActivity {

    private static List<GpioComponent> components = new ArrayList<>();
    private ComponentsAdapter curAdapter;
    private boolean itemsEnabled;
    private TextView componentsMsg;
    private boolean joinedRoom;
    private ImageButton newComponentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_components_screen);

        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.editComponentToolbar);
        setSupportActionBar(myToolbar);*/


        itemsEnabled = true;

        curAdapter = new ComponentsAdapter(this, components);
        setListAdapter(curAdapter);

        componentsMsg = (TextView) findViewById(R.id.componentsMsg);
        newComponentBtn = (ImageButton) findViewById(R.id.newComponentBtn);
        newComponentBtn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, NewComponentController.class);
            startActivity(intent);
        });

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
            ServerConnection.getInstance().scheduleRequest(RequestCode.JOIN_COMPONENTS_ROOM, null, data -> {
                Logger.log("Joined components room.");
                joinedRoom = true;
            });
        }

        ServerConnection.getInstance().scheduleRequest(RequestCode.COMPONENTS, null, data -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
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

                curAdapter.notifyDataSetChanged();
                componentsMsg.setText(msgId);
            });
        });
    }

    @Override
    public void onBackPressed() {
        ServerConnection.getInstance().scheduleRequest(RequestCode.LEAVE_COMPONENTS_ROOM, null, data -> {
            Logger.log("Left components room.");
            joinedRoom = false;
        });

        Intent intent = new Intent(this, HomeScreenController.class);
        startActivity(intent);
    }

    private void startEditComponentActivity(GpioComponent componentToEdit) {
        Intent intent = new Intent(this, EditComponentController.class);
        intent.putExtra("editComp", componentToEdit);
        startActivity(intent);
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
            return true; // If true, the whole row is clickable.
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null) {
                if (data.get(position).getDirection().equals("out")) {
                    vi = inflater.inflate(R.layout.component_toggle, null);
                }
                else {
                    // TODO: render "IN" component
                }
            }
            ((TextView) vi.findViewById(R.id.mainTitle)).setText(data.get(position).getName());
            // roughly 28 characters plus 3 dots can fit on the screen nicely.
            // TODO: check if you can enforce this in xml
            String description = data.get(position).getDescription();
            if (description.length() > 28) {
                description = description.substring(0, 28) + "...";
            }
            ((TextView) vi.findViewById(R.id.subtitle)).setText(description);

            if (data.get(position).getDirection().equals("out")) {
                RadioGroup toggle = (RadioGroup) vi.findViewById(R.id.toggle);
                RadioButton toggleOn = vi.findViewById(R.id.toggleOn);
                RadioButton toggleOff = vi.findViewById(R.id.toggleOff);

                // must set to null first otherwise an action is fired during setup
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
                    ServerConnection.getInstance().scheduleRequest(RequestCode.TOGGLE_COMPONENT, extra, (serverData) -> {
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

                toggleOn.setEnabled(itemsEnabled);
                toggleOff.setEnabled(itemsEnabled);
            }
            else {
                // TODO: render "IN" component
            }

            vi.setOnClickListener(v -> startEditComponentActivity(data.get(position)));

            // needed for correct rendering
            vi.setEnabled(itemsEnabled);

            return vi;
        }
    }
}
