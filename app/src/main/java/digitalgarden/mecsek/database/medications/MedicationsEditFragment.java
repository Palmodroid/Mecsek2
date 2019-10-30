package digitalgarden.mecsek.database.medications;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.calendar.CalendarTable;
import digitalgarden.mecsek.database.patients.PatientsControllActivity;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsControllActivity;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.formtypes.EditFieldDate;
import digitalgarden.mecsek.formtypes.EditFieldText;
import digitalgarden.mecsek.formtypes.ExternKey;
import digitalgarden.mecsek.formtypes.ForeignKey;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.library.LibraryDatabase.CALENDAR;
import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class MedicationsEditFragment extends GenericEditFragment
	{
    @Override
    public int defineTableIndex()
        {
        return MEDICATIONS;
        }

	@Override
	protected int defineFormLayout()
		{
		return 	R.layout.medication_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout()
		{
		Scribe.note("MedicationsEditFragment setupFormLayout");

        // EditTextField
        EditFieldText medicationNameField = (EditFieldText)addEditField( R.id.edittextfield_medication_name, MedicationsTable.NAME );

        // EditTextField
        EditFieldDate medicationDateField = (EditFieldDate)addEditField( R.id.editdatefield_medication_date, MedicationsTable.DATE );

        // ForeignKey
        ForeignKey pillKey = addForeignKey( MedicationsTable.PILL_ID, PILLS,
                PillsControllActivity.class,
                getActivity().getString( R.string.select_pill ),
                medicationNameField );

		// ForeignKey
        ForeignKey patientKey = addForeignKey( MedicationsTable.PATIENT_ID, PATIENTS,
                PatientsControllActivity.class,
				getActivity().getString( R.string.select_patient ),
				medicationNameField );

		// ForeignTextField
        pillKey.addEditField( R.id.foreigntext_pill_name, PillsTable.NAME );

		// ForeignTextField
		patientKey.addEditField( R.id.foreigntext_patient_name, PatientsTable.NAME );
        patientKey.addEditField( R.id.foreigntext_patient_dob, PatientsTable.DOB );

        // ExternKey
        ExternKey calendarKey = addExternKey( MedicationsTable.CALENDAR_ID, CALENDAR);

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
