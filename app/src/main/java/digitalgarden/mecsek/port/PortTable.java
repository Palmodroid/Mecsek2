package digitalgarden.mecsek.port;

import android.content.Context;
import android.database.Cursor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import digitalgarden.mecsek.database.DatabaseMirror;
import digitalgarden.mecsek.generic.GenericTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.Debug.PORT;
import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.database;


/**
 * Minden egyes GenericTable-hez tartozik egy PortTable osztály is.
 * (Ezt a GenericTable.tableExportImport privát változó tárolja, melyet a
 * GenericTable.port() metódussal kérhetünk el.
 * Fontos! A TableExportrImport osztálynak szüksége van a context-re (hogy elkérje a
 * contentResolver-t) Ezt a DatabaseMirror.start() metódus tölti fel a setupContext() metódussal)
 *
 * Minden egyes verziónak külön exportimport szabályt állíthatunk fel (vagyis megmondhatjuk, hogy
 * abban a verzióban melyik oszlopokat exportáljuk/importáljuk.
 * Az egyes verzióban exportálni/importálni tervezett oszlopokat az <b>recordVersions[]</b>
 * tömb tárolja. (Eggyel több eleme van, mint az utolsó verziószámnak, hiszen azt is tárolnia kell)
 *
 * Minden egyes szabály tárolja:
 *
 * exportImportColumns listát, mely az egyes - exportálni kívánt - oszlopok indexét tartalmazza.
 *
 * exportImportExternKey listát, mely tartalmazza:
 *      externKeyIndex - foreign key oszlop indexét,
 *      externTableIndex - foreign table indexét
 *      externColumns - foreign táblában lévő foreign oszlopok NEVÉT.
 *
 * exportImportForeignKey listát, mely tartalmazza:
 *      foreignKeyIndex - foreign key oszlop indexét,
 *      foreignTableIndex - foreign table indexét
 *      foreignColumns - foreign táblában lévő foreign oszlopok NEVÉT.
 *
 *  FONTOS!
 *  
 *  A foreign key és az extern key is egy külső tábla egyetlen sorára hivatkozik.
 *  DE! 
 *  A foreign key csak "kiválasztja" ezt a sort (vagyis több key is hivatkozhat ugyanarra a 
 *  sorra) Emiatt a foreign táblát előbb importáljuk és abban már minden sornak meg kell lennie.
 *  Az extern key által hivatkozott sor része ennek az adatsornak, csak másik táblában tároljuk.
 *  (Emiatt pl. lehetnek azonosak is a sorok ebben a táblában) 
 *  Ilyenkor nem kikeressük a megfelelő sort az extern táblában, hanem létrehozunk egy új sort az
 *  hivatkozást tartalmazó adatsor számára.
 *  Fontos, hogy az extern táblákban NEM szabad exportálni (vagy legalábbis importálni) azokat a 
 *  sorokat, amelyekhez hivatkozó adatsorok tartoznak.
 *  
 * AZ exportálni kívánt részeket az add... metódusokkal adjuk hozzá.
 */
