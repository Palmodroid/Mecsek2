package digitalgarden.mecsek.database.library;

import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.database.books.BooksTable;
import digitalgarden.mecsek.database.calendar.CalendarTable;
import digitalgarden.mecsek.database.medications.MedicationsTable;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.generic.database.GenericDatabase;

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
    public static int PILLS;
    public static int PATIENTS;
    public static int MEDICATIONS;


    // Ahhoz, hogy a drop table működjön, előbb kell kitörölni a másokra hivatkozó táblákat!
    // Viszont az export igényli, hogy előbb a hivatkozott táblák exportálódjanak

    @Override
    public void defineTables()
        {
        AUTHORS = addTable( new AuthorsTable() );
        BOOKS = addTable( new BooksTable() );

        CALENDAR = addTable( new CalendarTable() );
        PILLS = addTable( new PillsTable() );
        PATIENTS = addTable( new PatientsTable() );
        MEDICATIONS = addTable( new MedicationsTable() );
        }
    }
