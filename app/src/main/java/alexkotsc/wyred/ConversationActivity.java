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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import alexkotsc.wyred.db.WyredOpenHelper;
import alexkotsc.wyred.peer.Peer;


public class ConversationActivity extends ActionBarActivity {

    WyredOpenHelper wyredOpenHelper;
    private int messageCount = 0;

    ImageButton backBtn;
    TextView titleText, messageCounter;
    Button sendBtn;
    EditText msgText;

    Peer currentPeer;
    ListView messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation2);

        Intent i = getIntent();

        currentPeer = i.getParcelableExtra("peer");
        if(currentPeer!=null) {
            Toast.makeText(this, currentPeer.getPeerName(), Toast.LENGTH_SHORT).show();
        }

        msgText = (EditText) findViewById(R.id.conversationMessageText);

        titleText = (TextView) findViewById(R.id.conversationPeerName);
        titleText.setText(currentPeer.getPeerName());

        messagesList = (ListView) findViewById(R.id.conversationMessageList);
        messagesList.setEmptyView(findViewById(R.id.emptylist));

        readMessages();

        messageCounter = (TextView) findViewById(R.id.conversationMessageCounter);

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

        sendBtn = (Button) findViewById(R.id.conversationSendMessageButton);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        storeMessage();

        //sendMessage();

        readMessages();

    }

    private void storeMessage() {
        ChatMessage msg = new ChatMessage();

        msg.setMessage(msgText.getText().toString());
        msg.setPeerPublicKey(currentPeer.getPublicKey());
        msg.isSender(true);

        SQLiteDatabase dbWrite = wyredOpenHelper.getWritableDatabase();

        dbWrite.insert(WyredOpenHelper.TABLE_NAME_MESSAGES, null, msg.generateInsertValues());

        dbWrite.close();
    }

    private void readMessages() {
        SQLiteDatabase dbRead = wyredOpenHelper.getReadableDatabase();
        List<ChatMessage> messages = new ArrayList<>();

        Cursor c = dbRead.query(WyredOpenHelper.TABLE_NAME_MESSAGES,
                null, "publicKey=?", new String[]{currentPeer.getPublicKey()}, null, null, null, null);

        messageCount = c.getCount();

        c.moveToFirst();
        while(!c.isAfterLast()){
            ChatMessage msg = new ChatMessage();

            msg.isSender(c.getInt(c.getColumnIndex("isSender"))==1);
            msg.setMessage(c.getString(c.getColumnIndex("message")));
            msg.setPeerPublicKey(c.getString(c.getColumnIndex("publicKey")));
            msg.setDate(c.getString(c.getColumnIndex("timestamp")));

            messages.add(msg);

            c.moveToNext();
        }
        dbRead.close();

        MessageAdapter msgAdapter = new MessageAdapter(this, 0, messages);
        messagesList.setAdapter(msgAdapter);
        messagesList.setSelection(msgAdapter.getCount()-1);
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
