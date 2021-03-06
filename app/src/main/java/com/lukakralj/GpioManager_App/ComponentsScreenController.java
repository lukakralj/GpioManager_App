package com.lukakralj.GpioManager_App;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
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

/**
 * This activity displays the list of all the components that are currently registered.
 *
 *  @author Luka Kralj
 *  @version 1.0
 */
public class ComponentsScreenController extends ListActivity {

    private static List<GpioComponent> components = new ArrayList<>();
    public static boolean joinedRoom;

    private ComponentsAdapter curAdapter;
    private boolean itemsEnabled;
    private TextView componentsMsg;
    private ImageButton newComponentBtn;
    private View loadingScreen;
    private TextView loadingScreenMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_components_screen);

        loadingScreen = (View) findViewById(R.id.loadingScreen);
        loadingScreenMsg = (TextView) findViewById(R.id.loadingScreenMsg);

        itemsEnabled = true;

        // Set custom adapter.
        curAdapter = new ComponentsAdapter(this, components);
        setListAdapter(curAdapter);

        componentsMsg = (TextView) findViewById(R.id.componentsMsg);
        newComponentBtn = (ImageButton) findViewById(R.id.newComponentBtn);
        newComponentBtn.setOnClickListener((view) -> {
            leaveComponentsRoom();
            Intent intent = new Intent(this, NewComponentController.class);
            startActivity(intent);
        });

        ServerConnection.getInstance().subscribeOnConnectEvent(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                enableAll();
                retrieveData();
            });
        });

        ServerConnection.getInstance().subscribeOnDisconnectEvent(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::disableAll);
        });

        ServerConnection.getInstance().subscribeComponentsChangeEvent(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::retrieveData);
        });

        if (ServerConnection.getInstance().isConnected()) {
            enableAll();
            retrieveData();
        }
        else {
            disableAll();
        }
    }

    @Override
    public void onBackPressed() {
        leaveComponentsRoom();
        Intent intent = new Intent(this, HomeScreenController.class);
        startActivity(intent);
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
        newComponentBtn.setEnabled(true);
        itemsEnabled = true;
        curAdapter.notifyDataSetChanged();
    }

    /**
     * Disable all elements and display the loading screen.
     */
    private void disableAll() {
        loadingScreenON(getText(R.string.waitingConnection));
        newComponentBtn.setEnabled(false);
        itemsEnabled = false;
        curAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onListItemClick(ListView list, View vi, int position, long id) {
        super.onListItemClick(list, vi, position, id);
    }

    private void leaveComponentsRoom() {
        ServerConnection.getInstance().scheduleRequest(RequestCode.LEAVE_COMPONENTS_ROOM, data -> {
            Logger.log("Left components room.");
            joinedRoom = false;
        });
    }

    /**
     * Open edit component screen for the selected component.
     *
     * @param componentToEdit Component that the user wants to edit.
     */
    private void startEditComponentActivity(GpioComponent componentToEdit) {
        leaveComponentsRoom();
        Intent intent = new Intent(this, EditComponentController.class);
        intent.putExtra("editComp", componentToEdit);
        startActivity(intent);
    }

    /**
     * Retrieves data of all the components from the server.
     */
    private void retrieveData() {
        disableAll();
        loadingScreenON(getText(R.string.retrievingData));
        if (!joinedRoom) {
            ServerConnection.getInstance().scheduleRequest(RequestCode.JOIN_COMPONENTS_ROOM, data -> {
                Logger.log("Joined components room.");
                joinedRoom = true;
            });
        }

        ServerConnection.getInstance().scheduleRequest(RequestCode.COMPONENTS, data -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                int msgId;
                try {
                    if (data.getString("status").equals("OK")) {
                        JSONArray comps = data.getJSONArray("components");
                        components.clear();
                        for (int i = 0; i < comps.length(); ++i) {
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
                    msgId = R.string.sthWentWrong;
                }

                curAdapter.notifyDataSetChanged();
                componentsMsg.setText(msgId);
                enableAll();
            });
        });
    }

    /**
     * Custom adapter for the ListView.
     */
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
            View vi;
            if (data.get(position).getDirection().equals("out")) {
                vi = inflater.inflate(R.layout.out_component, null);
            }
            else {
                vi = inflater.inflate(R.layout.in_component, null);
            }
            ((TextView) vi.findViewById(R.id.mainTitle)).setText(data.get(position).getName());

            // roughly 28 characters plus 3 dots can fit on the screen nicely.
            String description = data.get(position).getDescription();
            if (description.length() > 28) {
                description = description.substring(0, 28) + "...";
            }
            ((TextView) vi.findViewById(R.id.subtitle)).setText(description);

            if (data.get(position).getDirection().equals("out")) {
                RadioGroup toggle = (RadioGroup) vi.findViewById(R.id.toggle);
                RadioButton toggleOn = vi.findViewById(R.id.toggleOn);
                RadioButton toggleOff = vi.findViewById(R.id.toggleOff);

                // Must set to null first otherwise an action is fired during setup.
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
                        Logger.log(e.getMessage(), Level.ERROR);
                        extra = null;
                    }
                    ServerConnection.getInstance().scheduleRequest(RequestCode.TOGGLE_COMPONENT, extra, (serverData) -> {
                        try {
                            if (serverData.getString("status").equals("OK")) {
                                Logger.log("Toggle successful for component id: " + data.get(position).getId() + ".");
                            }
                        }
                        catch (JSONException e) {
                            Logger.log(e.getMessage(), Level.ERROR);
                        }
                    });
                });

                toggleOn.setEnabled(itemsEnabled);
                toggleOff.setEnabled(itemsEnabled);
            }
            else {
                TextView statusIndicator = (TextView) vi.findViewById(R.id.statusIndicator);
                if (data.get(position).getCurValue() == 1) {
                    statusIndicator.setEnabled(true);
                    statusIndicator.setText(R.string.ON);
                }
                else {
                    statusIndicator.setEnabled(false);
                    statusIndicator.setText(R.string.OFF);
                }
                // If nothing is set the edit screen will pop up.
                statusIndicator.setOnClickListener(v -> { /* Do nothing. */ });
            }

            vi.setOnClickListener(v -> startEditComponentActivity(data.get(position)));

            // Needed for correct rendering.
            vi.setEnabled(itemsEnabled);

            return vi;
        }
    }
}
