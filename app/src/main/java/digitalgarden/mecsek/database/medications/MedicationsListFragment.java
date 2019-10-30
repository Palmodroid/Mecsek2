package digitalgarden.mecsek.database.medications;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.calendar.CalendarTable;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.generic.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class MedicationsListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long limit )
		{
        GenericListFragment listFragmenet = new MedicationsListFragment();

        Bundle args = new Bundle();

        // args.putLong( SELECTED_ITEM , SELECT_DISABLED ); Nincs szelektálás!

        args.putLong( LIMITED_ITEM, limit );
        args.putString( LIMITED_COLUMN, columnFull_id( PILLS ));
        args.putString( ORDERED_COLUMN, columnFull( MedicationsTable.NAME ));
        // args.putString( FILTERED_COLUMN, BooksTable.FULL_SEARCH);
        args.putStringArray( FILTERED_COLUMN, new String[] { columnFull(PillsTable.SEARCH), columnFull(MedicationsTable.SEARCH)});

        listFragmenet.setArguments(args);

        return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return MEDICATIONS;
        }

    @Override
    protected int defineRowLayout()
        {
        return R.layout.medication_list_row_view;
        }

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.pill, PillsTable.NAME );
        addField( R.id.patient, PatientsTable.NAME );
        addField( R.id.patient_dob, PatientsTable.DOB );
        addField( R.id.medication, MedicationsTable.NAME );
        addField( R.id.date, MedicationsTable.DATE );
        addField( R.id.note, CalendarTable.NOTE);
        addIdField();
        }

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.put( column(MedicationsTable.NAME), "2003.01.02");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		values.put( column(MedicationsTable.NAME), "2017.12.20");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		values.put( column(MedicationsTable.NAME), "2018.05.01");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		}
	}
