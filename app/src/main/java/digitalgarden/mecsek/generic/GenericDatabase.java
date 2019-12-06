package digitalgarden.mecsek.generic;

import android.net.Uri;

import static digitalgarden.mecsek.database.DatabaseMirror.addTableToDatabase;
import static digitalgarden.mecsek.database.DatabaseMirror.database;

public abstract class GenericDatabase
	{
    public abstract String name();

    public abstract int version();

    public abstract String authority();

    public String contentCount()
        {
        return "/count";
        }

    public abstract void defineTables();

    protected int addTable( GenericTable table )
        {
        return addTableToDatabase(table);
        }

    public Uri contentUri()
        {
        return Uri.parse("content://" + authority());
        }
	}
