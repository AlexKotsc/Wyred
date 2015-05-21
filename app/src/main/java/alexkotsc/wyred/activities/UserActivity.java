package alexkotsc.wyred.activities;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import alexkotsc.wyred.CreateUserAsyncTask;
import alexkotsc.wyred.R;
import alexkotsc.wyred.db.WyredOpenHelper;
import alexkotsc.wyred.util.KeyUtil;


public class UserActivity extends ActionBarActivity {

    public static final String TAG = "UserActivity";

    EditText username, password, screenname;
    Button clearBtn, submitBtn;

    WyredOpenHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = new WyredOpenHelper(this);

        username = (EditText) findViewById(R.id.userNameText);
        password = (EditText) findViewById(R.id.userPasswordText);
        screenname = (EditText) findViewById(R.id.userScreenNameText);

        clearBtn = (Button) findViewById(R.id.userClearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

        submitBtn = (Button) findViewById(R.id.userSubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFields();
            }
        });
    }

    private void submitFields() {
        SQLiteDatabase sqlRead = db.getReadableDatabase();
        Cursor c = sqlRead.query(WyredOpenHelper.TABLE_NAME_USERS, null, "username=?", new String[]{username.getText().toString()}, null, null, null);

        if(c.getCount()>0){
            Toast.makeText(this, "Username already exists, please choose another.", Toast.LENGTH_LONG).show();
            username.requestFocus();
            username.getText().clear();

            sqlRead.close();
            return;
        }

        sqlRead.close();

        generateKeyPair();
    }

    private void generateKeyPair() {
        CreateUserAsyncTask createUserAsyncTask = new CreateUserAsyncTask(UserActivity.this);
        createUserAsyncTask.execute(new String[]{username.getText().toString()});
    }

    public void keyPairGenerated(PublicKey pk){
        SQLiteDatabase sqlWrite = db.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("username", username.getText().toString());
        cv.put("password", password.getText().toString());
        cv.put("screenname", screenname.getText().toString());
        cv.put("publickey", KeyUtil.publicKeyToString(pk));

        if(sqlWrite.insert(WyredOpenHelper.TABLE_NAME_USERS, null, cv)!=-1){
            Toast.makeText(this, "User created succesfully, you can now login.", Toast.LENGTH_LONG).show();
            sqlWrite.close();
        } else {
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show();
        }
        sqlWrite.close();

        SharedPreferences.Editor sp = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE).edit();

        //sp.putString("publicKey", KeyUtil.publicKeyToString(pk));
        sp.commit();

        finish();
    }

    private void clearFields() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
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
