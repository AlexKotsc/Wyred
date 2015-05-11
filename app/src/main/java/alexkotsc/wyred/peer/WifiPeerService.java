package alexkotsc.wyred.peer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public class WifiPeerService extends Service {

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

    private final String TAG = "WifiPeerService";

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

            /*if(txtRecordMap.get("wyred").equals("enabled")){
                Peer tempPeer = new Peer();
                tempPeer.setWifiP2pDevice(srcDevice);
                tempPeer.setPeerName(txtRecordMap.get("name"));
                tempPeer.setPublicKey(txtRecordMap.get("publicKey"));

                peers.put(tempPeer.getDeviceAddress(), tempPeer);

                currentPeers.add(srcDevice);
                peerMap.put(srcDevice.deviceAddress, srcDevice);
            }*/
        }
    }

    public WifiPeerService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mReceiver = new mReceiver(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(mReceiver, intentFilter);

        currentPeers = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {

        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mManager == null) {
            //Log.d(TAG, "WifiP2pManager initialized");
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

                    if(currentlyConnectedTo != null){
                        Toast.makeText(WifiPeerService.this, "Connected to " + currentlyConnectedTo.deviceName, Toast.LENGTH_SHORT).show();
                    }
                    final String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

                    if (info.groupFormed && info.isGroupOwner) {
                        Toast.makeText(WifiPeerService.this, "I'm group owner!", Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    ServerSocket serverSocket = new ServerSocket(8080);
                                    Socket socketClient = null;

                                    while(true) {
                                        socketClient = serverSocket.accept();

                                        ServerAsyncTask serverAsyncTask = new ServerAsyncTask();
                                        serverAsyncTask.execute(new Socket[] {socketClient});
                                        Log.d(TAG, "Connected with client");

                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "FAILED to establish connection with client: " + e.getMessage());
                                }
                            }
                        }).start();

                        Log.d(TAG, "ServerSocket thread started!");

                    } else if (info.groupFormed) {
                        Toast.makeText(WifiPeerService.this, "I have joined the group!", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    Socket socket = new Socket(InetAddress.getByName(groupOwnerAddress), 8080);

                                    String result = null;

                                    while(socket.isConnected()){
                                        InputStream is = socket.getInputStream();

                                        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

                                        BufferedReader br = new BufferedReader(new InputStreamReader(is));

                                        result = br.readLine();

                                        pw.write(result + ", well hello back!");
                                        Log.d(TAG, "Connected with server: " + result);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "FAILED to communicate with server: " + e.getMessage());
                                }
                            }
                        }).start();
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
        if(mManager != null){
            if(activity!=null){
                activity.handlePeers(peers);
            }
        }
    }

    private void startServiceRegistration(){
        if(!serviceStarted){

            Map record = new HashMap();

            Log.d(TAG, "starting service registration");

            if(thisDevice==null){
                record.put("name", "unknown");
            } else {
                record.put("name", "WYRED-" + thisDevice.deviceName);
            }

            record.put("publicKey", "test1234test");

            record.put("available", "visible");
            record.put("wyred","enabled");

            WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_wyredapp", "_aodv._tcp", record);

            mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    discoverServices();
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Local service could not be started: " + reason);
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(mManager == null) {
            mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this,getMainLooper(), null);

            mManager.setDnsSdResponseListeners(mChannel, dnsSdServiceResponseListener, dnsSdTxtRecordListener);
            discoverServices();
        }

        return binder;
    }

    private void setServiceListeners(){
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener(){

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Service request succesfully added.");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "FAILED to add service request: " + reason);
                    }
                }
        );
    }

    public void discoverServices() {
        setServiceListeners();

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
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
                                    setServiceListeners();
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
                }
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

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                currentlyConnectedTo = clickedPeer;
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

    public class mReceiver extends BroadcastReceiver {

        WifiPeerService wifiPeerService;

        public mReceiver(WifiPeerService ws){
            wifiPeerService = ws;
        }

        public mReceiver () {}

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                    wifiPeerService.changeState(true);
                } else {
                    wifiPeerService.changeState(false);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                wifiPeerService.requestPeers();
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                wifiPeerService.connectionChanged((NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO));
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                thisDevice = device;
                Log.d(TAG, "local device set! " + thisDevice.deviceName);
                startServiceRegistration();

            }
        }
    }
}
