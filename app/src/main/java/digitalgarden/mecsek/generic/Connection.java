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

/**
 * A connection osztály köti össze az egy adatbázis sorhoz tartozó elemeket a field mezőkkel.
 * Egy connection példány egy tábla oszlopait tudja kezelni.
 * A field-ek megvalósítják a connectable interface-t.
 * A connectable interface:
 * - megadja a field-hez csatolt oszlop nevét
 * - pullData() hívásnál lehívja az oszlopban szereplő adatokat, és azokat átadja a field-eknek
 * - pushData() hívásnál feltölti az egyes field-ekben tárolt adatokat.
 */

public class Connection
    {
    public interface Connectable
        {
        void addColumn( List<String> columns );
        void pullData( Cursor cursor );
        void pushData( ContentValues values );
        void pushSource( int tableIndex, long rowIndex );
        void saveData(Bundle data);
        void retrieveData(Bundle data);
        boolean isEdited();
        }

    private Context context;
    private int tableIndex;
    private List<Connectable> connectables = new ArrayList<>();


    public Connection(Context context, int tableIndex)
        {
        this.context = context;
        this.tableIndex = tableIndex;
        }

    public void add(Connectable connectable)
        {
        connectables.add( connectable );
        }

    private Uri getItemContentUri( long rowIndex )
        {
        return Uri.parse( table( tableIndex ).contentUri() + "/" + rowIndex );
        }


    public void pullData( long rowIndex )
        {
        // Negatív rowIndex azt jelenti, hogy új adatsort készítünk
        if ( rowIndex < 0L )
            return;

        // A projection-t nem kell előre összerakni, mert a lekérés csak egyszer történik meg
        List<String> projection = new ArrayList<>();
        for ( Connectable connectable : connectables )
            {
            connectable.addColumn( projection );
            }

        //https://stackoverflow.com/questions/4042434/converting-arrayliststring-to-string-in-java
        Cursor cursor = context.getContentResolver().query( getItemContentUri( rowIndex ),
                projection.toArray(new String[0]), null, null, null );

        if (cursor != null) // Ez vajon kell?
            {
            cursor.moveToFirst();

            for ( Connectable connectable : connectables )
                {
                connectable.pullData( cursor );
                }

            // Always close the cursor
            cursor.close();
            }
        }

    // Az űrlap mezőinek értékét egy ContentValue-ba tesszük
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
            connectable.pushData(values);
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

        // rowIndex is ready; now we can set "source" for extern items
        for (Connectable connectable : connectables)
            {
            connectable.pushSource( tableIndex, rowIndex );
            }

        return rowIndex;
        }

    public void saveData( Bundle data )
        {
        for (Connectable connectable : connectables)
            {
            connectable.saveData( data );
            }
        }

    public void retrieveData( Bundle data )
        {
        for (Connectable connectable : connectables)
            {
            connectable.retrieveData( data );
            }
        }

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
