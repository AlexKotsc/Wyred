package alexkotsc.wyred;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import alexkotsc.wyred.peer.Peer;


public class PeerActivity extends ActionBarActivity {

    ExpandableListAdapter listAdapter;
    List<String> listHeaders;
    HashMap<String, List<Peer>> listData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer);

        ExpandableListView elv = (ExpandableListView) findViewById(R.id.expandableListView);

        setupTestData();

        listAdapter = new PeerExpandableListAdapter(this, listHeaders, listData);

        elv.setAdapter(listAdapter);

        for(int i = 0; i<listHeaders.size(); i++){
            elv.expandGroup(i);
        }

        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Peer peer = listData.get(listHeaders.get(groupPosition)).get(childPosition);

                Toast.makeText(PeerActivity.this, peer.hashCode() + " clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    private void setupTestData() {
        listHeaders = new ArrayList<>();
        listHeaders.add("Available");
        listHeaders.add("Unavailable");

        listData = new HashMap<>();

        List<Peer> availablePeers = new ArrayList<>();
        availablePeers.add(new Peer());
        availablePeers.add(new Peer());
        availablePeers.add(new Peer());

        List<Peer> unavailablePeers = new ArrayList<>();
        unavailablePeers.add(new Peer());
        unavailablePeers.add(new Peer());
        unavailablePeers.add(new Peer());
        unavailablePeers.add(new Peer());

        listData.put(listHeaders.get(0), availablePeers);
        listData.put(listHeaders.get(1), unavailablePeers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_peer, menu);
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
