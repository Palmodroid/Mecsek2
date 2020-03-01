package digitalgarden.mecsek.tables.calendar;

import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;
import digitalgarden.mecsek.tables.category.CategoriesTable;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.tables.LibraryDatabase.CALENDAR;


public class CalendarListFragment extends GenericCombinedListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericCombinedListFragment newInstance(long select )
		{
		GenericCombinedListFragment listFragmenet = new CalendarListFragment();
	
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

		addStyleField( CategoriesTable.STYLE );
		}

	@Override
	protected void addExamples()
		{
		}
	}
