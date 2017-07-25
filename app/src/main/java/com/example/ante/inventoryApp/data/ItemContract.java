package com.example.ante.inventoryApp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ante on 22/07/2017.
 */

public class ItemContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.ante.inventoryApp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryApp/items_in_store/ is a valid path for
     * looking at item data.
     */
    public static final String PATH_ITEMS = "items_in_store";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ItemContract() {
    }

    /**
     * Inner class that defines constant values for database table.
     */
    public static final class ItemEntry implements BaseColumns {

        /**
         * The content URI to access the item data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * Name of database table
         */
        public final static String TABLE_NAME = "items_in_store";

        /**
         * Unique ID number for each row in table (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the item.
         * Type: TEXT
         */
        public final static String ITEM_NAME = "name";

        /**
         * Item price.
         * Type: INTEGER
         */
        public final static String ITEM_PRICE = "price";

        /**
         * Number of items in stock.
         * Type: INTEGER
         */
        public final static String ITEM_QUANTITY = "quantity";

        /**
         * String path to stored image.
         * Type: TEXT
         */
        public final static String ITEM_IMAGE = "image";

        /**
         * Name of the item supplier.
         * Type: TEXT
         */
        public final static String SUPPLIER_NAME = "supplier_name";

        /**
         * Supplier email address.
         * Type: TEXT
         */
        public final static String SUPPLIER_MAIL = "supplier_mail";

        /**
         * Supplier phone number.
         * Type: TEXT
         */
        public final static String SUPPLIER_PHONE = "supplier_phone";
    }
}
