package digitalgarden.mecsek.database.pills;


import digitalgarden.mecsek.generic.database.GenericTable;

public final class PillsTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "pills";
        }

    public static int NAME;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn( TYPE_TEXT, "name" );
        SEARCH = addSearchColumnFor( NAME );
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( PillsTable.NAME );
        }

    /*
    @Override
    public void importRow(String[] records)
        {
        // Mivel csak egy adat van, hossz ellenőrzése nem szükséges

        records[1] = StringUtils.revertFromEscaped( records[1] );

        // Uniqe ellenőrzés kódból. Lehetne adatbázis szinten is, hiba ellenőrzésével
        String[] projection = {
                column(PillsTable.NAME) };
        Cursor cursor = getContentResolver()
                .query( table(PILLS).contentUri(), projection,
                        column(PillsTable.NAME) + "='" + records[1] + "'", null, null);

        // http://stackoverflow.com/a/16108435
        if (cursor == null || cursor.getCount() == 0)
            {
            ContentValues values = new ContentValues();
            values.put( column(PillsTable.NAME), records[1]);

            getContentResolver()
                    .insert( table(PILLS).contentUri(), values);
            Scribe.debug( "Pill [" + records[1] + "] was inserted.");
            }
        else
            Scribe.note( "Pill [" + records[1] + "] already exists! Item was skipped.");

        if ( cursor != null )
            cursor.close();
        }
    */
    }
