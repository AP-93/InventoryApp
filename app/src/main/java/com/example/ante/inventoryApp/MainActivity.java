package com.example.ante.inventoryApp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ante.inventoryApp.data.ItemContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;

    ItemCursorAdapter mItemCursorAdapter;
    TextView quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listViewItems = (ListView) findViewById(R.id.display_items_list);

        quantity = (TextView) findViewById(R.id.item_quantity);

        //floating button click listener
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

        // setup an adapter to create list item for each row of data in the Cursor
        // there is no data until loader finishes so pass null for the Cursor
        mItemCursorAdapter = new ItemCursorAdapter(this, null);
        listViewItems.setAdapter(mItemCursorAdapter);

        //start loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);

        //listens for clicks on list items
        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // create new intent to go to AddItemActivity
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);

                //form the content URI that represents specific item that was clicked on
                //by appending the "id" (passed as input to this method) onto the {@link ItemEntry#CONTENT_URI}
                // for example, the URI would be content://com.example.ante.inventoryApp/items_in_store/2
                //if the item with ID 2 wac clicked on
                Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);

                //set the URI on the data fields of the intent
                intent.setData(currentItemUri);

                //launch AddItemActivity
                startActivity(intent);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // A "projection" defines the columns that will be returned for each row
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.ITEM_NAME,
                ItemContract.ItemEntry.ITEM_PRICE,
                ItemContract.ItemEntry.ITEM_QUANTITY,
                ItemContract.ItemEntry.ITEM_IMAGE
        };

        // this loader will execute the contentProvider's query method on a background thread
        return new CursorLoader(this,                 //parent activity context
                ItemContract.ItemEntry.CONTENT_URI,   // The content URI of the items table
                projection,                           // The columns to return for each row
                null,                                 // Either null, or the word the user entered
                null,                                 // Either empty, or the string the user entered
                null);                                // The sort order for the returned rows
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Update ItemCursorAdapter with this new cursor containing updated item data
        mItemCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Callback called when the data needs to be deleted
        mItemCursorAdapter.swapCursor(null);
    }
}