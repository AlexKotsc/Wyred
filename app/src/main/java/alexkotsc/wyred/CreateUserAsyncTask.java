package alexkotsc.wyred;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import alexkotsc.wyred.activities.UserActivity;
import alexkotsc.wyred.util.KeyUtil;

/**
 * Created by AlexKotsc on 21-05-2015.
 */
public class CreateUserAsyncTask extends AsyncTask<String, Void, KeyPair> {

    private ProgressDialog dialog;
    private UserActivity activity;

    private static final String TAG = "CreateUserAsyncTask";

    public CreateUserAsyncTask(UserActivity activity){
        this.activity = activity;
    }

    @Override
    protected KeyPair doInBackground(String... params) {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();

        String username = params[0];

        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");

            kpg.initialize(new KeyPairGeneratorSpec.Builder(activity)
                    .setAlias(username.toString() + "-wyredkey")
                    .setStartDate(now)
                    .setEndDate(end)
                    .setSerialNumber(BigInteger.valueOf(1))
                    .setSubject(new X500Principal("CN=test1"))
                    .build());

            KeyPair kp = kpg.generateKeyPair();

            String publicKey = Base64.encodeToString(kp.getPublic().getEncoded(), Base64.DEFAULT);

            if(kp.getPublic().equals(KeyUtil.publicKeyFromString(publicKey))){
                Log.d(TAG, "KeyPair succesfully generated.");
            }

            /*KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            Enumeration<String> aliases = ks.aliases();
            List<String> aliasList = Collections.list(aliases);

            for(String s : aliasList){
                Log.d(TAG, s);
            }*/

            return kp;


        } catch (Exception e){
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(activity, "Please wait...", "Creating user", true);
    }

    @Override
    protected void onPostExecute(KeyPair keyPair) {
        dialog.dismiss();
        activity.keyPairGenerated(keyPair.getPublic());
    }
}
