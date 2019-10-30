package digitalgarden.mecsek.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

import digitalgarden.mecsek.generic.database.GenericTable;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.Debug.DB;
import static digitalgarden.mecsek.database.DatabaseMirror.allTables;
import static digitalgarden.mecsek.database.DatabaseMirror.database;


public class DatabaseOpenHelper extends SQLiteOpenHelper
	{
    public DatabaseOpenHelper(Context context )
		{
		super(context, database().name(), null, database().version());
		Scribe.debug(DB, "DATABASE: Database constructed");
        }
	
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db)
		{
        db.setLocale( Locale.getDefault() );
        for (GenericTable table : allTables() )
            {
            table.create( db );
            }
		}
	
	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
        for (GenericTable table : allTables() )
            {
            table.drop( db );
            }
        // Create tables again
		onCreate(db);
		}


	// CheckColumns megoldása az egyes ...Database osztályokba kerülhet, jelenleg töröltük.
	
	// http://stackoverflow.com/a/3266882
	// http://code.google.com/p/android/issues/detail?id=11607
	// A foreign keys constraint-et engedélyezni kell. Csak 3.6.19 felett, azaz 2.2 verzió felett elérhető
	// Sajnos úgy tűnik, hogy a beindítása sem egyszerű, minden megnyitáskor ki kell adni
	@Override
	public void onOpen(SQLiteDatabase db) 
		{
	    super.onOpen(db);
	    if (!db.isReadOnly()) 
	    	{
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    	}
		}
	}
