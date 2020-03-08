package digitalgarden.mecsek.tables.records;

import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.category.CategoriesTable;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.generic.GenericTable;

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

    public static int NAME;
    public static int DATE;
//     public static int CATEGORY_ID;
    public static int PATIENT_ID;
    public static int CALENDAR_ID;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn( TYPE_TEXT, "name" );
        DATE = addColumn( TYPE_DATE, "date" );
        PATIENT_ID = addForeignKey( "patient_id", PATIENTS );

        CALENDAR_ID = addExternKey( "calendar_id", CALENDAR );
        addFarawayForeignQuery( CalendarTable.CATEGORY_ID, CATEGORIES );

        SEARCH = addSearchColumnFor( NAME );

        // CATEGORY_ID = addForeignKey( "title_id", CATEGORIES);

        //addUniqueColumn
        //addUniqueContstraint(NAME, DOB, TAJ);

        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( RecordsTable.NAME );
        exportImport().addColumnAllVersions( RecordsTable.DATE );
        exportImport().addForeignKeyAllVersions( PATIENT_ID, PATIENTS, PatientsTable.NAME, PatientsTable.DOB, PatientsTable.TAJ );
        exportImport().addExternKeyAllVersions( CALENDAR_ID, CALENDAR, CalendarTable.NOTE, CalendarTable.DATE);

        // exportImport().addForeignKeyAllVersions(CATEGORY_ID, CATEGORIES, CategoriesTable.NAME);
        }

    @Override
    public Class getControllActivity()
        {
        return RecordsControllActivity.class;
        }
    }

