package digitalgarden.mecsek.tables.category;

import android.content.ContentValues;
import android.widget.LinearLayout;
import android.widget.TextView;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.tables.records.RecordsControllActivity;
import digitalgarden.mecsek.fieldtypes.EditField;
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

    private LinearLayout backgroundCategoryName;
    private TextView titleCategoryName;
    private EditField categoryNameField;

    @Override
    protected void setupFormLayout()
        {
        Scribe.note("CategoriesEditFragment setupFormLayout");

        backgroundCategoryName = getView().findViewById( R.id.background_category_name );
        titleCategoryName = getView().findViewById( R.id.title_category_name );
        categoryNameField = addField(R.id.editfieldtext_category_name, CategoriesTable.NAME);

        addField(R.id.stylebutton_category_style, CategoriesTable.STYLE);

        setupListButton(RecordsControllActivity.class,
                getActivity().getString(R.string.button_record_list),
                getActivity().getString(R.string.records_with),
                categoryNameField);
        }

    public void onColumnValueChanged(ContentValues values)
        {
        Long style = values.getAsLong( column(CategoriesTable.STYLE));

        Longstyle.override( style,
                titleCategoryName,
                categoryNameField,
                backgroundCategoryName );
        }

    }
