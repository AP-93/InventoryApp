package com.example.ante.inventoryApp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ante on 22/07/2017.
 */

public class ItemDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ItemDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "items.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ItemDbHelper}.
     *
     * @param context of the app
     */
    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a String that contains the SQL statement to create the habits table
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemContract.ItemEntry.TABLE_NAME + "("
                + ItemContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemContract.ItemEntry.ITEM_NAME + " TEXT NOT NULL,"
                + ItemContract.ItemEntry.ITEM_PRICE + " INTEGER NOT NULL,"
                + ItemContract.ItemEntry.ITEM_QUANTITY + " INTEGER NOT NULL,"
                + ItemContract.ItemEntry.ITEM_IMAGE + " TEXT NOT NULL ,"
                + ItemContract.ItemEntry.SUPPLIER_NAME + " TEXT NOT NULL,"
                + ItemContract.ItemEntry.SUPPLIER_MAIL + " TEXT NOT NULL,"
                + ItemContract.ItemEntry.SUPPLIER_PHONE + " TEXT NOT NULL)";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
