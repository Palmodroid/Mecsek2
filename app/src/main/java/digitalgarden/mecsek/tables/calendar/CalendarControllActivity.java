package digitalgarden.mecsek.tables.calendar;


// res:
// http://stackoverflow.com/a/5796606


import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;

public class CalendarControllActivity extends GenericControllActivity
	{
	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new CalendarEditFragment();
		}


	@Override
	protected GenericCombinedListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericCombinedListFragment.SELECTED_ITEM, CalendarListFragment.SELECT_DISABLED);
		return CalendarListFragment.newInstance( initiallySelectedItem );
		}

	}
