package digitalgarden.mecsek.tables.records;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.category.CategoriesTable;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORDS;


public class RecordsListFragment extends GenericCombinedListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericCombinedListFragment newInstance(long limit )
		{
        GenericCombinedListFragment listFragmenet = new RecordsListFragment();

        Bundle args = new Bundle();

        // args.putLong( SELECTED_ITEM , SELECT_DISABLED ); Nincs szelektálás!

        args.putLong( LIMITED_ITEM, limit );
        args.putInt( LIMITED_COLUMN, RecordsTable.PATIENT_ID );
        args.putString( ORDERED_COLUMN, columnFull( RecordsTable.DETAILS));
        // args.putString( FILTERED_COLUMN, BooksTable.FULL_SEARCH);
        args.putStringArray( FILTERED_COLUMN, new String[] { columnFull(CategoriesTable.SEARCH), columnFull(RecordsTable.SEARCH)});

        listFragmenet.setArguments(args);

        return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return RECORDS;
        }

    @Override
    protected int defineRowLayout()
        {
        return R.layout.records_list_row_view;
        }

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.category, CategoriesTable.NAME );
        addField( R.id.patient, PatientsTable.NAME );
        addField( R.id.patient_dob, PatientsTable.DOB );
        addField( R.id.record, RecordsTable.DETAILS );
        addField( R.id.date, CalendarTable.DATE );
        addField( R.id.note, CalendarTable.NOTE );
        addIdField();

        addStyleField( CategoriesTable.STYLE );
        }

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.put( column(RecordsTable.DETAILS), "2003.01.02");
		getActivity().getContentResolver().insert( table(RECORDS).contentUri(), values);

		values.put( column(RecordsTable.DETAILS), "2017.12.20");
		getActivity().getContentResolver().insert( table(RECORDS).contentUri(), values);

		values.put( column(RecordsTable.DETAILS), "2018.05.01");
		getActivity().getContentResolver().insert( table(RECORDS).contentUri(), values);

		}
	}
