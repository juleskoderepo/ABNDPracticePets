package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetsContract;

public class PetCursorAdapter extends CursorAdapter {

    /**
     * Constructs  a new {@link PetCursorAdapter}
     *
     * @param context The context
     * @param cursor The cursor from which to get the data
     * @param flags Flag to determine behavior of the adapter. No behavior specified = 0
     */
    public PetCursorAdapter(Context context,
                            Cursor cursor,
                            int flags /*FLAG_AUTO_REQUERY, FLAG_REGISTER_CONTENT_OBSERVER*/){
        super(context,cursor, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet
     * @param context app context
     * @param cursor The cursor from which to get the data
     * @param viewGroup The parent to which the new view is attached
     * @return The newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,viewGroup,false);
    }

    // Bind data to a given view such as setting the text on a TextView

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name
     * TextView in the list item layout.
     *
     * @param view Existing view, returned by newView() method
     * @param context app context
     * @param cursor The cursor from which to get the data. The cursor is already moved to the
     *               correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameTV = (TextView) view.findViewById(R.id.name);
        TextView summaryTV = (TextView) view.findViewById(R.id.summary);

        // Find the columns of pet attributes that we want
        int nameColumnIndex = cursor.getColumnIndex(PetsContract.petsEntry.COLUMN_PET_NAME);
        int nameBreedIndex = cursor.getColumnIndex(PetsContract.petsEntry.COLUMN_PET_BREED);

        // Extract properties from cursor
        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(nameBreedIndex);

        // Populate fields with extracted properties
        nameTV.setText(petName);
        summaryTV.setText(petBreed);

    }
}
