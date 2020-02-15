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
import digitalgarden.mecsek.generic.GenericCombinedListFragment;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


/**
 * table
 * foreignkey (columnindex)
 */
/**
 * Foreign key points to a row in a foreign table. Foreign key can be changed to point to an other foreign record,
 * but values of the foreign record will not change throgh foreign key.
 * <p>Extern key always points to the same extern record. Extern key will never change, but the values of the extern
 * record can change.</p>
 * <p>MAIN TABLE - foreign key column (changes) --> FOREIGN TABLE - foreign columns </p>
 */
public class ForeignKey implements Connection.Connectable, GenericEditFragment.UsingSelector
    {
    /** Connection of the foreign table contains fields of the foreign columns */
    private Connection foreignConnection;

    /** Form of the table - should be stored because Fields could be added to the form under ForeignKey */
    private GenericEditFragment editFragment;

    /** Index of the Foreign Key Column inside MAIN table */
    private int foreignKeyColumnIndex;

    /** Common CODE between Foreign Key and selectorActivity Request Code
     *  With this common code selectorActivity can identify its calling ForeignKey */
    private int selectorCode = -1;

    /** Parameters of the selectorActivity defined by {@link #setupSelector(Class, String, TextView)} */
    private Class<?> selectorActivity;
    private String selectorTitle;
    private TextView selectorOwner;

    /** Actual value of the Foreign Key Column */
    private long foreignKeyValue = -1L;

    /** TRUE if value of the Foreign Key was changed */
    private boolean edited = false;

    /**
     * Constructor to create foreign key. Fields of the Foreign Table can be added later to this ForeignKey.
     * @param editFragment form of the fields
     * @param foreignKeyColumnIndex index of the Foreign Key Column (inside MAIN table)
     * @param foreignTableIndex index of the Foreign Table (stored inside foreignConnection)
     */
    public ForeignKey(GenericEditFragment editFragment, int foreignKeyColumnIndex, int foreignTableIndex )
        {
        this.editFragment = editFragment;
        this.foreignKeyColumnIndex = foreignKeyColumnIndex;
        selectorCode = editFragment.getCode();

        foreignConnection = new Connection( editFragment.getContext(), foreignTableIndex );
        }

    /**
     * Selector activity will select one record from the foreign table and give back its row index
     * @param selectorActivity ControllActivity of the foreign table
     * @param selectorTitle Beginning of the title
     * @param selectorOwner TextView descripting current item to choose
     */
    public void setupSelector(final Class<?> selectorActivity, final String selectorTitle, final TextView selectorOwner)
        {
        this.selectorActivity = selectorActivity;
        this.selectorTitle = selectorTitle;
        this.selectorOwner = selectorOwner;
        }

    /** Adds ForeignKey column to the Connection of the MAIN table. FOREIGN KEY is the only column needed from MAIN
     * TABLE. */
    @Override
    public void addColumnToProjection(List<String> projection)
        {
        projection.add( column(foreignKeyColumnIndex) );
        }

    /**
     * Adds an EDIT FIELD (any subclass) to a FOREIGN COLUMN of the FOREIGN TABLE.
     * <p>On touch Selector Activity is started to select one record of the Foreign Table. {@link #selectorCode} is
     * unique to this Foreign Key, so Selector Activity can found it.</p>
     * ??? What happens with Selector Code on config changes ??? Maybe nothing, because foreign keys get selector
     * code in the same order. Selector code could be te index of the ForeignKeyArray, too !!!
     * @param editFieldId id of the field
     * @param foreignColumnIndex index of the column (inside extern table)
     * @return the created, connected editField
     */
    public EditField addEditField(int editFieldId, int foreignColumnIndex )
        {
        final EditField editField = (EditField) editFragment.getView().findViewById( editFieldId );
        //editField.setBackground(null);
        editField.connect( editFragment, foreignConnection, foreignColumnIndex );
        //foreignConnection.add( editField ); added to connect, not needed any more

        // link csak akkor lehetséges, ha a ForeignKey már az űrlaphoz kötött!!
        // és a selector-t beállítottuk
        if (editFragment == null || selectorActivity == null)
            {
            Scribe.error("Foreign Key was not connected to GenericEditFragment or Selector was not setStyle!");
            throw new IllegalArgumentException("Foreign Key was not connected to GenericEditFragment or Selector was not setStyle!");
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
                    intent.putExtra( GenericCombinedListFragment.SELECTED_ITEM, getValue() );
                    editFragment.startActivityForResult( intent, selectorCode );
                    }
                return true; // nem engedjük mást sem csinálni
                }
            });

        return editField;
        }

    /**
     * Set a new value means: Setting up foreignKeyValue (not pushed to database yet) and refresh fields of foreign
     * table through foreignConnection
     * @param newId new id of the foreign record
     */
    public void setValue( long newId )
        {
        foreignKeyValue = newId;
        foreignConnection.pullData( foreignKeyValue );
        }

    /**
     * Value of foreignKey (not certanly pushed to the database yet)
     * @return value of foreignKey
     */
    public long getValue()
        {
        return foreignKeyValue;
        }

    /** ForeignKey is PULLED from MAIN Connection. Can be null if not yet defined */
    @Override
    public void getDataFromPull(Cursor cursor)
        {
        int column = cursor.getColumnIndexOrThrow(column(foreignKeyColumnIndex));
        if (cursor.isNull(column))
            setValue(-1L);
        else
            setValue(cursor.getLong(column));
        }

    /** ForeignKey (or null if not yet defined) is added to PUSH of MAIN Connection */
    @Override
    public void addDataToPush(ContentValues values)
        {
        if (getValue() >= 0)
            values.put( column(foreignKeyColumnIndex), getValue());
        else
            values.putNull( column(foreignKeyColumnIndex) );
        }

    /** ForeignKeys do not have any SOURCE (only externKeys has got sources */
    @Override
    public void pushSource( int tableIndex, long rowIndex )
        {
        // No source for foreign fields
        }

    /** Save foreignKey value during config changes. Foreign fields cannot change so save of the foreignConnection is
     * not needed */
    @Override
    public void saveData(Bundle data)
        {
        data.putLong( column(foreignKeyColumnIndex), getValue() );
        }

    /** Retrieve foreignKey value on config changes */
    @Override
    public void retrieveData(Bundle data)
        {
        // Ezzel le is kéri a hozzá tartozó adatokat, melyek elvileg változatlanok
        setValue( data.getLong( column(foreignKeyColumnIndex) ) );
        }

    // ForeignKey ált. selectorActivity-ból való visszatérés során változik.
    // Ezzel a metódussal nézhetjük meg, hogy visszatérés után a konkrét példánynak kell-e változnia
    /**
     * Activity (when SelectorActivity returns) checks here which ForeignKey called SelectorActivity. If selector
     * code is identical then value is set.
     * @param selectorCode unique selector code (returned by Selector activity)
     * @param data data returned by Activity, containing id of the newly selected foreign record
     */
    public void checkReturningSelector(int selectorCode, Intent data)
        {
        if (this.selectorCode == selectorCode)
            {
            long selectedId = data.getLongExtra(GenericCombinedListFragment.SELECTED_ITEM,
                    GenericCombinedListFragment.SELECTED_NONE);

            if ( selectedId != getValue() ) edited = true;
            // Foreign values could change during the run of the external activity - these values should be repulled,
            // even if value is nOT changed
            setValue(selectedId);
            }
        }

    public boolean isEdited()
        {
        return edited;
        }
    }
