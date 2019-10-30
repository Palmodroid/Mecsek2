package digitalgarden.mecsek.database.pills;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.medications.MedicationsControllActivity;
import digitalgarden.mecsek.formtypes.EditField;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class PillsEditFragment extends GenericEditFragment
	{
    @Override
    public int defineTableIndex()
        {
        return PILLS;
        }

    @Override
	protected int defineFormLayout()
		{
		return 	R.layout.pill_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout()
		{
		Scribe.note("PillsEditFragment setupFormLayout");
		
        EditField pillNameField = addEditField( R.id.foreigntext_pill_name, PillsTable.NAME);

		setupListButton( MedicationsControllActivity.class,
    			getActivity().getString( R.string.button_medication_list ),
    			getActivity().getString( R.string.medications_with ),
    			pillNameField );
    	}
	}
