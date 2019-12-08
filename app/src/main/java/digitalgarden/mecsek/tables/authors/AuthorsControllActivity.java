package digitalgarden.mecsek.tables.authors;


// res:
// http://stackoverflow.com/a/5796606


import android.content.Intent;

import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;
import digitalgarden.mecsek.tables.books.BooksControllActivity;

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

    // Author was selected to show his/her books
    // Author record (as header) could be edited there
    @Override
    public void onItemEditing(long id)
        {
        if ( id < 0L )
            {
            super.onItemEditing(id);
            return;
            }

        Intent intent = new Intent( this, BooksControllActivity.class);
        // intent.putExtra( GenericControllActivity.TITLE, listTitle + listOwner.getText() );
        intent.putExtra( GenericCombinedListFragment.LIMITED_ITEM, id );
        startActivity( intent );
        }


	}
