package digitalgarden.mecsek.fieldtypes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


/**
 * TEXT type field
 */
public class EditFieldText extends EditField
	{
    public EditFieldText(Context context)
    	{
        super(context);
    	}

    public EditFieldText(Context context, AttributeSet attrs)
    	{
        super(context, attrs);
    	}

    public EditFieldText(Context context, AttributeSet attrs, int defStyle)
    	{
        super(context, attrs, defStyle);
    	}

    /** pulls text data of Field's Column */
    public void getDataFromPull(Cursor cursor )
        {
        setText(cursor.getString(cursor.getColumnIndexOrThrow( column( columnIndex ))));
        }

    /** push text data of Field's Column */
    public void addDataToPush(ContentValues values)
        {
        values.put(column( columnIndex ), getText().toString() );
        }
    }
