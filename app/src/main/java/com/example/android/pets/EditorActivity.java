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

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
    // ID for loader
    private static final int EDITOR_LOADER = 201;
    // Declare Uri variable for existing pet record
    private Uri currentPetUri;
    // Declare boolean variable for changes to pet form
    private boolean petHasChanged = false;
    // OnTouchListener that listens for any user touches on a View, i.e. modifying,
    // and updates boolean to true
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            petHasChanged = true;
            return false;
        }
    };

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
            // Invalidate the options menu, so 'Delete' can be hidden since pet hasn't been
            // created yet.
            invalidateOptionsMenu();
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

        mNameEditText.setOnTouchListener(touchListener);
        mBreedEditText.setOnTouchListener(touchListener);
        mWeightEditText.setOnTouchListener(touchListener);
        mGenderSpinner.setOnTouchListener(touchListener);

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
    private void savePet(){
        // Get text from EditText fields, convert it to a String, and remove all leading
        // and trailing whitespace.
        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        int petGender = mGender;
        String weightStr = mWeightEditText.getText().toString().trim();
        /*Integer petWeight = Integer.parseInt(weightStr);*/
        Integer petWeight = 0;
        if(!weightStr.isEmpty()){
            petWeight = Integer.parseInt(weightStr);
        }

        if(currentPetUri == null && petName.isEmpty() && petBreed.isEmpty()
                && petGender == GENDER_UNKNOWN && weightStr.isEmpty()){
            Toast.makeText(this,"No data entered. Save unsuccessful",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create a map of key-value pairs
        ContentValues values = new ContentValues();
        values.put(petsEntry.COLUMN_PET_NAME,petName);
        values.put(petsEntry.COLUMN_PET_BREED,petBreed);
        values.put(petsEntry.COLUMN_PET_GENDER,petGender);
        values.put(petsEntry.COLUMN_PET_WEIGHT,petWeight);

        if(currentPetUri == null) {
            // Insert new row of values. Return new URI.
            Uri newUri = getContentResolver().insert(petsEntry.CONTENT_URI, values);
            // Parse row ID returned in URI. On insert error, -1 will be returned.
            long newID = ContentUris.parseId(newUri);

            // Show toast on insert result
            if (newUri == null || newID == -1) {
                Toast.makeText(this, getString(R.string.error_saving_pet),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.pet_saved),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(currentPetUri,values,
                    null,null);

            if(rowsUpdated == 0){
                Toast.makeText(this,getString(R.string.error_updating_pet),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,getString(R.string.pet_updated),
                        Toast.LENGTH_LONG).show();
            }
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
                savePet();
                // Exit to Activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity) if the pet hasn't changed
                if(!petHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are changes, set up a dialog to warn the user
                // Create a click listener to handle the user confirming that changes
                // should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked 'Discard'
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show dialog to notify user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet; hide the 'Delete' menu item
        if(currentPetUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Best practice: Specify columns to return. There are not may columns in this table
        // however it is best practice to include the columns to return from the query to
        // avoid performance issues
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
        // Exit early if the cursor is null or there is less than 1 row in
        // the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }
        // Move cursor to position 0 before extracting values
        if (data.moveToFirst()) {
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
            switch (data.getInt(genderColumnIndex)) {
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
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the 'Keep editing' button, so dismiss the dialog and
                // continue editing the pet.
                if(dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if(!petHasChanged){
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, set up a dialog to the user
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                        // User clicked the 'Discard' button, close the currnt activity
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}