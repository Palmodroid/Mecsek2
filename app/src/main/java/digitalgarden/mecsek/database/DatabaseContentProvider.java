package digitalgarden.mecsek.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;

import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.GenericTable;

import static digitalgarden.mecsek.Debug.CP;
import static digitalgarden.mecsek.database.DatabaseMirror.allTables;
import static digitalgarden.mecsek.database.DatabaseMirror.match;


/**
 * {@link DatabaseContentProvider} provides access to the database. {@link #onCreate()} is the entry point for the
 * whole database.
 * Standard CRUD methods are:
 * <ul>
 * <li>{@link #insert(Uri, ContentValues)},</li>
 * <li>{@link #delete(Uri, String, String[])}, </li>
 * <li>{@link #update(Uri, ContentValues, String, String[])} and </li>
 * <li>{@link #query(Uri, String[], String, String[], String)}</li>
 * </ul>
 * <p>Delete should not be used, <em>DELETE FLAG</em> is a better idea. It is not implemented yet, only standard
 * delete.</p>
 * <p>Upsert: Insert with overwrite is not a good idea, because it deletes record and all foreign connections first,
 * and then inserts the new record. Upsert is better, because it inserts record with a given id (if not exists yet)
 * or updates the record with the given id (if it already exists) - but never deletes it.</p>
 * <p>There are two possible ways to call upsert: <em>INSERT/ITEMID</em> or <em>UPDATE/DIRID and _id among the
 * values</em> NOne of them were allowed before upsert. Both possibilities are implemented experimentally.</p>
 */
public class DatabaseContentProvider extends ContentProvider
	{
	// DatabaseOpenHelper loaded in onCreate and used by the CRUD methods
	private DatabaseOpenHelper databaseOpenHelper;

	// MIME Type
	// Minden, ami az egyes táblákra jellemző, átment a GenericDatabase alosztályaiba

    /**
     * Entry point for database access
     *
     * <p>Starts {@link DatabaseMirror} with context, which will create a "mirror" of the database structure. The data
     * of the mirror helps identify data (tables etc.) inside the database.</p>
     * <p>Opens {@link DatabaseOpenHelper} which helps to open database inside the CRUD methods.</p>
     * <p>({@link DatabaseOpenHelper#onCreate(SQLiteDatabase)} will create all the tables.)</p>
     *
     * @return true, becasue Content Provider was able to start :)
     */
	@Override
	public boolean onCreate()
		{
		Scribe.note(CP, "CONTENTPROVIDER: onCreate");

        DatabaseMirror.start( getContext() );
		databaseOpenHelper = new DatabaseOpenHelper( getContext() );
		return true; 
		}

	/** !! Type is not implemented yet !! */
	@Override
	public String getType(Uri uri)
		{
		// MIME típust ad vissza
		return null;
		}
	
	
    /**
     * Inserts a new record (defined by <em>ContentValues</em>) into the table defined by <em>Uri</em>. All tables
     * are called. If the called table identifies itself and inserts the new row then it will return the uri of the
     * row. At this point this method notifies all the observers (depending on uri) and finishes.
     * Work is performed by {@link GenericTable#insert(SQLiteDatabase, Uri, int, ContentValues)}
     * @param uri uri of the table to insert into (and NOT the record!)
     *            <em>JUST TRYING!</em> uri of the record means, that record will be inserted/updated with the given _id
     * @param values values of the record (without _id)
     * @return uri of the inserted record
     * @throws IllegalArgumentException if no table was found with the given uri (and no record was inserted)
     */
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


    /**
     * Updates an existing record (defined by Uri) with the values (given by <em>ContentValues</em>). All tables are
     * called. If the called table identifies itself and updates the row then it will return 1, as one row is
     * updated. At this point this method notifies all the observers (depending on uri) and finishes.
     * <em>WHERE</em> parameters can be used.
     * Work is performed by {@link GenericTable#update(SQLiteDatabase, Uri, int, ContentValues, String, String[])}
     * Experimentally a UPSERT is implemented: URI (DIRID) defines table, and _id should be given among values.
     * @param uri uri of the record to be updated (with the given _id)
     *            <em>JUST TRYING!</em> uri of the table to insert into (and _id among values)
     * @param values values of the record (without _id / or with _id for UPSERT )
     * @param whereClause
     * @param whereArgs
     * @return number of updated rows
     * @throws IllegalArgumentException if no record was found with the given uri (and no record was updated)
     */
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


    /**
     * Queries database and returns cursor.
     * <ol>
     * <li>Each {@link GenericTable#buildQuery(Uri, int, SQLiteQueryBuilder)} is called, </li>
     * <li>If table was found {@link GenericTable#buildProjection(int, String[])} is called just to change projection
     * to count, if needed, </li>
     * <li>Database is queried using selection parameters, </li>
     * <li>and listeners are notified. </li>
     * </ol>
     * If no table can be identified than an IllegalArgumentException is thrown.
     * @param uri URI of one table / one item / count
     * @param projection string array of names of the needed columns (use {@link DatabaseMirror#column(int)}
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return cursor containing the results
     * @throws IllegalArgumentException if no uri can be identified
     */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
		{
		// Using SQLiteQueryBuilder instead of query() method
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
