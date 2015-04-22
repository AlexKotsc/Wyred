package alexkotsc.wyred.peer;

import android.content.Context;

/**
 * Created by AlexKotsc on 21-04-2015.
 */
public class WifiPeerManager implements PeerManager {
    @Override
    public void setContext(Context c) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public Peer[] getPeers() {
        return new Peer[0];
    }

    @Override
    public void discoverPeers() {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
