package digitalgarden.mecsek.database;

import android.content.Context;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;

import android.provider.BaseColumns;
import digitalgarden.mecsek.exportimport.TableExportImport;
import digitalgarden.mecsek.tables.LibraryDatabase;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.GenericDatabase;
import digitalgarden.mecsek.generic.GenericTable;

import static android.provider.BaseColumns._ID;
import static digitalgarden.mecsek.Debug.DB;


/**
 * Contains static convenience methods to get description constants for the structure of the database. Eventually it
 * mirrors the whole database structure. (And database is created with the use of this "mirror" by the way.)
 * <p>{@link #start(Context)} is the entry point of the whole database/content provider process</p>
 */
public class DatabaseMirror
    {
    /*** DEFINE DATABASE ***/

    /** GenericDatabase instance to contain mirrored structure data */
    protected static GenericDatabase defineDatabase()
        {
        return new LibraryDatabase();
        }


    /*** START ***/

    /**
     * Entry point for the whole database. It is called by {@link DatabaseContentProvider#onCreate()}, (right before
     * calling {@link DatabaseOpenHelper#onCreate(SQLiteDatabase)} to create the database (== all the tables)
     * <ol>
     * Database creation process:
     * <li>{@link DatabaseContentProvider#onCreate()} starts first, and calls</li>
     * <li><em>start(Context)</em> </li>
     * <li>new {@link LibraryDatabase} mirror is created by {@link #defineDatabase()} </li>
     * <li>all table's mirror is created by {@link GenericDatabase#defineTables()} </li>
     * <li>each table contains one {@link TableExportImport} instance, which needs <em>context</em> (?? Couldn't it be
     * forwarded by defineTables() ??) </li>
     * <li>each table defines its columns by its {@link GenericTable#defineColumns()}</li>
     * <li>each table defines its URI matches by its {@link GenericTable#defineUriMatcher(UriMatcher)}</li>
     * <li>when all tables are ready - each table defines which of its columns should be exported by its
     * {@link GenericTable#defineExportImportColumns()} </li>
     * <li>after returning to {@link DatabaseContentProvider#onCreate()} each
     * {@link GenericTable#create(SQLiteDatabase)} is called at the end</li>
     * And database is created!
     * </ol>
     * @param context context
     */
    public static void start( Context context )
        {
        Scribe.locus(DB);

        database = defineDatabase();

        database().defineTables();

        for (GenericTable table : allTables())
            {
            table.exportImport().setupContext( context );
            table.defineColumns();
            table.defineUriMatcher( uriMatcher );
            }
        // Ehhez az összes táblának készen kell lennie
        for (GenericTable table : allTables())
            {
            table.defineExportImportColumns();
            }
        }


    /*** URIMatcher ***/

    /**
     * UriMatcher to store nodes.
     * Each table will add its URI to match <em>id(DIRID)</em>, its record's URI to match <em>id(ITEMID)</em> and
     * its count URI to match <em>id(COUNTID)</em>.
     * Check {@link GenericTable#defineUriMatcher(UriMatcher)} and {@link GenericTable#id(int)}!
     * uriMatcher is filled by {@link #start(Context)}
     */
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Convenience method to match URIs
     * @param uri URI to identify
     * @return uriType - id(DIRID), id(ITEMID) or id(COUNTID) of the identified table
     */
    public static int match (Uri uri)
        {
        return uriMatcher.match(uri);
        }

    /*** DATABASE ***/

    /** Database (mirror) defined by {@link #start(Context)} */
    private static GenericDatabase database;

    /** Convenience method to return database instance */
    public static GenericDatabase database()
        {
        return database;
        }


    /*** TABLES ***/

    /** List of all tables (mirror) */
    private static ArrayList<GenericTable> tables = new ArrayList<>();

    /** Used by {@link GenericDatabase#addTable(GenericTable)} */
    public static int addTableToDatabase(GenericTable table )
        {
        int index = tables.size();

        tables.add(table);
        table.setId( index );

        return index;
        }

    /** Convenience method to get table by its index (== id). Indices are stored in {@link LibraryDatabase}  */
    public static GenericTable table( int index )
        {
        return tables.get(index);
        }

    /** Convenience method to get all tables in one list */
    public static Iterable<GenericTable> allTables()
        {
        return tables;
        }

    /*** COLUMNS ***/

    /**
     * Description (mirror) for each column in the whole database (from all tables).
     * <ul>
     * Data stored for each column:
     * <li><em>columnName</em> - name + "_" + tableId format differences columns in different tables with similar
     * names (otherwise alias should be used)</li>
     * <li><em>columnType</em> - GenericTable.TYPE_KEY, TYPE_TEXT, TYPE_DATE, TYPE_COLOR etc. Type needed by
     * Export/Import</li>
     * <li><em>tableId</em> - index/id of the table. {@link #addTableToDatabase(GenericTable)} always return the same
     * index from the table list - that is the ID of the table.</li>
     * <li><em>referenceTableId</em> - ONLY FOR FOREIGN KEYS! - Needed for headers/LIMITED COLUMN</li>
     * </ul>
     */
    private static class Column
        {
        private String columnName;
        private int columnType;
        private int tableId; // "START": defineTables() - setId - is before table.defineColumns()

        private int referenceTableId; // Used only for foreignKey-s

        Column(String columnName, int columnType, int tableId)
            {
            this.columnName = columnName;
            this.columnType = columnType;
            this.tableId = tableId;
            }
        }

    /** List of all columns (mirror) */
    private static ArrayList<Column> columns = new ArrayList<>();

    /** Used by {@link GenericTable#addColumn(int, String, boolean)} */
    public static int addColumnToDatabase(String columnName, int columnType, int tableId )
        {
        columns.add( new Column( columnName, columnType, tableId ));
        return columns.size() - 1;
        }

    /** Convenience method to get column's NAME by columnIndex
     * (columnIndex is stored in {@link GenericTable} subclasses */
    public static String column(int columnIndex )
        {
        return columns.get(columnIndex).columnName;
        }

    /** Convenience method to get column's FULL NAME (table.column) by columnIndex
     * (columnIndex is stored in {@link GenericTable} subclasses */
    public static String columnFull(int columnIndex )
        {
        return table(columns.get( columnIndex ).tableId ).name() + "." + column(columnIndex);
        }

    /** Convenience method to get _id column's NAME: always "_id" from {@link BaseColumns} */
    public static String column_id()
        {
        return _ID;
        }

    /** Convenience method to get _id column's FULL NAME (table._id) "_id" from {@link BaseColumns} */
    public static String columnFull_id(int tableIndex )
        {
        return table( tableIndex ).name() + "." + _ID;
        }


    /** Convenience method to get column's TYPE by columnIndex
     * (columnIndex is stored in {@link GenericTable} subclasses */
    public static int columnType(int columnIndex )
        {
        return columns.get(columnIndex).columnType;
        }

    // Itt jobb lenne egy getColumn(), amin belül lehetne setStyle... és getRefTableIndex

    /**
     * Sets column's reference table (Only for Foreign Keys!) (Because it needed only by foreign keys, it is not part
     * of the constructor)
     * @param columnIndex column index (column containing reference to foreign table)
     * @param referenceTableIndex table index (of referenced table)
     */
    public static void setColumnReferenceTableId(int columnIndex, int referenceTableIndex )
        {
        columns.get(columnIndex).referenceTableId = referenceTableIndex;
        }

    /**
     * Gets column's reference table (Only for Foreign Keys!)
     * @param columnIndex column index (column containing reference to foreign table)
     * @return table index (of referenced table)
     */
    public static int getColumnReferenceTableId(int columnIndex )
        {
        return columns.get(columnIndex).referenceTableId;
        }

    }
