package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


// Ez a mező csak annyival tud többet, hogy az értékváltozást jelzi
// 18.06.14 - és belepakoljuk a hozzárendelt értékekekt is
// 18.07.22 - Lett belőle egy külön EditFieldText
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

    public void pullData(Cursor cursor )
        {
        setText(cursor.getString(cursor.getColumnIndexOrThrow( column( columnIndex ))));
        }

    public void pushData(ContentValues values)
        {
        values.put(column( columnIndex ), getText().toString() );
        }
    }
