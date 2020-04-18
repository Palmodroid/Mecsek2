package digitalgarden.mecsek.port;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import digitalgarden.mecsek.generic.GenericTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


/**
 * PortRecord is the base class for each record to port.
 * <p>It contains<ul>
 * <li>{@link #table} - the table of the record - added by constructor,</li>
 * <li>{@link #columnList} - the columns of the record - added by {@link #addColumn(GenericPortColumn)}.</li>
 * </ul></p>
 * <p><em>Record EXPORT</em> Very similar to the LIST - all (even joined) tables are queried to the columns listed in
 * projection. Value of each (even foreign or extern) columns containing data can be written to file.</p>
 * <p><ul>
 * <li>{@link #getProjection()} - collects each (even joined) columns to create projection</li>
 * <li>{@link #createExportFromRecord(Cursor)} - creates string from data of the record (using projection) </li>
 * </ul></p>
 * <p><em>Record IMPORT</em> Very similar to the FORM (EDIT) - Columns of the table are read and will be created (if
 * containing real data). Foreign keys: record in the referenced table is selected and its row index is stored as key.
 * Extern keys: record in the referenced table is created and its row index is stored as key. Recursion is needed as
 * extern/foreign tables can contain further foreign keys.
 * <p><ul>
 * <li>{@link #createRecordFromImport(Iterator)} - creates record from data string</li>
 * </ul></p>
 */
public class PortRecord
    {
    // Table of the record (Context: table.port().getContext())
    protected GenericTable table;

    // Columns in this record
    //  PortDataColumn
    //  PortForeignKey
    //  PortExternKey
    protected List<GenericPortColumn> columnList = new ArrayList<>();

    /** Id columns should be specially treated.
     *  idColumnValue is cleared before import {@link #getColumnValuesFromImport(Iterator)}
     *  {@link PortIdColumn} sets idColumnValue, if id column was exported. Records with ID-s should be UPSERT instead of INSERT
     *  AFTER record creation it contains the row id of the newly created record (by {@link #createRecordFromImport(Iterator)} */
    protected long idColumnValue = -1L;


    /**
     * @param table table containing these records ( table id: .id(), context: getContext() )
     * columns are added by {@link #addColumn(GenericPortColumn)}
     */
    public PortRecord( GenericTable table )
        {
        this.table = table;
        }


    /**
     * Adds columns of the table as PortDataColumn, PortForeignKey or PortExternKey
     * @param column of the table
     */
    public void addColumn( GenericPortColumn column )
        {
        columnList.add( column );
        }


    /** ALL RECORDS ARE QUERIED FROM TABLE TOGETHER **/


    /**
     * Creates projection from all columns including joined (foreign, extern) columns
     * Table level performs query with this projection
     */
    public List<String> getProjection()
        {
        List<String> projection = new ArrayList<>();

        for (GenericPortColumn column : columnList )
            {
            column.addToProjection( projection );
            }

        return projection;
        }

    /**
     * Creates list of strings from one record of the cursor (queried on table level, using projection given by
     * {@link #getProjection()}
     * @param cursor Result of the query on table level.
     */
    public List<String> createExportFromRecord( Cursor cursor )
        {
        List<String> data = new ArrayList<>();

        for (GenericPortColumn column : columnList )
            {
            column.addToExport( cursor, data );
            }

        return data;
        }

    /** EACH RECORDS ARE CREATED INDIVIDUALLY FROM EACH IMPORTED LINE **/


    /**
     * Collects value from data string for EACH column.
     * @return Values in ContentValues format, or NULL if NONE of the values is valid
     */
    protected ContentValues getColumnValuesFromImport( Iterator<String> data )
        {
        // eachNull is true, if EACH column contains NULL - so record doesn't exists
        boolean eachNull = true;
        idColumnValue = -1L;

        ContentValues values = new ContentValues();

        for (GenericPortColumn column : columnList )
            {
            if ( column.getFromImport( values, data ) ) // TRUE if data is valid
                eachNull = false;                       // eachNull becomes false, if any data is valid
            }

        return eachNull ? null : values;
        }


    /**
     * Id columns should be treated specially - records with id should be UPSERT instead of INSERT. Previous record
     * with the same id will be overwritten.
     * <p>{@link PortIdColumn} calls its {@link PortRecord} to set id. {@link #createRecordFromImport(Iterator)} will
     * use UPSERT with the given id, instead of simple INSERT. </p>
     * @param id value of the id column
     */
    public void setIdColumnValue( long id )
        {
        idColumnValue = id;
        }


    /**
     * Creates record from data strings
     * <p>First "word" of data line identifies table - {@link PortTable#importRow(int, String[])} skips it.</p>
     * <p>Record is created using columns of {@link #columnList}.</p>
     * <p>Table of the record is identified by {@link #table}.</p>
     * @param data
     * @return
     */
    public long createRecordFromImport( Iterator<String> data )
        {
        ContentValues values = getColumnValuesFromImport( data );

        // None of the columns contains valid data - Record doesn't exists
        if ( values == null )
            return -1L;

        // id was set - UPSERT should be used!
        // INSERT : insert with DIRID
        // UPSERT : insert with ITEMID
        // (!! Currently update with DIRID, and added _id into ContentValues also works as UPSERT)
        Uri destination = idColumnValue >= 0 ? table( table.id() ).itemContentUri(idColumnValue) :
                table( table.id() ).contentUri();

        // TÁROLÁS (insert+DIRID - INSERT / insert+ITEMID (for idColumn set) - UPSERT
        Uri uri =
                table.port().getContext().getContentResolver()
                        .insert( destination, values );
        idColumnValue = ContentUris.parseId(uri);

        for (GenericPortColumn column : columnList )
            {
            column.onRecordCreated( idColumnValue );
            }

        return idColumnValue;
        }
    }
