package digitalgarden.mecsek.tables.books;

import digitalgarden.mecsek.tables.authors.AuthorsTable;
import digitalgarden.mecsek.generic.GenericTable;

import static digitalgarden.mecsek.tables.LibraryDatabase.AUTHORS;


// http://martin.cubeactive.com/android-using-joins-with-a-provider-sqlite/
public final class BooksTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "books";
        }

    public static int TITLE;
    public static int IMAGE;
    public static int AUTHOR_ID;
    public static int NOTE;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        TITLE = addColumn( TYPE_TEXT, "title" );
        IMAGE = addColumn( TYPE_IMAGE, "image");
        NOTE = addColumn( TYPE_TEXT, "note" );
        SEARCH = addSearchColumnFor(TITLE);
        AUTHOR_ID = addForeignKey("author_id", AUTHORS);
        }

    @Override
    public void definePortColumns()
        {
        port().addColumnAllVersions( BooksTable.TITLE );
        port().addForeignKeyAllVersions( AUTHOR_ID, AUTHORS, AuthorsTable.NAME );
        }
    }
