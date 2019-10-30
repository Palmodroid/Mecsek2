package digitalgarden.mecsek.database;

import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;

import java.util.ArrayList;

import digitalgarden.mecsek.database.library.LibraryDatabase;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.database.GenericDatabase;
import digitalgarden.mecsek.generic.database.GenericTable;

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
        private String tableName;
        private String columnName;
        private int columnType;

        Column(String columnName, int columnType, String tableName)
            {
            this.columnName = columnName;
            this.columnType = columnType;
            this.tableName = tableName;
            }
        }

    private static ArrayList<Column> columns = new ArrayList<>();

    public static int addColumnToDatabase(String columnName, int columnType, String tableName )
        {
        columns.add( new Column( columnName, columnType, tableName ));
        return columns.size() - 1;
        }

    public static String column(int columnIndex )
        {
        return columns.get(columnIndex).columnName;
        }

    public static String columnFull(int columnIndex )
        {
        return columns.get( columnIndex ).tableName + "." + column(columnIndex);
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
    }
