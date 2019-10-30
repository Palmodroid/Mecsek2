package digitalgarden.mecsek.database.patients;


// res:
// http://stackoverflow.com/a/5796606

import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericListFragment;

public class PatientsControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new PatientsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericListFragment.SELECTED_ITEM, PatientsListFragment.SELECT_DISABLED);
		return PatientsListFragment.newInstance( initiallySelectedItem );
		}

	}
