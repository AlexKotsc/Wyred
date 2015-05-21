package alexkotsc.wyred.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import alexkotsc.wyred.R;
import alexkotsc.wyred.db.WyredOpenHelper;


public class LoginActivity extends ActionBarActivity {

    private static final String TAG = "LoginActivity";
    WyredOpenHelper dbHelper;

    Button newUserBtn, clearBtn, submitBtn;
    EditText username, password;

    public static final String PREF_NAME = "wyred_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new WyredOpenHelper(this);

        newUserBtn = (Button) findViewById(R.id.loginNewUserBtn);
        newUserBtn.setBackground(null);
        newUserBtn.setTypeface(newUserBtn.getTypeface(), Typeface.ITALIC);
        newUserBtn.setTextColor(Color.GRAY);
        newUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, UserActivity.class);
                startActivity(i);
            }
        });

        submitBtn = (Button) findViewById(R.id.loginLoginBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLogin();
            }
        });

        clearBtn = (Button) findViewById(R.id.loginClearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

        username = (EditText) findViewById(R.id.loginUserText);

        password = (EditText) findViewById(R.id.loginPasswordText);

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);


        if(sharedPreferences.getString("username",null)!=null){
            if(checkLogin(sharedPreferences.getString("username", ""), sharedPreferences.getString("password", ""))!=null){
                Toast.makeText(this, "Logged in as " + sharedPreferences.getString("screenname", "unknown"), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, PeerActivity.class);
                startActivity(i);
            }
        }
    }

    private void clearFields() {
        username.getText().clear();
        password.getText().clear();
        username.requestFocus();
    }

    private void submitLogin() {

        String user = username.getText().toString();
        String pass = password.getText().toString();

        user = "a";
        pass = "a";

        String screenName = checkLogin(user, pass);

        if(screenName!=null){

            Toast.makeText(this, "Login succesful.", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(this, PeerActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "Login failed, please try again.", Toast.LENGTH_LONG).show();
            password.getText().clear();
        }
    }

    private String checkLogin(String username, String password){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor result = db.query(WyredOpenHelper.TABLE_NAME_USERS, null, "username = ? AND password = ?", new String[]{password, username},null, null, null);

        result.moveToFirst();
        if(result.getCount()==1){

            SharedPreferences.Editor prefEditor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
            prefEditor.putString("username", username);
            prefEditor.putString("password", password);
            prefEditor.putString("screenname", result.getString(result.getColumnIndex("screenname")));
            prefEditor.putString("publicKey", result.getString(result.getColumnIndex("publickey")));
            prefEditor.commit();

            Log.d(TAG, "publickey: " + result.getString(result.getColumnIndex("publickey")).hashCode());

            db.close();

            return result.getString(3);
        }

        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
