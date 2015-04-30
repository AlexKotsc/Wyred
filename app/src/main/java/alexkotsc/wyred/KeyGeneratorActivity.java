package alexkotsc.wyred;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;


public class KeyGeneratorActivity extends ActionBarActivity {

    EditText keyPrivateKeyTxtField;
    TextView keyPublicKeyTxt;
    Button keyGenerateBtn;

    Button keySaveKeyBtn;

    private KeyPair keys;
    private Editable password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_generator);

        keyPrivateKeyTxtField = (EditText) findViewById(R.id.keyPrivateKeyTxtField);
        keyPublicKeyTxt = (TextView) findViewById(R.id.keyPublicText);
        keyGenerateBtn = (Button) findViewById(R.id.keyGenerateBtn);
        keySaveKeyBtn = (Button) findViewById(R.id.keySaveKeyBtn);

        keyGenerateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateKey();
            }
        });

        keySaveKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentKey();
            }
        });

    }

    private void saveCurrentKey() {
        if(keys == null){
            Toast.makeText(this, "Generate new key pair first.", Toast.LENGTH_SHORT).show();
        } else {

            final EditText input = new EditText(this);
            String value;

            new AlertDialog.Builder(KeyGeneratorActivity.this)
                    .setTitle("Encrypt with password")
                    .setMessage("Enter a password for encrypting your private key.")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            setPassword(input.getText());
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
        }
    }

    private void setPassword(Editable text) {
        password = text;

        if(password != null){
            Toast.makeText(this, "Saving private key with pass: " + password.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void generateKey() {
        if(keys == null){
            try {
                keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();

                PublicKey pk = keys.getPublic();
                keyPublicKeyTxt.setText(keys.getPublic().getFormat());

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_key_generator, menu);
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
