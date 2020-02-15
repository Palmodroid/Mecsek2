package digitalgarden.mecsek.tables.category;

import android.content.ContentValues;
import android.widget.TextView;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.records.RecordsControllActivity;
import digitalgarden.mecsek.formtypes.EditField;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.viewutils.Longstyle;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.tables.LibraryDatabase.CATEGORIES;


public class CategoriesEditFragment extends GenericEditFragment
    {
    @Override
    public int defineTableIndex()
        {
        return CATEGORIES;
        }

    @Override
    protected int defineFormLayout()
        {
        return R.layout.category_edit_fragment_form;
        }

    private TextView textViewStyle;

    @Override
    protected void setupFormLayout()
        {
        Scribe.note("CategoriesEditFragment setupFormLayout");

        EditField categoryNameField = addEditField(R.id.editfieldtext_category_name, CategoriesTable.NAME);
        addStyleButton(R.id.stylebutton_category_style, CategoriesTable.STYLE);

        textViewStyle = getView().findViewById( R.id.textview_style );

        setupListButton(RecordsControllActivity.class,
                getActivity().getString(R.string.button_record_list),
                getActivity().getString(R.string.records_with),
                categoryNameField);
        }

    public void onColumnValueChanged(ContentValues values)
        {
        Long style = values.getAsLong( column(CategoriesTable.STYLE));

        // Style has changed
        if ( style != null )
            {
            Longstyle longstyle = new Longstyle( getContext(), style );
            textViewStyle.setTextColor( longstyle.getInkColor() );
            textViewStyle.invalidate();
            }
        }

    }
