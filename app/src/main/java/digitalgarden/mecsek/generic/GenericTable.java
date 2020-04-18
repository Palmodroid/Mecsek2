package digitalgarden.mecsek.generic;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import java.util.ArrayList;

import digitalgarden.mecsek.database.DatabaseContentProvider;
import digitalgarden.mecsek.database.DatabaseMirror;
import digitalgarden.mecsek.database.DatabaseOpenHelper;
import digitalgarden.mecsek.port.PortTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.tables.LibraryDatabase;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.addColumnToDatabase;
import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.database;
import static digitalgarden.mecsek.database.DatabaseMirror.setColumnReferenceTableId;
import static digitalgarden.mecsek.database.DatabaseMirror.table;

/**
 * Az adatbázis tábláinak szerkezetét a GenericTable osztály leszármazottai tükrözik le a
 * program számára.
 * A GenericTable segítségével gyűjtjük össze a tábla oszlopait. Néhány kombinált mezőhöz külön
 * segítséget kapunk.
 *
 * Egyszerű adatoszlopok:
 *
 *  addColumn( TYPE_KEY vagy TYPE_TEXT vagy TYPE_DATE, "oszlop_neve", boolean unique )
 *
 * (az oszlop neve kiegészül a tábla számával is, így minden egyes oszlop egyedi lesz)
 * (az adat megjelenítését a mező típusa szabályozza (pl. TYPE_DATE esetén DateView
 * vagy EditFieldDate)
 *
 * Keresőoszlop (egyetlen ilyen oszlop lehet!):
 *
 * addSearchColumnFor(int columnIndex )
 *
 * (Az oszlop tartalma normalizálva - vagyis ékezetek és írásjelek nélkül kerül ide. Ezen adat
 * alapján történik a keresés.)
 * (Az exportimport részben a search column NEM szerepel, hanem újra kitöltésre kerül!)
 *
 * Külső hivatkozás:
 *
 * addForeignKey(String columnName, int referenceTableIndex)
 *
 * (A foreign key egy külső tábla egy elemére (sorára) hivatkozik. Maga a külső elem nem
 * változtatható meg a foreign key-en keresztül, hanem egy másik elem választható ki ugyanabból a
 * listából.)
 *
 * ExportImport?? - először mindig a foreign táblát kell exportálni. Ezt követően a foreign key-t
 * tartalmazó táblában a foreign key által hivatkozott elemeket is exportáljuk, majd ezeket
 * keressük ki az import során.)
 *
 * addExternKey(String columnName, int referenceTableIndex)
 *
 * (Az extern key épp a foreign key fordítottja: az idegen táblában mindig ugyanarra az elemre
 * mutat (ez nem változtatható meg, viszont maga az elem tartalma változtatható az extern key
 * felől.)
 * (Ennek akkor van értelme, ha több különböző tábla azonos szerkezetű részt is tartalmaz, mint
 * például a naptárbejegyzések. A külső tábla felől viszont az eredeti, létrehozó bejegyzéshez
 * tudunk visszajutni; mert a táblát és az elem sorszámát is tároljuk.)
 *
 * ExportImport?? - itt valószínűleg csak azokat az elemeket kell exportálni, melyeknek NINCS
 * hivatkozott extern key forrása, mert a hivatkozott elemeket az extern key mellett tudjuk
 * exportálni.)
 */
