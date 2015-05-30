package alexkotsc.wyred.peer.conn;

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
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import alexkotsc.wyred.activities.LoginActivity;
import alexkotsc.wyred.aodv.RouteEntry;
import alexkotsc.wyred.aodv.RouteTable;
import alexkotsc.wyred.db.WyredOpenHelper;
import alexkotsc.wyred.peer.ChatMessage;
import alexkotsc.wyred.peer.IPeerActivity;
import alexkotsc.wyred.peer.Peer;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public class WifiPeerService extends Service implements WifiP2pManager.ConnectionInfoListener, Handler.Callback, WifiP2pManager.GroupInfoListener, WifiP2pManager.DnsSdServiceResponseListener, WifiP2pManager.DnsSdTxtRecordListener {

    private final IBinder binder = new WifiPeerServiceBinder();
    private ArrayList<WifiP2pDevice> currentPeers = new ArrayList<>();
    private boolean P2PEnabled = false;
    private IPeerActivity activity;
    private WifiP2pManager mManager = null;
    private WifiP2pManager.Channel mChannel;
    private boolean serviceStarted = false;

    public final String SERVICE_NAME = "service_name";
    public final String SERVICE_KEY = "service_key";
    public final String SERVICE_WYRED = "service_wyred";

    private WifiP2pDevice thisDevice;
    private BroadcastReceiver mReceiver;

    private WifiP2pDevice currentlyConnectedTo = null;

    private final String INSTANCE_NAME = "wyred";
    private final String REG_TYPE = "_aodv._tcp";

    private String DNS_PUBLIC_KEY;
    private String DNS_PEER_NAME;

    private final String TAG = "WifiPeerService";

    private HashMap<String, Peer> visiblePeers;
    private HashMap<String, Peer> knownPeers;

    private String screenName;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    private Handler handler = new Handler(this);

    private WyredOpenHelper wyredOpenHelper;

    private ConnectionManager connectionManager;

    private WifiP2pGroup currentGroup;

    private RouteTable routeTable;
    private LinkedList<ChatMessage> queuedMessages;

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        Thread handler;

        if(info.groupFormed){
            final String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

            if(info.isGroupOwner){
                Log.d(TAG, "Group owner");
                try {
                    handler = new GroupSocketHandler(this.getHandler());
                    handler.start();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create server thread - " + e.getMessage());
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
        queuedMessages.add(chatMessage);

        sendMessages();
    }

    private void sendMessages() {
        ChatMessage cm = null;
        while((cm=queuedMessages.poll())!=null){
            if(visiblePeers.containsKey(cm.getPeerPublicKey())){
                connect(visiblePeers.get(cm.getPeerPublicKey()).getWifiP2pDevice());
            } else {

            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case ConnectionManager.ManagerHandle:
                connectionManager = (ConnectionManager) msg.obj;
                activity.setConnectionManager(connectionManager);
                break;
            case ConnectionManager.ChatMessageObject:
                ChatMessage cm = (ChatMessage) msg.obj;



                SQLiteDatabase dbw = wyredOpenHelper.getWritableDatabase();

                long rowIDw = dbw.insert(WyredOpenHelper.TABLE_NAME_MESSAGES, null, cm.generateInsertValues());

                Log.d(TAG, cm.getPeerPublicKey());

                dbw.close();

                Log.d(TAG, "Received message stored: " + rowIDw);
                activity.receiveMessage(cm.getMessage());

        }

        return true;
    }



    public WifiPeerService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();

        queuedMessages = new LinkedList<>();

        SharedPreferences sp = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);

        Gson gson = new Gson();

        knownPeers = new HashMap<>();

        //Set knownPeers from preference file if it is stored, else create new hashmap.
        if(sp.getString("peers",null)!=null){
            Type stringPeerMap = new TypeToken<Map<String, Peer>>(){}.getType();
            Map<String, Peer> tempPeers = gson.fromJson(sp.getString("peers", null), stringPeerMap);

            for(Map.Entry<String, Peer> entry : tempPeers.entrySet()){
                knownPeers.put(entry.getKey(), entry.getValue());
            }
            Log.d(TAG, "Loaded " + knownPeers.size() + " known peers from preference file.");
        }

        visiblePeers = new HashMap<>();

        Peer temp = new Peer();
        temp.setPublicKey("testPeer123");
        temp.setPeerName("Test!");
        temp.setWifiP2pDevice(new WifiP2pDevice(null));

        //knownPeers.put(temp.getPublicKey(), temp);

        //Set route table from preference file if it is stored, else create new route table.
        if(sp.getString("routetable", null)!=null){
            Type routeTableType = new TypeToken<RouteTable>(){}.getType();
            routeTable = gson.fromJson(sp.getString("routetable",null), routeTableType);

            Log.d(TAG, "Loaded " + routeTable.getEntryCount() + " route entries from preference file.");
        } else {
            routeTable = new RouteTable();
        }

        RouteEntry routeEntry = new RouteEntry();
        routeEntry.setDestKey("destinationKey");
        routeEntry.setDestSeq(0);
        routeEntry.setFlags(new HashMap<String, Integer>());
        routeEntry.setHopCount(0);
        routeEntry.setNextHop("nextHop");
        routeEntry.setTTL(100);

        //routeTable.addEntry(routeEntry);
        Log.d(TAG, "Routetable: " + gson.toJson(routeTable));


        screenName = sp.getString("screenname", "notfound");

        mReceiver = new mReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(mReceiver, intentFilter);

        currentPeers = new ArrayList<>();


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        wyredOpenHelper = new WyredOpenHelper(this);

        startServiceRegistration();
    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "onDestroy");

        try {
            unregisterReceiver(mReceiver);

            if(currentGroup!=null){

                if(mManager != null && mChannel != null){
                    Log.d(TAG, "CurrentGroup wasn't null, calling removeGroup on manager.");
                    mManager.removeGroup(mChannel, new LoggingListener(TAG, "removeGroup"));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        wyredOpenHelper.close();

        SharedPreferences.Editor sp = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE).edit();

        Gson gson = new Gson();

        sp.putString("peers", gson.toJson(knownPeers));
        sp.putString("routetable", gson.toJson(routeTable));
        sp.commit();

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

    private void changeState(boolean b) {

        P2PEnabled = b;

        if(activity!=null){
            activity.wifiStateChanged(P2PEnabled);
        }
    }

    public void requestPeers() {
        activity.handlePeers(visiblePeers, knownPeers);
    }

    public ConnectionManager getConnectionManager(){
        return connectionManager;
    }

    private void startServiceRegistration(){
        Map record = new HashMap();

        Log.d(TAG, "starting service registration");

        /*SharedPreferences sp = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);
        String publicKey = sp.getString("publicKey", null);

        Log.d(TAG, "My publicKey: " + publicKey);

        if(publicKey!=null){

            int noOfSplits = (int) Math.ceil(publicKey.length()/50);

            Log.d(TAG, "Splitting " + publicKey.length() + " into " + noOfSplits + " pieces.");

            for(int i = 0; i<=noOfSplits; i++){

                int startSubstring = i*50;
                int stopSubstring = (i+1)*50;
                Log.d(TAG, "Taking substring: " + startSubstring + ", " + stopSubstring);

                String s = publicKey.substring(startSubstring, Math.min(stopSubstring,publicKey.length()));
                record.put("publickeysplit" + i, s);

            }

            record.put("publickeysplits", String.valueOf(noOfSplits));

        } else {
            Log.e(TAG, "The publicKey was not set in shared preferences.. WTF man.");
            record.put("publicKey", screenName);
        }*/

        record.put(SERVICE_NAME, screenName);
        record.put(SERVICE_KEY, screenName);
        record.put(SERVICE_WYRED, "enabled");

        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(INSTANCE_NAME, REG_TYPE, record);

        mManager.addLocalService(mChannel, serviceInfo, new LoggingListener(TAG, "addLocalService"));

        discoverServices();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(TAG, "onBind - when does this get called?");
        String deviceName = intent.getStringExtra("name");

        if(deviceName != null){
            DNS_PEER_NAME = deviceName;
        }

        return binder;
    }

    public void discoverServices() {

        visiblePeers.clear();

        mManager.setDnsSdResponseListeners(mChannel, this, this);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel, serviceRequest, new LoggingListener(TAG, "addServiceRequest"));

        mManager.discoverServices(mChannel, new LoggingListener(TAG, "discoverServices"));

    }

    public void setActivity(IPeerActivity activity) {
        this.activity = activity;
        this.activity.wifiStateChanged(P2PEnabled);
        requestPeers();
    }

    public void connect(final WifiP2pDevice clickedPeer) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = clickedPeer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        if(serviceRequest!=null){
            mManager.removeServiceRequest(mChannel, serviceRequest, new LoggingListener(TAG, "removeServiceRequest"));
        }

        mManager.connect(mChannel, config, new LoggingListener(TAG, "connect"));
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        if(group!=null) {
            Log.d(TAG, "Group was set: " + group.getNetworkName());
            currentGroup = group;
        }
    }

    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        if (instanceName.equals(INSTANCE_NAME)) {
            //Log.d(TAG, "onDnsSdService: " + instanceName);
        }
    }

    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
        Log.d(TAG, "DnsSdTxtRecord: " + fullDomainName + ": " + txtRecordMap.get(SERVICE_WYRED));

        if (txtRecordMap.get(SERVICE_WYRED) != null) {
            Peer peer = new Peer();

            /*int noOfSplits = Integer.valueOf(txtRecordMap.get("publickeysplits"));

            StringBuilder publicKeyBuilder = new StringBuilder();

            for(int i = 0; i<=noOfSplits; i++){
                String s = txtRecordMap.get("publickeysplit" + i);
                publicKeyBuilder.append(s);
            }

            peer.setPublicKey(publicKeyBuilder.toString());*/

            peer.setPublicKey(txtRecordMap.get(SERVICE_KEY));
            peer.setPeerName(txtRecordMap.get(SERVICE_NAME));
            peer.setWifiP2pDevice(srcDevice);

            visiblePeers.put(peer.getPublicKey(), peer);
            knownPeers.put(peer.getPublicKey(), peer);

            requestPeers();
        }
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

            if(activity!=null){
                activity.updatePeerList();
            }

            String action = intent.getAction();

            switch (action){
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                        changeState(true);
                    } else {
                        changeState(false);
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:

                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                    if(networkInfo.isConnected()) {
                        Log.d(TAG, "Requesting connection info...");
                        mManager.requestConnectionInfo(mChannel, WifiPeerService.this);
                        mManager.requestGroupInfo(mChannel, WifiPeerService.this);
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                    Log.d(TAG, "Device status: " + thisDevice.status);
            }
        }
    }
}
