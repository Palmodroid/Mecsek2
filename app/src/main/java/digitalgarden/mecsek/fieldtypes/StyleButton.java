package digitalgarden.mecsek.fieldtypes;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;
import digitalgarden.mecsek.color.StylePickerActivity;
import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;

import java.util.List;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


/**
 * Style (as longstyle) is stored in a column (WHICH????). StyleButton defines a Button (WHICH???). Pressing the
 * button starts StylePickerActivity to choose a new style for this column. Changing the column should call (?????)
 * method of the parent GenericEditFragment, to change the colors of different parts.
 */
public class StyleButton extends AppCompatImageButton
        implements Connection.Connectable, GenericEditFragment.UsingSelector
    {
    public StyleButton(Context context)
        {
        super(context);
        }

    public StyleButton(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public StyleButton(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }


    /** Form of the table - should be stored because after returning from selector form should be recolored */
    private GenericEditFragment editFragment;

    /** Style's column - styleButton sets data of this column */
    protected int columnIndex;

    /** TRUE if value of the style was changed */
    private boolean edited = false;

    /** Actual value of the style Column - this is NOT stored by the button */
    private long styleValue = 0L;

    /** Common CODE between Style Button and selectorActivity Request Code
     *  With this common code selectorActivity can identify its calling Field */
    private int selectorCode = -1;


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
        selectorCode = editFragment.getCode();

        setOnClickListener(new OnClickListener()
            {
            @Override
            public void onClick(View v)
                {
                Scribe.note("StyleButton: StylePickerActivity started!");
                Intent intent = new Intent( editFragment.getActivity(), StylePickerActivity.class );
                // intent.putExtra( GenericControllActivity.TITLE, selectorTitle + selectorOwner.getText() );
                // intent.putExtra( GenericCombinedListFragment.SELECTED_ITEM, getValue() );
                editFragment.startActivityForResult( intent, selectorCode );
                }
            });

        // Could be set by an independent "hint" parameter
        // !!! https://android-developers.googleblog.com/2009/05/drawable-mutations.html - use .mutate() !!
        setValue( 0L );
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

    /** Style is added to PUSH of MAIN Connection. */
    @Override
    public void addDataToPush(ContentValues values)
        {
        values.put( column( columnIndex ), styleValue);
        }

    /** Styles do not have any SOURCE (only externKeys has got sources */
    @Override
    public void pushSource(int tableIndex, long rowIndex){ }

    /** Save style value during config changes. */
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

    @Override
    public boolean isEdited()
        {
        return edited;
        }

    /**
     * Activity (when SelectorActivity returns) checks here which Field called SelectorActivity. If selector
     * code is identical then value is set.
     * @param selectorCode unique selector code (returned by Selector activity)
     * @param data data returned by ACtivity containing selected style
     */
    @Override
    public void checkReturningSelector( int selectorCode, Intent data )
        {
        Scribe.locus();

        if (this.selectorCode == selectorCode)
            {
            long selectedStyle = data.getLongExtra( StylePickerActivity.LONGSTYLE_KEY,0L);

            if ( selectedStyle != styleValue)    edited = true;

            // To recolor all necessary fields setValue() should be called always! Color of the same index could be
            // changed
            setValue( selectedStyle );
            }
        }
    }
