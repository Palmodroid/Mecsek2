package digitalgarden.mecsek.database.books;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.authors.AuthorsControllActivity;
import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.formtypes.EditField;
import digitalgarden.mecsek.formtypes.ForeignKey;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.BOOKS;


public class BooksEditFragment extends GenericEditFragment
	{
    @Override
    public int defineTableIndex()
        {
        return BOOKS;
        }

    @Override
	protected int defineFormLayout()
		{
		return R.layout.book_edit_fragment_form;
		}
	
	@Override
	protected void setupFormLayout()
		{
		// EditTextField
        EditField editTextTitle = addEditField( R.id.edittext_title, BooksTable.TITLE );

        // ForeignKey
        ForeignKey authorKey = addForeignKey( BooksTable.AUTHOR_ID, AUTHORS,
                AuthorsControllActivity.class,
        		getActivity().getString( R.string.select_author ),
        		editTextTitle );

        authorKey.addEditField( R.id.edittext_author, AuthorsTable.NAME );
		}
    }
