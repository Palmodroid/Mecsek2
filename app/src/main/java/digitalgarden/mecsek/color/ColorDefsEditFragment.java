package digitalgarden.mecsek.color;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericEditFragment;
import static digitalgarden.mecsek.tables.LibraryDatabase.COLOR_DEFS;

/**
 * 1. Create new EditFragment extending GenericEditFragment and implement missing methods
 * 2. defineTableIndex() should return table index generated by LibraryDatabase.java
 * 3. color_def_edit_fragment_form.xml should be generated (see it there) ...
 * 3a. strings should be added to library_strings.xml
 * 3b. and returned by defineFormLayout()
 *         return R.layout.color_def_edit_fragment_form;
 * 4. setupFormLayout() should contain addEditField() methods to join fields to database columns
 *         addEditField( R.id.edittext_color, ColorDefsTable.COLOR );
 *
 * ControllActivity.createEditFragment() should return new ...EditFragment();
 */
public class ColorDefsEditFragment extends GenericEditFragment
    {
    @Override
    public int defineTableIndex()
        {
        return COLOR_DEFS;
        }

    @Override
    protected int defineFormLayout()
        {
        return R.layout.color_def_edit_fragment_form;
        }

    @Override
    protected void setupFormLayout()
        {
        addField( R.id.edittext_color, ColorDefsTable.COLOR );
        addField( R.id.edittext_value, ColorDefsTable.VALUE );
        }
    }
