package digitalgarden.mecsek.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;

import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.database.GenericTable;

import static digitalgarden.mecsek.Debug.CP;
import static digitalgarden.mecsek.database.DatabaseMirror.allTables;
import static digitalgarden.mecsek.database.DatabaseMirror.match;


public class DatabaseContentProvider extends ContentProvider
	{
	private DatabaseOpenHelper databaseOpenHelper;

	// MIME Type
	// Minden, ami az egyes táblákra jellemző, átment a GenericDatabase alosztályaiba
	

	@Override
	public boolean onCreate()
		{
		Scribe.note(CP, "CONTENTPROVIDER: onCreate");

        DatabaseMirror.start( getContext() );
		databaseOpenHelper = new DatabaseOpenHelper( getContext() );
		return true; 
		}

	
	@Override
	public String getType(Uri uri)
		{
		// MIME típust ad vissza
		return null;
		}
	
	
	// Az Uri a szükséges táblát adja meg (nem egy elemet!) melybe az adatokat be kívánjuk illeszteni
	// A Visszatérési URi ezzel szemben a konkrét beillesztett elem
	@Override
	public Uri insert(Uri uri, ContentValues values)
        {
        Uri uriInserted = null;

        SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
        int uriType = match(uri);

        for (GenericTable table : allTables())
            {
            uriInserted = table.insert(db, uri, uriType, values);
            if (uriInserted != null)
                break;
            }

        if (uriInserted == null)
            throw new IllegalArgumentException("Unknown URI: " + uri);

        getContext().getContentResolver().notifyChange(uri, null);

        Scribe.note( CP, "CONTENTPROVIDER: " + uriInserted + " inserted");
        return uriInserted;
        }


	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs)
		{
		int rowsDeleted = -1;

		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		int uriType = match(uri);

        for (GenericTable table : allTables())
            {
            rowsDeleted = table.delete( db, uri, uriType, whereClause, whereArgs);
            if ( rowsDeleted != -1 )
                break;
            }

        if ( rowsDeleted == -1 )
			throw new IllegalArgumentException("Unknown URI: " + uri);

		if (rowsDeleted > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		Scribe.note( CP, "CONTENTPROVIDER: " + rowsDeleted + " rows deleted");
		return rowsDeleted;
		}
	
	
	@Override
	public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) 
		{
		int rowsUpdated = -1;

		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		int uriType = match(uri);

        for (GenericTable table : allTables())
            {
            rowsUpdated = table.update( db, uri, uriType, values, whereClause, whereArgs);
            if ( rowsUpdated != -1 )
                break;
            }

        if ( rowsUpdated == -1 )
            throw new IllegalArgumentException("Unknown URI: " + uri);

		if (rowsUpdated > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		Scribe.note( CP, "CONTENTPROVIDER " + rowsUpdated + " rows updated");
		return rowsUpdated;
		}

	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
		{
		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		int uriType = match(uri);
        boolean ready = false;

        for (GenericTable table : allTables())
            {
            ready = table.buildQuery(uri, uriType, queryBuilder);
            if (ready)
                {
                projection = table.buildProjection( uriType, projection );
                break;
                }
            }

        if ( !ready )
            throw new IllegalArgumentException("Unknown URI: " + uri);

		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		Scribe.note( CP, "CONTENTPROVIDER " + " queried");
		return cursor;
		}

    public final static String DROP_METHOD = "drop";

    @Override
    public Bundle call(String method, String arg, Bundle extras)
        {

        // DROP nem használható biztonságosan, mert a foreign constraint miatt vagy nem
        // töröl (mert még létezik a másik tábla), vagy ON DELETE CASCADE esetén keresi a másik
        // táblát, amit kitöröltünk. Érdekes, hogy update estén működik
        if ( DROP_METHOD.equals(method) )
            {
            // SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
            // databaseOpenHelper.drop(db);
            // databaseOpenHelper.onCreate(db);
            // No close is needed in contet provider
            }

        return(null);
        }
	}
