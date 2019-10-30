package digitalgarden.mecsek.database.patients;

import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;


public class PatientsListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long select )
		{
		GenericListFragment listFragmenet = new PatientsListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { column(PatientsTable.SEARCH) });
		args.putString( ORDERED_COLUMN, column(PatientsTable.NAME) );

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return PATIENTS;
        }
	
	@Override
	protected int defineRowLayout()
		{
		return R.layout.patient_list_row_view;
		}

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.name, PatientsTable.NAME );
        addField( R.id.dob, PatientsTable.DOB );
        addField( R.id.taj, PatientsTable.TAJ );
        addField( R.id.phone, PatientsTable.PHONE );
        addField( R.id.note, PatientsTable.NOTE );
        addIdField();
        }

	@Override
	protected void addExamples()
		{
		/*
		ContentValues values = new ContentValues();

		values.put( AuthorsTable.NAME, "Láng Attila D.");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);

		values.put( AuthorsTable.NAME, "Gárdonyi Géza");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);

		values.put( AuthorsTable.NAME, "Molnár Ferenc");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);

		values.put( AuthorsTable.NAME, "Szabó Magda");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);

		values.put( AuthorsTable.NAME, "Fekete István");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);
		*/
		}
	}
