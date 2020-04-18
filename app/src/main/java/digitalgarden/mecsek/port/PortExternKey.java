package digitalgarden.mecsek.port;

import android.content.ContentValues;
import android.database.Cursor;
import digitalgarden.mecsek.database.DatabaseMirror;
import digitalgarden.mecsek.scribe.Scribe;

import java.util.Iterator;
import java.util.List;

import static digitalgarden.mecsek.Debug.PORT;
import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.getColumnTableId;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


/**
 * PortExternKey refers to an extern key column (identified by int columnIndex). Extern Key refers to a extern
 * record (row index is the extern key value) inside an extern table. The extern record is stored by {@link PortRecord}.
 * <ul>
 * <li><em>Projection</em> - adds EACH EXTERN column name from the extern table </li>
 * <li><em>Export</em> - adds EACH EXTERN column data (as strings)</li>
 * <li><em>Import</em> - adds complete EXTERN record (using EACH EXTERN column) and adds its id as ONE (EXTERN KEY
 * VALUE) </li>
 * </ul>
 */
public class PortExternKey extends PortRecord implements GenericPortColumn
    {

    // Extern Key column index - inside MAIN table
    int externKeyIndex;


    public PortExternKey(int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        // superclass is the Extern Record inside Extern Table (referenced by Extern Key inside MAIN table
        // EXTERN TABLE is added to superclass
        super( table(externTableIndex) );

        // EXTERN COLUMNS are added to superclass
        for ( int index : externColumnIndices )
            {
            addColumn( new PortDataColumn( index ) );
            }

        /* More EXETRN COLUMNS can be added by {@link #addColumn(GenericPortColumn)} */

        this.externKeyIndex = externKeyIndex;
        }


    /**
     * Each extern column name (stored inside {@link PortRecord#columnList} is added to projection
     * ExternKey (inside MAIN) is NOT added to projection!!
     */
    @Override
    public void addToProjection(List<String> projection)
        {
        Scribe.debug(PORT, "Column (EXTERN KEY) " + column(externKeyIndex) + " adds extern columns to projection");

        for (GenericPortColumn column : columnList )
            {
            column.addToProjection( projection );
            }

        }


    /**
     * Each extern column data (stored inside {@link PortRecord#columnList} is exported
     * ExtrenKey (inside MAIN) is NOT exported (row index of Extern Record)!!
     */
    @Override
    public void addToExport(Cursor cursor, List<String> data)
        {
        for (GenericPortColumn column : columnList )
            {
            column.addToExport( cursor, data );
            }
        }


    /** Returns name of ExternKey column inside MAIN table */
    @Override
    public String getKeyColumnName()
        {
        return column( externKeyIndex );
        }


    /** Creates extern record (using exported data) and adds record's id as ExternKey to values
     *  @return true if data is valid, false if extern record doesn't exists*/
    @Override
    public boolean getFromImport(ContentValues values, Iterator<String> data)
        {
        // 1. Create extern record by {@link PortRecord}
        // row is the same as {@link #idColumnValue} !!!
        long row = createRecordFromImport( data );

        // Result: Each column contains null, so external record doesn't exist
        if ( row < 0L )
            {
            Scribe.note( "Extern record does not exists!");
            return false;
            }

        // row id is valid - foreign row will be added to values (coming through parameters)
        values.put( column(externKeyIndex), row );
        return true;
        }


    /**
     * It is called after MAIN record is created, to insert it's row index as SOURCE
     *
     * MAIN PortRecord collects its columns' data.
     * Among them is an EXTERN PortRecord.
     * EXTERN PortRecord creates its EXTERN record, then returns its EXTERN Row index to MAIN PortRecord
     * MAIN PortRecord creates its MAIN record containing the previous EXTERN RowIndex
     *
     * THEN:
     *
     * EXTERN PortRecord should UPDATE its EXTERN Record (identified by row index) to contain
     * MAIN TableId and MAIN RowIndex as sources (if needed)
     *
     * EXTERN: This PortRecord
     * {@link #table}           EXTERN TABLE
     * {@link #idColumnValue}   EXTERN ROW ID (after created by {@link #createRecordFromImport(Iterator)}
     *
     * MAIN:
     * {@link #externKeyIndex}  MAIN table: column index of EXTERN KEY
     * {@link DatabaseMirror#getColumnTableId(int externKeyIndex)}
     *                          MAIN table id
     */
    @Override
    public void onRecordCreated(long rowIndex)
        {
        // table is the EXTERN table (This portRecord)
        if ( table.hasSourceColumns())
            {
            ContentValues values = new ContentValues();

            // MAIN table id is stored together externKeyIndex
            values.put(
                    column( table.SOURCE_TABLE), getColumnTableId(externKeyIndex) );
            // MAIN table row index comes as rowIndex parameter
            values.put(
                    column( table.SOURCE_ROW), rowIndex );

            // EXTERN (this) record's id idColumnValue after record was created by createRecordFromImport
            table.port().getContext().getContentResolver()
                    .update( table.itemContentUri( idColumnValue ), values,
                    null, null);

            // ??? Hibakezelés szükséges ???
            }
        }
    }
