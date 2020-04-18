package digitalgarden.mecsek.tables.category;


import digitalgarden.mecsek.generic.GenericTable;

public final class CategoriesTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "categories";
        }

    public static int NAME;
    public static int STYLE;

    public static int SEARCH;


    @Override
    public void defineColumns()
        {
        NAME = addUniqueColumn( TYPE_TEXT, "name" );
        STYLE = addColumn(TYPE_STYLE, "style");

        SEARCH = addSearchColumnFor(NAME);
        }

    @Override
    public void definePortColumns()
        {
        port().addColumnAllVersions( CategoriesTable.NAME );
        port().addColumnAllVersions( CategoriesTable.STYLE );
        }

    /*
    @Override
    public void importRow(String[] records)
        {
        // Mivel csak egy adat van, hossz ellenőrzése nem szükséges

        records[1] = StringUtils.revertFromEscaped( records[1] );

        // Uniqe ellenőrzés kódból. Lehetne adatbázis szinten is, hiba ellenőrzésével
        String[] projection = {
                column(CategoriesTable.NAME) };
        Cursor cursor = getContentResolver()
                .query( table(CATEGORIES).contentUri(), projection,
                        column(CategoriesTable.NAME) + "='" + records[1] + "'", null, null);

        // http://stackoverflow.com/a/16108435
        if (cursor == null || cursor.getCount() == 0)
            {
            ContentValues values = new ContentValues();
            values.put( column(CategoriesTable.NAME), records[1]);

            getContentResolver()
                    .insert( table(CATEGORIES).contentUri(), values);
            Scribe.debug( "Record type [" + records[1] + "] was inserted.");
            }
        else
            Scribe.note( "Record type [" + records[1] + "] already exists! Item was skipped.");

        if ( cursor != null )
            cursor.close();
        }
    */
    }
