package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericListFragment;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


/**
 * table
 * foreignkey (columnindex)
 */

public class ForeignKey implements Connection.Connectable
    {
    private Connection connection;

    private GenericEditFragment editFragment;
    private int tableIndex; // lehet, hogy elég Connection-ben tárolni!
    private int columnIndex;

    // ForeignKey és kapcsolódó mezők közös selectorCode-ja, vagyis a selectorActivity requestCode-ja
    private int selectorCode = -1;

    // És a selector adatai
    private Class<?> selectorActivity;
    private String selectorTitle;
    private TextView selectorOwner;

    private long foreignKeyValue = -1L;

    private boolean edited = false;


    public ForeignKey(GenericEditFragment editFragment, int foreignKeyColumnIndex, int foreignTableIndex )
        {
        this.tableIndex = foreignTableIndex;
        this.columnIndex = foreignKeyColumnIndex;
        this.editFragment = editFragment;
        selectorCode = editFragment.getCode();

        connection = new Connection( editFragment.getContext(), tableIndex );
        }

    // selectorActivity - a megfelelő táblához tartozó GenericControllActivity
    // selectorTitle - selector címének eleje
    // selectorOwner - a jelenlegi elemet leginkább jellemző TextView
    public void setupSelector(final Class<?> selectorActivity, final String selectorTitle, final TextView selectorOwner)
        {
        this.selectorActivity = selectorActivity;
        this.selectorTitle = selectorTitle;
        this.selectorOwner = selectorOwner;
        }

    @Override
    public void addColumn( List<String> columns )
        {
        columns.add( column(columnIndex) );
        }


    public EditField addEditField(int editFieldId, int columnIndex )
        {
        final EditField editField = (EditField) editFragment.getView().findViewById( editFieldId );
        //editField.setBackground(null);
        editField.connect( editFragment, columnIndex );
        connection.add( editField );

        // link csak akkor lehetséges, ha a ForeignKey már az űrlaphoz kötött!!
        // és a selector-t beállítottuk
        if (editFragment == null || selectorActivity == null)
            {
            Scribe.error("Foreign Key was not connected to GenericEditFragment or Selector was not set!");
            throw new IllegalArgumentException("Foreign Key was not connected to GenericEditFragment or Selector was not set!");
            }

        // Beállítjuk, hogy érintésre a megfelelő selectorActivity elinduljon
        editField.setOnTouchListener( new View.OnTouchListener()
            {
            @Override
            public boolean onTouch(View v, MotionEvent event)
                {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                    Scribe.note("ForeignTextField: Selector started!");
                    Intent intent = new Intent( editFragment.getActivity(), selectorActivity);
                    intent.putExtra( GenericControllActivity.TITLE, selectorTitle + selectorOwner.getText() );
                    intent.putExtra( GenericListFragment.SELECTED_ITEM, getValue() );
                    editFragment.startActivityForResult( intent, selectorCode );
                    }
                return true; // nem engedjük mást sem csinálni
                }
            });

        return editField;
        }

    // Set: értékadás a listener-ek értesítésével
    public void setValue( long newId )
        {
        foreignKeyValue = newId;
        connection.pullData( foreignKeyValue);
        }

    // Get: id lekérdezés
    public long getValue()
        {
        return foreignKeyValue;
        }

    @Override
    public void pullData(Cursor cursor)
        {
        int column = cursor.getColumnIndexOrThrow(column(columnIndex));
        if (cursor.isNull(column))
            setValue(-1L);
        else
            setValue(cursor.getLong(column));
        }

    @Override
    public void pushData(ContentValues values)
        {
        if (getValue() >= 0)
            values.put( column(columnIndex), getValue());
        else
            values.putNull( column(columnIndex) );
        }

    @Override
    public void pushSource( int tableIndex, long rowIndex )
        {
        // No source for edit fields
        }

    /*
    Az egyes elemek állapotát nem kell elmenteni, hiszen azok változatlanok
     */
    @Override
    public void saveData(Bundle data)
        {
        data.putLong( column(columnIndex), getValue() );
        }

    @Override
    public void retrieveData(Bundle data)
        {
        // Ezzel le is kéri a hozzá tartozó adatokat, melyek elvileg változatlanok
        setValue( data.getLong( column(columnIndex) ) );
        }

    // ForeignKey ált. selectorActivity-ból való visszatérés során változik.
    // Ezzel a metódussal nézhetjük meg, hogy visszatérés után a konkrét példánynak kell-e változnia
    public void checkReturningSelector(int selectorCode, long id)
        {
        if (this.selectorCode == selectorCode && id != getValue())
            {
            setValue(id);
            edited = true;
            }
        }

    public boolean isEdited()
        {
        return edited;
        }
    }
