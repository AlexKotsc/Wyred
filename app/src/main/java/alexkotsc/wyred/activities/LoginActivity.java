package alexkotsc.wyred.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import alexkotsc.wyred.R;
import alexkotsc.wyred.db.WyredOpenHelper;


public class LoginActivity extends ActionBarActivity {

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
    }

    private void clearFields() {
        username.getText().clear();
        password.getText().clear();
        username.requestFocus();
    }

    private void submitLogin() {
        SQLiteDatabase sqlRead = dbHelper.getReadableDatabase();

        String user = "a";//username.getText().toString();
        String pass = "a";//password.getText().toString();

        String selection = "username = ? AND password = ?";
        String[] selectionArgs = new String[]{pass,user};


        Cursor c = sqlRead.query(WyredOpenHelper.TABLE_NAME_USERS, null, selection,
                selectionArgs, null, null, null);

        c.moveToFirst();

        if(c.getCount()==1){
            //Set shared preferences.

            Toast.makeText(this, c.getString(c.getColumnIndex("screenname")), Toast.LENGTH_SHORT).show();

            Toast.makeText(this, "Login succesful!", Toast.LENGTH_LONG).show();

            SharedPreferences.Editor prefEditor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
            prefEditor.putString("username", user);
            prefEditor.putString("password", pass);
            prefEditor.putString("screenname", c.getString(3));
            prefEditor.commit();

            Intent i = new Intent(this, PeerActivity.class);

            startActivity(i);

            sqlRead.close();
            return;
        } else {
            Toast.makeText(this, "Login failed, please try again.", Toast.LENGTH_LONG).show();
            password.getText().clear();
        }

        sqlRead.close();
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
