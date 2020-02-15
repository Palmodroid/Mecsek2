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
        return 1;
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

    public static int CALENDAR;
    public static int PATIENTS;
    public static int CATEGORIES;
    public static int RECORDS;


    @Override
    public void defineTables()
        {
        COLOR_DEFS = addTable( new ColorDefsTable() );
        AUTHORS = addTable( new AuthorsTable() );
        BOOKS = addTable( new BooksTable() );

        CALENDAR = addTable( new CalendarTable() );
        PATIENTS = addTable( new PatientsTable() );
        CATEGORIES = addTable( new CategoriesTable() );
        RECORDS = addTable( new RecordsTable() );
        }
    }
