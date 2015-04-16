package alexkotsc.wyred;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by AlexKotsc on 15-04-2015.
 */
public class PeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

    private Context context;
    private List<WifiP2pDevice> values;

    public PeerListAdapter(Context context, int resource, List<WifiP2pDevice> objects) {
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

        peerNameText.setText(values.get(position).deviceName);
        peerAddressText.setText(values.get(position).deviceAddress);
        peerOtherText.setText(values.get(position).primaryDeviceType);
        peerStatusText.setText("Status: " + values.get(position).status);

        return rowView;
    }
}
