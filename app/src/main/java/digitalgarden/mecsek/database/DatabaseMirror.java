package digitalgarden.mecsek.database;

import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;

import java.util.ArrayList;

import digitalgarden.mecsek.tables.LibraryDatabase;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.GenericDatabase;
import digitalgarden.mecsek.generic.GenericTable;

import static android.provider.BaseColumns._ID;
import static digitalgarden.mecsek.Debug.DB;


public class DatabaseMirror
    {
    /*** DEFINE DATABASE ***/

    protected static GenericDatabase defineDatabase()
        {
        return new LibraryDatabase();
        }


    /*** START ***/

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

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static int match (Uri uri)
        {
        return uriMatcher.match(uri);
        }

    /*** DATABASE ***/

    private static GenericDatabase database;

    public static GenericDatabase database()
        {
        return database;
        }


    /*** TABLES ***/

    private static ArrayList<GenericTable> tables = new ArrayList<>();

    public static int addTableToDatabase(GenericTable table )
        {
        int index = tables.size();

        tables.add(table);
        table.setId( index );

        return index;
        }

    public static GenericTable table( int index )
        {
        return tables.get(index);
        }

    public static Iterable<GenericTable> allTables()
        {
        return tables;
        }

    /*** COLUMNS ***/

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

    private static ArrayList<Column> columns = new ArrayList<>();

    public static int addColumnToDatabase(String columnName, int columnType, int tableId )
        {
        columns.add( new Column( columnName, columnType, tableId ));
        return columns.size() - 1;
        }

    public static String column(int columnIndex )
        {
        return columns.get(columnIndex).columnName;
        }

    public static String columnFull(int columnIndex )
        {
        return table(columns.get( columnIndex ).tableId ).name() + "." + column(columnIndex);
        }

    public static String column_id()
        {
        return _ID;
        }

    public static String columnFull_id(int tableIndex )
        {
        return table( tableIndex ).name() + "." + _ID;
        }

    public static int columnType(int columnIndex )
        {
        return columns.get(columnIndex).columnType;
        }

    // Itt jobb lenne egy getColumn(), amin belül lehetne setStyle... és getRefTableIndex

    public static void setColumnReferenceTableId(int columnIndex, int referenceTableIndex )
        {
        columns.get(columnIndex).referenceTableId = referenceTableIndex;
        }

    public static int getColumnReferenceTableId(int columnIndex )
        {
        return columns.get(columnIndex).referenceTableId;
        }

    }
