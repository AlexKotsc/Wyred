package alexkotsc.wyred;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alexkotsc.wyred.peer.IPeerActivity;
import alexkotsc.wyred.peer.Peer;
import alexkotsc.wyred.peer.WifiPeerService;


public class PeerActivity extends ActionBarActivity implements IPeerActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView elv;
    List<String> listHeaders;
    HashMap<String, List<Peer>> listData;
    boolean wifiEnabled = false;
    boolean wifiBound = false;

    final String TAG = "PeerActivity";

    WifiPeerService wifiPeerService;

    HashMap<String, Peer> currentPeers;

    String peerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer);



        elv = (ExpandableListView) findViewById(R.id.expandableListView);

        setupTestData();
        updateAdapter();


        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Peer peer = listData.get(listHeaders.get(groupPosition)).get(childPosition);

                Intent i = new Intent(PeerActivity.this, ConversationActivity.class);
                i.putExtra("peer", peer);
                startActivity(i);
                //Toast.makeText(PeerActivity.this, peer.hashCode() + " clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    private void setupTestData() {
        listHeaders = new ArrayList<>();
        listHeaders.add("Available");
        listHeaders.add("Unavailable");

        listData = new HashMap<>();

        List<Peer> availablePeers = new ArrayList<>();
        /*availablePeers.add(new Peer());
        availablePeers.add(new Peer());
        availablePeers.add(new Peer());*/

        List<Peer> unavailablePeers = new ArrayList<>();
        /*unavailablePeers.add(new Peer());
        unavailablePeers.add(new Peer());
        unavailablePeers.add(new Peer());
        unavailablePeers.add(new Peer());*/

        listData.put(listHeaders.get(0), availablePeers);
        listData.put(listHeaders.get(1), unavailablePeers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_peer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        peerName = String.valueOf(hashCode());

        Log.d(TAG, "Binding to service.");

        Intent i = new Intent(this, WifiPeerService.class);
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
    public void handlePeers(HashMap<String, Peer> peers) {
        Log.d(TAG, "Received current peers from service.");

        currentPeers = peers;

        if(peers != null){

            List<Peer> tempList = new ArrayList<>();

            for(Map.Entry<String,Peer> e : peers.entrySet()){

                tempList.add(e.getValue());

            }

            listData.put(listHeaders.get(0), tempList);

            updateAdapter();

        }

        for(Map.Entry<String, Peer> e : peers.entrySet()){
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public String getPeerName() {
        return String.valueOf(hashCode());
    }

    private void updateAdapter() {
        listAdapter = new PeerExpandableListAdapter(this, listHeaders, listData);

        elv.setAdapter(listAdapter);

        for(int i = 0; i<listHeaders.size(); i++){
            elv.expandGroup(i);
        }
    }
}
