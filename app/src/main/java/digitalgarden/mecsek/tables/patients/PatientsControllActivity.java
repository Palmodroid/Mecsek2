package digitalgarden.mecsek.tables.patients;


// res:
// http://stackoverflow.com/a/5796606

import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;

public class PatientsControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new PatientsEditFragment();
		}


	@Override
	protected GenericCombinedListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericCombinedListFragment.SELECTED_ITEM, PatientsListFragment.SELECT_DISABLED);
		return PatientsListFragment.newInstance( initiallySelectedItem );
		}

	}