public class PortTable
    {
    /**
     * PortTable is part of the {@link GenericTable} class.
     * It is created in the constructor of {@link GenericTable#GenericTable()}
     * Which is (tables) are created inside {@link DatabaseMirror#start(Context)}
     * Context is added by {@link #setupContext(Context)} right after table instantiation.
     * (even before adding columns by {@link GenericTable#defineColumns()})
     * IMPORTANT:
     * {@link GenericTable#definePortColumns()} (addd... methods) is called AFTER Context is added!
     */
    private Context context;

    /** table to connect to
     *  TODO!!! It is part of each {@link PortRecord} class, it can be get from there */
    private GenericTable table;

    /** structure (columns) of the record for EACH version */
    private PortRecord[] recordVersions = new PortRecord[database().version()+1];


    private Cursor cursor;


    /** PortTable is part of the GenericTable class created right in the constructor of the GenericTable
     *  Context is added in the second step - but before anything else! */
    public PortTable(GenericTable table )
        {
        this.table = table;

        // Fel kell tölteni, nem lehet üres!
        for (int n = 0; n <= database().version(); n++)
            {
            // CONTEXT MÉG NULL LESZ!!!!
            recordVersions[n] = new PortRecord( table );
            }
        }

    /** Sets context right after the creation of the class */
    public void setupContext( Context context )
        {
        this.context = context.getApplicationContext();
        }

    /** Context for {@link PortRecord} */
    public Context getContext()
        {
        return context;
        }

    /** Returns recod structure for GIVEN version - needed by IMPORT */
    public PortRecord record(int version )
        {
        return recordVersions[ version ];
        }

    /** Returns recod structure for DATABASE CURRENT version - database always EXPORTS current version */
    public PortRecord record()
        {
        return record(database().version());
        }


    /************ A D D  C O L U M N S  T O  S T R U C T U R E *********************/


    /** Add DATA COLUMN */
    public void addIdColumnSomeVersions(int firstVersion, int lastVersion)
        {
        for (int n = firstVersion; n <= lastVersion; n++)
            {
            record(n).addColumn( new PortIdColumn( record(n) ));
            }
        }

    public void addIdColumn(int version)
        {
        addIdColumnSomeVersions( version, version );
        }

    public void addIdColumnFromVersion(int firstVersion )
        {
        addIdColumnSomeVersions( firstVersion, database().version());
        }

    public void addIdColumnAllVersions()
        {
        addIdColumnSomeVersions(0, database().version() );
        }


    /** Add DATA COLUMN */
    public void addColumnSomeVersions(int firstVersion, int lastVersion, int columnIndex)
        {
        for (int n = firstVersion; n <= lastVersion; n++)
            {
            record(n).addColumn( new PortDataColumn( columnIndex ));
            }
        }

    public void addColumn(int version, int columnIndex)
        {
        addColumnSomeVersions( version, version, columnIndex);
        }

    public void addColumnFromVersion(int firstVersion, int columnIndex)
        {
        addColumnSomeVersions( firstVersion, database().version(), columnIndex);
        }

    public void addColumnAllVersions(int columnIndex)
        {
        addColumnSomeVersions(0, database().version(), columnIndex);
        }


    /*********************************/


    /** Foreign Key */
    public void addForeignKeySomeVersions(int firstVersion, int lastVersion,
                                          int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        PortForeignKey portForeignKey = new PortForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices);

        for (int n = firstVersion; n <= lastVersion; n++)
            {
            record(n).addColumn(portForeignKey);
            }
        }

    public void addForeignKey(int version, int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        addForeignKeySomeVersions( version, version,
                foreignKeyIndex, foreignTableIndex, foreignColumnIndices );
        }

    public void addForeignKeyFromVersion(int firstVersion,
                                          int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        addForeignKeySomeVersions( firstVersion, database().version(),
                foreignKeyIndex, foreignTableIndex, foreignColumnIndices );
        }

    public void addForeignKeyAllVersions(int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        addForeignKeySomeVersions( 0, database().version(),
                foreignKeyIndex, foreignTableIndex, foreignColumnIndices );
        }


    /*********************************/


    /** Extern Key */
    public PortExternKey addExternKeySomeVersions(int firstVersion, int lastVersion,
                                         int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        PortExternKey portExternKey = new PortExternKey(
                externKeyIndex, externTableIndex, externColumnIndices);

        for (int n = firstVersion; n <= lastVersion; n++)
            {
            record(n).addColumn(portExternKey);
            }

        return portExternKey;
        }

    public PortExternKey addExternKey(int version, int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        return addExternKeySomeVersions( version, version,
                externKeyIndex, externTableIndex, externColumnIndices);
        }

    public PortExternKey addExternKeyFromVersion(int firstVersion,
                                         int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        return addExternKeySomeVersions( firstVersion, database().version(),
                externKeyIndex, externTableIndex, externColumnIndices);
        }

    public PortExternKey addExternKeyAllVersions(int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        return addExternKeySomeVersions( 0, database().version(),
                externKeyIndex, externTableIndex, externColumnIndices);
        }


    /*********** E X P O R T *********************
     *
     * EXPORT:
     * {@link #collateRowsToExport()} gets projection from {@link PortRecord#getProjection()} and
     * collates ALL records to a common cursor.
     * {@link #getNextRowToExport()} takes each row(record) and converts it to string to export.
     * {@link #closeCursorToExport()} should close cursor at the end.
     *
     * These methods are called by {@link AsyncTaskExport}
     * */


    /**
     * Lekérdezi a join tábla összes sorát az exportálni kívánt oszlopokra. A cursor értéket
     * az osztályban tárolja. Ezt majd a getNextRowToExport() fogja felhasználni, és a close() bezárni.
     * @return sorok száma
     */
    public int collateRowsToExport()
        {
        Scribe.debug(PORT, "PORT " + table.name() + ": Collating rows");

        List<String> projection = record().getProjection();

        // At TABLE level - only records with NULL SOURCE column should be exported
        // SOURCE column is NOT part of the columns added by table definitions, it should be added automatically
        // SOURCE columns are NOT exported, just the fact, that it HAS SOURCE, so only one SOURCE column is enough
        if ( table.hasSourceColumns() )
            {
            projection.add(column(table.SOURCE_TABLE));
            }

        cursor = context.getContentResolver().query( table.contentUri(),
                projection.toArray( new String [0]), null, null, null);

        if (cursor == null)
            {
            Scribe.debug(PORT, "PORT " + table.name() + ": is containing NO rows");
            return 0;
            }
        else
            {
            Scribe.debug(PORT, "PORT " + table.name() + ": is containing " + cursor.getCount() + " rows");
            return cursor.getCount();
            }
        }

    /**
     *
     * @return
     */
    String getNextRowToExport()
        {
        if ( cursor!= null && cursor.moveToNext() )
            {
            StringBuilder builder = new StringBuilder();

            builder.append( StringUtils.convertToEscaped( table.name() ));

            // SOURCE column is NOT part of the columns added by table definitions, it should be added automatically
            if ( table.hasSourceColumns() )
                {
                int column = cursor.getColumnIndexOrThrow( column (table.SOURCE_TABLE));
                builder.append('\t').append( cursor.isNull(column) ? "Standalone" : "Has source");
                }

            String[] data = record().createExportFromRecord( cursor ).toArray( new String [0]);
            for (int n=0; n < data.length; n++)
                {
                builder.append('\t');
                // Null ellenőrzés!!!
                builder.append( StringUtils.convertToEscaped( data[n] ));
                }

            builder.append('\n');

            Scribe.debug(PORT, "Exporting: " + builder.toString());
            return builder.toString();
            }
        else
            {
            Scribe.debug(PORT, "* Exporting " + table.name() + " is finished");
            return null;
            }
        }


    public void closeCursorToExport()
        {
        if (cursor != null)
            cursor.close();
        }


    /*********** I M P O R T *********************
     *
     * IMPORT:
     * Each individual record is created after read
     *
     * This is called by {@link AsyncTaskImport} */
    public void importRow(int version, String[] records)
        {
        Iterator<String> data = Arrays.asList( records ).iterator();

        // First element is ALWAYS table name; let's skip it!
        // TODO !!! It can be done by AsyncTaskImport previously !!!
        data.next();

        if ( table.hasSourceColumns() && data.hasNext() && data.next().equals("Has source") )
            {
            // These records will be created by their SOURCES - this line can be skipped
            return;
            }

        record(version).createRecordFromImport( data );
        }
    }
