package digitalgarden.mecsek.tables.records;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.recordtypes.RecordTypesTable;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.generic.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORDS;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORD_TYPES;


public class RecordsListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long limit )
		{
        GenericListFragment listFragmenet = new RecordsListFragment();

        Bundle args = new Bundle();

        // args.putLong( SELECTED_ITEM , SELECT_DISABLED ); Nincs szelektálás!

        args.putLong( LIMITED_ITEM, limit );
        args.putString( LIMITED_COLUMN, columnFull_id(RECORD_TYPES));
        args.putString( ORDERED_COLUMN, columnFull( RecordsTable.NAME ));
        // args.putString( FILTERED_COLUMN, BooksTable.FULL_SEARCH);
        args.putStringArray( FILTERED_COLUMN, new String[] { columnFull(RecordTypesTable.SEARCH), columnFull(RecordsTable.SEARCH)});

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
        addField( R.id.record_type, RecordTypesTable.NAME);
        addField( R.id.patient, PatientsTable.NAME );
        addField( R.id.patient_dob, PatientsTable.DOB );
        addField( R.id.record, RecordsTable.NAME );
        addField( R.id.date, RecordsTable.DATE );
        addField( R.id.note, CalendarTable.NOTE);
        addIdField();
        }

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.put( column(RecordsTable.NAME), "2003.01.02");
		getActivity().getContentResolver().insert( table(RECORDS).contentUri(), values);

		values.put( column(RecordsTable.NAME), "2017.12.20");
		getActivity().getContentResolver().insert( table(RECORDS).contentUri(), values);

		values.put( column(RecordsTable.NAME), "2018.05.01");
		getActivity().getContentResolver().insert( table(RECORDS).contentUri(), values);

		}
	}
