package com.example.ante.inventoryApp.data;

/**
 * Created by Ante on 23/07/2017.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class ItemProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    /**
     * Database helper object
     */
    private ItemDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the whole "items_in_store" table
     */
    public static final int ITEMS = 100;

    /**
     * URI matcher code for the content URI for a single item in the "items_in_store" table
     */
    public static final int ITEM_ID = 101;

    /**
     * URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer.
    // The calls to addURI() go here, for all of the content URI patterns that the provider
    // should recognize. All paths added to the UriMatcher have a corresponding code to return
    // when a match is found.
    static {
        /* The content URI of the form "content://com.example.android.inventoryApp/items_in_store" will map to the
           integer code {@link #ITEMS}. This URI is used to provide access to MULTIPLE rows
           of the items_in_store table. */
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);

        /*The content URI of the form "content://com.example.android.inventoryApp/items_in_store/#" will map to the
          integer code {@link #ITEM_ID}. This URI is used to provide access to ONE single row of the items_in_store table.
          In this case, the "#" wildcard is used where "#" can be substituted for an integer.
          For example, "content://com.example.android.inventoryApp/items_in_store/3" matches, but
          ""content://com.example.android.inventoryApp/items_in_store" (without a number at the end) doesn't match.  */
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mDbHelper = new ItemDbHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // For the ITEMS code, query the "items_in_store" table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the "items_in_store" table.
                cursor = database.query(ItemContract.ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventoryApp/items_in_store/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the "items_in_store" table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ItemContract.ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // set notification URI on Cursor
        // so we know what content URI the Cursor was created for
        // if data at this URI changes, then we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return deleteItem(uri, selection, selectionArgs);
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteItem(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * Insert a item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new item with the given values
        long id = database.insert(ItemContract.ItemEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri, null);

        database.close();

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link ItemEntry#ITEM_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ItemContract.ItemEntry.ITEM_NAME)) {
            String name = values.getAsString(ItemContract.ItemEntry.ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        // If the {@link ItemEntry#ITEM_PRICE} key is present,
        // check that the price is not null.
        if (values.containsKey(ItemContract.ItemEntry.ITEM_PRICE)) {
            Integer price = values.getAsInteger((ItemContract.ItemEntry.ITEM_PRICE));
            if (price == null) {
                throw new IllegalArgumentException("Item requires  price");
            }
        }

        // If the {@link ItemEntry#ITEM_QUANTITY} key is present,
        // check that the quantity is not null.
        if (values.containsKey((ItemContract.ItemEntry.ITEM_QUANTITY))) {
            Integer quantity = values.getAsInteger((ItemContract.ItemEntry.ITEM_QUANTITY));
            if (quantity == null) {
                throw new IllegalArgumentException("Item requires quantity");
            }
        }

        // If the {@link ItemEntry#ITEM_IMAGE} key is present,
        // check that the name value is not null.
        if (values.containsKey(ItemContract.ItemEntry.ITEM_IMAGE)) {
            String name = values.getAsString(ItemContract.ItemEntry.ITEM_IMAGE);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a item image path");
            }
        }

        // If the {@link ItemEntry#SUPPLIER_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ItemContract.ItemEntry.SUPPLIER_NAME)) {
            String name = values.getAsString(ItemContract.ItemEntry.SUPPLIER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a supplier name");
            }
        }

        // If the {@link ItemEntry#SUPPLIER_MAIL} key is present,
        // check that the name value is not null.
        if (values.containsKey(ItemContract.ItemEntry.SUPPLIER_MAIL)) {
            String name = values.getAsString(ItemContract.ItemEntry.SUPPLIER_MAIL);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a supplier mail");
            }
        }

        // If the {@link ItemEntry#SUPPLIER_PHONE} key is present,
        // check that the name value is not null.
        if (values.containsKey(ItemContract.ItemEntry.SUPPLIER_PHONE)) {
            String name = values.getAsString(ItemContract.ItemEntry.SUPPLIER_PHONE);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a supplier phone");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ItemContract.ItemEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        database.close();

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete items in the database.Delete the rows
     * specified in the selection and selection arguments.
     * Return the number of rows that were successfully deleted.
     */
    private int deleteItem(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int deleted = database.delete(ItemContract.ItemEntry.TABLE_NAME, selection, selectionArgs);

        if (deleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        database.close();

        return deleted;
    }
}
