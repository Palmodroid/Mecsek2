package digitalgarden.mecsek.database.medications;

import digitalgarden.mecsek.database.calendar.CalendarTable;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.generic.database.GenericTable;

import static digitalgarden.mecsek.database.library.LibraryDatabase.CALENDAR;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;

public final class MedicationsTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "medications";
        }

    public static int NAME;
    public static int DATE;
    public static int PILL_ID;
    public static int PATIENT_ID;
    public static int CALENDAR_ID;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn( TYPE_TEXT, "name" );
        DATE = addColumn( TYPE_DATE, "date" );
        PILL_ID = addForeignKey( "pill_id", PILLS );
        PATIENT_ID = addForeignKey( "patient_id", PATIENTS );
        CALENDAR_ID = addExternKey( "calendar_id", CALENDAR );
        SEARCH = addSearchColumnFor( NAME );

        //addUniqueColumn
        //addUniqueContstraint(NAME, DOB, TAJ);
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( MedicationsTable.NAME );
        exportImport().addColumnAllVersions( MedicationsTable.DATE );
        exportImport().addForeignKeyAllVersions( PILL_ID, PILLS, PillsTable.NAME );
        exportImport().addForeignKeyAllVersions( PATIENT_ID, PATIENTS, PatientsTable.NAME, PatientsTable.DOB, PatientsTable.TAJ );
        exportImport().addExternKeyAllVersions( CALENDAR_ID, CALENDAR, CalendarTable.NOTE);
        }

    @Override
    public Class getControllActivity()
        {
        return MedicationsControllActivity.class;
        }
    }

