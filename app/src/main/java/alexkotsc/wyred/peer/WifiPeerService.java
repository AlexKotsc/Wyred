package alexkotsc.wyred.peer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alexkotsc.wyred.ChatMessage;
import alexkotsc.wyred.LoginActivity;
import alexkotsc.wyred.db.WyredOpenHelper;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public class WifiPeerService extends Service implements WifiP2pManager.ConnectionInfoListener, Handler.Callback {

    private final IBinder binder = new WifiPeerServiceBinder();
    private HashMap<String, Peer> peers = new HashMap<>();
    private ArrayList<WifiP2pDevice> currentPeers = new ArrayList<>();
    private HashMap<String, WifiP2pDevice> peerMap = new HashMap<>();
    private boolean P2PEnabled = false;
    private IPeerActivity activity;
    private WifiP2pManager mManager = null;
    private WifiP2pManager.Channel mChannel;
    private boolean serviceStarted = false;

    private WifiP2pDevice thisDevice;
    private BroadcastReceiver mReceiver;
    private final WifiP2pManager.DnsSdTxtRecordListener dnsSdTxtRecordListener = new mDnsTxtRecordListener();
    private final WifiP2pManager.DnsSdServiceResponseListener dnsSdServiceResponseListener = new mDnsResponseListener();

    private WifiP2pDevice currentlyConnectedTo = null;

    private final String INSTANCE_NAME = "wyred";
    private final String REG_TYPE = "_aodv._tcp";

    private String DNS_PUBLIC_KEY;
    private String DNS_PEER_NAME;

    private final String TAG = "WifiPeerService";

    private HashMap<String, Peer> visiblePeers;

    private String screenName;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    private Handler handler = new Handler(this);

    private WyredOpenHelper wyredOpenHelper;

    public void clearGroups() {
        //Use reflection if it's possible to clear groups.
        if(mManager != null) {
            try {
                Method[] managerMethods = Class.forName("WifiP2pManager").getDeclaredMethods();

                Class managerTarget = Class.forName("WifiP2PManager");

                Method clearGroupMethod;
                clearGroupMethod = managerTarget.getDeclaredMethod("deletePersistentGroup");

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        Thread handler = null;

        if(info.groupFormed){
            Log.d(TAG, "Group was formed.");
            final String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

            if(info.isGroupOwner){
                Log.d(TAG, "Group owner");
                try {
                    handler = new GroupSocketHandler(this.getHandler());
                    handler.start();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create server thread - " + e.getMessage());
                    return;
                }

            } else {
                Log.d(TAG, "Group participant, owner: " + groupOwnerAddress);
                try {
                    handler = new ClientSocketHandler(this.getHandler(), info.groupOwnerAddress);
                    handler.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private Handler getHandler() {
        return handler;
    }

    public void sendMessage(ChatMessage chatMessage) {
        Log.d(TAG, "Sending message: " + chatMessage.getDate());
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case 666:
                Object obj = msg.obj;
                activity.setConnectionManager((ConnectionManager) obj);
                break;
            case 665:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0 , msg.arg1);

                ChatMessage c = new ChatMessage();

                try {
                    JSONObject receivedMessage = new JSONObject(readMessage);

                    c = ChatMessage.fromJSON(receivedMessage);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                SQLiteDatabase db = wyredOpenHelper.getWritableDatabase();

                long rowID = db.insert(WyredOpenHelper.TABLE_NAME_MESSAGES, null, c.generateInsertValues());

                db.close();

                Log.d(TAG, "Received message stored: " + rowID);
                activity.receiveMessage(readMessage);

        }

        return true;
    }


    public class mDnsResponseListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
            Log.d(TAG, "DnsSdService: " + instanceName);
        }
    }

    public class mDnsTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {

        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
            Log.d(TAG, "DnsSdTxtRecord: " + fullDomainName + ": " + txtRecordMap.get("wyred"));

            if(txtRecordMap.get("wyred")!=null){
                Peer tempPeer = new Peer();

                tempPeer.setWifiP2pDevice(srcDevice);
                tempPeer.setPeerName(txtRecordMap.get("name"));
                tempPeer.setPublicKey(txtRecordMap.get("publicKey"));

                peers.put(tempPeer.getDeviceAddress(), tempPeer);

                requestPeers();
            }
        }
    }

    public WifiPeerService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Log.d(TAG, "onCreate - does this even get called?!");
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);
        screenName = sharedPreferences.getString("screenname", "notfound");


        mReceiver = new mReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(mReceiver, intentFilter);

        currentPeers = new ArrayList<>();
        visiblePeers = new HashMap<>();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        wyredOpenHelper = new WyredOpenHelper(this);

        startServiceRegistration();
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {

        }

        if(mManager != null && mChannel != null){
            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Couldn't remove group on destroy: " + reason);
                }
            });
        }

        wyredOpenHelper.close();

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand - does this get called?");
        if(mManager == null) {
            //Log.d(TAG, "WifiP2pManager initialized");
            mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this,getMainLooper(), null);
        }
        return Service.START_NOT_STICKY;
    }

    private void connectionChanged(NetworkInfo ni) {

        Log.d(TAG, "Connection changed called.");

        if(mManager == null){
            Log.e(TAG, "Manager not initialized.");
            return;
        }

        if(ni.isConnected()){
            Log.d(TAG, "Sending request for Connection Info");
            mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                    Log.d(TAG, "Connection info available!");
                    if(info.groupFormed){
                        Log.d(TAG, "Group was formed.");
                        final String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

                        if(info.isGroupOwner){
                            Log.d(TAG, "Group owner");


                        } else {
                            Log.d(TAG, "Group participant, owner: " + groupOwnerAddress);

                        }
                    }
                }
            });
        }
    }

    private void changeState(boolean b) {

        P2PEnabled = b;

        if(activity!=null){
            activity.wifiStateChanged(P2PEnabled);
        }
    }

    public void requestPeers() {
        if(visiblePeers!=null){
            activity.handlePeers(visiblePeers);
        }
    }

    private void startServiceRegistration(){
        Map record = new HashMap();

        Log.d(TAG, "starting service registration");


        record.put("name", screenName);
        record.put("publicKey", screenName);
        record.put("available", "visible");
        record.put("wyred","enabled");

        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(INSTANCE_NAME, REG_TYPE, record);

        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Local service could not be started: " + reason);
            }
        });

        discoverServices();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind - when does this get called?");

        String deviceName = intent.getStringExtra("name");

        if(deviceName != null){
            DNS_PEER_NAME = deviceName;
        }

        return binder;
    }

    public void discoverServices() {
        visiblePeers.clear();

        mManager.setDnsSdResponseListeners(mChannel, new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                if (instanceName.equals(INSTANCE_NAME)) {
                    //Log.d(TAG, "onDnsSdService: " + srcDevice.hashCode());
                }
            }
        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                if (txtRecordMap.get("wyred") != null) {
                    Peer peer = new Peer();

                    peer.setPublicKey(txtRecordMap.get("publicKey"));
                    peer.setPeerName(txtRecordMap.get("name"));
                    peer.setWifiP2pDevice(srcDevice);

                    visiblePeers.put(peer.getPublicKey(), peer);
                    requestPeers();
                }
            }
        });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "addServiceRequest failed: " + reason);
            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

                Log.e(TAG, "Failed to discover services.");

            /*

                Log.e(TAG, "FAILED to discover services: " + reason);
                changeState(false);

                if (reason == WifiP2pManager.NO_SERVICE_REQUESTS) {

                    // initiate a stop on service discovery
                    mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // initiate clearing of the all service requests
                            mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    // reset the service listeners, service requests, and discovery
                                    discoverServices();
                                }

                                @Override
                                public void onFailure(int i) {
                                    Log.d(TAG, "FAILED to clear service requests ");
                                }
                            });

                        }

                        @Override
                        public void onFailure(int i) {
                            Log.d(TAG, "FAILED to stop discovery");
                        }
                    });
                }*/
            }
        });
    }

    public void setActivity(IPeerActivity activity) {
        this.activity = activity;
        this.activity.wifiStateChanged(P2PEnabled);
    }

    public void connect(final WifiP2pDevice clickedPeer) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = clickedPeer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        if(serviceRequest!=null){
            mManager.removeServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Failed to remove service request: " + reason);
                }
            });
        }

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Log.d(TAG, "Succesfully sent connect to framework. Waiting for connection info...");
                //currentlyConnectedTo = clickedPeer;
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Connecting to peer failed: " + reason);
            }
        });
    }


    public class WifiPeerServiceBinder extends Binder {
        public WifiPeerService getService() {
            return WifiPeerService.this;
        }
    }

    public class mReceiver extends BroadcastReceiver {

        public mReceiver () {}

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
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if(networkInfo.isConnected()) {
                    Log.d(TAG, "Requesting connection info...");
                    mManager.requestConnectionInfo(mChannel, WifiPeerService.this);
                }

                //connectionChanged((NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO));
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

                thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Log.d(TAG, "Device status: " + thisDevice.status);
                //startServiceRegistration();
            }
        }
    }
}
