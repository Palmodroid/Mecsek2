package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericTable;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


/**
 * <p>Extern key points to a row in an extern (foreign) table. Once extern key is defined, it will never change! Extern
 * columns are like extern columns of the main table - they always remain at the same position, but the values can be
 * changed. </p>
 * <p>(Foreign key on the contrary: selects one record from the foreign table. Foreign key can change to select
 * another record, but the value of the record will not change (on foreign key changes.) </p>
 * <p>MAIN TABLE - extern key column (always fix) --> EXTERN TABLE - extern columns (value can change)</p>
 * <p>MAIN TABLE - foreign key column (can change to select other record) --> FOREIGN TABLE - foreign columns (fix
 * value) </p>
 * <p>EXTERN TABLEs can contain records from different MAIN TABLEs. SOURCE COLUMNS can identify the "parent" table,
 * to reach original data in different tables. (SQLite itself cannot identify different tables)</p>
 * <p>EXTERN KEY itself is {@link Connection.Connectable} and connects to the Connection of the Form. EXTERN KEY
 * contains an other {@link Connection} for the extern table.</p>
 */
public class ExternKey implements Connection.Connectable
    {
    private static final long EXTERN_INACTIVE = -2L;
    private static final long EXTERN_CREATE = -1L;

    /** Connects columns of the extern table to the database */
    private Connection externConnection;

    /** Form containing ExternKey (and its fields) */
    private GenericEditFragment editFragment;

    /** Extern table */
    private int externTableIndex;

    /** Column containing extern key inside main table */
    private int externKeyColumnIndex;

    /**
     * Value of the Extern Key Column (Points to the Extern Record of Extern Table, and cannot be changed)
     * <p><em>INACTIVE</em> - Extern Record is not yet loaded (it will be loaded during PULL process if exists) </p>
     * <p><em>CREATE</em> - If Extern Record does not exist then it should be created during the PUSH process </p>
     * <p><em>extern index</em> - Index (id) of the Extern Record in the Extern Table </p>
     */
    private long externKeyValue = EXTERN_INACTIVE;


    /**
     * Creates an ExternKey. Fields of the ExternTable can be added later to this ExternKey.
     * @param editFragment form containing fields (fields are added later to ExternKey)
     * @param externKeyColumnIndex index of the extern key column (inside main table) - value will never change!
     * @param externTableIndex index of the extern table
     */
    public ExternKey(GenericEditFragment editFragment, int externKeyColumnIndex, int externTableIndex )
        {
        this.externTableIndex = externTableIndex;
        this.externKeyColumnIndex = externKeyColumnIndex;
        this.editFragment = editFragment;

        externConnection = new Connection( editFragment.getContext(), this.externTableIndex);

        // !!! EZT KI KELL INNEN VENNI, HA NEM AKARJUK, HOGY MINDIG KÉSZÍTSEN EGY EXTERNKEY-T !!!
        createExtern();
        }


    /** Adds ExternKey column to the Connection of the MAIN table. EXTERN KEY is the only column needed from MAIN
     * TABLE. */
    @Override
    public void addColumnToProjection(List<String> projection)
        {
        projection.add( column(externKeyColumnIndex) );
        }

    /**
     * Adds an EDIT FIELD (any subclass) to an EXTERN COLUMN of the EXTERN TABLE. Value of the EXTERN COLUMN can
     * change, but EXTERN KEY will always remain the same, pointing to the same record of the EXTERN TABLE.
     * @param editFieldId id of the field
     * @param externColumnIndex index of the column (inside extern table)
     * @return the created, connected editField
     */
    public EditField addEditField(int editFieldId, int externColumnIndex )
        {
        EditField editField = (EditField) editFragment.getView().findViewById( editFieldId );
        editField.connect( editFragment, externConnection, externColumnIndex );
        // externConnection.add( editField ); added to connect, not needed any more

        return editField;
        }


    /**
     * ForeignKey is a special "field" without an existing cell on the form. ForeignKey is tied to the column of the
     * MAIN table (defined by foreignKeyColumnIndex) which value is the id of a record of the FOREIGN table (defined
     * by foreignTableIndex).
     * <p>As there is no field cell, connection of Form, Column (and foreignTableIndex) is performed inside
     * ForeignKey constructor. ForeignKey should be added to {@link #connection} </p>
     * <p>Value of ForeignKey (row id of the foreign table) is selected by the use of an external ControllActivity
     * (Selector). Because of the use of the Selector ForeignKey should be added to {@link #usingSelectors} </p>
     * !!! NOT READY !!!
     * @param foreignKeyColumnIndex
     * @param foreignTableIndex
     * @param selectorActivity
     * @param selectorTitle
     * @param selectorTitleOwner
     * @return
     */
    public ForeignKey addForeignKey( int foreignKeyColumnIndex, int foreignTableIndex,
                                     Class<?> selectorActivity, String selectorTitle, TextView selectorTitleOwner )
        {
        ForeignKey foreignKey = new ForeignKey( editFragment, foreignKeyColumnIndex, foreignTableIndex );
        externConnection.add( foreignKey );

        foreignKey.setupSelector( selectorActivity, selectorTitle, selectorTitleOwner );
        editFragment.addFieldUsingSelector( foreignKey );

        return foreignKey;
        }


    /**
     * Extern record will be created during PUSH process if it does not yet exist
     * <p>This command is part of the constructor, so extern record will be created automatically for each main
     * record. This command could be called by the form (automatically or on button-tap).</p>
     */
    public void createExtern()
        {
        if ( externKeyValue == EXTERN_INACTIVE )
            externKeyValue = EXTERN_CREATE;
        }

    /**
     * Pulls data from database during the pull process of the form's {@link Connection}
     * <p>If column exists (ExternRecord already defined) then {@link #externKeyValue} is pulled (value never
     * changes!), and pull of extern Connection is started. Pulls from the Extern Table, row is defined by
     * externKeyValue.</p>
     * <p>If column doesn't exist (No Extern Record exists) then {@link #externKeyValue} will be INACTIVE (or remains
     * CREATE, if it should be created during push process. </p>
     * @param cursor cursor containing value of Extern Key Column (projection defined by {@link #addColumnToProjection(List)}
     */
    @Override
    public void getDataFromPull(Cursor cursor)
        {
        int column = cursor.getColumnIndexOrThrow(column(externKeyColumnIndex));
        if (cursor.isNull(column))
            {
            if ( externKeyValue != EXTERN_CREATE )
                externKeyValue = EXTERN_INACTIVE;
            }
        else
            {
            externKeyValue = cursor.getLong(column);
            externConnection.pullData( externKeyValue);
            }
        }


    /**
     * Performs PUSH on externConnection (all columns/fields of the Extern Record are pushed) if extern Key is not
     * EXTERN_INACTIVE. Extern Record will be updated or created (if ExternKey == EXTERN_CREATE). ExternKeyValue is
     * updated to contain extern index after create.
     * If extern record is exist then value of ExternKey is added to the main PUSH process of the form (Main table
     * push). If no Extern Key exists (EXTERN_INACTIVE) then null is pushed into ExternKey Column.
     * @param values values of the MAIN table PUSH (ExternKey value should be added, if not inactive)
     */
    @Override
    public void addDataToPush(ContentValues values)
        {
        if ( externKeyValue != EXTERN_INACTIVE )
            {
            externKeyValue = externConnection.pushData(externKeyValue);
            }

        if (externKeyValue >= 0L)
            values.put(column(externKeyColumnIndex), externKeyValue);
        else
            values.putNull(column(externKeyColumnIndex)); // Does it really needed?
        }


    /**
     * Each Extern Record has got only one source. These sources can be in different tables. SQLite itself cannot
     * store table data. SOURCE means that Extern Record stores its SOURCE_TABLE and its SOURCE_ROW in it. With these
     * data original record can be found in any table.
     * <p>Important! EXTERN TABLE should contain source columns! These can be added by
     * {@link GenericTable#addSourceColumns()} </p>
     * <p>If tha MAIN and EXTERN record is created together then no MAIN row information is available at the time of
     * the creation of the EXTERN record.</p>
     * <ul> Check {@link Connection#pushData(long)}!
     * <li>PUSH of the MAIN Connection collects data from its Connectables (Fields)</li>
     * <li>Data collection of EXTERN KEY triggers PUSH of the EXTERN Connection</li>
     * <li>EXTERN Connection collects data from its Connectables (Fields) and creates EXTEN RECORD first</li>
     * <li>After Data collection of the MAIN Connection MAIN Record is created (already containing row index of
     * EXTERN RECORD inside ExternKey Column) </li>
     * <li>EXTERN RECORD should be updated with the row index of the MAIN record (which was created later)</li>
     * </ul>
     *
     * @param sourceTableIndex MAIN table index coming from MAIN {@link Connection#pushData(long)}
     * @param sourceRowIndex MAIN Record row index coming from MAIN {@link Connection#pushData(long)}
     */
    @Override
    public void pushSource( int sourceTableIndex, long sourceRowIndex )
        {
        if (externKeyValue >= 0L && table(externTableIndex).hasSourceColumns())
            {
            ContentValues values = new ContentValues();

            values.put(
                    column( table(externTableIndex).SOURCE_TABLE),
                    (long) (table(sourceTableIndex).id()));
            values.put(
                    column( table(externTableIndex).SOURCE_ROW),
                    sourceRowIndex);

            try
                {
                editFragment.getContext().getContentResolver().update(getItemContentUri(), values, null, null);
                }
            catch (Exception e)
                {
                Toast.makeText(editFragment.getContext(), "ERROR: Update source of item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
                }
            }
        }

    /** ItemContentUri of the pointed EXTERN RECORD in the EXTREN TABLE (this is the record EXTERN KEY is pointing at */
    private Uri getItemContentUri( )
        {
        return table(externTableIndex).itemContentUri( externKeyValue );
        // return Uri.parse( table( externTableIndex ).contentUri() + "/" + rowIndex );
        }


    /**
     * ExternKey class is not a custom view, so it won't save its data during config changes.
     * Value of ExternKey column should be saved here.
     * All fields of externConnection can save their data here, too.
     */
    @Override
    public void saveData(Bundle data)
        {
        data.putLong( column(externKeyColumnIndex), externKeyValue );
        externConnection.saveData( data );
        }


    /**
     * ExternKey class is not a custom view, so it won't save its data during config changes.
     * Value of ExternKey column should be retrieved here.
     * All fields of externConnection can retrieve their data here, too.
     */
    @Override
    public void retrieveData(Bundle data)
        {
        externKeyValue = data.getLong( column(externKeyColumnIndex) );
        externConnection.retrieveData( data );
        }


    /**
     * Return true if any of the fields of extern connection was edited.
     * Extern Key itself cannot be changed (created at most).
     * Result of isEdited() is returned to the isEdited() check of the MAIN Connection. If any of the fields was
     * edited then true is returned nad confirmation dialog is shown during cancel.
     * @return true if any of the fields of the extern connection was edited
     */
    @Override
    public boolean isEdited()
        {
        // extern key cannot be changed (created at most)
        return externConnection.isEdited();
        }
    }
