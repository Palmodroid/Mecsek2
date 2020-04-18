package digitalgarden.mecsek.tables.authors;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.books.BooksControllActivity;
import digitalgarden.mecsek.fieldtypes.EditField;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.tables.LibraryDatabase.AUTHORS;


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
        EditField nameField = addField( R.id.edittext_name, AuthorsTable.NAME );

    	setupListButton( BooksControllActivity.class,
    			getActivity().getString( R.string.button_books_list ), 
    			getActivity().getString( R.string.books_of ),
    			nameField );
		}
    }
