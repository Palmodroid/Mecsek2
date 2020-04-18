package digitalgarden.mecsek.tables.calendar;

import android.content.ContentValues;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.fieldtypes.EditField;
import digitalgarden.mecsek.fieldtypes.ForeignKey;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.tables.category.CategoriesControllActivity;
import digitalgarden.mecsek.tables.category.CategoriesTable;
import digitalgarden.mecsek.viewutils.Longstyle;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.tables.LibraryDatabase.CALENDAR;
import static digitalgarden.mecsek.tables.LibraryDatabase.CATEGORIES;


public class CalendarEditFragment extends GenericEditFragment
	{
	public static String DATE_HINT = "DATE_HINT";

    EditField editFieldCategoryName;

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
        ForeignKey calendarCategoryKey;

        EditField calendarNote = addField( R.id.edittextfield_calendar_note, CalendarTable.NOTE );
        addField( R.id.editdatefield_calendar_date, CalendarTable.DATE, DATE_HINT );
        //addEditField( R.id.calendar_source_table, table(CALENDAR).SOURCE_TABLE );
        //addEditField( R.id.calendar_source_row, table(CALENDAR).SOURCE_ROW );
        addField( R.id.calendar_source_button );

        // ForeignKey
        calendarCategoryKey = addForeignKey( CalendarTable.CATEGORY_ID,
                CategoriesControllActivity.class,
                getActivity().getString( R.string.select_category),
                calendarNote );

        // ForeignTextField
        editFieldCategoryName = calendarCategoryKey.addField( R.id.editfieldtext_category_name, CategoriesTable.NAME);

        // ForeignStyleField - will call {@link #onColumnValueChanged(ContentValues)}
        calendarCategoryKey.addStyleField( CategoriesTable.STYLE );
        }

    @Override
    public void onColumnValueChanged(ContentValues values)
        {
        Long style = values.getAsLong( column(CategoriesTable.STYLE));

        Longstyle.override( style,
                editFieldCategoryName );
        }
    }
