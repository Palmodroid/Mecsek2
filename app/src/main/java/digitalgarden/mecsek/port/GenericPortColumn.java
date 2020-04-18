package digitalgarden.mecsek.port;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Iterator;
import java.util.List;


public interface GenericPortColumn
    {
    /**
     * Adds COLUMN(S) to projection.
     * <p><ul><li>PortDataColumn adds only one column,</li>
     * <li>ForeignKey adds foreign columns - inside (joined) foreign table,</li>
     * <li>ExternKey adds extern columns - inside (joined) extern table.</li>
     * </ul>
     * ForeignKey/PortExternKey column inside main table is normally not needed to be exported.</p>
     * <p>COLUMN can be part of any joined table, because queries contain each (joined) tables.</p>
     * @param projection list of column-names. New column names should be added to this list.
     */
    void addToProjection(List<String> projection );

    /**
     * Gets values from of each columns, and adds this value in readable (string) format to data.
     * <p><ul><li>PortDataColumn adds value of its own (one) column, after converting it to string:
     * {@link PortDataColumn#addToExport(Cursor, List)}</li>
     * <li>ForeignKey adds values of each foreign columns - inside (joined) foreign table,</li>
     * <li>ExternKey adds values of each extern columns - inside (joined) extern table.</li>
     * </ul></p>
     * <p>Column names are NOT added, order defines placement of the exported values.</p>
     * @param cursor already scrolled to records row - contains the record itself
     * @param data values of the columns - as list of strings. Placement determines columns
     */
    void addToExport(Cursor cursor, List<String> data );

    /**
     * Gets the name of the "key" column.
     * <p>Key column is the column inside main table, which refers to the data part. In the case of
     * {@link PortDataColumn} key column is the column itself (Same column is the part of the projection). In the
     * case of {@link PortForeignKey} and {@link PortExternKey} key column is the column which refers to the record
     * inside foreign/extern table. The key column and its value (row index of the foreign record) is NOT exported!</p>
     * @return name of the key column
     */
    String getKeyColumnName();

    /**
     * Gets strings from data and converts them to values of the record.
     * <p>{@link #addToExport(Cursor, List)} and {@link #getFromImport(ContentValues, Iterator)} should use
     * the <em>same order</em> to place data to the correct columns!</p>
     * <p><ul><li>PortDataColumn gets value of its own (one) column, after converting it to string:
     * {@link PortDataColumn#getFromImport(ContentValues, Iterator)}</li>
     * <li>ForeignKey finds the id of the foreign record, and adds it as ForeignKey column,</li>
     * <li>ExternKey creates extern record, and adds the id of the extern record as ExternKey column.
     * <p>Because MAIN column doesn't exist when extern record is created, SOURCE data should be added later (when
     * creation is ready), and extern column should be updated with SOURCE data.</p></li>
     * </ul></p>
     * @param values values of columns as column_name-column_value pairs
     * @param data values of this record as list of strings
     */
    boolean getFromImport(ContentValues values, Iterator<String> data );

    void onRecordCreated( long rowIndex );
    }
