/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetsContract.petsEntry;

import java.net.URI;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    PetCursorAdapter cursorAdapter;

    private static final int PETAPP_LOADER_ID = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find ListView to populate
        ListView petListView = findViewById(R.id.list);

        // Find and set the empty view on the list view when there are no items in the list
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Set up cursor adapter using cursor
        cursorAdapter = new PetCursorAdapter(this, null, 0);
        // Attach cursor adapter to ListView
        petListView.setAdapter(cursorAdapter);

        // Set up item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create a new intent to launch the EditorActivity
                Intent openEditor = new Intent(CatalogActivity.this,
                        EditorActivity.class);

                // Form the content URI to the specific pet.
                // Append the "id" onto the {@link petsEntry#CONTENT_URI}
                Uri currentPetUri = ContentUris.withAppendedId(petsEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                openEditor.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display data for the current pet
                startActivity(openEditor);

            }
        });

        // Initialize the CursorLoader
        getLoaderManager().initLoader(PETAPP_LOADER_ID, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                Toast.makeText(this, getString(R.string.pet_saved), Toast.LENGTH_LONG).show();

                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Respond to a click on the "Delete All Pets" menu option
                int rowsDeleted = getContentResolver().delete(petsEntry.CONTENT_URI,
                        String.valueOf(1), null);
                Toast.makeText(this, getString(R.string.rows_deleted) + rowsDeleted,
                        Toast.LENGTH_LONG).show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPet() {
        ContentValues values = new ContentValues();
        values.put(petsEntry.COLUMN_PET_NAME, "Toto");
        values.put(petsEntry.COLUMN_PET_BREED, "Terrier");
        values.put(petsEntry.COLUMN_PET_GENDER, petsEntry.GENDER_MALE);
        values.put(petsEntry.COLUMN_PET_WEIGHT, 7);

        Log.i("CatalogActivity", "Content Values: " + values);

        // Call the ContentResolver to insert values into the database table
        Uri uri = getContentResolver().insert(petsEntry.CONTENT_URI, values);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i /*Loader ID*/, Bundle bundle) {
        // Declare and assign values for parameters needed for CursorLoader
        String[] projection = {
                petsEntry._ID,
                petsEntry.COLUMN_PET_NAME,
                petsEntry.COLUMN_PET_BREED};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        // Return a CursorLoader that executes the ContentProvider's query method on a
        // background thread
        return new CursorLoader(this,
                petsEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Moves the query results into the adapter, causing the ListView fronting the
        // adapter to re-display
        cursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear out adapter's reference to the Cursor to prevent memory leaks
        cursorAdapter.swapCursor(null);
    }
}
