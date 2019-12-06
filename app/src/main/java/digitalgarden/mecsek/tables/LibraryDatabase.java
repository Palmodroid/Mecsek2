package digitalgarden.mecsek.tables;

import digitalgarden.mecsek.tables.authors.AuthorsTable;
import digitalgarden.mecsek.tables.books.BooksTable;
import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.records.RecordsTable;
import digitalgarden.mecsek.tables.recordtypes.RecordTypesTable;
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

    public static int AUTHORS;
    public static int BOOKS;

    public static int CALENDAR;
    public static int PATIENTS;
    public static int RECORD_TYPES;
    public static int RECORDS;

    //
    public final static int MAX_TABLES = 99;

    // Ahhoz, hogy a drop table működjön, előbb kell kitörölni a másokra hivatkozó táblákat!
    // Viszont az export igényli, hogy előbb a hivatkozott táblák exportálódjanak

    @Override
    public void defineTables()
        {
        AUTHORS = addTable( new AuthorsTable() );
        BOOKS = addTable( new BooksTable() );

        CALENDAR = addTable( new CalendarTable() );
        PATIENTS = addTable( new PatientsTable() );
        RECORD_TYPES = addTable( new RecordTypesTable() );
        RECORDS = addTable( new RecordsTable() );
        }
    }
