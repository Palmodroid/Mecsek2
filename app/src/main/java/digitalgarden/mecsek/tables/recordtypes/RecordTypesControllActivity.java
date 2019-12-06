package digitalgarden.mecsek.tables.recordtypes;


// res:
// http://stackoverflow.com/a/5796606

import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;


public class RecordTypesControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new RecordTypesEditFragment();
		}


	@Override
	protected GenericCombinedListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericCombinedListFragment.SELECTED_ITEM,
				RecordTypesListFragment.SELECT_DISABLED);
		return RecordTypesListFragment.newInstance( initiallySelectedItem );
		}

	}
