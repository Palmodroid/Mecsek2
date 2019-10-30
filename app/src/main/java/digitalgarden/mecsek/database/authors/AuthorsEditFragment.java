package digitalgarden.mecsek.database.authors;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.books.BooksControllActivity;
import digitalgarden.mecsek.formtypes.EditField;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;


public class AuthorsEditFragment extends GenericEditFragment
	{
    @Override
    public int defineTableIndex()
        {
        return AUTHORS;
        }

    @Override
	protected int defineFormLayout()
		{
		return 	R.layout.author_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout()
		{
        EditField nameField = addEditField( R.id.edittext_name, AuthorsTable.NAME );

    	setupListButton( BooksControllActivity.class,
    			getActivity().getString( R.string.button_books_list ), 
    			getActivity().getString( R.string.books_of ),
    			nameField );
		}
    }
