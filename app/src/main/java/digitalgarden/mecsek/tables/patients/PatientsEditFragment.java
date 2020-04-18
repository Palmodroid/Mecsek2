package digitalgarden.mecsek.tables.patients;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.fieldtypes.EditField;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.tables.records.RecordsControllActivity;

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
		
        EditField nameField = addField( R.id.edittext_patient_name, PatientsTable.NAME );
        addField( R.id.edittext_patient_dob, PatientsTable.DOB );
        addField( R.id.edittext_patient_taj, PatientsTable.TAJ );
        addField( R.id.edittext_patient_phone, PatientsTable.PHONE );
        addField( R.id.edittext_patient_note, PatientsTable.NOTE );

		setupListButton( RecordsControllActivity.class,
				getActivity().getString( R.string.button_books_list ),
				getActivity().getString( R.string.books_of ),
				nameField );
		}


	}
