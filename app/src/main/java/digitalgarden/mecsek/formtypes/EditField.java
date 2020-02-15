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


/**
 * Base class for all fields
 * <p>PUSH and PULL methods should be implemented by subclasses!!!</p>
 */
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

    /** Field's column - field shows/sets data of this column */
    protected int columnIndex;

    /** Adds Field's column to projection to query data */
    public void addColumnToProjection(List<String> projection)
        {
        projection.add( column(columnIndex) );
        }

    /** Flag to check if field was edited */
    private boolean edited = false;

    /** Returns TRUE if field was edited */
    public boolean isEdited()
        {
        return edited;
        }

    /** clears edited flag - eg. {@link EditFieldDate} writes date in different types: text is changed, but value was
     *  not edited */
    protected void clearEdited()
        {
        edited = false;
        }

    /**
     * Connect field with column (database).
     * <p>Field should be added to Connection to be connected to database! Table Id is stored inside Connection.
     * (??? These to cannot be added together ???)</p>
     * @param form form ({@link GenericEditFragment} of the field - needed only by onTextChanged
     * @param columnIndex field's column stored inside field
     */
    public void connect(final GenericEditFragment form, Connection connection, int columnIndex)
		{
		// column index (or indices) are stored inside field
        // table index (only one) is stored inside connection
        // so connect will add both data at the same place, connection.add is not needed as a separate row

        this.columnIndex = columnIndex;
        connection.add( this );

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

    /** Not needed - views (fields) can save their data in most cases */
    @Override
    public void saveData(Bundle data)
        {
        // Nincs rá szükség
        }

    /** Not needed - views (fields) can save their data in most cases */
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

    /** Hint can be set from Intent
     *  <p>??? It will be overwritten by pulled data ???</p> */
    public void setHint( Bundle arguments, String hintKey )
        {
        String hint = arguments.getString( hintKey );
        if ( hint != null )
            {
            setText( hint );
            }
        }
    }
