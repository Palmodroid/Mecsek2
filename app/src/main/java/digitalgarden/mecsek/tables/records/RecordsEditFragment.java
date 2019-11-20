package digitalgarden.mecsek.tables.records;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.calendar.CalendarTable;
import digitalgarden.mecsek.tables.recordtypes.RecordTypesTable;
import digitalgarden.mecsek.tables.patients.PatientsControllActivity;
import digitalgarden.mecsek.tables.patients.PatientsTable;
import digitalgarden.mecsek.tables.recordtypes.RecordTypesControllActivity;
import digitalgarden.mecsek.formtypes.EditFieldDate;
import digitalgarden.mecsek.formtypes.EditFieldText;
import digitalgarden.mecsek.formtypes.ExternKey;
import digitalgarden.mecsek.formtypes.ForeignKey;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.tables.LibraryDatabase.CALENDAR;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORDS;
import static digitalgarden.mecsek.tables.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.tables.LibraryDatabase.RECORD_TYPES;


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

        // EditTextField
        EditFieldText recordNameField = (EditFieldText)addEditField( R.id.edittextfield_record_name, RecordsTable.NAME );

        // EditTextField
        EditFieldDate recordDateField = (EditFieldDate)addEditField( R.id.editdatefield_record_date, RecordsTable.DATE );

        // ForeignKey
        ForeignKey recordTypeKey = addForeignKey( RecordsTable.TYPE_ID, RECORD_TYPES,
                RecordTypesControllActivity.class,
                getActivity().getString( R.string.select_record_type),
                recordNameField );

		// ForeignKey
        ForeignKey patientKey = addForeignKey( RecordsTable.PATIENT_ID, PATIENTS,
                PatientsControllActivity.class,
				getActivity().getString( R.string.select_patient ),
				recordNameField );

		// ForeignTextField
        recordTypeKey.addEditField( R.id.foreigntext_record_type_name, RecordTypesTable.NAME);

		// ForeignTextField
		patientKey.addEditField( R.id.foreigntext_patient_name, PatientsTable.NAME );
        patientKey.addEditField( R.id.foreigntext_patient_dob, PatientsTable.DOB );

        // ExternKey
        ExternKey calendarKey = addExternKey( RecordsTable.CALENDAR_ID, CALENDAR);

        // ExternTextField
        calendarKey.addEditField( R.id.externtext_calendar_note, CalendarTable.NOTE );

    	/*
		setupListButton( BooksControllActivity.class,
    			getActivity().getString( R.string.button_books_list ), 
    			getActivity().getString( R.string.books_of ),
    			nameField );
    	*/
		}
	}
