package digitalgarden.mecsek.tables.records;

import android.content.ContentValues;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.fieldtypes.EditField;
import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.category.CategoriesTable;
import digitalgarden.mecsek.tables.patients.PatientsControllActivity;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.tables.category.CategoriesControllActivity;
import digitalgarden.mecsek.fieldtypes.EditFieldText;
import digitalgarden.mecsek.fieldtypes.ExternKey;
import digitalgarden.mecsek.fieldtypes.ForeignKey;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.viewutils.Longstyle;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.tables.LibraryDatabase.CALENDAR;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORDS;
import static digitalgarden.mecsek.tables.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.tables.LibraryDatabase.CATEGORIES;


public class RecordsEditFragment extends GenericEditFragment
    {
    EditField editFieldCategoryName;

    @Override
    public int defineTableIndex()
        {
        return RECORDS;
        }

    @Override
    protected int defineFormLayout()
        {
        return 	R.layout.record_edit_fragment_form;
        }

    @Override
    protected void setupFormLayout()
        {
        Scribe.note("RecordsEditFragment setupFormLayout");

        /*
        Fields used record_edit_fragment_form

        <!-- DATA OF THE PATIENT (OWNER OF THE RECORD) -->
        android:id="@+id/title_patient"
			android:id="@+id/foreigntext_patient_name"
			android:id="@+id/foreigntext_patient_dob"

        <!-- CATEGORY OF THE DATE -->
        android:id="@+id/title_record_category"
            android:id="@+id/editfieldtext_category_name"

        <!-- DATE OF THE RECORD -->
        android:id="@+id/title_record_date"
                android:id="@+id/externdate_calendar_date"
                android:id="@+id/externtext_calendar_note"

        <!-- DETAILS OF THE RECORD -->
        android:id="@+id/title_record_details"
            android:id="@+id/editfieldtext_record_details"
         */


        EditFieldText recordDetailsField;
        ForeignKey patientKey;
        ExternKey calendarKey;

        // EditTextField - DETALIS
        recordDetailsField = addField( R.id.editfieldtext_record_details, RecordsTable.DETAILS);

        // ForeignKey - PATIENT
        patientKey = addForeignKey( RecordsTable.PATIENT_ID,
                PatientsControllActivity.class,
                getActivity().getString( R.string.select_patient ),
                recordDetailsField );
        // ForeignTextField
        patientKey.addField( R.id.foreigntext_patient_name, PatientsTable.NAME );
        patientKey.addField( R.id.foreigntext_patient_dob, PatientsTable.DOB );

        // ExternKey - DATE and...
        calendarKey = addExternKey( RecordsTable.CALENDAR_ID );
        // ExternTextField
        calendarKey.addField( R.id.externtext_calendar_note, CalendarTable.NOTE );
        // ExternDateField
        calendarKey.addField( R.id.externdate_calendar_date, CalendarTable.DATE );

        // ForeignKey inside ExternKey ...DATE'S CATEGORY
        ForeignKey categoryKey = calendarKey.addForeignKey( CalendarTable.CATEGORY_ID, CATEGORIES,
                                CategoriesControllActivity.class,
                                getActivity().getString( R.string.select_category),
                                recordDetailsField );
        // Foreign TextField
        categoryKey.addField( R.id.editfieldtext_category_name, CategoriesTable.NAME );
        }

    @Override
    public void onColumnValueChanged(ContentValues values)
        {
        Long style = values.getAsLong( column(CategoriesTable.STYLE));

        Longstyle.override( style,
                editFieldCategoryName );
        }
    }
