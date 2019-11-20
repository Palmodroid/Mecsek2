package digitalgarden.mecsek.tables.patients;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.formtypes.EditField;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.tables.books.BooksControllActivity;
import digitalgarden.mecsek.tables.records.RecordsControllActivity;
import digitalgarden.mecsek.tables.recordtypes.RecordTypesControllActivity;

import static digitalgarden.mecsek.tables.LibraryDatabase.PATIENTS;


public class PatientsEditFragment extends GenericEditFragment
	{
    @Override
    public int defineTableIndex()
        {
        return PATIENTS;
        }

	@Override
	protected int defineFormLayout()
		{
		return 	R.layout.patient_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout()
		{
		Scribe.note("PatientsEditFragment setupFormLayout");
		
        EditField nameField = addEditField( R.id.edittext_patient_name, PatientsTable.NAME );
        addEditField( R.id.edittext_patient_dob, PatientsTable.DOB );
        addEditField( R.id.edittext_patient_taj, PatientsTable.TAJ );
        addEditField( R.id.edittext_patient_phone, PatientsTable.PHONE );
        addEditField( R.id.edittext_patient_note, PatientsTable.NOTE );

		setupListButton( RecordsControllActivity.class,
				getActivity().getString( R.string.button_books_list ),
				getActivity().getString( R.string.books_of ),
				nameField );
		}


	}
