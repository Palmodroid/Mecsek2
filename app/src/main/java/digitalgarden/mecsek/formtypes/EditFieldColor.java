package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


/**
 * COLOR is an unsigned 4-byte integer (AARRGGBB), which is int in java, but stored as
 * (long in java, INTEGER in SQLite)
 * IT should be stored and retrieved as long, otherwise it would be stored as eg. string.
 *
 * Anyway, to use this EditFieldColor needed:
 * - checking at focus change (like date)
 * - separate background and ink colors
 */
public class EditFieldColor extends EditField
	{
    public EditFieldColor(Context context)
    	{
        super(context);
    	}

    public EditFieldColor(Context context, AttributeSet attrs)
    	{
        super(context, attrs);
    	}

    public EditFieldColor(Context context, AttributeSet attrs, int defStyle)
    	{
        super(context, attrs, defStyle);
    	}

    public void pullData(Cursor cursor )
        {
        // !! cursor.getType() could automate this project!

        long value = cursor.getLong(cursor.getColumnIndexOrThrow( column( columnIndex )));
        setText( Long.toHexString( value ) );
        }

    public void pushData(ContentValues values)
        {
        long value = Long.parseLong( getText().toString(), 16 );
        values.put(column( columnIndex ), value );
        }
    }
