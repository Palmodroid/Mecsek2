package digitalgarden.mecsek.tables.recordtypes;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORD_TYPES;


public class RecordTypesListFragment extends GenericCombinedListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericCombinedListFragment newInstance(long select )
		{
		GenericCombinedListFragment listFragmenet = new RecordTypesListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { column(RecordTypesTable.SEARCH)});
		args.putString( ORDERED_COLUMN, column(RecordTypesTable.NAME));

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return RECORD_TYPES;
        }

	@Override
	protected int defineRowLayout()
		{
		return R.layout.record_types_list_row_view;
		}

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.record_type, RecordTypesTable.NAME);
        addIdField();
        }

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.put( column(RecordTypesTable.NAME), "Kontroll");
		getActivity().getContentResolver().insert( table(RECORD_TYPES).contentUri(), values);

		values.put( column(RecordTypesTable.NAME), "Műtét");
		getActivity().getContentResolver().insert( table(RECORD_TYPES).contentUri(), values);

		values.put( column(RecordTypesTable.NAME), "Altatóorvos");
		getActivity().getContentResolver().insert( table(RECORD_TYPES).contentUri(), values);

		values.put( column(RecordTypesTable.NAME), "Műtét előtti vizsgálatok");
		getActivity().getContentResolver().insert( table(RECORD_TYPES).contentUri(), values);

		values.put( column(RecordTypesTable.NAME), "Felvétel");
		getActivity().getContentResolver().insert( table(RECORD_TYPES).contentUri(), values);
		}
	}
