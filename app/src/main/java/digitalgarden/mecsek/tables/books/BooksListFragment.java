package digitalgarden.mecsek.tables.books;


import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.authors.AuthorsTable;
import digitalgarden.mecsek.generic.GenericCombinedListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.tables.LibraryDatabase.AUTHORS;
import static digitalgarden.mecsek.tables.LibraryDatabase.BOOKS;


public class BooksListFragment extends GenericCombinedListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericCombinedListFragment newInstance(long limit )
		{
		GenericCombinedListFragment listFragmenet = new BooksListFragment();

		Bundle args = new Bundle();
		
		// args.putLong( SELECTED_ITEM , SELECT_DISABLED ); Nincs szelektálás!
		
		args.putLong( LIMITED_ITEM, limit );
		args.putInt( LIMITED_COLUMN, BooksTable.AUTHOR_ID );
		args.putString( ORDERED_COLUMN, columnFull(AuthorsTable.NAME));
		// args.putString( FILTERED_COLUMN, BooksTable.FULL_SEARCH);
		args.putStringArray( FILTERED_COLUMN, new String[] {columnFull(AuthorsTable.SEARCH), columnFull(BooksTable.SEARCH)});
		
		listFragmenet.setArguments(args);

		return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return BOOKS;
        }

    @Override
    protected int defineRowLayout()
        {
        return R.layout.book_list_row_view;
        }

    @Override
    protected void setupRowLayout()
        {
        addField( R.id.author, AuthorsTable.NAME );
        addField( R.id.title, BooksTable.TITLE );
        addIdField();
        }

    @Override
    protected Header defineHeader()
        {
        return new Header()
            {
            @Override
            protected int defineLimitedForeignKeyIndex()
                {
                return BooksTable.AUTHOR_ID;
                }

            protected int defineRowLayout()
                {
                return R.layout.book_list_header_view;
                }

            protected void setupRowLayout()
                {
                addField( R.id.author, AuthorsTable.NAME );
                addIdField();
                }
            };
        }


    @Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.putNull( column(BooksTable.AUTHOR_ID) );
		values.put( column(BooksTable.TITLE), "Urania");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( column(BooksTable.AUTHOR_ID) );
		values.put( column(BooksTable.TITLE), "Elrontottam!");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( column(BooksTable.AUTHOR_ID) );
		values.put( column(BooksTable.TITLE), "Egri csillagok");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( column(BooksTable.AUTHOR_ID) );
		values.put( column(BooksTable.TITLE), "A Pál utcai fiúk");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( column(BooksTable.AUTHOR_ID) );
		values.put( column(BooksTable.TITLE), "Abigél");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( column(BooksTable.AUTHOR_ID) );
		values.put( column(BooksTable.TITLE), "Tüskevár");
		getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( column(BooksTable.AUTHOR_ID) );
		values.put( column(BooksTable.TITLE), "Ábel a rengetegben");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( column(BooksTable.AUTHOR_ID) );
		values.put( column(BooksTable.TITLE), "Példa Fibinek");
		getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);
		}

	}
