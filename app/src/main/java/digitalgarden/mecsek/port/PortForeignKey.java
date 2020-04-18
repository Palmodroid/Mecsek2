package digitalgarden.mecsek.port;

import android.content.ContentValues;
import android.database.Cursor;
import digitalgarden.mecsek.generic.GenericTable;
import digitalgarden.mecsek.scribe.Scribe;

import java.util.*;

import static digitalgarden.mecsek.Debug.PORT;
import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.generic.GenericTable.TYPE_STYLE;


/**
 * PortForeignKey refers to a foreign key column (identified by int foreignKeyIndex). Foreign Key refers to a foreign
 * record (row index is the foreign key value) inside a foreign table. The foreign record is stored by
 * {@link PortRecord}.
 * <ul>
 * <li><em>Projection</em> - adds EACH FOREIGN column name from the foreign table </li>
 * <li><em>Export</em> - adds EACH FOREIGN column data (as strings)</li>
 * <li><em>Import</em> - gets ONE (FOREIGN KEY VALUE) column data / pairs ONE (FOREIGN KEY) column name</li>
 * Important! Import finds foreign record by imported foreign data, BUT inserts only the row index of the found forign
 * record as foreign key!
 * </ul>
 */
public class PortForeignKey extends PortRecord implements GenericPortColumn
    {
    /** Result of {@link #findRow(GenericTable, ContentValues)} - Foreign record referenced, but doesn't exist!! */
    private long ID_MISSING = -2L;

    /** Result of {@link #findRow(GenericTable, ContentValues)} - No Foreign record is referenced (Foreign Key is NULL) */
    private long ID_NULL = -1L;


    // Foreign Key column index - inside MAIN table
    int foreignKeyIndex;


    public PortForeignKey(int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        // superclass is the Foreign Record inside Foreign Table (referenced by Foreign Key inside MAIN table
        // FOREIGN TABLE is added to superclass
        super( table(foreignTableIndex) );

        // FOREIGN COLUMNS are added to superclass
        for ( int index : foreignColumnIndices )
            {
            addColumn( new PortDataColumn( index ) );
            }

        /* More FOREIGN COLUMNS can be added by {@link #addColumn(GenericPortColumn)} */

        this.foreignKeyIndex = foreignKeyIndex;
        }

    /**
     * Each foreign column name (stored inside {@link PortRecord#columnList} is added to projection
     * ForeignKey (inside MAIN) is NOT added to projection!!
     */
    @Override
    public void addToProjection(List<String> projection)
        {
        Scribe.debug(PORT, "Column (FOREIGN KEY) " + column(foreignKeyIndex) + " adds extern columns to projection");

        for (GenericPortColumn column : columnList )
            {
            column.addToProjection( projection );
            }

        }


    /**
     * Each foreign column data (stored inside {@link PortRecord#columnList} is exported
     * ForeignKey (inside MAIN) is NOT exported (row index of Foreign Record)!!
     */
    @Override
    public void addToExport(Cursor cursor, List<String> data)
        {
        for (GenericPortColumn column : columnList )
            {
            column.addToExport( cursor, data );
            }
        }


    /** Returns name of ForeignKey column inside MAIN table */
    @Override
    public String getKeyColumnName()
        {
        return column( foreignKeyIndex );
        }


    /** Finds foreign record (using exported data) and adds record's id as ForeignKey to values
     *  @return true if data is valid, false if data is null or missing */
    @Override
    public boolean getFromImport(ContentValues values, Iterator<String> data)
        {
        // 1. Import data for each foreign column - data can be {@link GenericTable#COLUMN_TYPES} INTEGER or TEXT
        ContentValues foreignValues = getColumnValuesFromImport( data );

        // 2. No such record - EACH Column is NULL
        // Same as ID_NULL (any of the values is null)
        if ( foreignValues == null )
            {
            values.putNull(column(foreignKeyIndex));
            return false;
            }

        // 3. try to find matching row (using previously prepared foreignValues) inside foreign table
        long row = findRow( table, foreignValues);

        // Result: no such row - record is missing!!
        if ( row == ID_MISSING )
            {
            Scribe.note( "Item does not exists! Row was skipped.");
            return false;
            }

        // row id is null - that is values were empty
        // In this case not ALL, but ANY NULL value results in ID_NULL
        if ( row == ID_NULL )
            {
            values.putNull(column(foreignKeyIndex));
            return false;
            }

        // row id is valid - foreign row will be added to values (coming through parameters)
        else
            {
            values.put(column(foreignKeyIndex), row);
            return true;
            }
        }


    /*
    A keresőrutin bizonyos mezők alapján keres.
    Tegyük be az egész hóbelevancot egy ContentValues tömbbe, ahol a KEY értékeknek megfelelő
    oszlopokban a VALUE értéknek kell szerepelnie.
    */

    /**
     * Searches foreignTable for a record matching foreignColumnValues.
     * <p>Values can be String(TEXT) or Long(INTEGER).</p>
     * @param foreignTable where to search the record
     * @param foreignColumnValues data of the record to match
     * @return ID_MISSING, ID_NULL, or row id of the found record
     */
    long findRow(GenericTable foreignTable, ContentValues foreignColumnValues)
        {
        // NULL ellenőrzés vajon szükséges?
        long row = ID_MISSING;

        // ContentValues cannot be iterated, valueSet() returns this set, which is iterable
        Set< Map.Entry<String, Object> > valueSet = foreignColumnValues.valueSet();

        String[] projection = new String[valueSet.size() + 1];
        StringBuilder selection = new StringBuilder();

        int i = 0;
        projection[i++] = column_id();
        for ( Map.Entry<String, Object> entry : valueSet )
            {
            // none of the values can be null!!
            if ( entry.getValue() == null )
                return ID_NULL;

            // add AND between the entries
            if ( selection.length() != 0 )
                selection.append(" AND ");

            selection.append(entry.getKey()).append("=");
            if ( entry.getValue() instanceof Long ) // entry is LONG
                {
                selection.append( entry.getValue().toString() );
                }
            else // entry should be TEXT
                {
                selection.append("\'").append( entry.getValue() ).append("\'");
                }

            projection[i++] = entry.getKey();
            }

        Cursor cursor = table.port().getContext().getContentResolver()
                .query( foreignTable.contentUri(), projection, selection.toString(), null, null);

        if ( cursor != null)
            {
            if (cursor.moveToFirst())
                row = cursor.getLong( cursor.getColumnIndexOrThrow( column_id() ) );
            cursor.close();
            }

        Scribe.debug("FINDROW: " + row + "(selection: " + selection + ")");

        return row;
        }


    @Override
    public void onRecordCreated(long rowIndex)
        {
        // DO NOTHING, NO FURTHER CHANGES ARE NEEDED
        }
    }
