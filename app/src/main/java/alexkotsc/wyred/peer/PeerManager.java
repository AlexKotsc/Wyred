package alexkotsc.wyred.peer;

import android.content.Context;

/**
 * Created by AlexKotsc on 21-04-2015.
 */
public interface PeerManager {
    public void setContext(Context c);
    public Context getContext();

    public Peer[] getPeers();
    public void discoverPeers();
    public boolean isEnabled();
}
