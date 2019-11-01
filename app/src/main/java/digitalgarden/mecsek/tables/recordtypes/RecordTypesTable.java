package digitalgarden.mecsek.tables.recordtypes;


import digitalgarden.mecsek.generic.GenericTable;

public final class RecordTypesTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "recordtypes";
        }

    public static int NAME;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn( TYPE_TEXT, "name" );
        SEARCH = addSearchColumnFor(NAME);
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( RecordTypesTable.NAME);
        }

    /*
    @Override
    public void importRow(String[] records)
        {
        // Mivel csak egy adat van, hossz ellenőrzése nem szükséges

        records[1] = StringUtils.revertFromEscaped( records[1] );

        // Uniqe ellenőrzés kódból. Lehetne adatbázis szinten is, hiba ellenőrzésével
        String[] projection = {
                column(RecordTypesTable.NAME) };
        Cursor cursor = getContentResolver()
                .query( table(RECORD_TYPES).contentUri(), projection,
                        column(RecordTypesTable.NAME) + "='" + records[1] + "'", null, null);

        // http://stackoverflow.com/a/16108435
        if (cursor == null || cursor.getCount() == 0)
            {
            ContentValues values = new ContentValues();
            values.put( column(RecordTypesTable.NAME), records[1]);

            getContentResolver()
                    .insert( table(RECORD_TYPES).contentUri(), values);
            Scribe.debug( "Record type [" + records[1] + "] was inserted.");
            }
        else
            Scribe.note( "Record type [" + records[1] + "] already exists! Item was skipped.");

        if ( cursor != null )
            cursor.close();
        }
    */
    }
