package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.data.PetsContract.petsEntry;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String NAME_EXCEPTION = "Pet requires a name";
    private static final String GENDER_EXCEPTION = "Pet requires a valid gender";
    private static final String WEIGHT_EXCEPTION = "Pet requires a valid weight";

    private static final String QUERY_EXCEPTION = "Cannot query unknown URI ";
    private static final String INSERT_NOT_SUPPORTED_EXCEPTION = "Insertion is not supported for ";
    private static final String UPDATE_EXCEPTION = "Update is not supported for ";
    private static final String DELETE_EXCEPTION = "Deletion is not supported for ";

    // Set up URI matcher with URI patterns that ContentProvider will accept
    static {
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS + "/#", PET_ID);
    }

    /**
     * Tag for log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    PetDbHelper dbHelper;

    /**
     * Initialize the provider and the database helper object
     *
     * @return boolean
     */
    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        dbHelper = new PetDbHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Declare cursor to hold query result
        Cursor cursor;

        // Determine if URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = db.query(petsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = petsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = db.query(petsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(QUERY_EXCEPTION + uri);
        }

        // Set notification URI on the Cursor, so we know what content URI the Cursor
        // was created for. If the data at this URI changes, then update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case PETS:
                return petsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return petsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException(INSERT_NOT_SUPPORTED_EXCEPTION + uri);
        }
    }

    /**
     * Helper method to perform db insert
     *
     * @param uri           URI to the pets table
     * @param contentValues Values to be inserted into the database table
     * @return New URI with the ID assigned to the new row
     */
    private Uri insertPet(Uri uri, ContentValues contentValues) {
        // Name should not be null
        String name = contentValues.getAsString(petsEntry.COLUMN_PET_NAME);
        Integer gender = contentValues.getAsInteger(petsEntry.COLUMN_PET_GENDER);
        Integer weight = contentValues.getAsInteger(petsEntry.COLUMN_PET_WEIGHT);

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(NAME_EXCEPTION);
        }

        if (gender == null || !petsEntry.isValidGender(gender)) {
            throw new IllegalArgumentException(GENDER_EXCEPTION);
        }

        if (weight != null && weight < 0) {
            throw new IllegalArgumentException(WEIGHT_EXCEPTION);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long newRowId = db.insert(petsEntry.TABLE_NAME, null, contentValues);

        // Log error if newRowId is -1 meaning insert failed
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
        }

        if(newRowId != -1) {
            // Notify all listeners that the data has changed for the pet content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the new URI with the ID assigned to the new row
        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                // Perform delete on db that will return number of rows affected
                int rowsDeleted = db.delete(petsEntry.TABLE_NAME, selection, selectionArgs);

                if(rowsDeleted != 0) {
                    // Notify all listeners that the data has changed for the pet content URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                // Return number of rows deleted
                return rowsDeleted;
            case PET_ID:
                selection = petsEntry._ID + "=?";;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Perform delete on db for specific row that will return number of rows affected
                rowsDeleted = db.delete(petsEntry.TABLE_NAME, selection, selectionArgs);

                if(rowsDeleted != 0){
                    // Notify all listeners that the data has changed for the pet content URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                // Return number of rows deleted
                return rowsDeleted;
            default:
                throw new IllegalArgumentException(DELETE_EXCEPTION + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                selection = petsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(UPDATE_EXCEPTION + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there are no values to update, exit early and return 0
        if(values.size() == 0){
            return 0;
        }

        // Check if the ContentValues object contains each of the pet attributes
        boolean nameExists = values.containsKey(petsEntry.COLUMN_PET_NAME);
        boolean genderExists = values.containsKey(petsEntry.COLUMN_PET_GENDER);
        boolean weightExists = values.containsKey(petsEntry.COLUMN_PET_WEIGHT);

        // Validate the name value if it present in the ContentValues object
        if (nameExists) {
            String name = values.getAsString(petsEntry.COLUMN_PET_NAME);

            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException(NAME_EXCEPTION);
            }
        }

        // Validate the gender value if it present in the ContentValues object
        if (genderExists) {
            Integer gender = values.getAsInteger(petsEntry.COLUMN_PET_GENDER);

            if (gender == null || !petsEntry.isValidGender(gender)) {
                throw new IllegalArgumentException(GENDER_EXCEPTION);
            }
        }

        // Validate the weight value if it present in the ContentValues object
        if (weightExists) {
            Integer weight = values.getAsInteger(petsEntry.COLUMN_PET_WEIGHT);

            if (weight != null && weight < 0) {
                throw new IllegalArgumentException(WEIGHT_EXCEPTION);
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Perform update on db that will return number of rows affected
        int rowsUpdated = db.update(petsEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            // Notify all listeners that the data has changed for the pet content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return number of rows updated
        return rowsUpdated;

    }
}
