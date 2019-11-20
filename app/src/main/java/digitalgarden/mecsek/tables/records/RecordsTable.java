package digitalgarden.mecsek.tables.records;

import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.recordtypes.RecordTypesTable;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.generic.GenericTable;

import static digitalgarden.mecsek.tables.LibraryDatabase.CALENDAR;
import static digitalgarden.mecsek.tables.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORD_TYPES;

public final class RecordsTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "records";
        }

    public static int NAME;
    public static int DATE;
    public static int TYPE_ID;
    public static int PATIENT_ID;
    public static int CALENDAR_ID;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn( TYPE_TEXT, "name" );
        DATE = addColumn( TYPE_DATE, "date" );
        TYPE_ID = addForeignKey( "title_id", RECORD_TYPES);
        PATIENT_ID = addForeignKey( "patient_id", PATIENTS );
        CALENDAR_ID = addExternKey( "calendar_id", CALENDAR );
        SEARCH = addSearchColumnFor( NAME );

        //addUniqueColumn
        //addUniqueContstraint(NAME, DOB, TAJ);
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( RecordsTable.NAME );
        exportImport().addColumnAllVersions( RecordsTable.DATE );
        exportImport().addForeignKeyAllVersions(TYPE_ID, RECORD_TYPES, RecordTypesTable.NAME);
        exportImport().addForeignKeyAllVersions( PATIENT_ID, PATIENTS, PatientsTable.NAME, PatientsTable.DOB, PatientsTable.TAJ );
        exportImport().addExternKeyAllVersions( CALENDAR_ID, CALENDAR, CalendarTable.NOTE);
        }

    @Override
    public Class getControllActivity()
        {
        return RecordsControllActivity.class;
        }
    }

