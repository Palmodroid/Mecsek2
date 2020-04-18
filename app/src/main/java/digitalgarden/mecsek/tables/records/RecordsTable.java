package digitalgarden.mecsek.tables.records;

import digitalgarden.mecsek.port.PortExternKey;
import digitalgarden.mecsek.port.PortForeignKey;
import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.category.CategoriesTable;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.generic.GenericTable;

import java.util.Calendar;

import static digitalgarden.mecsek.tables.LibraryDatabase.CALENDAR;
import static digitalgarden.mecsek.tables.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.tables.LibraryDatabase.CATEGORIES;

public final class RecordsTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "records";
        }

    public static int DETAILS;          // Currently DETAILS is the only own column
    public static int PATIENT_ID;       // NAME DOB TAJ
    public static int CALENDAR_ID;      // NOTE DATE and CATEGORY_ID
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        DETAILS = addColumn( TYPE_TEXT, "name" ); // Details

        PATIENT_ID = addForeignKey( "patient_id", PATIENTS );

        CALENDAR_ID = addExternKey( "calendar_id", CALENDAR );

        // It should be exported separatedly
        addFarawayForeignQuery( CalendarTable.CATEGORY_ID, CATEGORIES );

        SEARCH = addSearchColumnFor(DETAILS);
        }

    @Override
    public void definePortColumns()
        {
        port().addColumnAllVersions( RecordsTable.DETAILS);
        port().addForeignKeyAllVersions( PATIENT_ID, PATIENTS, PatientsTable.NAME, PatientsTable.DOB, PatientsTable.TAJ );
        PortExternKey portExternKey =
                port().addExternKeyAllVersions( CALENDAR_ID, CALENDAR, CalendarTable.NOTE, CalendarTable.DATE);

        // EXTERN record also contains a FOREIGN record - it should be added here
        portExternKey.addColumn( new PortForeignKey( CalendarTable.CATEGORY_ID, CATEGORIES, CategoriesTable.NAME,
                CategoriesTable.STYLE ));
        }

    @Override
    public Class getControllActivity()
        {
        return RecordsControllActivity.class;
        }
    }

