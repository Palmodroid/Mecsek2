package digitalgarden.mecsek.color;

import digitalgarden.mecsek.generic.GenericTable;


/**
 * How to create new table?
 * 1. LibraryDatabase.version() - should be incremented (previous table will be deleted)
 * 1a. new table should be added to LibraryDatabase.defineTables(), and its index should be
 * stored in a PUBLIC STATIC INT VAR!
 *         COLOR_DEFS = addTable( new ColorDefsTable() );
 * !! REFERENCED TABLES SHOULD BE ADDED FIRST !!
 * 2. Create new table (ColorTable) - extending GenericTable, and implement missing methods
 * 3. name() should return unique name (name in prular)
 * 4. defineColumns() ...
 * 4a. - all addColumn() methods return column's unique index - store it in a PUBLIC STATIC INT VAR!
 * 5. defineExportImportColumns() ...
 * 5a. - add columns to export with their INDEX
 * GO TO ControllActivity!
 */

/**
 * How to introduce new column type for color?
 * SQLite INTEGER datatype is stored in 1-8 bytes depending on the magnitude of the integer, but
 * are always read as 8-byte signed integer (java long).
 * 0. Increase LibraryDatabase.version() !!
 * 1. Introduce new type and corresponding datatype in GenericTable.java
 *     public static final int TYPE_COLOR = 3;
 *     private static final String[] COLUMN_TYPES = { (exactly 3 previous types), "INTEGER"};
 * !! At this time non-text columns cannot be part of search columns (other special columns?) !!
 * !! At this time no export-import is possible !!
 * !! At this time no examples are possible !!
 *
 * 2. First expand ColorDefsEditFragment.java, and its color_def_edit_fragment_form.xls accordingly!
 * 2a. !! View type of EditField is defined by fragment_form.xls !!
 * 3. EditFieldColor is used temporary (INTEGER Field)
 *
 * 4. Expand ColorDefsListFragment.java, and its color_defs_list_row_layout.xls accordingly!
 *     protected void setupRowLayout()
 *         { addField( R.id.value, ColorDefsTable.VALUE ); ... }
 * 4a. !! View type of Field is definied by list_row_layout.xls !!
 *     <digitalgarden.mecsek.formtypes.ColorView ... />
 * 5. ColorView is used temporary (INTEGER Field)
 * 6. GenericCombinedPart.bindView() should inculde new ColorView
 *      else if ( v instanceof ColorView)
 *          {
 *          long color = cursor.getLong(from[i]);
 *          ((ColorView)v).setColor( color );
 *          }
 *
 *  IMPORTANT!!!
 *
 *  _id is a PRIMARY INTEGER KEY - which is an alias to ROWID
 *  It can be setStyle expicitly to any (unique) integer value, so can be used as index.
 *  It can (but should not) contain 0.
 *
 */
public class ColorDefsTable extends GenericTable
    {
    @Override
    public String name()
        {
        return "colordefs";
        }

    public static int COLOR;
    public static int VALUE;

    @Override
    public void defineColumns()
        {
        COLOR = addColumn( TYPE_TEXT, "color" );
        VALUE = addColumn( TYPE_COLOR, "value");
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( ColorDefsTable.COLOR );
        }

    }

