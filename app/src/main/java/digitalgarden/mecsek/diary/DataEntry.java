package digitalgarden.mecsek.diary;

import digitalgarden.mecsek.utils.Longtime;


/**
 * DataEntry stores data for one entry of the calendar table.
 * This is a copy of a row from the database.
 */
public class DataEntry
    {
    private final long id;
    private final String note;
    private final Longtime date;

    /** Constructor with all elements */
    public DataEntry(long id, String note, Longtime date)
        {
        this.id = id;
        this.note = note;
        this.date = date;
        }

    /** Get ID of the row */
    public long getId()
        {
        return id;
        }

    /** Get note (as string) */
    public String getNote()
        {
        return note;
        }

    /** Get date (as longtime) */
    public Longtime getDate()
        {
        return date;
        }
    }
