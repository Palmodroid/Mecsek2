package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;


import java.util.List;

import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


/**
 * SourceFieldButton is a "field" on the form connected to the SOURCE columns of an EXTERN TABLE. SourceFieldButton
 * should be placed on the Form of an Extern table. Tapping the Button will bring us to the MAIN table - the original
 * data of the EXTERN RECORD. Several MAIN tables can be reached from the same EXTERN table.
 * <p>Be careful! During this activity (activity of THIS table) THIS is the MAIN table.</p>
 * <p>But THIS table is also the EXTERN table of an OTHER TABLE. SOURCE columns of THIS (currently MAIN) table
 * identify the other ORIGIN table.</p>
 * <pre>
 * MAIN table   --> EXTERN table
 *              <-- SOURCE columns inside EXTERN TABLE
 *
 *                  MAIN table (in its own activity)
 * ORIGIN table <-- containing SOURCE columns
 * </pre>
 */
public class SourceButton extends AppCompatButton implements Connection.Connectable
    {
    /** Form of the button */
    private GenericEditFragment editFragment;

    /** Index of the ORIGIN table */
    private int sourceTableIndex;
    /** Index of the ORIGIN record inside ORIGIN table */
    private long sourceRowIndex;


    public SourceButton(Context context)
        {
        super(context);
        }

    public SourceButton(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public SourceButton(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }


    /**
     * Connects button to the database.
     * <p>If record is newly created (rowId < 0) then it has got no MAIN/ORIGIN record, so button can be GONE.</p>
     * <p>Otherwise onClickListener is set to start ORIGIN table's ControlActivity to show ORIGIN Record</p>
     * <p>If no SOURCE information can be found, (no source columns, or no ORIGIN record) then Button is set GONE in
     * {@link #getDataFromPull(Cursor)} so sourceTableIndex check could be redundant </p>
     * @param editFragment form of the button
     */
    public void connect(final GenericEditFragment editFragment, Connection connection )
        {
        this.editFragment = editFragment;
        connection.add( this );

        // Ha még csak most hozzuk létre, akkor nem lehet source!
        if ( editFragment.getRowIndex() < 0 )
            {
            setVisibility(View.GONE);
            }
        else
            {
            setOnClickListener(new View.OnClickListener()
                {
                public void onClick(View view)
                    {
                    if (sourceTableIndex >= 0L) // getControllACtivity-t kell vajon ellenőrizni?
                        {
                        Intent i = new Intent();

                        i.setClass(
                                SourceButton.this.editFragment.getContext(),
                                table(sourceTableIndex).getControllActivity());
                        i.putExtra(
                                GenericEditFragment.EDITED_ITEM, sourceRowIndex);
                        SourceButton.this.editFragment.startActivity(i);
                        }
                    }
                });
            }
        }


    /**
     * SOURCE Columns to pull from THIS (MAIN) table.
     * @param projection projection to add SOURCE columns
     */
    @Override
    public void addColumnToProjection(List<String> projection)
        {
        projection.add( column(table(editFragment.defineTableIndex()).SOURCE_TABLE));
        projection.add( column(table(editFragment.defineTableIndex()).SOURCE_ROW));
        }


    /**
     * Value of SOURCE Columns are loaded into {@link #sourceTableIndex} and {@link #sourceRowIndex}
     * If no value can be found, then Button is GONE
     * @param cursor cursor containing values for columns defined by {@link #addColumnToProjection(List)}
     */
    @Override
    public void getDataFromPull(Cursor cursor)
        {
        int column;

        column = cursor.getColumnIndexOrThrow(column(table(editFragment.defineTableIndex()).SOURCE_TABLE));
        sourceTableIndex = cursor.isNull(column) ? -1 : cursor.getInt(column);

        column = cursor.getColumnIndexOrThrow(column(table(editFragment.defineTableIndex()).SOURCE_ROW));
        sourceRowIndex = cursor.isNull(column) ? -1L : cursor.getLong(column);

        if ( sourceRowIndex == -1L || sourceTableIndex == -1 )
            {
            setVisibility( View.GONE );
            }
        }

    @Override
    public void addDataToPush(ContentValues values)
        {
        // These data cannot be pushed; source is set by the source table
        }

    @Override
    public void pushSource(int tableIndex, long rowIndex)
        {
        // Not needed here
        }

    /** Save data during config changes. Data is not part of the View. */
    @Override
    public void saveData(Bundle data)
        {
        data.putInt( column(table(editFragment.defineTableIndex()).SOURCE_TABLE),
                sourceTableIndex );
        data.putLong( column(table(editFragment.defineTableIndex()).SOURCE_ROW),
                sourceRowIndex );
        }

    /** Retrieve data during config changes. Data is not part of the View. */
    @Override
    public void retrieveData(Bundle data)
        {
        sourceTableIndex = data.getInt( column(table(editFragment.defineTableIndex()).SOURCE_TABLE) );
        sourceRowIndex = data.getLong( column(table(editFragment.defineTableIndex()).SOURCE_ROW) );
        }

    /** Source Button cannot be edited */
    @Override
    public boolean isEdited()
        {
        // source button cannot be changed
        return false;
        }
    }
