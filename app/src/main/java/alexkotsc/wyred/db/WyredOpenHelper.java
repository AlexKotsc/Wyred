package alexkotsc.wyred.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import alexkotsc.wyred.WifiP2P;

/**
 * Created by AlexKotsc on 08-05-2015.
 */
public class WyredOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "wyred";
    public static final String TABLE_NAME_MESSAGES = "wyred_messages";
    public static final String TABLE_NAME_USERS = "wyred_users";
    private static Context context;
    private static final String TABLE_CREATE_MESSAGES =
            "CREATE TABLE " + TABLE_NAME_MESSAGES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "isSender INTEGER," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                    "message VARCHAR(500)," +
                    "publicKey VARCHAR(500));";

    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_NAME_USERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "username VARCHAR(50), " +
                    "password VARCHAR(50), " +
                    "screenname VARCHAR(50));";

    public WyredOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create table for storing messages.
        db.execSQL(TABLE_CREATE_MESSAGES);

        //Create table for storing users.
        db.execSQL(TABLE_CREATE_USERS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(WifiP2P.TAG, "Upgrade DB called");
    }
}
