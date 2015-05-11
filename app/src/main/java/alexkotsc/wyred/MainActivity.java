package alexkotsc.wyred;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button gotoWifiTestBtn = (Button) findViewById(R.id.gotoWifiTestBtn);
        gotoWifiTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), WifiP2P.class);
                startActivity(i);
            }
        });

        Button gotoBluetoothTestBtn = (Button) findViewById(R.id.gotoBluetoothTestBtn);
        gotoBluetoothTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    Toast.makeText(getApplicationContext(), "Bluetooth Low Enery not supported.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = new Intent(getApplication(), BLEP2P.class);
                startActivity(i);
            }
        });

        Button gotoKeyGeneratorBtn = (Button) findViewById(R.id.mainGeneratorActivityBtn);
        gotoKeyGeneratorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), KeyGeneratorActivity.class);
                startActivity(i);
            }
        });

        Button gotoMessagingBtn = (Button) findViewById(R.id.mainMessagePreviewBtn);
        gotoMessagingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), ConversationActivity.class);
                i.putExtra("publicKey", "pkey");
                startActivity(i);
            }
        });

        Button gotoLoginBtn = (Button) findViewById(R.id.mainLoginBtn);
        gotoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), LoginActivity.class);
                startActivity(i);
            }
        });

        Button gotoPeerActivity = (Button) findViewById(R.id.gotoPeerActivity);
        gotoPeerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), PeerActivity.class);
                startActivity(i);
            }
        });

        if(!wifiState()){
            Toast.makeText(this, "Wi-Fi disabled, please enable.", Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public boolean wifiState()
    {
        WifiManager mng = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return mng.isWifiEnabled();
    }
}
