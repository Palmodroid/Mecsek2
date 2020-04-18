package digitalgarden.mecsek.tables.calendar;


import digitalgarden.mecsek.generic.GenericTable;
import digitalgarden.mecsek.tables.category.CategoriesTable;

import static digitalgarden.mecsek.tables.LibraryDatabase.CATEGORIES;

public final class CalendarTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "calendars";
        }

    public static int DATE;
    public static int NOTE;
    public static int CATEGORY_ID;

    @Override
    public void defineColumns()
        {
        DATE = addColumn( TYPE_DATE, "date" );
        NOTE = addColumn( TYPE_TEXT, "note" );
        CATEGORY_ID = addForeignKey( "title_id", CATEGORIES);
        addSourceColumns();
        }

    @Override
    public void definePortColumns()
        {
        port().addColumnAllVersions( CalendarTable.NOTE );
        port().addColumnAllVersions( CalendarTable.DATE );
        port().addForeignKeyAllVersions(CATEGORY_ID, CATEGORIES, CategoriesTable.NAME, CategoriesTable.STYLE);
        }

    }
