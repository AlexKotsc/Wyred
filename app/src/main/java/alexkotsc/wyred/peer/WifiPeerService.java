package alexkotsc.wyred.peer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

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
    private boolean serviceStarted = false;

    private WifiP2pDevice thisDevice;
    private BroadcastReceiver mReceiver;
    private final WifiP2pManager.DnsSdTxtRecordListener dnsSdTxtRecordListener = new mDnsTxtRecordListener();
    private final WifiP2pManager.DnsSdServiceResponseListener dnsSdServiceResponseListener = new mDnsResponseListener();

    public class mReceiver extends BroadcastReceiver {

        public mReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                    changeState(true);
                } else {
                    changeState(false);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                requestPeers();
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                connectionChanged((NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO));
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                thisDevice = device;
            }
        }
    }

    public class mDnsResponseListener implements WifiP2pManager.DnsSdServiceResponseListener {

        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
            Log.d(WifiP2P.logtag, "DnsSdService: " + instanceName);
        }
    }

    public class mDnsTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {

        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
            Log.d(WifiP2P.logtag, "DnsSdTxtRecord: " + fullDomainName);
        }
    }

    public WifiPeerService(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mManager == null) {
            //Log.d(WifiP2P.logtag, "WifiP2pManager initialized");
            mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this,getMainLooper(), null);
        }
        return Service.START_NOT_STICKY;
    }

    private void connectionChanged(NetworkInfo ni) {

        if(mManager == null){
            return;
        }


        if(ni.isConnected()){
            mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                    String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

                    if(info.groupFormed && info.isGroupOwner){

                    } else if (info.groupFormed){

                    }
                }
            });
        }
        //Log.d(WifiP2P.logtag, "Connected?");
    }

    private void changeState(boolean b) {

        P2PEnabled = b;

        if(activity!=null){
            activity.setP2PState(P2PEnabled);
        }
    }

    public void requestPeers() {
        if(mManager != null){
            mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    receivePeers(peers);
                }
            });
        }
    }

    private void startServiceRegistration(){
        if(!serviceStarted){

            Map record = new HashMap();

            if(thisDevice==null){
                record.put("name", "unknown");
            } else {
                record.put("name", thisDevice.deviceName);
            }

            record.put("available", "visible");
            record.put("wyred","enabled");

            WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_wyredapp", "_aodv._tcp", record);

            mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener(){

                @Override
                public void onSuccess() {
                    //Log.d(WifiP2P.logtag, "Local service started succesfully");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(WifiP2P.logtag, "Local service could not be started: " + reason);
                }
            });
        }
    }

    public void receivePeers(WifiP2pDeviceList wl){
        //Log.d(WifiP2P.logtag, "Listener received peers");
        if(activity!=null){
            activity.receivePeers(wl);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(mManager == null) {
            //Log.d(WifiP2P.logtag, "WifiP2pManager initialized");
            mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this,getMainLooper(), null);

            startServiceRegistration();
            discoverPeers();

            mManager.setDnsSdResponseListeners(mChannel, dnsSdServiceResponseListener, dnsSdTxtRecordListener);
            discoverServices();
        }

        return binder;
    }

    public void discoverServices() {



        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener(){

                    @Override
                    public void onSuccess() {
                        Log.d(WifiP2P.logtag, "addServiceRequest");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(WifiP2P.logtag, "addServiceRequest: " + reason);
                    }
                }
        );

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(WifiP2P.logtag, "discoverServices");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WifiP2P.logtag, "discoverServices: " + reason);
            }
        });
    }

    public void setActivity(WifiP2P wifiP2P) {
        this.activity = wifiP2P;
        this.activity.setP2PState(P2PEnabled);
    }

    public void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                changeState(true);
                //Log.d("WifiP2P", "Succesfully initiated peer discovery");
                requestPeers();
            }


            @Override
            public void onFailure(int reason) {
                changeState(false);
                Log.e(WifiP2P.logtag, "discoverPeers: " + reason);
                Toast.makeText(getApplicationContext(), ("Failed to utilize Wifi-Direct: " + reason), Toast.LENGTH_SHORT);
            }
        });
    }

    public void connect(WifiP2pDevice clickedPeer) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = clickedPeer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WifiPeerService.this, "Connection failed: " + reason + ", retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class WifiPeerServiceBinder extends Binder {
        public WifiPeerService getService() {
            return WifiPeerService.this;
        }
    }
}