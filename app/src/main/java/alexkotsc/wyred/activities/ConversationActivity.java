package alexkotsc.wyred.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import alexkotsc.wyred.R;
import alexkotsc.wyred.db.WyredOpenHelper;
import alexkotsc.wyred.peer.ChatMessage;
import alexkotsc.wyred.peer.IPeerActivity;
import alexkotsc.wyred.peer.MessageAdapter;
import alexkotsc.wyred.peer.Peer;
import alexkotsc.wyred.peer.conn.ConnectionManager;
import alexkotsc.wyred.peer.conn.WifiPeerService;


public class ConversationActivity extends ActionBarActivity implements IPeerActivity {

    private static final String TAG = "ConversationActivity";
    WyredOpenHelper wyredOpenHelper;
    private int messageCount = 0;
    private boolean wifiBound;
    private WifiPeerService wifiPeerService;
    private String peerName;
    private Peer currentPeer;
    private TextView messageCounter, messagePeername;
    private Button sendBtn;
    private ImageButton backBtn;
    private EditText inputText;
    private ListView messageList;
    private ConnectionManager conMan;
    private boolean connected = false;

    Date d = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation2);

        sendBtn = (Button) findViewById(R.id.conversationSendMessageButton);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        backBtn = (ImageButton) findViewById(R.id.conversationBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        inputText = (EditText) findViewById(R.id.conversationMessageText);

        Intent i = getIntent();

        currentPeer = i.getParcelableExtra("peer");
        if(currentPeer!=null) {
            Toast.makeText(this, currentPeer.getPeerName(), Toast.LENGTH_SHORT).show();
            setTitle("Wyred - " + currentPeer.getPeerName());
        }

        peerName = i.getStringExtra("peername");

        messageList = (ListView) findViewById(R.id.conversationMessageList);
        messageList.setEmptyView(findViewById(R.id.emptymessages));

        updateMessages();

        messagePeername = (TextView) findViewById(R.id.conversationPeerName);
        messagePeername.setText(currentPeer.getPeerName());

        connected = i.getBooleanExtra("connected", false);

    }

    private void sendMessage() {

        ChatMessage cm = new ChatMessage();
        cm.isSender(true);
        cm.setPeerPublicKey(currentPeer.getPublicKey());
        cm.setMessage(inputText.getText().toString());

        SQLiteDatabase db = wyredOpenHelper.getWritableDatabase();

        Long rowId = db.insert(WyredOpenHelper.TABLE_NAME_MESSAGES, null, cm.generateInsertValues());
        Log.d(TAG, "Inserting msg with public key at " + rowId + ": " + cm.getPeerPublicKey());

        db.close();

        db = wyredOpenHelper.getReadableDatabase();

        Cursor c = db.query(WyredOpenHelper.TABLE_NAME_MESSAGES, null, "id = ?", new String[]{rowId.toString()}, null, null,  null);

        List<ChatMessage> fetchedMessages = fetchMessages(c);

        if(conMan != null){
            ChatMessage msg = fetchedMessages.get(0);

            SharedPreferences sp = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);

            msg.isSender(!msg.isSender());
            msg.setPeerPublicKey(sp.getString("publicKey", null));

            if(msg.getPeerPublicKey().equals(sp.getString("publicKey", null))){
                Log.d(TAG, "Msg public peer, matches preferences.");
            }

            Log.d(TAG, "Sending msg with own public key: " + msg.getPeerPublicKey().hashCode());

            conMan.write(msg);
        } else {
            Log.e(TAG, "connection manager isn't set, couldn't send message.");
        }

        db.close();

        updateMessages();
        inputText.getText().clear();

    }

    private void updateMessages() {

        List<ChatMessage> newMessages;

        wyredOpenHelper = new WyredOpenHelper(this);

        SQLiteDatabase database = wyredOpenHelper.getReadableDatabase();
        Cursor results =
                database.query(WyredOpenHelper.TABLE_NAME_MESSAGES, null, "publicKey = ?",
                        new String[]{(currentPeer.getPublicKey())}, null, null, null, null);

        messageCount = results.getCount();


        newMessages = fetchMessages(results);

        wyredOpenHelper.close();

        MessageAdapter messageAdapter = new MessageAdapter(this, 0, newMessages);
        messageList.setAdapter(messageAdapter);
        messageList.setSelection(messageAdapter.getCount() - 1);

        messageCounter = (TextView) findViewById(R.id.conversationMessageCounter);

        if(messageCount == 1){
            messageCounter.setText(Integer.toString(messageCount) + " message");
        } else {
            messageCounter.setText(Integer.toString(messageCount) + " messages");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.action_clear:
                deleteMessages();
                return true;
            case R.id.action_test:
                runTest();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void runTest() {
        Log.d("WyredTest", "Starting test");

        final int total = 100;

        final long startTime = System.currentTimeMillis();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int tens = 0;

                for(int i = 0; i<total; i++){
                    if((i%10)==0 && i>0){
                        tens++;
                        Log.d("WyredTest", tens*10 + " messages sent: " + (System.currentTimeMillis() - startTime));
                    }
                    testMessage();
                }
            }
        });

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.d("WyredTest", "Sending " + total + " messages took: " + elapsedTime);
    }

    public void testMessage(){
        inputText.setText("" + System.currentTimeMillis());
        sendMessage();
    }

    private void deleteMessages() {

        wyredOpenHelper = new WyredOpenHelper(this);

        SQLiteDatabase db = wyredOpenHelper.getWritableDatabase();

        int result = db.delete(WyredOpenHelper.TABLE_NAME_MESSAGES, "publicKey = ?", new String[]{currentPeer.getPublicKey()});

        Toast.makeText(this, "Deleted " + result + " messages.", Toast.LENGTH_SHORT).show();

        wyredOpenHelper.close();

        updateMessages();
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
            wifiPeerService.setActivity(ConversationActivity.this);
            Log.d(TAG, "Bound to service");
            wifiBound = true;
            if(connected){
                conMan = wifiPeerService.getConnectionManager();
            }
            connectToPeer();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {wifiBound = false;
        }
    };

    private void connectToPeer() {
        WifiP2pDevice device = currentPeer.getWifiP2pDevice();
        if(device!=null){
            Log.d(TAG, "Trying to connect to peer.");
            wifiPeerService.connect(device);
        }
    }

    @Override
    public void wifiStateChanged(boolean state) {
        if(!state){
            Toast.makeText(this, "Make sure your WiFi is enabled.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void handlePeers(HashMap<String, Peer> peers, HashMap<String, Peer> knownPeers) {
        //Do nothing.
    }

    @Override
    public String getPeerName() {
        return peerName;
    }

    @Override
    public void connectedTo(WifiP2pDevice p) {
        Log.d(TAG, "Connected with: " + p.deviceName);
        if(p.equals(currentPeer.getWifiP2pDevice())){
            Toast.makeText(this, "Connected with " + currentPeer.getPeerName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setConnectionManager(ConnectionManager obj) {
        this.conMan = obj;
    }

    @Override
    public void receiveMessage(String readMessage) {
        Log.d(TAG, "Received: " + readMessage);
        updateMessages();
    }

    @Override
    public void updatePeerList() {

    }

    private List<ChatMessage> fetchMessages(Cursor c){
        List<ChatMessage> messages = new ArrayList<>();

        c.moveToFirst();
        while(!c.isAfterLast()){
            ChatMessage cm = new ChatMessage();

            if(c.getInt(c.getColumnIndex("isSender"))==1){
                cm.isSender(true);
            } else {
                cm.isSender(false);
            }

            cm.setMessage(c.getString(c.getColumnIndex("message")));
            cm.setPeerPublicKey(c.getString(c.getColumnIndex("publicKey")));
            cm.setDate(c.getString(c.getColumnIndex("timestamp")));

            messages.add(cm);

            c.moveToNext();
        }

        return messages;
    }
}
