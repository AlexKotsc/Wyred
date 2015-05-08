package alexkotsc.wyred;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;


public class ConversationActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation2);

        ArrayList<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(true));
        messages.add(new ChatMessage(false));
        messages.add(new ChatMessage(true));
        messages.add(new ChatMessage(true));
        messages.add(new ChatMessage(true));
        messages.add(new ChatMessage(false));
        messages.add(new ChatMessage(true));
        messages.add(new ChatMessage(true));

        ListView listView = (ListView) findViewById(R.id.conversationMessageList);
        listView.setEmptyView(findViewById(R.id.emptymessages));
        MessageAdapter messageAdapter = new MessageAdapter(this, 0, messages);
        listView.setAdapter(messageAdapter);
        listView.setSelection(messageAdapter.getCount()-1);

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
