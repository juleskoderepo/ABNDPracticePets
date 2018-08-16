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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetsContract.petsEntry;
import static com.example.android.pets.data.PetsContract.petsEntry.*;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = GENDER_UNKNOWN;

    private static final int EDITOR_LOADER = 201;

    private Uri currentPetUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Use getIntent() and getData() to get the associated URI
        Intent openEditor = getIntent();
        currentPetUri = openEditor.getData();

        // Set title of EditorActivity on which situation we have
        // If the EditorActivity was opened using the ListView item, then we will
        // have uri of pet so change app bar to say "Edit Pet"
        // Otherwise if this is a new pet, uri is null so change app bar to say "Add a Pet"
        if(currentPetUri == null){
            // New pet so update the app bar title to "Add a Pet"
            setTitle(getString(R.string.editor_title_add_a_pet));
        } else {
            // Existing pet so update the title to "Edit Pet"
            setTitle(getString(R.string.editor_title_edit_pet));

            getLoaderManager().initLoader(EDITOR_LOADER, null,this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = GENDER_FEMALE; // Female
                    } else {
                        mGender = GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = GENDER_UNKNOWN; // Unknown
            }
        });
    }

    /**
     * Helper method to insert pet data into the database.
     */
    private void insertPet(){
        // Get text from EditText fields, convert it to a String, and remove all leading
        // and trailing whitespace.
        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        int petGender = mGender;
        String weightStr = mWeightEditText.getText().toString().trim();
        /*Integer petWeight = Integer.parseInt(weightStr);*/
        Integer petWeight = null;
        if(!weightStr.isEmpty()){
            petWeight = Integer.parseInt(weightStr);
        }

        // Create a map of key-value pairs
        ContentValues values = new ContentValues();
        values.put(petsEntry.COLUMN_PET_NAME,petName);
        values.put(petsEntry.COLUMN_PET_BREED,petBreed);
        values.put(petsEntry.COLUMN_PET_GENDER,petGender);
        values.put(petsEntry.COLUMN_PET_WEIGHT,petWeight);

        // Insert new row of values. Return new URI.
        Uri newUri = getContentResolver().insert(petsEntry.CONTENT_URI, values);
        // Parse row ID returned in URI. On insert error, -1 will be returned.
        long newID = ContentUris.parseId(newUri);

        // Show toast on insert result
        if(newUri == null || newID == -1){
            Toast.makeText(this, getString(R.string.error_saving_pet), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,getString(R.string.pet_saved), Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Insert pet in table
                insertPet();
                // Exit to Activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        return new CursorLoader(this,
                currentPetUri,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Move cursor to position 0 before extracting values
        data.moveToFirst();

        // Get name column index and set value on name field
        int nameColumnIndex = data.getColumnIndex(petsEntry.COLUMN_PET_NAME);
        mNameEditText.setText(data.getString(nameColumnIndex));
        // Get breed column index and set value on breed field
        int breedColumnIndex = data.getColumnIndex(petsEntry.COLUMN_PET_BREED);
        mBreedEditText.setText(data.getString(breedColumnIndex));
        // Get weight column index and set value on weight field
        int weightColumnIndex = data.getColumnIndex(petsEntry.COLUMN_PET_WEIGHT);
        mWeightEditText.setText(String.valueOf(data.getInt(weightColumnIndex)));
        //Get gender column index and set value on gender spinner
        int genderColumnIndex = data.getColumnIndex(petsEntry.COLUMN_PET_GENDER);
        switch(data.getInt(genderColumnIndex)){
            case petsEntry.GENDER_UNKNOWN:
                mGenderSpinner.setSelection(0);
                break;
            case petsEntry.GENDER_MALE:
                mGenderSpinner.setSelection(1);
                break;
            case petsEntry.GENDER_FEMALE:
                mGenderSpinner.setSelection(2);
                break;
            default:
                mGenderSpinner.setSelection(0);
                break;
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}