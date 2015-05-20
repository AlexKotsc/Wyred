package alexkotsc.wyred;

import android.view.View;

import alexkotsc.wyred.activities.WifiP2P;

/**
 * Created by AlexKotsc on 15-04-2015.
 */
public class searchBtnOnClickListener implements View.OnClickListener {

    private WifiP2P activity;

    public searchBtnOnClickListener(WifiP2P activity){
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        activity.discoverPeers();
    }
}
