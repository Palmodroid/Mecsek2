package digitalgarden.mecsek.generic;

import android.content.Context;
import android.net.Uri;
import digitalgarden.mecsek.database.DatabaseMirror;
import digitalgarden.mecsek.tables.LibraryDatabase;

import static digitalgarden.mecsek.database.DatabaseMirror.addTableToDatabase;


/**
 * Subclasses of <em>GenericDatabase</em> abstract class contains description data of the database. These data can be
 * used to build, and then to reach the database, and to get data from the database. These data are the "mirror" of
 * tha database because they are similar of the constants which builds the database structure.
 * Subclass for <em>Mecsek</em> is {@link LibraryDatabase}
 */
public abstract class GenericDatabase
	{
	/** Name of the database (and the file of the database) */
    public abstract String name();

    /** Version of the database. (Should be incremented with structural changes - all data will be DELETED!) */
    public abstract int version();

    /** Authority of the database to use for URIs. Currently the package name of the Content Provider */
    public abstract String authority();

    /** Content count path ("/count") for URIs */
    public String contentCount()
        {
        return "/count";
        }

    /**
     * Definition of the tables, called by {@link digitalgarden.mecsek.database.DatabaseMirror#start(Context)}
     *
     * <p> Each table should be added. Referenced tables should precede tables containing the reference because
     * export/import routines. (References should be already ready when tables referencing them are loaded.)</p>
     * <p>DROP is just the opposite - referencing tables should be dropped first and references at the end.
     * But DROP is not used (outside version changes)</p>
     * <p>Use {@link #addTable(GenericTable)} to add tables. Store returned index for later reference!</p>
     * TABLES/ORDER CANNOT BE CHANGED without creating new version of the database!!!
     */
    public abstract void defineTables();

    /**
     * Adds table inside {@link #defineTables()}. Convenience method to call
     * {@link DatabaseMirror#addTableToDatabase(GenericTable)}
     * <p>Each table (new instances of {@link GenericTable} subclasses) should be added. Returned value (index ==
     * <em>id</em> of the table) should be stored for further reference.
     * {@link DatabaseMirror#table(int)} is used to identify tables.</p>
     * @param table new instances of {@link GenericTable} subclasse
     * @return <em>id</em> of the table
     */
    protected int addTable( GenericTable table )
        {
        return addTableToDatabase(table);
        }

    /**
     * Content URI for the whole database
     * It is also the leading part for all "lower" uri-s, like the content URIs of the tables
     */
    public Uri contentUri()
        {
        return Uri.parse("content://" + authority());
        }
	}
