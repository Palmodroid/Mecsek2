package digitalgarden.mecsek.tables.records;

import digitalgarden.mecsek.R;
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

import static digitalgarden.mecsek.tables.LibraryDatabase.CALENDAR;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORDS;
import static digitalgarden.mecsek.tables.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.tables.LibraryDatabase.CATEGORIES;


public class RecordsEditFragment extends GenericEditFragment
	{
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
        ForeignKey recordTypeKey;
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
        recordTypeKey = addForeignKey( RecordsTable.TYPE_ID, CATEGORIES,
                CategoriesControllActivity.class,
                getActivity().getString( R.string.select_category),
                recordNameField );

        // ForeignTextField
        recordTypeKey.addEditField( R.id.editfieldtext_category_name, CategoriesTable.NAME);

        // ExternKey
        calendarKey = addExternKey( RecordsTable.CALENDAR_ID, CALENDAR);

        // ExternTextField
        calendarKey.addEditField( R.id.externtext_calendar_note, CalendarTable.NOTE );

        // ExternDateField
        calendarKey.addEditField( R.id.externdate_calendar_date, CalendarTable.DATE );
        // EditFieldDate recordDateField = (EditFieldDate)addEditField( R.id.externdate_calendar_date,
        //        RecordsTable.DATE );

    	/*
		setupListButton( BooksControllActivity.class,
    			getActivity().getString( R.string.button_books_list ), 
    			getActivity().getString( R.string.books_of ),
    			nameField );
    	*/
		}
	}
