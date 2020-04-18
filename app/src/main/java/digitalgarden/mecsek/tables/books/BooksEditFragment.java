package digitalgarden.mecsek.tables.books;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.authors.AuthorsControllActivity;
import digitalgarden.mecsek.tables.authors.AuthorsTable;
import digitalgarden.mecsek.fieldtypes.EditField;
import digitalgarden.mecsek.fieldtypes.ForeignKey;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.tables.LibraryDatabase.AUTHORS;
import static digitalgarden.mecsek.tables.LibraryDatabase.BOOKS;


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
		// ImageField
        addField( R.id.image_book, BooksTable.IMAGE );

		// EditTextField
        EditField editTextTitle = addField( R.id.edittext_book, BooksTable.TITLE );

        // ForeignKey
        ForeignKey authorKey = addForeignKey( BooksTable.AUTHOR_ID,
                AuthorsControllActivity.class,
        		getActivity().getString( R.string.select_author ),
        		editTextTitle );

        authorKey.addField( R.id.edittext_author, AuthorsTable.NAME );
		}
    }
