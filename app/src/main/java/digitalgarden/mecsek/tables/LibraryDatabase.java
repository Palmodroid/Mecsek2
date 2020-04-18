package digitalgarden.mecsek.tables;

import digitalgarden.mecsek.color.ColorDefsTable;
import digitalgarden.mecsek.tables.authors.AuthorsTable;
import digitalgarden.mecsek.tables.books.BooksTable;
import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.records.RecordsTable;
import digitalgarden.mecsek.tables.category.CategoriesTable;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.generic.GenericDatabase;

public class LibraryDatabase extends GenericDatabase
    {
    @Override
    public String name()
        {
        return "library";
        }

    @Override
    public int version()
        {
        return 2;
        }

    @Override
    public String authority()
        {
        return "digitalgarden.mecsek.contentprovider";
        }

    /* Id-s/indices of the tables used by DatabaseMIrror.table( int index */

    public static int COLOR_DEFS;

    public static int AUTHORS;
    public static int BOOKS;

    public static int CATEGORIES;
    public static int CALENDAR;
    public static int PATIENTS;
    public static int RECORDS;


    @Override
    public void defineTables()
        {
        // App tables
        COLOR_DEFS = addTable( new ColorDefsTable() );

        // User tables
        AUTHORS = addTable( new AuthorsTable() );
        BOOKS = addTable( new BooksTable() );

        // NAME(SEARCH), STYLE
        CATEGORIES = addTable( new CategoriesTable() );
        // DATE, NOTE, Source, Foreign: CATEGORY_ID
        CALENDAR = addTable( new CalendarTable() );
        // Unique{NAME(SEARCH), DOB, TAJ} PHONE NOTE
        PATIENTS = addTable( new PatientsTable() );
        // NAME(SEARCH), DATE, Foreign: PATIENTS_ID, Extern: CALENDAR_ID Faraway: CATEGORY_ID
        RECORDS = addTable( new RecordsTable() );
        }
    }
