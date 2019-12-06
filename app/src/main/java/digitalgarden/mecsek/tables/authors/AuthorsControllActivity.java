package digitalgarden.mecsek.tables.authors;


// res:
// http://stackoverflow.com/a/5796606


import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;

public class AuthorsControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new AuthorsEditFragment();
		}


	@Override
	protected GenericCombinedListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericCombinedListFragment.SELECTED_ITEM, AuthorsListFragment.SELECT_DISABLED);
		return AuthorsListFragment.newInstance( initiallySelectedItem );
		}

	}
