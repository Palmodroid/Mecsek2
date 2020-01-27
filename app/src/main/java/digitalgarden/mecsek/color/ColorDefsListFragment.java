package digitalgarden.mecsek.color;


import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.tables.LibraryDatabase.COLOR_DEFS;

/**
 * 1. Create new ListFragment extending GenericCombinedListFragment and implement missing methods
 * 2. defineTableIndex() should return table index generated by LibraryDatabase.java
 * 3. color_def_list_row_view.xml should be generated (see it there) ...
 * 3a. and returned by defineRowLayout()
 *      return R.layout.color_def_list_row_view;
 * 4. setupRowLayout() should contain addField() methods to join fields to database columns
 *      addField( R.id.color, ColorDefsTable.COLOR );
 *      addIdField();
 * 5. (( Examples can be added ))
 * 6. Create static newInstance method!
 *      public static GenericCombinedListFragment newInstance( )
 *         {
 *         GenericCombinedListFragment listFragment = new ColorDefsListFragment();
 *         Bundle args = new Bundle();
 *         listFragment.setArguments(args);
 *         return listFragment;
 *         }
 * 6a. (( Setting arguments is obligatory! ))
 *
 * ControllActivity.createListFragment() should return ListFragment.newInstance()
 *
 */
public class ColorDefsListFragment extends GenericCombinedListFragment
    {
    public static GenericCombinedListFragment newInstance( )
        {
        GenericCombinedListFragment listFragment = new ColorDefsListFragment();

        Bundle args = new Bundle();

        listFragment.setArguments(args);

        return listFragment;
        }


    @Override
    protected int defineTableIndex()
        {
        return COLOR_DEFS;
        }

    @Override
    protected int defineRowLayout()
        {
        return R.layout.color_def_list_row_view;
        }

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.color, ColorDefsTable.COLOR );
        addField( R.id.value, ColorDefsTable.VALUE );

        addIdField();
        }

    @Override
    protected void addExamples()
        {
        ContentValues values = new ContentValues();

        values.put( column( ColorDefsTable.COLOR ), "Fehér");
        getActivity().getContentResolver().insert( table(COLOR_DEFS).contentUri(), values);

        values.put( column( ColorDefsTable.COLOR ), "Barna");
        getActivity().getContentResolver().insert( table(COLOR_DEFS).contentUri(), values);

        values.put( column( ColorDefsTable.COLOR ), "Kék");
        getActivity().getContentResolver().insert( table(COLOR_DEFS).contentUri(), values);
        }
    }