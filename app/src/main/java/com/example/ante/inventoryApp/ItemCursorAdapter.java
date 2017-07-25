package com.example.ante.inventoryApp;

/**
 * Created by Ante on 22/07/2017.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ante.inventoryApp.data.ItemContract;

import java.io.File;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /**
     * This method binds the store items data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Extract properties from cursor
        int id = cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ItemContract.ItemEntry.ITEM_NAME));
        double price = cursor.getInt(cursor.getColumnIndexOrThrow(ItemContract.ItemEntry.ITEM_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ItemContract.ItemEntry.ITEM_QUANTITY));
        String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(ItemContract.ItemEntry.ITEM_IMAGE));

        //set image to view using glide library
        Glide.with(context)
                .load(new File(imageUri)).placeholder(R.drawable.no_image_available)
                .into(viewHolder.productImage);

        // Populate fields with extracted properties
        viewHolder.itemName.setText(name);
        viewHolder.itemPrice.setText(String.valueOf(price) + context.getString(R.string.dollar));
        viewHolder.itemQuantity.setText(context.getString(R.string.in_stock) + String.valueOf(quantity));

        //get uri for current row in list view
        final Uri uri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);

        //set listener to sale buttons in each list row
        viewHolder.sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantity > 0) {
                    int qq = quantity;

                    //if sale button is clicked reduce quantity by one and update database
                    values.put(ItemContract.ItemEntry.ITEM_QUANTITY, --qq);
                    resolver.update(uri, values, null, null);
                    context.getContentResolver().notifyChange(uri, null);
                } else {
                    Toast.makeText(context, R.string.item_out_of_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //create view holder  with all views
    public static class ViewHolder {
        public final ImageView productImage;
        public final TextView itemQuantity;
        public final TextView itemPrice;
        public final TextView itemName;
        public final Button sale;

        public ViewHolder(View view) {
            itemName = (TextView) view.findViewById(R.id.item_name);
            itemPrice = (TextView) view.findViewById(R.id.item_price);
            itemQuantity = (TextView) view.findViewById(R.id.item_quantity);
            productImage = (ImageView) view.findViewById(R.id.imageInList);
            sale = (Button) view.findViewById(R.id.saleBtn);
        }
    }
}