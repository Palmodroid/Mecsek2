package digitalgarden.mecsek.tables.category;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.tables.LibraryDatabase.CATEGORIES;


public class CategoriesListFragment extends GenericCombinedListFragment
    {
    // static factory method
    // http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
    public static GenericCombinedListFragment newInstance(long select )
        {
        GenericCombinedListFragment listFragmenet = new CategoriesListFragment();

        Bundle args = new Bundle();

        args.putLong( SELECTED_ITEM, select );
        // args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

        args.putStringArray( FILTERED_COLUMN, new String[] { column(CategoriesTable.SEARCH)});
        args.putString( ORDERED_COLUMN, column(CategoriesTable.NAME));

        listFragmenet.setArguments(args);

        return listFragmenet;
        }

    @Override
    protected int defineTableIndex()
        {
        return CATEGORIES;
        }

    @Override
    protected int defineRowLayout()
        {
        return R.layout.category_list_row_view;
        }

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.category, CategoriesTable.NAME);
        addField( R.id.style, CategoriesTable.STYLE);
        addIdField();

        addStyleField( CategoriesTable.STYLE );
        }

    @Override
    protected void addExamples()
        {
        ContentValues values = new ContentValues();

        values.put( column(CategoriesTable.NAME), "Kontroll");
        getActivity().getContentResolver().insert( table(CATEGORIES).contentUri(), values);

        values.put( column(CategoriesTable.NAME), "Műtét");
        getActivity().getContentResolver().insert( table(CATEGORIES).contentUri(), values);

        values.put( column(CategoriesTable.NAME), "Altatóorvos");
        getActivity().getContentResolver().insert( table(CATEGORIES).contentUri(), values);

        values.put( column(CategoriesTable.NAME), "Műtét előtti vizsgálatok");
        getActivity().getContentResolver().insert( table(CATEGORIES).contentUri(), values);

        values.put( column(CategoriesTable.NAME), "Felvétel");
        getActivity().getContentResolver().insert( table(CATEGORIES).contentUri(), values);
        }
    }
