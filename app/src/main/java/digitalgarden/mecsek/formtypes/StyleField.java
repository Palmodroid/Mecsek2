package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import digitalgarden.mecsek.color.StylePickerActivity;
import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;

import java.util.List;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.viewutils.Longstyle.SELECTOR_BLUE;


/**
 * StyleField is very similar to {@link StyleButton} but no style selection is allowed. It can be used in
 * {@link ForeignKey}-s to connect record with foreign styles. Style changes if ForeignKey changes.
 * <p>Style is stored in a (mostly foreign) column. StyleField only connects this column to the form, and any changes
 * (of the ForeignKey) will trigger {@link GenericEditFragment#onColumnValueChanged(ContentValues)} call.</p>
 */
public class StyleField implements Connection.Connectable
    {
    /** Form of the table - should be stored because after returning from selector form should be recolored */
    private GenericEditFragment editFragment;

    /** Style's column - styleButton sets data of this column */
    protected int columnIndex;

    /** Actual value of the style Column - this is NOT stored by the button */
    private long styleValue = 0L;


    /**
     * Set a new value means: Setting up styleValue (not pushed to database yet) and
     * call {@link GenericEditFragment#onColumnValueChanged(ContentValues)} to refresh colors of the corresponding
     * fields.
     * <p>Important! First change (during pull) happens, when {@link GenericEditFragment#setupFormLayout()} is
     * finished, so all fields are ready</p>
     * @param newStyle new style
     */
    public void setValue( long newStyle )
        {
        styleValue = newStyle;

        // ContentValues are used (as inSQLite) to let later developments
        // KEY is the given STYLE COLUMN - Several style columns can be used
        ContentValues values = new ContentValues();
        values.put( column(columnIndex), styleValue);
        editFragment.onColumnValueChanged( values );
        }

    /**
     * Connect styleButton with column (database).
     * <p>StyleButton should be added to Connection to be connected to database! Table Id is stored inside Connection.</p>
     * <p>Click is defined here - click will start {@link StylePickerActivity} to pick style.</p>
     * @param form form ({@link GenericEditFragment} of the styleButton
     * @param columnIndex style's column stored inside styleBUtton
     */
    public void connect(final GenericEditFragment form, Connection connection, int columnIndex)
        {
        // column index (or indices) are stored inside field
        // table index (only one) is stored inside connection
        // so connect will add both data at the same place, connection.add is not needed as a separate row

        this.editFragment = form;
        this.columnIndex = columnIndex;
        connection.add( this );

        // Could be set by an independent "hint" parameter
        // !!! https://android-developers.googleblog.com/2009/05/drawable-mutations.html - use .mutate() !!
        setValue( SELECTOR_BLUE );
        }


    /** Adds style column to the Connection (to the projecton to get column's data) of the MAIN table. */
    @Override
    public void addColumnToProjection(List<String> projection)
        {
        projection.add( column( columnIndex ) );
        }


    /** Style is PULLED from MAIN Connection. Theoretically cannot be null, but null validation (coming from
     * ForeignKey) is still there: 0L longstyle means: not defined, default values are returned */
    @Override
    public void getDataFromPull(Cursor cursor)
        {
        int column = cursor.getColumnIndexOrThrow(column( columnIndex ));
        setValue( cursor.isNull( column ) ? 0L : cursor.getLong(column) );
        }

    /** StyleField cannot be changed, so PUSH is not needed */
    @Override
    public void addDataToPush(ContentValues values) { }

    /** Styles do not have any SOURCE (only externKeys has got sources */
    @Override
    public void pushSource(int tableIndex, long rowIndex){ }

    /** Save style value during config changes (only pulled, never changed values) */
    @Override
    public void saveData(Bundle data)
        {
        data.putLong( column( columnIndex ), styleValue);
        }

    /** Retrieve style value on config changes
     *  Retrieve happens after {@link GenericEditFragment#setupFormLayout()}, so all Fields will be ready */
    @Override
    public void retrieveData(Bundle data)
        {
        setValue(data.getLong( column(columnIndex)) );
        }

    /** EditorField cannot be edited, it just helps to change the look of the form */
    @Override
    public boolean isEdited()
        {
        return false;
        }
    }
