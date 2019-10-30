package digitalgarden.mecsek.database.calendar;


import digitalgarden.mecsek.generic.database.GenericTable;

public final class CalendarTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "cal";
        }

    public static int DATE;
    public static int NOTE;

    @Override
    public void defineColumns()
        {
        DATE = addColumn( TYPE_DATE, "date" );
        NOTE = addColumn( TYPE_TEXT, "note" );
        addSourceColumns();
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addNoSourceOnlyAllVersions();
        exportImport().addColumnAllVersions( CalendarTable.NOTE );
        exportImport().addColumnAllVersions( CalendarTable.DATE );
        }

    }
