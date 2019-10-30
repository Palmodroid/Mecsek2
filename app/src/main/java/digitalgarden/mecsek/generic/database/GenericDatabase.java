package digitalgarden.mecsek.generic.database;

import static digitalgarden.mecsek.database.DatabaseMirror.addTableToDatabase;

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
	}
