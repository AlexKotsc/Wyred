package alexkotsc.wyred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;


public class WifiP2P extends ActionBarActivity {

    public final static String logtag = "WifiP2P";

    private boolean P2PState = false;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private List<WifiP2pDevice> peers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_wi_fi_p2_p);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiP2PReceiver(mManager, mChannel, this);

        peers = new ArrayList<WifiP2pDevice>();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION  );

        Button searchBtn = (Button) findViewById(R.id.wifiPeerSearchBtn);
        searchBtn.setOnClickListener(new searchBtnOnClickListener(this));

        ListView lw = (ListView) findViewById(R.id.listView);
        lw.setEmptyView(findViewById(R.id.emptylist));



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
        } else {
            stateText.setText("Disabled");
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void discoverPeers() {

        Log.d(logtag, "Discovering peers");

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(WifiP2P.logtag, "ActionListener - Success");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(WifiP2P.logtag, "ActionListener - Failure: " + reason);
            }
        });
    }

    public void receivePeers(WifiP2pDeviceList peerlist){

        peers = new ArrayList<WifiP2pDevice>();

        for(WifiP2pDevice wd : peers){
            peers.add(wd);
        }

        ListView lw = (ListView) findViewById(R.id.listView);
        lw.setAdapter(new PeerListAdapter(this, R.layout.peerlistview, peers));

        Log.d(logtag, "Receiving peers");
    }

}
