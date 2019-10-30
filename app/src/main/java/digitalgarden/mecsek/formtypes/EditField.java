package digitalgarden.mecsek.formtypes;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;


import java.util.List;

import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


// Ez a mező csak annyival tud többet, hogy az értékváltozást jelzi
// 18.06.14 - és belepakoljuk a hozzárendelt értékekekt is
public abstract class EditField extends AppCompatEditText implements Connection.Connectable
	{
    public EditField(Context context)
    	{
        super(context);
    	}

    public EditField(Context context, AttributeSet attrs)
    	{
        super(context, attrs);
    	}

    public EditField(Context context, AttributeSet attrs, int defStyle)
    	{
        super(context, attrs, defStyle);
    	}

    protected int columnIndex;

    public void addColumn( List<String> columns )
        {
        columns.add( column(columnIndex) );
        }

    private boolean edited = false;

    // Pl. EditFieldDate automatikusan javítja a beírást focus váltásakor.
    public boolean isEdited()
        {
        return edited;
        }

    protected void clearEdited()
        {
        edited = false;
        }

    public void connect(final GenericEditFragment form, int columnIndex)
		{
        this.columnIndex = columnIndex;

		addTextChangedListener(new TextWatcher() 
        	{
	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) 
	        	{
	        	// A felhasználó csak Resumed állapotban változtat, egyébként értékadás történt!
	        	if (form.isResumed())
                    {
                    edited = true;
                    }
	        	}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
				;
				}

			@Override
			public void afterTextChanged(Editable s)
				{
				;
				} 
        	});
		}

    @Override
    public void saveData(Bundle data)
        {
        // Nincs rá szükség
        }

    @Override
    public void retrieveData(Bundle data)
        {
        // Nincs rá szükség
        }

    @Override
    public void pushSource( int tableIndex, long rowIndex )
        {
        // No source for edit fields
        }

    public void setHint( Bundle arguments, String hintKey )
        {
        String hint = arguments.getString( hintKey );
        if ( hint != null )
            {
            setText( hint );
            }
        }
    }
