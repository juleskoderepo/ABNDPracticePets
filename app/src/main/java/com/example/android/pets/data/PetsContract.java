package com.example.android.pets.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class PetsContract {

    // Empty constructor to prevent instantiating the contract class
    private PetsContract() {}

    // Content authority for URI
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    // Base to use for all Content URIs. Concatenate scheme and content authority
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Path to the type of data i.e. table name
    public static final String PATH_PETS = "pets";

    /* Inner class that defines the contents of the pets table */
    public static final class petsEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "pets";

        // Columns names
        // ID, primary key for the table
        public static final String COLUMN_ID = BaseColumns._ID;

        // Name of the pet
        public static final String COLUMN_PET_NAME = "name";

        // Breed of the pet
        public static final String COLUMN_PET_BREED = "breed";

        // Gender of the pet represented by an integer
        public static final String COLUMN_PET_GENDER = "gender";

        // Weight of the pet
        public static final String COLUMN_PET_WEIGHT = "weight";

        // Value constants used for gender
        // Constant for unknown gender
        public static final int GENDER_UNKNOWN = 0;

        // Constant for male
        public static final int GENDER_MALE = 1;

        // Constant for female
        public static final int GENDER_FEMALE = 2;

        // Constant for Content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PETS);

    }
}
