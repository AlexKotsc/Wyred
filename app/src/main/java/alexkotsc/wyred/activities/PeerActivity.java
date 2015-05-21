package alexkotsc.wyred.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import alexkotsc.wyred.R;
import alexkotsc.wyred.peer.IPeerActivity;
import alexkotsc.wyred.peer.Peer;
import alexkotsc.wyred.peer.PeerExpandableListAdapter;
import alexkotsc.wyred.peer.conn.ConnectionManager;
import alexkotsc.wyred.peer.conn.WifiPeerService;


public class PeerActivity extends ActionBarActivity implements IPeerActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView elv;
    List<String> listHeaders;
    HashMap<String, List<Peer>> listData;
    boolean wifiEnabled = false;
    boolean wifiBound = false;

    final String TAG = "PeerActivity";

    WifiPeerService wifiPeerService;

    List<Peer> availablePeers;
    List<Peer> unavailablePeers;

    String screenName;
    ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer);

        availablePeers = new ArrayList<>();
        unavailablePeers = new ArrayList<>();

        elv = (ExpandableListView) findViewById(R.id.expandableListView);

        setupTestData();
        updateAdapter();

        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Peer peer = listData.get(listHeaders.get(groupPosition)).get(childPosition);

                Intent i = new Intent(PeerActivity.this, ConversationActivity.class);

                i.putExtra("peer", peer);
                i.putExtra("peername", screenName);
                i.putExtra("connected", true);

                if(i.getParcelableExtra("peer")==null){
                    Log.d(TAG, "Couldn't add peer to intent.. ");
                }
                startActivity(i);
                return true;
            }
        });
    }

    private void setupTestData() {
        listHeaders = new ArrayList<>();
        listHeaders.add("Available");
        listHeaders.add("Unavailable");

        listData = new HashMap<>();

        listData.put(listHeaders.get(0), availablePeers);
        listData.put(listHeaders.get(1), unavailablePeers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_peer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.action_search:
                wifiPeerService.discoverServices();
                return true;
            case R.id.action_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {

        SharedPreferences.Editor sp = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE).edit();

        sp.remove("username");
        sp.remove("password");
        sp.remove("screenname");

        sp.commit();

        Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show();

        finish();


    }

    @Override
    protected void onStop() {
        if(wifiBound){
            unbindService(serviceConnection);
            wifiBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);
        screenName = sharedPreferences.getString("screenname", "notfound");

        Log.d(TAG, "Binding to service.");

        Intent i = new Intent(this, WifiPeerService.class);
        i.putExtra("name", "PeerActivity: " + hashCode());
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            WifiPeerService.WifiPeerServiceBinder binder = (WifiPeerService.WifiPeerServiceBinder) service;
            wifiPeerService = binder.getService();
            wifiPeerService.setActivity(PeerActivity.this);
            Log.d(TAG, "Bound to service");
            wifiBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {wifiBound = false;
        }
    };

    @Override
    public void wifiStateChanged(boolean state) {
        wifiEnabled = state;
    }

    @Override
    public void handlePeers(HashMap<String, Peer> currentPeers, HashMap<String, Peer> knownPeers) {
        Log.d(TAG, "Received current peers from service.");

        availablePeers.clear();
        unavailablePeers.clear();

        for(Peer p : currentPeers.values()){
            availablePeers.add(p);
        }

        for(Peer p : knownPeers.values()){
            if(!currentPeers.containsKey(p.getPublicKey())){
                unavailablePeers.add(p);
            }
        }
        /*

        for(Peer p : peers.values()){
            if(!availablePeers.contains(p)){
                availablePeers.add(p);
            }
        }

        Iterator<Peer> availableIterator = availablePeers.iterator();
        while(availableIterator.hasNext()){

            Peer peer = availableIterator.next();
            if(!peers.values().contains(peer)){
                if(!unavailablePeers.contains(peer)) {
                    unavailablePeers.add(peer);
                }
                availableIterator.remove();
            }
        }

        Iterator<Peer> unavailableIterator = unavailablePeers.iterator();
        while(unavailableIterator.hasNext()){
            Peer peer = unavailableIterator.next();

            if(availablePeers.contains(peer)){
                unavailableIterator.remove();
            }
        }*/

        updateAdapter();
    }

    @Override
    public String getPeerName() {
        return String.valueOf(hashCode());
    }

    @Override
    public void connectedTo(WifiP2pDevice p) {

    }

    @Override
    public void setConnectionManager(ConnectionManager obj) {
        this.connectionManager = obj;
    }

    @Override
    public void receiveMessage(String readMessage) {
        Log.d(TAG, "Received: " + readMessage);
    }

    @Override
    public void updatePeerList() {
        updateAdapter();
    }

    private void updateAdapter() {
        listData.put(listHeaders.get(0), availablePeers);
        listData.put(listHeaders.get(1), unavailablePeers);

        listAdapter = new PeerExpandableListAdapter(this, listHeaders, listData);

        elv.setAdapter(listAdapter);

        for(int i = 0; i<listHeaders.size(); i++){
            elv.expandGroup(i);
        }
    }
}
