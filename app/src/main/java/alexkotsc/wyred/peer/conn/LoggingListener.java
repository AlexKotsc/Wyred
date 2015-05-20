package alexkotsc.wyred.peer.conn;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by AlexKotsc on 20-05-2015.
 */
public class LoggingListener implements WifiP2pManager.ActionListener {

    private String context;
    private String TAG;

    public LoggingListener(String TAG, String context){
        this.context = context;
        this.TAG = TAG;
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(int reason) {
        String error = "";

        switch (reason) {
            case WifiP2pManager.BUSY:
                error = "BUSY";
                break;
            case WifiP2pManager.ERROR:
                error = "ERROR";
                break;
            case WifiP2pManager.P2P_UNSUPPORTED:
                error = "P2P_UNSUPPORTED";
        }
        Log.e(TAG, context + ": " + error);
    }
}
