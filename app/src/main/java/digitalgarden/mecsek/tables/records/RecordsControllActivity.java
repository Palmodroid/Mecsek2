package digitalgarden.mecsek.tables.records;


// res:
// http://stackoverflow.com/a/5796606

import android.content.Intent;

import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericListFragment;


public class RecordsControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new RecordsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long titlesIdLimit = getIntent().getLongExtra(GenericListFragment.LIMITED_ITEM, -1L);
		return RecordsListFragment.newInstance( titlesIdLimit );
		}

    // Ez ahhoz kell, hogy a Fragment megkapja a hívást
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
        super.onActivityResult(requestCode, resultCode, data);
        }
    }
