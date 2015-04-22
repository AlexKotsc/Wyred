package alexkotsc.wyred;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private List<WifiP2pDevice> peers;

    private HashMap<String, WifiP2pDevice> oldpeers;


    private WifiPeerService wifiPeerService;
    private boolean isWifiBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_wi_fi_p2_p);

        oldpeers = new HashMap<>();

        /*mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiP2PReceiver(mManager, mChannel, this);*/

        peers = new ArrayList<>();

        /*mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION  );*/

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
                    Toast.makeText(getApplicationContext(), clickedPeer.deviceName + " : clicked.", Toast.LENGTH_SHORT).show();
                    Log.d(logtag, clickedPeer.deviceName + " clicked");
                } else {
                    Log.e(logtag, "Peer was not found in map.");
                }
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi_p2, menu);
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
        //registerReceiver(mReceiver, mIntentFilter);
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

        Intent i = new Intent(this, WifiPeerService.class);
        Bundle b = new Bundle();
        b.putString("action", WifiPeerService.WifiAction.PEERS_CHANGED.toString());
        i.putExtras(b);
        startService(i);



        /*mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(WifiP2P.logtag, "ActionListener - Success");

                //peers.add(new WifiP2pDevice());

                //receivePeers(new WifiP2pDeviceList());

            }

            @Override
            public void onFailure(int reason) {
                Log.d(WifiP2P.logtag, "ActionListener - Failure: " + reason);
            }
        });*/
    }

    public void receivePeers(WifiP2pDeviceList peerlist){

        Log.d(logtag, "Receiving peers");

        peers = new ArrayList<>();

        Collection<WifiP2pDevice> peerCollection = peerlist.getDeviceList();

        for(WifiP2pDevice wd : peerCollection){
            Log.d(logtag, "Peer: " + wd.toString());
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
}
