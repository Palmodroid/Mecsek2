package digitalgarden.mecsek.database.calendar;

import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.library.LibraryDatabase.CALENDAR;


public class CalendarListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long select )
		{
		GenericListFragment listFragmenet = new CalendarListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		// args.putStringArray( FILTERED_COLUMN, new String[] { column(AuthorsTable.SEARCH) });
		args.putString( ORDERED_COLUMN, column( CalendarTable.DATE ) );

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return CALENDAR;
        }

	@Override
	protected int defineRowLayout()
		{
		return R.layout.calendar_list_row_view;
		}

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.date, CalendarTable.DATE );
        addField( R.id.note, CalendarTable.NOTE );
        addIdField();
        }

	@Override
	protected void addExamples()
		{
		}
	}
