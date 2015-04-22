package alexkotsc.wyred.peer;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import alexkotsc.wyred.WifiP2P;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public class WifiPeerService extends Service {

    private final IBinder binder = new WifiPeerServiceBinder();
    private Peer[] currentPeers;
    private WifiP2pDeviceList deviceList;
    private boolean P2PEnabled = false;
    private WifiP2P activity;
    private WifiP2pManager mManager = null;
    private WifiP2pManager.Channel mChannel;

    public WifiPeerService(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(mManager == null) {
            Log.d(WifiP2P.logtag, "WifiP2pManager initialized");
            mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this,getMainLooper(), null);
        }

        //Log.d(WifiP2P.logtag, "On start: " + intent.getStringExtra("action"));

        String action = intent.getStringExtra("action");

        if(action != null) {

            switch (WifiAction.valueOf(action)) {
                case P2P_ENABLED:
                    setState(true);
                    break;
                case P2P_DISABLED:
                    setState(false);
                    break;
                case PEERS_CHANGED:
                    requestPeers();
                    break;
                default:
                    Log.d(WifiP2P.logtag, "Unsupported action: " + action);
            }
        }

        return Service.START_NOT_STICKY;
    }

    private void setState(boolean b) {

        P2PEnabled = b;

        if(activity!=null){
            activity.setP2PState(P2PEnabled);
        }
    }

    private void requestPeers() {
        if(mManager != null){
            mManager.requestPeers(mChannel, new PeerListener());
        }
    }

    public void receivePeers(WifiP2pDeviceList wl){
        Log.d(WifiP2P.logtag, "Listener received peers");
        if(activity!=null){
            activity.receivePeers(wl);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(mManager == null) {
            Log.d(WifiP2P.logtag, "WifiP2pManager initialized");
            mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this,getMainLooper(), null);
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    setState(true);
                }

                @Override
                public void onFailure(int reason) {
                    setState(false);
                    Toast.makeText(getApplicationContext(), ("Failed to utilize Wifi-Direct: " + reason), Toast.LENGTH_SHORT);
                }
            });
        }

        return binder;
    }

    public void setActivity(WifiP2P wifiP2P) {
        this.activity = wifiP2P;
        this.activity.setP2PState(P2PEnabled);
    }

    public class WifiPeerServiceBinder extends Binder {
        public WifiPeerService getService() {
            return WifiPeerService.this;
        }
    }

    public class PeerListener implements WifiP2pManager.PeerListListener {
        public PeerListener() {

        }

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {

            receivePeers(peers);
        }
    }

    public boolean isEnabled(){
        return false;
    }

    public enum WifiAction {
        PEERS_CHANGED, P2P_ENABLED, P2P_DISABLED, RECEIVE_PEERS, CONNECTION_CHANGED, DEVICE_CHANGED
    }
}
