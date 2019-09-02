package sliit.ranjitha.fyp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import sliit.ranjitha.fyp.model.WifiNetwork;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "wifidatabase";
    private static final String TABLE_CONTACTS = "wifi";
    private static final String KEY_ID = "id";
    private static final String KEY_SSIS = "ssid";
    private static final String KEY_BSSID = "bssid";
    private static final String KEY_TRUSTED = "trusted";
    private static final String KEY_BLOCKED = "blocked";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_SSIS + " TEXT," + KEY_BSSID + " TEXT,"
                + KEY_TRUSTED + " INTEGER DEFAULT 0," + KEY_BLOCKED + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    // code to add the new wifinetwork
    public long  addWifiNetwork(WifiNetwork user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getId()); // Set Id
        values.put(KEY_SSIS, user.getSsid()); // Set ssid
        values.put(KEY_BSSID, user.getBssid()); // Set bssid
        values.put(KEY_TRUSTED, user.getIsTrusted()); // Set trusted
        values.put(KEY_BLOCKED, user.getIsBlocked()); // Set blocked

        // Inserting Row
        long  insertedValue = db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection

        return insertedValue;
    }

    // code to get the wifi network
    public WifiNetwork getWifiNetwork(String bssid) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID,
                        KEY_SSIS, KEY_BSSID, KEY_TRUSTED, KEY_BLOCKED}, KEY_BSSID + "=?",
                new String[]{bssid}, null, null, null, null);

        if( cursor != null && cursor.moveToFirst() ){
            WifiNetwork user = new WifiNetwork();
            user.setId(cursor.getString(0));
            user.setSsid(cursor.getString(1));
            user.setBssid(cursor.getString(2));
            user.setIsTrusted(cursor.getInt(3));
            user.setIsBlocked(cursor.getInt(4));
            cursor.close();

            return user;
        }



        // return wifi
        return null;
    }

}