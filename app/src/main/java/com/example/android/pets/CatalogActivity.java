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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetsContract.petsEntry;
import com.example.android.pets.data.PetDbHelper;



/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    PetDbHelper mDbHelper;

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

        // Database helper that provides access to the database
        mDbHelper = new PetDbHelper(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();


    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        PetDbHelper mDbHelper = new PetDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection of columns, if needed

        // Filter results with selection and selectionArgs, if needed

        // Define the db query to get a Cursor that contains all the rows from the pets
        // table to mimic the previous rawQuery
        Cursor cursor = db.query(petsEntry.TABLE_NAME,null,null,null,null,null,null);

        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            displayView.setText("The pets table contains " + cursor.getCount() + " pets. \n\n");

            displayView.append(petsEntry.COLUMN_ID + " - " +
                    petsEntry.COLUMN_PET_NAME + " - " +
                    petsEntry.COLUMN_PET_BREED + " - " +
                    petsEntry.COLUMN_PET_GENDER + " - " +
                    petsEntry.COLUMN_PET_WEIGHT + "\n\n");

            // Get index positions for each column
            int idColumnIndex = cursor.getColumnIndex(petsEntry.COLUMN_ID);
            int nameColumnIndex = cursor.getColumnIndex(petsEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(petsEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(petsEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(petsEntry.COLUMN_PET_WEIGHT);

            // Loop through all rows in the cursor, return column values, and display values
            // on screen.
            while (cursor.moveToNext()){
                // Extract values from each row using the defined column indices
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(breedColumnIndex);
                int currentGender = cursor.getInt(genderColumnIndex);
                int currentWeight = cursor.getInt(weightColumnIndex);

                // Translate the gender value to the string readable equivalent
                String currentGenderStr;

                switch(currentGender){
                    case 0:
                        currentGenderStr = "Unknown";
                        break;
                    case 1:
                        currentGenderStr = "Male";
                        break;
                    case 2:
                        currentGenderStr = "Female";
                        break;
                    default:
                        currentGenderStr = "Unknown";
                        break;
                }

                // Append values to the display view
                displayView.append(currentID + " - " +
                        currentName + " - " +
                        currentBreed + " - " +
                        currentGenderStr + " - " +
                        currentWeight + "kg" + "\n");
            }

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPet(){
        // Get the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(petsEntry.COLUMN_PET_NAME,"Toto");
        values.put(petsEntry.COLUMN_PET_BREED,"Terrier");
        values.put(petsEntry.COLUMN_PET_GENDER,petsEntry.GENDER_MALE);
        values.put(petsEntry.COLUMN_PET_WEIGHT,7);

        Log.i("CatalogActivity","Content Values: " + values);

        Log.i("CatalogActivity","DB name: " + db.toString());

        // insert method will either return the id of the row inserted or -1 if
        // there is an error
        long newRowId = db.insert(petsEntry.TABLE_NAME, null, values);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                displayDatabaseInfo();

                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
