package digitalgarden.mecsek.port;

import android.content.ContentValues;
import android.database.Cursor;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.Longtime;
import digitalgarden.mecsek.utils.StringUtils;
import digitalgarden.mecsek.utils.Utils;

import java.util.Iterator;
import java.util.List;

import static digitalgarden.mecsek.Debug.PORT;
import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnType;
import static digitalgarden.mecsek.generic.GenericTable.TYPE_STYLE;
import static digitalgarden.mecsek.generic.GenericTable.TYPE_DATE;
import static digitalgarden.mecsek.generic.GenericTable.TYPE_TEXT;


/**
 * PortDataColumn refers to a single column (identified by int columnIndex) containing simple data.
 * <ul>
 * <li><em>Projection</em> - adds ONE column name</li>
 * <li><em>Export</em> - adds ONE column data (as string)</li>
 * <li><em>Import</em> - gets ONE column data / pairs ONE known column name</li>
 * </ul>
 */
public class PortDataColumn implements GenericPortColumn
    {
    // index of the data column
    int columnIndex;


    /** Data column is only identified by its index */
    PortDataColumn(int columnIndex )
        {
        this.columnIndex = columnIndex;
        }


    /** Data column's name should be added to projection */
    @Override
    public void addToProjection( List<String> projection)
        {
        Scribe.debug(PORT, "Column (DATA) " + getKeyColumnName() + " is added to projection");

        projection.add( getKeyColumnName() );
        }


    /**
     * Cursor should be set to the current record.
     * Columns value (from the record) is added to export (strings)
     */
    @Override
    public void addToExport(Cursor cursor, List<String> data)
        {
        int col = cursor.getColumnIndexOrThrow( getKeyColumnName() );

        if ( columnType( columnIndex ) == TYPE_TEXT )
            {
            // Column value is string
            data.add( cursor.getString( col ));
            }
        else if ( columnType( columnIndex ) == TYPE_DATE )
            {
            // Column value is longtime as LONG. Longtime is exported in human readable string format
            Longtime longtime = new Longtime();
            longtime.set( cursor.getLong( col ));
            data.add( longtime.toString(false));
            // Ha hibás, akkor hol lesz jelzés??
            }
        else if ( columnType( columnIndex ) == TYPE_STYLE)
            {
            // Column value is longcolor as LONG. Longcolor is exported as LONG
            data.add(Utils.convertLongToString( cursor.getLong( col )));
            // Ha hibás, akkor hol lesz jelzés??
            }
        }


    /**
     * Returns key column name which is the column of the data. This data is exported
     */
    @Override
    public String getKeyColumnName()
        {
        return column(columnIndex);
        }


    /**
     * Exported value is read from the next data string and added to values to create the record from these values
     * @return true if data is valid, false if data is null or missing
     */
    @Override
    public boolean getFromImport(ContentValues values, Iterator<String> data)
        {
        if ( data.hasNext() )
            {
            String word = StringUtils.revertFromEscaped(data.next());

            if (word == null)
                {
                values.putNull(getKeyColumnName());
                return false;
                }

            // Convert form data.next() --> values.put(getKeyColumnName(), _CONVERTED DATA COMES HERE_ );
            if (columnType(columnIndex) == TYPE_TEXT)
                {
                // Can return NULL
                values.put(getKeyColumnName(), word);
                }
            else if (columnType(columnIndex) == TYPE_DATE)
                {
                Longtime longtime = new Longtime();
                longtime.setDate( word );
                values.put(getKeyColumnName(), longtime.get());
                }
            else if (columnType(columnIndex) == TYPE_STYLE)
                {
                // TODO Check if parse returns error!
                values.put(getKeyColumnName(), Utils.convertStringToLong( word ));
                }
            return true;
            }

        return false;
        }


    @Override
    public void onRecordCreated(long rowIndex)
        {
        // DO NOTHING, NO FURTHER CHANGES ARE NEEDED
        }
    }

