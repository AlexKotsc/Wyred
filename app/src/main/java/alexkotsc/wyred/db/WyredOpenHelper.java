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
    private static final String TABLE_NAME = "wyred_messages";
    private static Context context;
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "isSender INTEGER," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                    "message VARCHAR(500)," +
                    "publicKey VARCHAR(500));";

    public WyredOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(WifiP2P.TAG, "Upgrade DB called");
    }
}
