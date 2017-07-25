package com.example.ante.inventoryApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ante.inventoryApp.data.ItemContract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class AddItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final int EXISTING_ITEM_LOADER = 0;
    private static final int GET_FROM_GALLERY = 3;

    EditText nameEditText, priceEditText, quantityEditText, supplierNameEditText, supplierMailEditText, supplierPhoneEditText;
    ImageView addProductImg;
    double price;
    int quantity;
    String name, supplierName, supplierMail, supplierPhone;
    Button addImageBtn, reduceStock, addStock;
    Bitmap bitmap = null;

    /**
     * Content URI for the existing pet (null if it's a new pet)
     */
    private Uri mCurrentItemUri;

    //Validation Variables
    Boolean productHasChanged = false;
    Boolean bitmapCheck;//check if we have loaded image in image view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        bitmapCheck = false;

        // get data from intent used to launch this activity,
        //in order to figure out if we are creating new Item or editing existing one
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        //buttons for add or reduce quantity of existing items
        reduceStock = (Button) findViewById(R.id.reduceQuantity);
        addStock = (Button) findViewById(R.id.addQuantity);

        //change app bar text and hide or show buttons in case of adding new item or editing existing one
        if (mCurrentItemUri == null) {

            addStock.setVisibility(View.GONE);
            reduceStock.setVisibility(View.GONE);
            setTitle("Add a Item");
        } else {

            addStock.setVisibility(View.VISIBLE);
            reduceStock.setVisibility(View.VISIBLE);
            setTitle("Edit Item");
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        //edit text field in which we enter data
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        priceEditText = (EditText) findViewById(R.id.priceEditText);
        quantityEditText = (EditText) findViewById(R.id.quantityEditText);
        supplierNameEditText = (EditText) findViewById(R.id.supplierNameEditText);
        supplierMailEditText = (EditText) findViewById(R.id.supplierMailEditText);
        supplierPhoneEditText = (EditText) findViewById(R.id.supplierPhoneEditText);
        addProductImg = (ImageView) findViewById(R.id.addImageView);

        //when clicked opens gallery for image insertion
        addImageBtn = (Button) findViewById(R.id.addItemButton);

        //add listener to edit text fields so we can know if user has changed anything
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);
        supplierNameEditText.setOnTouchListener(mTouchListener);
        supplierMailEditText.setOnTouchListener(mTouchListener);
        supplierPhoneEditText.setOnTouchListener(mTouchListener);

        //set listeners on buttons
        reduceStock.setOnClickListener(this);
        addStock.setOnClickListener(this);
        addImageBtn.setOnClickListener(this);
    }

    /**
     * Helper method to insert hardcoded item data into the database. For debugging purposes only.
     */
    private void insertItem() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        name = nameEditText.getText().toString().trim();
        supplierName = supplierNameEditText.getText().toString().trim();
        supplierMail = supplierMailEditText.getText().toString().trim();
        supplierPhone = supplierPhoneEditText.getText().toString().trim();

        try {
            price = Double.parseDouble(priceEditText.getText().toString().trim());
            quantity = Integer.parseInt(quantityEditText.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }

        //check if some field is left empty
        if (isEmpty(nameEditText) || isEmpty(priceEditText) || isEmpty(quantityEditText) || isEmpty(supplierNameEditText) || isEmpty(supplierMailEditText) || isEmpty(supplierPhoneEditText) || bitmapCheck == false) {
            Toast.makeText(this, "item insert failed, enter required data",
                    Toast.LENGTH_SHORT).show();
        } else {

            // Create a ContentValues object where column names are the keys,
            // and name,price,quantity, supplier...  attributes are the values.
            ContentValues values = new ContentValues();

            values.put(ItemContract.ItemEntry.ITEM_NAME, name);
            values.put(ItemContract.ItemEntry.ITEM_PRICE, price);
            values.put(ItemContract.ItemEntry.ITEM_QUANTITY, quantity);
            values.put(ItemContract.ItemEntry.SUPPLIER_NAME, supplierName);
            values.put(ItemContract.ItemEntry.SUPPLIER_MAIL, supplierMail);
            values.put(ItemContract.ItemEntry.SUPPLIER_PHONE, supplierPhone);

            //check if we are creating new item or updating existing
            //this is the case of new item creation
            if (mCurrentItemUri == null) {

                //call method that saves chosen image and returns its path as string
                String stringUri = saveImage(this, bitmap, name);

                //add the image path to values
                values.put(ItemContract.ItemEntry.ITEM_IMAGE, stringUri);

                // Insert a new row for item into the provider using the ContentResolver.
                // Use the {@link ItemEntry#CONTENT_URI} to indicate that we want to insert
                // into the "items_in_store" database table.
                // Receive the new content URI that will allow us to access added item data in the future.
                Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, R.string.item_insert_failed,
                            Toast.LENGTH_SHORT).show();
                } else {
                    //set edit text fields to empty if data insert was successful
                    nameEditText.setText("");
                    priceEditText.setText("");
                    quantityEditText.setText("");
                    supplierNameEditText.setText("");
                    supplierMailEditText.setText("");
                    supplierPhoneEditText.setText("");

                    //finish this activity
                    finish();
                }
            } else {
                // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentItemUri will already identify the correct row in the database that
                // we want to modify.

                //check if image has changed
                if (bitmap != null) {
                    String stringUri = saveImage(this, bitmap, name);

                    values.put(ItemContract.ItemEntry.ITEM_IMAGE, stringUri);
                }
                //call update method from ItemProvider and pass current item uri and values
                int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, R.string.item_update_failed,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, R.string.item_update_successfull,
                            Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        }
    }

    //check idf editText field is empty
    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    //in case of editing existing item loader is used to load data
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // A "projection" defines the columns that will be returned for each row
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.ITEM_NAME,
                ItemContract.ItemEntry.ITEM_PRICE,
                ItemContract.ItemEntry.ITEM_QUANTITY,
                ItemContract.ItemEntry.ITEM_IMAGE,
                ItemContract.ItemEntry.SUPPLIER_NAME,
                ItemContract.ItemEntry.SUPPLIER_MAIL,
                ItemContract.ItemEntry.SUPPLIER_PHONE
        };

        // this loader will execute the contentProvider's query method on a background thread
        return new CursorLoader(this,                 //parent activity context
                mCurrentItemUri,                      // Query the content URI for the current item
                projection,                           // The columns to return for each row
                null,                                 // Either null, or the word the user entered
                null,                                 // Either empty, or the string the user entered
                null);                                // The sort order for the returned rows
    }

    //insert loaded data into editText fields and image to imageView
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Extract properties from cursor
            String name = data.getString(data.getColumnIndexOrThrow(ItemContract.ItemEntry.ITEM_NAME));
            double price = data.getInt(data.getColumnIndexOrThrow(ItemContract.ItemEntry.ITEM_PRICE));
            int quantityA = data.getInt(data.getColumnIndexOrThrow(ItemContract.ItemEntry.ITEM_QUANTITY));
            String suppName = data.getString(data.getColumnIndexOrThrow(ItemContract.ItemEntry.SUPPLIER_NAME));
            String suppMail = data.getString(data.getColumnIndexOrThrow(ItemContract.ItemEntry.SUPPLIER_MAIL));
            String suppPhone = data.getString(data.getColumnIndexOrThrow(ItemContract.ItemEntry.SUPPLIER_PHONE));
            String imgUri = data.getString(data.getColumnIndexOrThrow(ItemContract.ItemEntry.ITEM_IMAGE));

            quantity = quantityA;

            // Populate fields with extracted properties
            nameEditText.setText(name);
            priceEditText.setText(String.valueOf(price));
            quantityEditText.setText(String.valueOf(quantityA));
            supplierNameEditText.setText(suppName);
            supplierMailEditText.setText(suppMail);
            supplierPhoneEditText.setText(suppPhone);

            //glide library used for loading images from String path
            Glide.with(getApplicationContext())
                    .load(new File(imgUri)).placeholder(R.drawable.no_image_available)
                    .into(addProductImg);

            bitmapCheck = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    //method called when user wants to load image to app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();

            //put selected image into Bitmap bitmap variable which will be saved on device
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

                addProductImg.setImageBitmap(bitmap);

                bitmapCheck = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //method called to save selected bitmap image into device
    public String saveImage(Context context, Bitmap b, String imageName) {
        FileOutputStream foStream;
        try {
            foStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG, 40, foStream);
            foStream.close();

            File file = getApplicationContext().getFileStreamPath(imageName);

            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.d("saveImage", "Exception 2, Something went wrong!");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.second_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save_menuBtn:
                // Save or update to database
                insertItem();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete_menuBtn:
                //show dialog so user needs to confirm item deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Order" menu option
            case R.id.order_menuBtn:
                orderSupplier();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Respond to a click on the "Add Item" button and open phone gallery so user can pick image
            case R.id.addItemButton:
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
                break;
            // Respond to a click on the "+" button for quantity in case of editing existing item
            case R.id.addQuantity:
                quantity++;
                quantityEditText.setText(String.valueOf(quantity));
                productHasChanged = true;
                break;
            // Respond to a click on the "-" button for quantity in case of editing existing item
            case R.id.reduceQuantity:
                quantity--;
                if (quantity < 0)
                    quantity = 0;
                quantityEditText.setText(String.valueOf(quantity));
                productHasChanged = true;
                break;
        }
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.item_delete_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, R.string.item_delete_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_this_item);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Yes" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "No" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Order from supplier
    private void orderSupplier() {

        String[] addresses = {supplierMailEditText.getText().toString()};
        String subject = "Order " + quantityEditText.getText().toString() + " " + nameEditText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the productHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        //Go back if we have no changes
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }
        //otherwise Protect user from loosing info
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Call this method if user tries to leave activity without saving or deleting item
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
