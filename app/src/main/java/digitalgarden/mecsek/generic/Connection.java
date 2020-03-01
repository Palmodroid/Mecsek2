package digitalgarden.mecsek.generic;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.DatabaseMirror.table;

/*
 * A connection osztály köti össze az egy adatbázis sorhoz tartozó elemeket a field mezőkkel.
 * Egy connection példány egy tábla oszlopait tudja kezelni.
 * A field-ek megvalósítják a connectable interface-t.
 * A connectable interface:
 * - megadja a field-hez csatolt oszlop nevét
 * - pullData() hívásnál lehívja az oszlopban szereplő adatokat, és azokat átadja a field-eknek
 * - pushData() hívásnál feltölti az egyes field-ekben tárolt adatokat.
 */

/**
 * Connection connects columns (of one database record) with field of the form (layout).
 * <p>One Connection instance handels only ONE table (foreign/extern keys should have independent connection
 * instances). Database COLUMNS are stored inside fields (connectables). <em>Row index</em> should be set in each
 * method independently, because it can change (??? Class level row index should be better ???).</p>
 * <ul>
 * <li>PULL: Connection collects column(s) of each field for a common projection ({@link Connectable#addColumnToProjection(List)}
 * then pulls data from database. Each field can take its data from the Cursor during
 * {@link Connectable#getDataFromPull(Cursor)} </li>
 * <li>PUSH: Connection collects column/data pairs from each field {@link Connectable#addDataToPush(ContentValues)} than
 * inserts/updates row in database </li>
 * <li>SAVE/RETRIEVE: Each field gets the pussibiliti to save/retrive their data during configuration changes.
 * (Because these data are sometimes independent form View data (ex.keys)) </li>
 * </ul>
 * <p>All fields shoud implement {@link Connectable} interface!</p>
 */
public class Connection
    {

    /** Connectable interface should be implemented by each field - to connect it to the database */
    public interface Connectable
        {
        /** add column(s) to projection to pull data */
        void addColumnToProjection(List<String> projection );

        /** cursor already contains pulled data for all columns given by
         * { @link Connectable#addColumnToProjection(java.util.List)} */
        void getDataFromPull(Cursor cursor );

        /** column/data pairs should be added to values and - at the end - these data will be inserted/added to
         * database */
        void addDataToPush(ContentValues values );

        void pushSource( int tableIndex, long rowIndex );

        /** add data to save during config. changes */
        void saveData(Bundle data);

        /** retrieve data after config. changes */
        void retrieveData(Bundle data);

        /** return true if this filed was edited */
        boolean isEdited();
        }

    private Context context;
    private int tableIndex;
    private List<Connectable> connectables = new ArrayList<>();


    /**
     * Connection can work with ONLY ONA table defined here. Foreign/Extern tables should have independent
     * connection instances!
     * @param context contex
     * @param tableIndex table of the connection
     */
    public Connection(Context context, int tableIndex)
        {
        this.context = context;
        this.tableIndex = tableIndex;
        }

    /**
     * Each FIELD should implement connectable interface to pull/push data of database. These fields (of ONE table!)
     * should be added to Connection
     * @param connectable filed implemented connectable interface
     */
    public void add(Connectable connectable)
        {
        connectables.add( connectable );
        }

    /**
     * Generates URI for the given row in the table of the Connection
     * @param rowIndex row index (table is defined by Connection)
     * @return URI for this specific record
     */
    private Uri getItemContentUri( long rowIndex )
        {
        return table( tableIndex ).itemContentUri( rowIndex );
        // Uri.parse( table( tableIndex ).contentUri() + "/" + rowIndex );
        }


    /**
     * Pulls data (from ONE table) for each Fields added to this Connection
     * @param rowIndex row index (can be < 0L if new record is created. In this case pull doesn't do anything)
     */
    public void pullData( long rowIndex )
        {
        // Negatív rowIndex azt jelenti, hogy új adatsort készítünk
        // Néhány Field (pl. StyleButton) igényli az inicializálást, ezt tehetjük meg itt
        if ( rowIndex < 0L )
            {
            return;
            }

        // A projection-t nem kell előre összerakni, mert a lekérés csak egyszer történik meg
        List<String> projection = new ArrayList<>();
        for ( Connectable connectable : connectables )
            {
            connectable.addColumnToProjection( projection );
            }

        //https://stackoverflow.com/questions/4042434/converting-arrayliststring-to-string-in-java
        Cursor cursor = context.getContentResolver().query( getItemContentUri( rowIndex ),
                projection.toArray(new String[0]), null, null, null );

        if (cursor != null) // Ez vajon kell?
            {
            cursor.moveToFirst();

            for ( Connectable connectable : connectables )
                {
                connectable.getDataFromPull( cursor );
                }

            // Always close the cursor
            cursor.close();
            }
        }

    /**
     *  Collects column/data pairs from each field in this connection and pushes data to ONE table.
     * @param rowIndex index of the record to update with collected data (or <0 to insert data as a new record)
     * @return row index of the pushed record
     */
    public long pushData( long rowIndex )
        {
        ContentValues values = new ContentValues();

        if ( context == null )
            {
            Scribe.note("IMPOSSIBLE! ACTIVITY MISSING!!!");
            return -1L;
            }

        for (Connectable connectable : connectables)
            {
            connectable.addDataToPush(values);
            }

        if (rowIndex < 0L) // add item
            {
            try
                {
                Uri uri = context.getContentResolver().insert( table( tableIndex ).contentUri(), values);
                rowIndex = ContentUris.parseId(uri);
                }
            catch (Exception e)
                {
                Toast.makeText( context, "ERROR: Add item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
                }
            }

        else                // update item
            {
            try
                {
                context.getContentResolver().update( getItemContentUri( rowIndex ), values, null, null);
                }
            catch (Exception e)
                {
                Toast.makeText( context, "ERROR: Update item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
                }
            }

        // row index of MAIN record is ready; now we can set MAIN "source" inside EXTERN Records
        // {@link ExternKey#pushSource(int, long)}
        for (Connectable connectable : connectables)
            {
            connectable.pushSource( tableIndex, rowIndex );
            }

        return rowIndex;
        }

    /** Each field can save its data during config changes */
    public void saveData( Bundle data )
        {
        for (Connectable connectable : connectables)
            {
            connectable.saveData( data );
            }
        }

    /** Each field can retrieve data after config changes */
    public void retrieveData( Bundle data )
        {
        for (Connectable connectable : connectables)
            {
            connectable.retrieveData( data );
            }
        }

    /** Return true if any of the connected fields was edited */
    public boolean isEdited()
        {
        for (Connectable connectable : connectables)
            {
            if ( connectable.isEdited() )
                return true;
            }
        return false;
        }
    }
