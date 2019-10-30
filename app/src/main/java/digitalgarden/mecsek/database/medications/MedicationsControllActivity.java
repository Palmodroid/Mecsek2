package digitalgarden.mecsek.database.medications;


// res:
// http://stackoverflow.com/a/5796606

import android.content.Intent;

import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericListFragment;


public class MedicationsControllActivity extends GenericControllActivity
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new MedicationsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long pillIdLimit = getIntent().getLongExtra(GenericListFragment.LIMITED_ITEM, -1L);
		return MedicationsListFragment.newInstance( pillIdLimit );
		}

    // Ez ahhoz kell, hogy a Fragment megkapja a hívást
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
        super.onActivityResult(requestCode, resultCode, data);
        }
    }
