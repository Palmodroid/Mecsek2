package digitalgarden.mecsek.tables.records;

import android.content.ContentValues;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.formtypes.EditField;
import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.category.CategoriesTable;
import digitalgarden.mecsek.tables.patients.PatientsControllActivity;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.tables.category.CategoriesControllActivity;
import digitalgarden.mecsek.formtypes.EditFieldText;
import digitalgarden.mecsek.formtypes.ExternKey;
import digitalgarden.mecsek.formtypes.ForeignKey;
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

        ForeignKey patientKey;
//        ForeignKey categoryKey;
        ExternKey calendarKey;
        EditFieldText recordNameField;

        // EditTextField
        recordNameField = (EditFieldText)addEditField( R.id.editfieldtext_record_details, RecordsTable.NAME );

        // ForeignKey
        patientKey = addForeignKey( RecordsTable.PATIENT_ID, PATIENTS,
                PatientsControllActivity.class,
                getActivity().getString( R.string.select_patient ),
                recordNameField );

        // ForeignTextField
        patientKey.addEditField( R.id.foreigntext_patient_name, PatientsTable.NAME );
        patientKey.addEditField( R.id.foreigntext_patient_dob, PatientsTable.DOB );

        // ForeignKey
//        categoryKey = addForeignKey( RecordsTable.CATEGORY_ID, CATEGORIES,
//                CategoriesControllActivity.class,
//                getActivity().getString( R.string.select_category),
//                recordNameField );

        // ForeignTextField
//        editFieldCategoryName = categoryKey.addEditField( R.id.editfieldtext_category_name, CategoriesTable.NAME);

        // ForeignStyleField - will call {@link #onColumnValueChanged(ContentValues)}
//        categoryKey.addStyleField( CategoriesTable.STYLE );

        // ExternKey
        calendarKey = addExternKey( RecordsTable.CALENDAR_ID, CALENDAR);

        // ExternTextField
        calendarKey.addEditField( R.id.externtext_calendar_note, CalendarTable.NOTE );

        // ExternDateField
        calendarKey.addEditField( R.id.externdate_calendar_date, CalendarTable.DATE );

        // calendarKey.addEditField( R.id.editfieldtext_category_name, CategoriesTable.NAME );


        // EditFieldDate recordDateField = (EditFieldDate)addEditField( R.id.externdate_calendar_date,
        //        RecordsTable.DATE );

        /*
        setupListButton( BooksControllActivity.class,
                getActivity().getString( R.string.button_books_list ),
                getActivity().getString( R.string.books_of ),
                nameField );
        */
        }

    @Override
    public void onColumnValueChanged(ContentValues values)
        {
        Long style = values.getAsLong( column(CategoriesTable.STYLE));

        Longstyle.override( style,
                editFieldCategoryName );
        }
    }
