package com.example.android.pets.data;

import android.provider.BaseColumns;

public final class PetsContract {

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

    }
}
