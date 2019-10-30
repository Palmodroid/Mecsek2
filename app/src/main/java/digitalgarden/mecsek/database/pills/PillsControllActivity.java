package digitalgarden.mecsek.database.pills;


// res:
// http://stackoverflow.com/a/5796606

import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericListFragment;


public class PillsControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new PillsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericListFragment.SELECTED_ITEM, PillsListFragment.SELECT_DISABLED);
		return PillsListFragment.newInstance( initiallySelectedItem );
		}

	}
