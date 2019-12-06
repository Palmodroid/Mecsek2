package digitalgarden.mecsek.tables.books;

import android.content.Intent;

import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;


// res:
// http://stackoverflow.com/a/5796606


public class BooksControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new BooksEditFragment();
		}


	@Override
	protected GenericCombinedListFragment createListFragment()
		{
		long authorIdLimit = getIntent().getLongExtra(GenericCombinedListFragment.LIMITED_ITEM, -1L);
		return BooksListFragment.newInstance( authorIdLimit );
		}
	
	// Ez ahhoz kell, hogy a Fragment megkapja a hívást
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
		super.onActivityResult(requestCode, resultCode, data);
		}

	}
