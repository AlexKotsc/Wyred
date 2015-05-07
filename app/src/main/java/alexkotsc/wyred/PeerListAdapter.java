package alexkotsc.wyred;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import alexkotsc.wyred.peer.Peer;

/**
 * Created by AlexKotsc on 15-04-2015.
 */
public class PeerListAdapter extends ArrayAdapter<Peer> {

    private Context context;
    private List<Peer> values;

    public PeerListAdapter(Context context, int resource, List<Peer> objects) {
        super(context, R.layout.peerlistview, objects);

        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.peerlistview, parent, false);

        TextView peerNameText = (TextView) rowView.findViewById(R.id.listPeerName);
        TextView peerAddressText = (TextView) rowView.findViewById(R.id.listPeerAddress);
        TextView peerOtherText = (TextView) rowView.findViewById(R.id.listPeerOther);
        TextView peerStatusText = (TextView) rowView.findViewById(R.id.listPeerStatus);

        Peer currentPeer = values.get(position);

        peerNameText.setText(currentPeer.getPeerName());
        peerAddressText.setText(currentPeer.getDeviceAddress());
        peerOtherText.setText(currentPeer.getPublicKey());
        switch(currentPeer.getWifiP2pDevice().status){
            case WifiP2pDevice.AVAILABLE:
                peerStatusText.setText("Available");
                break;
            case WifiP2pDevice.CONNECTED:
                peerStatusText.setText("Connected");
                break;
            case WifiP2pDevice.FAILED:
                peerStatusText.setText("Failed");
                break;
            case WifiP2pDevice.INVITED:
                peerStatusText.setText("Invited");
                break;
            case WifiP2pDevice.UNAVAILABLE:
                peerStatusText.setText("Unavailable");
                break;
            default:
                peerStatusText.setText("Unknown (" + currentPeer.getWifiP2pDevice().status + ")");
        }

        return rowView;
    }
}
