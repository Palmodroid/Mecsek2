package digitalgarden.mecsek.database.patients;

import digitalgarden.mecsek.generic.database.GenericTable;


public final class PatientsTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "patients";
        }

    public static int NAME;
    public static int DOB;
    public static int TAJ;
    public static int PHONE;
    public static int NOTE;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn( TYPE_TEXT, "name" );
        DOB = addColumn( TYPE_TEXT, "dob" );
        TAJ = addColumn( TYPE_TEXT, "taj" );
        PHONE = addColumn( TYPE_TEXT, "phone" );
        NOTE = addColumn( TYPE_TEXT, "note" );
        SEARCH = addSearchColumnFor( NAME );

        addUniqueConstraint( PatientsTable.NAME, PatientsTable.DOB, PatientsTable.TAJ );
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( PatientsTable.NAME );
        exportImport().addColumnAllVersions( PatientsTable.DOB );
        exportImport().addColumnAllVersions( PatientsTable.TAJ );
        exportImport().addColumnAllVersions( PatientsTable.PHONE );
        exportImport().addColumnAllVersions( PatientsTable.NOTE );
        }

    /*
    @Override
    public void importRow(String[] records)
        {
        // Több adat miatt itt szükséges a hossz ellenőrzése
        if ( records.length < 6 )
            {
            Scribe.note( "Parameters missing from PATIENTS row. Item was skipped.");
            return;
            }

        ContentValues values = new ContentValues();

        records[1] = StringUtils.revertFromEscaped( records[1] );
        values.put( column(PatientsTable.NAME), records[1] );

        records[2] = StringUtils.revertFromEscaped( records[2] );
        values.put( column(PatientsTable.DOB), records[2] );

        records[3] = StringUtils.revertFromEscaped( records[3] );
        values.put( column(PatientsTable.TAJ), records[3] );

        records[4] = StringUtils.revertFromEscaped( records[4] );
        values.put( column(PatientsTable.PHONE), records[4] );

        records[5] = StringUtils.revertFromEscaped( records[5] );
        values.put( column(PatientsTable.NOTE), records[5] );

        getContentResolver()
                .insert( table(PATIENTS).contentUri(), values);
        Scribe.debug( "Patient [" + records[1] + "] was inserted.");
        }
    */
    }
