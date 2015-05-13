package alexkotsc.wyred;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import alexkotsc.wyred.db.WyredOpenHelper;
import alexkotsc.wyred.peer.Peer;


public class ConversationActivity extends ActionBarActivity {

    WyredOpenHelper wyredOpenHelper;
    private int messageCount = 0;

    ImageButton backBtn;
    TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation2);

        Intent i = getIntent();

        Peer currentPeer = i.getParcelableExtra("peer");
        if(currentPeer!=null) {
            Toast.makeText(this, currentPeer.getPeerName(), Toast.LENGTH_SHORT).show();
        }

        titleText = (TextView) findViewById(R.id.conversationPeerName);
        titleText.setText(currentPeer.getPeerName());
        String publicKey = null;

        if((publicKey = i.getStringExtra("publicKey"))==null){
            publicKey = "TestKey";
        }

        ArrayList<ChatMessage> messages = new ArrayList<>();

        ChatMessage cm1 = new ChatMessage(true);
        cm1.setMessage("Besked 1");
        cm1.setPeerPublicKey("TestKey");
        messages.add(cm1);
        ChatMessage cm2 = new ChatMessage(false);
        cm2.setMessage("Besked 2");
        cm2.setPeerPublicKey("TestKey");
        messages.add(cm2);
        ChatMessage cm3 = new ChatMessage(true);
        cm3.setMessage("Besked 3");
        cm3.setPeerPublicKey("TestKey");
        messages.add(cm3);
        ChatMessage cm4 = new ChatMessage(true);
        cm4.setMessage("Besked 4");
        cm4.setPeerPublicKey("TestKey");
        messages.add(cm4);
        ChatMessage cm5 = new ChatMessage(true);
        cm5.setMessage("Besked 5");
        cm5.setPeerPublicKey("pkey");
        messages.add(cm5);

        ArrayList<ChatMessage> newMessages = new ArrayList<>();

        wyredOpenHelper = new WyredOpenHelper(this);
        SQLiteDatabase database = wyredOpenHelper.getWritableDatabase();

        for(ChatMessage c : messages){
            database.insert("wyred_messages", null, c.generateInsertValues());
        }

        database.close();

        database = wyredOpenHelper.getReadableDatabase();
        Cursor results = database.query("wyred_messages", null, "publicKey = '" +  publicKey + "'", null, null, null, null);

        messageCount = results.getCount();

        results.moveToFirst();
        while(results.isAfterLast()== false){
            ChatMessage temp = new ChatMessage();

            if(results.getInt(results.getColumnIndex("isSender"))==1){
                temp.isSender(true);
            } else {
                temp.isSender(false);
            }

            temp.setMessage(results.getString(results.getColumnIndex("message")));
            temp.setPeerPublicKey(results.getString(results.getColumnIndex("publicKey")));
            temp.setDate(results.getString(results.getColumnIndex("timestamp")));

            newMessages.add(temp);

            Log.d(WifiP2P.TAG, temp.getMessage());

            results.moveToNext();
        }


        wyredOpenHelper.close();


        ListView listView = (ListView) findViewById(R.id.conversationMessageList);
        listView.setEmptyView(findViewById(R.id.emptymessages));
        MessageAdapter messageAdapter = new MessageAdapter(this, 0, newMessages);
        listView.setAdapter(messageAdapter);
        listView.setSelection(messageAdapter.getCount() - 1);

        TextView messageCounter = (TextView) findViewById(R.id.conversationMessageCounter);

        if(messageCount == 1){
            messageCounter.setText(Integer.toString(messageCount) + " message");
        } else {
            messageCounter.setText(Integer.toString(messageCount) + " messages");
        }

        backBtn = (ImageButton) findViewById(R.id.conversationBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
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
}
