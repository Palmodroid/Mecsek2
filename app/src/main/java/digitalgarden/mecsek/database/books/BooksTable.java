package digitalgarden.mecsek.database.books;

import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.generic.database.GenericTable;

import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;


// http://martin.cubeactive.com/android-using-joins-with-a-provider-sqlite/
public final class BooksTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "books";
        }

    public static int TITLE;
    public static int AUTHOR_ID;
    public static int NOTE;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        TITLE = addColumn( TYPE_TEXT, "title" );
        NOTE = addColumn( TYPE_TEXT, "note" );
        SEARCH = addSearchColumnFor(TITLE);
        AUTHOR_ID = addForeignKey("author_id", AUTHORS);
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( BooksTable.TITLE );
        exportImport().addForeignKeyAllVersions( AUTHOR_ID, AUTHORS, AuthorsTable.NAME );
        }
    }
