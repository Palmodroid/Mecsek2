package digitalgarden.mecsek.database.authors;


// res:
// http://stackoverflow.com/a/5796606


import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericListFragment;

public class AuthorsControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new AuthorsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericListFragment.SELECTED_ITEM, AuthorsListFragment.SELECT_DISABLED);
		return AuthorsListFragment.newInstance( initiallySelectedItem );
		}

	}
