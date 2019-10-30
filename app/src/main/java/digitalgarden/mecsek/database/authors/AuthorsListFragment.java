package digitalgarden.mecsek.database.authors;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;


public class AuthorsListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long select )
		{
		GenericListFragment listFragmenet = new AuthorsListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { column(AuthorsTable.SEARCH) });
		args.putString( ORDERED_COLUMN, column(AuthorsTable.NAME) );

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return AUTHORS;
        }

	@Override
	protected int defineRowLayout()
		{
		return R.layout.author_list_row_view;
		}

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.author, AuthorsTable.NAME );
        addIdField();
        }

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.put( column(AuthorsTable.NAME), "Láng Attila D.");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( column(AuthorsTable.NAME), "Gárdonyi Géza");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( column(AuthorsTable.NAME), "Molnár Ferenc");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( column(AuthorsTable.NAME), "Szabó Magda");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( column(AuthorsTable.NAME), "Fekete István");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);
		}
	}
