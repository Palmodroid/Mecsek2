package digitalgarden.mecsek.database.patients;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;


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
		
        addEditField( R.id.edittext_patient_name, PatientsTable.NAME );
        addEditField( R.id.edittext_patient_dob, PatientsTable.DOB );
        addEditField( R.id.edittext_patient_taj, PatientsTable.TAJ );
        addEditField( R.id.edittext_patient_phone, PatientsTable.PHONE );
        addEditField( R.id.edittext_patient_note, PatientsTable.NOTE );
		}

	}
