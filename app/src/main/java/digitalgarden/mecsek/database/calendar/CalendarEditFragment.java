package digitalgarden.mecsek.database.calendar;

import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.formtypes.EditFieldDate;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.database.library.LibraryDatabase.CALENDAR;


public class CalendarEditFragment extends GenericEditFragment
	{
	public static String DATE_HINT = "DATE_HINT";


    @Override
    public int defineTableIndex()
        {
        return CALENDAR;
        }

    @Override
	protected int defineFormLayout()
		{
		return 	R.layout.calendar_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout()
		{
        addEditField( R.id.edittextfield_calendar_note, CalendarTable.NOTE );
        addEditField( R.id.editdatefield_calendar_date, CalendarTable.DATE, DATE_HINT );
        //addEditField( R.id.calendar_source_table, table(CALENDAR).SOURCE_TABLE );
        //addEditField( R.id.calendar_source_row, table(CALENDAR).SOURCE_ROW );
        addSourceField( R.id.calendar_source_button );
		}
    }
