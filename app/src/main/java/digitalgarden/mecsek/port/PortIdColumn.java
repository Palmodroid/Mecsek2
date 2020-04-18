package digitalgarden.mecsek.port;

import android.content.ContentValues;
import android.database.Cursor;
import digitalgarden.mecsek.database.DatabaseMirror;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;
import digitalgarden.mecsek.utils.Utils;

import java.util.Iterator;
import java.util.List;

import static digitalgarden.mecsek.Debug.PORT;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;


/**
 * Id column needs special treatment.
 * <p>Each table has an id column, so full names are needed in projections. PortIdColumn needs the table.</p>
 * TODO!! {@link DatabaseMirror#columns} could store id columns and their tables as well
 * <ul>
 * <li><em>Projection</em> - adds FULL ID column name</li>
 * <li><em>Export</em> - adds ONE column data (as string)</li>
 * <li><em>Import</em> - gets ONE column data / pairs ONE known column name</li>
 * </ul>
 */
public class PortIdColumn implements GenericPortColumn
    {
    PortRecord portRecord;


    /** ID column needs it's {@link PortRecord} to identify full (table) name, and to call {@link PortRecord#setIdColumnValue(long)}
     *  to UPSERT this record instead of INSERT */
    PortIdColumn( PortRecord portRecord )
        {
        this.portRecord = portRecord;
        }


    /** ID column's FULL name should be added to projection (there can be sevaral ID columns in one projection) */
    @Override
    public void addToProjection( List<String> projection)
        {
        Scribe.debug(PORT, "Column (DATA) " + getKeyColumnName() + " is added to projection");

        projection.add( getKeyColumnName() );
        }


    /**
     * Cursor should be set to the current record.
     * ID Column's value (from the record) is added to export (strings)
     */
    @Override
    public void addToExport(Cursor cursor, List<String> data)
        {
        data.add( Utils.convertLongToString( cursor.getLong(cursor.getColumnIndexOrThrow( getKeyColumnName() ))));
        // Ha hibás, akkor hol lesz jelzés??
        }


    /**
     * Returns key column name. It is the FULL ID COLUMN NAME used by {@link #addToProjection(List)} and
     * {@link #addToExport(Cursor, List)}
     */
    @Override
    public String getKeyColumnName()
        {
        return columnFull_id( portRecord.table.id() );
        }


    /**
     * Exported value is read from the next data string and added to values to create the record from these values
     * ID cannot be null! Records are created without joined tables - only ONE _id column is allowed. Simple _id name
     * is used.
     * @return true if data is valid, false if data is null or missing
     */
    @Override
    public boolean getFromImport(ContentValues values, Iterator<String> data)
        {
        if ( data.hasNext() )
            {
            String word = StringUtils.revertFromEscaped(data.next());
            if (word != null)
                {
                // TODO Check if parse returns error!
                portRecord.setIdColumnValue( Utils.convertStringToLong(word) );
                // id values are NOT added to ContentValues, but set inside POrtRecord, to UPSERT and not insert record
                // values.put( column_id(), Long.parseLong( word ));
                return true;
                }
            }
        return false;
        }


    @Override
    public void onRecordCreated(long rowIndex)
        {
        // DO NOTHING, NO FURTHER CHANGES ARE NEEDED
        }
    }