public abstract class GenericTable
    {
    /** Unique id of the table - equals index of the table-list {@link DatabaseMirror#tables}
     *  (These will not change if {@link LibraryDatabase#defineTables()} does not change) */
    private int tableId;

    /** Every GenericTable subclass contains exactly one PortTable class to separate export/import */
    private PortTable portTable;

    /** Constructor just creates separated PortTable */
    public GenericTable( )
        {
        this.portTable = new PortTable( this );
        }

    /** id (index) is available (and therefore set) after table instantiation
     *  Check {@link DatabaseMirror#addTableToDatabase(GenericTable)} */
    public void setId( int tableId )
        {
        this.tableId = tableId;
        }

    /** id (index) of the table */
    public int id()
        {
        return tableId;
        }

    /**
     *  Gets separated export/import class for this table
     *  ((It should be called <em>getPortTable()</em> or something, but it is very important, so shorter name seems to
     *  be appropriate))
     */
    public PortTable port()
        {
        return portTable;
        }

    /**
     * Define data to export. Import is available for different versions.
     * <p>Get separated {@link PortTable} by {@link #port()} and use <em>add...</em> methods to add
     * different columns and versions. For available methods check {@link PortTable} class!</p>
     * <p>Ex.: <code>port().addColumnAllVersions( BooksTable.TITLE );</code></p>
     */
    public abstract void definePortColumns();

    /** Extern records could know their "SOURCE" MAIN Record */
    private static final String SOURCE_TABLE_COLUMN_NAME = "srctbl";
    private static final String SOURCE_ROW_COLUMN_NAME = "srcrow";

    /** Index of SOURCE_TABLE column (containing MAIN table inside EXTERN Record
     *  If Column Index is negative, then SOURCE Columns are NOT defined */
    public int SOURCE_TABLE = -1;

    /** Index of SOURCE_ROW column (containing MAIN Record index inside EXTERN Record */
    public int SOURCE_ROW;

    /** Extern Table could contain SOURCE columns (not obligatory) */
    protected void addSourceColumns()
        {
        SOURCE_TABLE = addColumn( TYPE_TEXT, SOURCE_TABLE_COLUMN_NAME);
        SOURCE_ROW = addColumn( TYPE_KEY, SOURCE_ROW_COLUMN_NAME);
        }

    /** True if SOURCE coulmns are defined and valid */
    public boolean hasSourceColumns()
        {
        return SOURCE_TABLE >= 0;
        }

    /** Extern record knows its SOURCE table index. Using its {@link GenericControllActivity} can show original data */
    public Class getControllActivity()
        {
        return null;
        }

    /**
     * Loader is notified only when its own table (== own URI) is changed
     * <p>If table contains foreign references, then observation range should be extended to whole database</p>
     * ((Own observer cannot be unregistered, but where? If we unregister it in onPause, then no
     * foreign changes will be observed - independent loader class will solve the problem.))
     */
    private boolean containsForeignReference = false;

    /**
     * Returns true, if table contains foreign reference.
     * <p>In these cases update can change data in further tables, so CursorLoader should be notified about changes
     * in the whole database, and not only about changes in the current table (which is the default behavior). Check
     * {@link GenericCombinedListFragment#onLoadFinished(Loader, Cursor)} for further details. </p>
     */
    public boolean containsForeignReference()
        {
        return containsForeignReference;
        }

    /**
     * Column types used by app. Every type has got a database-type pair in COLUMN_TYPES
     * <p>App uses more specific types: like TYPE_STYLE, which is stored inside database as simple "INTEGER" type</p>
     */
    public static final int TYPE_KEY = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_DATE = 2;
    public static final int TYPE_STYLE = 3;
    public static final int TYPE_IMAGE = 4;

    /** Corresponding database column types. */
    private static final String[] COLUMN_TYPES = {"INTEGER", "TEXT", "INTEGER", "INTEGER", "BLOB"};


    public static final int COUNTID = 0x100000;
    public static final int DIRID = 0x200000;
    public static final int ITEMID = 0x300000;

    /** id-s for {@link DatabaseMirror#match(Uri)} uri-matcher
     *  There are separate DIRID, ITEMID and COUNTID codes for each table */
    public int id(int ext)
        {
        return ext + id();
        }

    /** Name of the table */
    public abstract String name();

    /**
     * Columns for the table should be defined here using <em>add...</em> methods. Each column definition returns an
     * unique index for that column which should be stored inside this class in a public variable. Later on these
     * indices could identify columns: ex. {@link DatabaseMirror#column(int index)} returns column's name.
     */
    public abstract void defineColumns();


    /** Commands (strings) to create columns. Check <em>add...</em> and {@link #create(SQLiteDatabase)}  */
    private ArrayList<String> createColumns = new ArrayList<>();

    /** Commands to create foreign/extern columns. Check <em>add...</em> and {@link #create(SQLiteDatabase)} */
    private ArrayList<String> createForeignKeys = new ArrayList<>();

    /** Commands to create LEFT OUTER JOINS for foreign/extern tables (Used only in queries). Check <em>add...</em>
     *  and {@link #buildQuery(Uri, int, SQLiteQueryBuilder)} */
    private ArrayList<String> createLeftOuterJoin = new ArrayList<>();

    /** Command to create UNIQUE constraint for more columns. Only ONE constraint is allowed/table.
     *  Check {@link #addUniqueConstraint(int...)} and {@link #create(SQLiteDatabase)} */
    private String createUniqueConstraint = "";

    /** contains the index of the SEARCH column {@link #addSearchColumnFor(int)} */
    private int searchColumnIndex = -1;
    /** contains the index of the original (searchable) column {@link #addSearchColumnFor(int)}  */
    private int searchColumnIndexFor = -1;


    /**
     * Adds a simple database column to this table.
     * Adds a new line to createColumns: <p>NAME DBTYPE or NAME DBTYPE UNIQUE </p>
     * @param columnType TYPE_KEY, TYPE_TEXT etc. These are 'app-types'. Corresponding 'db-types' (like INTEGER) are
     *                   found in {@link #COLUMN_TYPES}
     * @param columnName Name of the column. It will be extended with '_tableIndex' to be unique
     * @param unique true, if this column should be UNIQUE
     * @return index of the column, which should be stored, because later this index could identify the column
     */
    protected int addColumn( int columnType, String columnName, boolean unique )
        {
        columnName = columnName + "_" + Integer.toString(tableId);

        createColumns.add(columnName + " " + COLUMN_TYPES[columnType] + (unique ? " UNIQUE " : ""));
        return addColumnToDatabase( columnName, columnType, tableId );
        }

    /** Same as {@link #addColumn(int, String, boolean)} without UNIQUE */
    protected int addColumn(int columnType, String columnName )
        {
        return addColumn( columnType, columnName, false );
        }

    /** Same as {@link #addColumn(int, String, boolean)} but always adding UNIQUE */
    protected int addUniqueColumn(int columnType, String columnName )
        {
        return addColumn( columnType, columnName, true );
        }

    /**
     * Foreign keys refers to one record (row) in the foreign table. Changing the value will refer to an other 
     * foreign record, but the foreign record itself will not change. (Extern keys are very similar, but they always 
     * refer to the same foreign record (or none). Changes change the foreign record's value and NOT the record's id)
     * Adds a new line to createForeignKeys:
     * <p>" FOREIGN KEY ( columnName ) REFERENCES foreignTableName ( _id ) ON DELETE CASCADE "</p>
     * Adds a new line to createLeftOuterJoin:
     * <p>" LEFT OUTER JOIN foreignTableName ON mainTableName.columnName = foreignTableName._id "</p>
     * ForeignTable should be stored for column.
     * {@link #containsForeignReference} should be TRUE, because the whole database could change if one record is
     * changed.
     * @param columnName column in the main table which contains the id of one record in the foreign table
     * @param referenceTableIndex foreign table index 
     * @return index of the column, which should be stored, because later this index could identify the column
     */
    protected int addForeignKey(String columnName, int referenceTableIndex)
        {
        int index = addColumn( TYPE_KEY, columnName);
        createForeignKeys.add(" FOREIGN KEY (" + column(index) +
                ") REFERENCES " + table(referenceTableIndex).name() + " (" + column_id() + ") ON DELETE CASCADE ");

        createLeftOuterJoin.add(" LEFT OUTER JOIN " + table(referenceTableIndex).name() +
                " ON " + columnFull( index ) + "=" + columnFull_id(referenceTableIndex) );

        containsForeignReference = true;

        // Important!! GenericListFragment.Header needs it!!
        setColumnReferenceTableId( index, referenceTableIndex);

        return index;
        }

    /**
     * Foreign tables can join other foreign tables to the query. Columns of these "far away" tables are not part of
     * the main column, they are connected through the foreign table (which contains other foreign keys).
     * TODO! These joins are exactly the same of the joins of the foreign table. This method could be automated -
     * TODO! just copy recursively {@link #createLeftOuterJoin} array of the joined (referenced by foreign key) tables
     * @param farawayForeignKey index of the ForeignKey column in a previously joined Foreign table
     * @param farawayReferenceTableIndex index of the Reference table of this ForeignColumn (next table). After this
     *                                   join all columns of this reference table can be added to the projections
     */
    protected void addFarawayForeignQuery(int farawayForeignKey, int farawayReferenceTableIndex)
        {
        createLeftOuterJoin.add(" LEFT OUTER JOIN " + table(farawayReferenceTableIndex).name() +
                " ON " + columnFull( farawayForeignKey ) + "=" + columnFull_id(farawayReferenceTableIndex) );

        // containsForeignReference = true;
        }

    /** Extern key - as database structure - is similar to foreign keys */
    protected int addExternKey(String columnName, int referenceTableIndex)
        {
        // Ez ugyanaz, mint a Foreign Key, csak nem a hivatkozás sorszáma, hanem maga a hivatkozott érték változik
        return addForeignKey( columnName, referenceTableIndex );
        }

    /**
     * Creates unique constraint for table creation as
     * ", UNIQUE ( index1, index2, index3,... ) "
     * Only one unique constraint is allowed per table
     * @param columnIndices list of columns (indices) to be unique together
     */
    protected void addUniqueConstraint(int... columnIndices)
        {
        if ( !createUniqueConstraint.isEmpty() )
            throw new IllegalArgumentException("Unique constraint is already defined in table " + name());
        StringBuilder sb = new StringBuilder();
        for (int columnIndex : columnIndices)
            {
            if ( sb.length()!=0 )
                sb.append(", ");
            sb.append( column( columnIndex));
            }
        createUniqueConstraint = ", UNIQUE ( " + sb.toString() + " ) ";
        }

    /**
     * Creates (adds) SEARCH column. Currently SEARCH column can contain only one other column's textual data. This
     * string is normailzed (without accented chars and white-spaces) to be easily searchable. Only ONE Search column
     * is allowed!
     * <p>{@link #searchColumnIndex} contains the index of the SEARCH column</p>
     * <p>{@link #searchColumnIndexFor} contains the index of the original column</p>
     * @param columnIndex column to search (its data will be stored (and normalized) in SEARCH column)
     * @return index of the SEARCH column (it is also stored internally)
     * @throws IllegalArgumentException if SEARCH column already exists
     */
    protected int addSearchColumnFor(int columnIndex )
        {
        if ( searchColumnIndex != -1 )
            throw new IllegalArgumentException("Search column is already defined in table " + name());
        searchColumnIndex = addColumn(TYPE_TEXT,  "search");
        searchColumnIndexFor = columnIndex;
        return searchColumnIndex;
        }

    /**
     * Create database table. Called by {@link DatabaseOpenHelper#onCreate(SQLiteDatabase)},  but check also
     * {@link DatabaseMirror#start(Context)}! Command string consists of:
     * <ol>
     * CREATE TABLE tableName (
     * <li> _id  INTEGER PRIMARY KEY,</li>
     * <li><em>each createColumns row as:</em> <p>columnName columnType,</p> <em>or</em> <p>columnName columnType UNIQUE,
     * </p></li>
     * <li><em>each createForeignKeys row as:</em> <p>?????????????????????????,</p></li>
     * <li><em>createUniqueConstraint if needed:</em> <p>???????????????????????</p></li>
     * )
     * </ol>
     * This command string will be executed.
     */
    public void create(SQLiteDatabase db)
        {
        StringBuilder sb = new StringBuilder("CREATE TABLE ");

        sb.append(name()).append(" (").append( column_id() ).append(" INTEGER PRIMARY KEY");

        for (String createColumn : createColumns)
            sb.append(", ").append(createColumn);

        for (String createForeignKey : createForeignKeys)
            sb.append(", ").append(createForeignKey);

        sb.append( createUniqueConstraint );

        sb.append(")");

        Scribe.note("DB Create: " + sb.toString());

        db.execSQL(sb.toString());
        }

    /** DROP table - used only by version changes */
    public void drop(SQLiteDatabase db)
        {
        Scribe.note("DB Drop: " + name());
        db.execSQL("DROP TABLE IF EXISTS " + name());
        }

    /** authority - for URIs - identical to database authority */
    public String authority()
        {
        return database().authority();
        }

    /** content count string - for URISs - "/count" */
    public String contentCount()
        {
        return database().contentCount();
        }

    // Itt mi lesz az s-sel a végén? Marad?
    public String contentSubtype()
        {
        return "vnd.digitalgarden.mecsek.contentprovider." + name();
        }

    public String contentType()
        {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + contentSubtype();
        }

    public String contentItemType()
        {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + contentSubtype();
        }

    /** Content uri for this table:
     * "content://digitalgarden.mecsek.contentprovider/TABLENAME" */
    public Uri contentUri()
        {
        return Uri.parse(database().contentUri() + "/" + name());
        }

    /** Content uri for one record in this table:
     * "content://digitalgarden.mecsek.contentprovider/TABLENAME/RECORDID" */
    public Uri itemContentUri( long itemId )
        {
        return Uri.parse(contentUri() + "/" + itemId);
        }

    /** Content count uri for this table:
     * "content://digitalgarden.mecsek.contentprovider/TABLENAME/count" */
    public Uri contentCountUri()
        {
        return Uri.parse(contentUri() + contentCount());
        }


    /***
     * CONTENT PROVIDER
     ***/


    public void defineUriMatcher(UriMatcher sURIMatcher)
        {
        sURIMatcher.addURI( authority(), name(), id(DIRID));
        sURIMatcher.addURI( authority(), name() + "/#", id(ITEMID));
        sURIMatcher.addURI( authority(), name() + contentCount(), id(COUNTID));
        }

    /**
     * All tables called by {@link digitalgarden.mecsek.database.DatabaseContentProvider#insert(Uri, ContentValues)} to
     * insert record into table.
     * <p>If this is THE table to insert data (that means: uriType == id(DIRID) - tableId+DIRID) then</p>
     * <p>searchColumn (if exists) is filled with normalized data - from searchColumnIndexFor column;</p>
     * <p>record is inserted into table.</p>
     * <li><em>id(DIRID)</em> - inserts new record under a new _id</li>
     * <li><em>id(ITEMID)</em> - upserts (inserts or updates if exist) the new record under the given _id (defined by
     * uri) <em>JUST TRYING!</em></li>
     * @param db database
     * @param uri uri of the table (or record if upsert)
     * @param uriType uri type (returned by match(uri))
     * @param values values of the record (without _id)
     * @return uri of the newly inserted record
     */
    public Uri insert( SQLiteDatabase db, Uri uri, int uriType, ContentValues values )
        {
        if ( uriType == id(DIRID) )
            {
            // Az ekezet nelkuli kereseshez meg egy oszlop hozzakerul
            if (searchColumnIndex != -1)
                {
                values.put(column(searchColumnIndex), StringUtils.normalize(
                        values.getAsString(column(searchColumnIndexFor))));
                }

            // ?? insertOnConflict - importnál jó lehet.
            // Egyébként meg meg kellene kérdezni, hogy felülírja-e az előzőt?
            long id = db.insert( name(), null, values );

            return itemContentUri( id ); // Uri.parse( contentUri() + "/" + id);
            }
        else if ( uriType == id(ITEMID))
            {
            if (searchColumnIndex != -1)
                {
                values.put(column(searchColumnIndex), StringUtils.normalize(
                        values.getAsString(column(searchColumnIndexFor))));
                }

            // It works also with strings, not only with longs!!
            String id = uri.getLastPathSegment();
            values.put( column_id(), id );

            long returnedId = db.insertWithOnConflict( name(), null, values,
                    SQLiteDatabase.CONFLICT_IGNORE );

            if ( returnedId < 0L )
                {
                // rowsUpdated = EZT MEG HOGYAN ELLENŐRZÖM LE???
                db.update( name(),
                        values,
                        column_id()  + "=" + id,
                        null);

                }

            return uri; // itemContentUri( returnedId ); // Uri.parse( contentUri() + "/" + id);
            // JUST TRYING !!!!!!!!!!!!!!!!!!!
            // Mégiscsak logikusabb a másikba tenni...
            }

        return null;
        }

    /** DELETE is implemented, but shouldn't be used! DELETE flag is under construction !!! */
    public int delete( SQLiteDatabase db, Uri uri, int uriType, String whereClause, String[] whereArgs )
        {
        int rowsDeleted = -1;

        if ( uriType == id(DIRID) )
            {
            rowsDeleted = db.delete( name(), whereClause, whereArgs);
            }
        else if ( uriType == id(ITEMID))
            {
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(whereClause))
                {
                rowsDeleted = db.delete( name(), column_id() + "=" + id, null);
                }
            else
                {
                rowsDeleted = db.delete( name(), column_id() + "=" + id + " and " + whereClause, whereArgs);
                }
            }
        return rowsDeleted;
        }


    /**
     * All tables called by {@link digitalgarden.mecsek.database.DatabaseContentProvider#update(Uri, ContentValues, String, String[])} to
     * update one given record in one, given table.
     * <p>If this is THE table to update record (that means: uriType == id(ITEMID) - tableId+ITEMID) then</p>
     * <p>searchColumn (if exists) is filled with normalized data - from searchColumnIndexFor column;</p>
     * <p>record with the given _id will be updated.</p>
     * If no record can be found, then exception is thrown by Content Provider.
     * <li><em>id(ITEMID)</em> - updates this record with the given _id (in uri)</li>
     * <li><em>id(DIRID)</em> - upserts (inserts or updates if exist) the new record under the given _id (among values)
     * <em>JUST TRYING!</em></li>
     * @param db database
     * @param uri uri of the record (or table if upsert)
     * @param uriType uri type (returned by match(uri))
     * @param values values of the record (without _id) or with _id if UPSERT
     * @param whereClause
     * @param whereArgs
     * @return 1 (number of updated records)
     */
    public int update(SQLiteDatabase db, Uri uri, int uriType, ContentValues values, String whereClause, String[] whereArgs )
        {
        int rowsUpdated = -1;

        if ( uriType == id(DIRID) )
            {
            if (searchColumnIndex != -1)
                {
                values.put(column(searchColumnIndex), StringUtils.normalize(
                        values.getAsString(column(searchColumnIndexFor))));
                }

            // !!!!!!!!! JUST TRYING IT !!!!!!!!!!!!!!!!!!!!!
            /*
            Speciális helyzet, ha egy adott id-hez tartozó rekordot akarunk módosítani.
            Az update csak akkor működik, ha a rekord már létezik.
            Az insert csak akkor működik, ha a rekord NEM létezik.
            Ez általában nem probléma, ill. nem is szabad felülírni rekordot (pl. azonos betegnevek)

            Az insertWithONConflict - CONFLICT_REPLACE ezzel szemben beírja, vagy ha már létezik, akkor törli az előző
            rekordot. Ennek az az eredménye, hogy egy új rekord keletkezik, ugyanazon az id-n.

            A kérdés, hogy ez a funkció hova kerüljön:
            az _id-t bele kell tenni a values-ba, és akkor az update alá, ahol Uri a table neve (DIRÖ
            vagy az insert/ITEM alá, ahol az id mehet az Uri-ba
            Vagy csináljunk egy REPLACE uri-t?

            Talán az insert/ITEM lenne a legjobb.

            Idea2
            További olvasással kiderült, hogy a probléma a REPLACE során a törléssel van, mely kaszkád szerűen mindent
            töröl.
            Úgyhogy először meg kell próbálni az INSERT-et, ha ez sikertelen (mert létezik), akkor jön az UPDATE

            A kérdés továbbra is az, hogy mi végezze el ezt az UPSERT metódust.
            Lehetne az INSERT/ID
            Lehetne az UPDATE/ID - akkor ugye csak ez működik
            Lehetne az UPDATE/DIR, Content Valuesban az ID-val.
             */

            Long id = values.getAsLong( column_id() );
            if ( id == null )
                throw new IllegalArgumentException("Upsert in " + name() + " needs id among values! " + uri);
            // long id = db.insertWithOnConflict( name(), null, values, SQLiteDatabase.CONFLICT_REPLACE);

            if ( db.insertWithOnConflict( name(), null, values, SQLiteDatabase.CONFLICT_IGNORE) < 0L )
                {
                rowsUpdated = db.update( name(),
                        values,
                        column_id()  + "=" + id,
                        null);
                }
            else
                {
                rowsUpdated = 1;
                }

            // throw new IllegalArgumentException("Multiple updates on " + name() + " are not allowed: " + uri);
            }
        else if ( uriType == id(ITEMID))
            {
            if (searchColumnIndex != -1)
                {
                values.put(column(searchColumnIndex), StringUtils.normalize(
                        values.getAsString(column(searchColumnIndexFor))));
                }

            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(whereClause))
                {
                rowsUpdated = db.update( name(),
                        values,
                        column_id()  + "=" + id,
                        null);
                }
            else
                {
                rowsUpdated = db.update( name(),
                        values,
                        column_id()  + "=" + id
                                + " and "
                                + whereClause,
                        whereArgs);
                }
            }
        return rowsUpdated;
        }

    /** 
     * Called by {@link DatabaseContentProvider} query()
     * ???????????????????????
     * If this table is needed:
     * Selects table with left outer join
     * Adds ITEM id to the where clause
     * <p>WHERE _id = RecordId</p>
     */
    public boolean buildQuery(Uri uri, int uriType, SQLiteQueryBuilder queryBuilder )
        {
        if ( uriType == id(DIRID) || uriType == id(COUNTID) )
            {
            StringBuilder sb = new StringBuilder( name() );
            for (String createLeftOuterJoin : this.createLeftOuterJoin)
                sb.append(createLeftOuterJoin);
            Scribe.debug("QUERY TABLES: " + sb.toString() );
            queryBuilder.setTables( sb.toString() );
            }
        else if ( uriType == id(ITEMID) )
            {
            StringBuilder sb = new StringBuilder( name() );
            for (String createLeftOuterJoin : this.createLeftOuterJoin)
                sb.append(createLeftOuterJoin);
            Scribe.debug("QUERY TABLES: " + sb.toString() );
            queryBuilder.setTables( sb.toString() );
            // Adding the ID to the original query
            queryBuilder.appendWhere( name() + "." + column_id() + "=" + uri.getLastPathSegment());
            }
        else
            return false;
        return true;
        }

    /** Called by {@link DatabaseContentProvider} query()
     *  Only replaces projection when records count are needed from table */
    public String[] buildProjection( int uriType, String[] projection )
        {
        if ( uriType == id(COUNTID) )
            {
            projection = new String[]{"count(*) as count"};
            }
        return projection;
        }

    }
