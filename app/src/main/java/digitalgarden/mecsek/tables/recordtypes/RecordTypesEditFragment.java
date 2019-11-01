package digitalgarden.mecsek.tables.recordtypes;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.records.RecordsControllActivity;
import digitalgarden.mecsek.formtypes.EditField;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.tables.LibraryDatabase.RECORD_TYPES;


public class RecordTypesEditFragment extends GenericEditFragment
	{
    @Override
    public int defineTableIndex()
        {
        return RECORD_TYPES;
        }

    @Override
	protected int defineFormLayout()
		{
		return 	R.layout.record_types_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout()
		{
		Scribe.note("RecordTypesEditFragment setupFormLayout");
		
        EditField recordTypeNameField = addEditField( R.id.foreigntext_record_type_name, RecordTypesTable.NAME);

		setupListButton( RecordsControllActivity.class,
                getActivity().getString( R.string.button_record_list),
                getActivity().getString( R.string.records_with ),
    			recordTypeNameField );
    	}
	}
