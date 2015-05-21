package alexkotsc.wyred.peer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import alexkotsc.wyred.R;

/**
 * Created by AlexKotsc on 11-05-2015.
 */
public class PeerExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listHeaders;
    private HashMap<String, List<Peer>> listData;

    public PeerExpandableListAdapter(Context context, List<String> listHeaders, HashMap<String, List<Peer>> listData){
        this.context = context;
        this.listHeaders = listHeaders;
        this.listData = listData;
    }

    @Override
    public int getGroupCount() {
        return listHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listData.get(listHeaders.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listData.get(listHeaders.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.activity_peer_list_group, null);

        TextView listGroupTextView = (TextView) view.findViewById(R.id.list_group_textview);
        listGroupTextView.setText(getGroup(groupPosition) + " - " + getChildrenCount(groupPosition) + " peers");

        if(getChildrenCount(groupPosition)<1){
            listGroupTextView.setTypeface(listGroupTextView.getTypeface(), Typeface.ITALIC);
        } else {
            listGroupTextView.setTypeface(listGroupTextView.getTypeface(), Typeface.BOLD);
        }

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.activity_peer_list_item, null);

        Peer peer = (Peer) getChild(groupPosition, childPosition);

        TextView listItemTextView = (TextView) view.findViewById(R.id.list_item_textview);

        String status = "";

        switch (peer.getWifiP2pDevice().status){
            case WifiP2pDevice.AVAILABLE:
                listItemTextView.setTextColor(Color.BLUE);
                status = "Available";
                break;
            case WifiP2pDevice.CONNECTED:
                listItemTextView.setTextColor(Color.GREEN);
                status = "Connected";
                break;
            case WifiP2pDevice.INVITED:
                listItemTextView.setTextColor(Color.YELLOW);
                status = "Invited";
                break;
            case WifiP2pDevice.UNAVAILABLE:
                listItemTextView.setTextColor(Color.RED);
                status = "Un-available";
                break;
            default:
                listItemTextView.setTextColor(Color.BLACK);
        }

        listItemTextView.setText(peer.getPeerName() + " - " + status);

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
