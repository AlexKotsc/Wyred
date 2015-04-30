package alexkotsc.wyred;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import alexkotsc.wyred.peer.Peer;
import alexkotsc.wyred.peer.PeerActivity;
import alexkotsc.wyred.peer.WifiPeerService;


public class WifiP2P extends ActionBarActivity implements PeerActivity {

    public final static String logtag = "WifiP2P";

    private boolean P2PState = false;

    private List<WifiP2pDevice> peers;

    private HashMap<String, WifiP2pDevice> oldpeers;


    private WifiPeerService wifiPeerService;
    private boolean isWifiBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_wi_fi_p2_p);



        oldpeers = new HashMap<>();

        peers = new ArrayList<>();

        Button searchBtn = (Button) findViewById(R.id.wifiPeerSearchBtn);
        searchBtn.setOnClickListener(new searchBtnOnClickListener(this));

        ListView lw = (ListView) findViewById(R.id.listView);
        lw.setEmptyView(findViewById(R.id.emptylist));


        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tw = (TextView) view.findViewById(R.id.listPeerAddress);

                String deviceAddress = tw.getText().toString();

                WifiP2pDevice clickedPeer = oldpeers.get(deviceAddress.toString());

                if(clickedPeer != null){
                    wifiPeerService.connect(clickedPeer);
                    //Toast.makeText(getApplicationContext(), clickedPeer.deviceName + " : clicked.", Toast.LENGTH_SHORT).show();
                    Log.d(logtag, clickedPeer.deviceName + " clicked");
                } else {
                    Log.e(logtag, "Peer was not found in map.");
                }
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wifi_p2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setP2PState(boolean state){
        P2PState = state;

        TextView stateText = (TextView) findViewById(R.id.wifiStateText);
        if(P2PState){
            stateText.setText("Enabled");
            findViewById(R.id.wifiPeerSearchBtn).setEnabled(true);
        } else {
            stateText.setText("Disabled");
            findViewById(R.id.wifiPeerSearchBtn).setEnabled(false);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isWifiBound){
            unbindService(serviceConnection);
            isWifiBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(logtag, "Binding to service");
        Intent i = new Intent(this, WifiPeerService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    public void discoverPeers() {

        Log.d(logtag, "Discovering peers");

        //wifiPeerService.discoverPeers();
        wifiPeerService.discoverServices();

        /*Intent i = new Intent(this, WifiPeerService.class);
        Bundle b = new Bundle();
        b.putString("action", WifiPeerService.WifiAction.PEERS_CHANGED.toString());
        i.putExtras(b);
        startService(i);*/

    }

    public void receivePeers(WifiP2pDeviceList peerlist){

        Log.d(logtag, "Receiving peers");

        peers = new ArrayList<>();

        Collection<WifiP2pDevice> peerCollection = peerlist.getDeviceList();

        for(WifiP2pDevice wd : peerCollection){
            Log.d(logtag, "Peer: " + wd.deviceName);
            peers.add(wd);
            oldpeers.put(wd.deviceAddress, wd);
        }

        ListView lw = (ListView) findViewById(R.id.listView);
        lw.setAdapter(new PeerListAdapter(this, R.layout.peerlistview, peers));

    }

    @Override
    public void receivePeers(Peer[] peers) {
        Log.d(logtag, "Peer[] received");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            WifiPeerService.WifiPeerServiceBinder binder = (WifiPeerService.WifiPeerServiceBinder) service;
            wifiPeerService = binder.getService();
            wifiPeerService.setActivity(WifiP2P.this);
            Log.d(logtag, "Bound to service");
            isWifiBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isWifiBound = false;
        }
    };


    public void receivePeerList(HashMap<String, WifiP2pDevice> currentPeers) {
        Log.d(logtag, "Receiving Wyred services");
        if(currentPeers != null) {

            List<WifiP2pDevice> tempPeers = new ArrayList<>(currentPeers.values());

            for(WifiP2pDevice wd : currentPeers.values()){
                Log.d(logtag, "Peer: " + wd.deviceName);
                peers.add(wd);
                oldpeers.put(wd.deviceAddress, wd);
            }

            ListView lw = (ListView) findViewById(R.id.listView);
            lw.setAdapter(new PeerListAdapter(this, R.layout.peerlistview, tempPeers));
        }
    }
}
