package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.Longtime;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


/**
 * EditFiledDate shows a text field for a date
 * Date is stored as longtime, so text is updated on focus change and on exit.
 * If has focus than no textual date is showed (during edit),
 * Otherwise full date is shown.
 */
public class EditFieldDate extends EditField implements View.OnFocusChangeListener
    {
    /*
    Longtime value of the field
    NOT correct during string edit (== when field has focus)
    Should be corrected - only when has focus!:
    - onFocusChange
    - on exit (isEdited check)
    - on exit (saveData)
     */
    private Longtime longtime = new Longtime();

    /*
    Helps to check edit:
    Set to longtime after pull, push and hint (clearEdited())
     */
    private long originalLongtime;


    public EditFieldDate(Context context)
        {
        super(context);
        init();
        }

    public EditFieldDate(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        init();
        }

    public EditFieldDate(Context context, AttributeSet attrs, int defStyle)
        {
        super(context, attrs, defStyle);
        init();
        }

    private void init()
        {
        setOnFocusChangeListener(this);
        }

    /**
     * Date hint can be given among the arguments.
     * Called before pull/retrieve
     */
    public void setHint( Bundle arguments, String hintKey )
        {
        longtime.set( arguments.getLong(hintKey) );
        setTextFromTime(!hasFocus());
        clearEdited();
        }

    /**
     * If focus goes away from field, than longtime should be updated
     * If has focus - date should be showed without texts
     * If hes NOT focus - date should be showed with texts
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus)
        {
        Scribe.debug(((EditFieldDate) v).getText().toString() + " has focus: " + hasFocus);
        if (!hasFocus)
            {
            getTimeFromText();
            }
        setTextFromTime(!hasFocus);
        }

    /**
     * Updates longtime from text
     */
    private void getTimeFromText()
        {
        if (longtime.setDate(getText().toString()))
            {
            Toast.makeText(getContext(), "Date is set: " + longtime.toString(false), Toast.LENGTH_SHORT).show();
            }
        }

    /**
     * Updates text from longtime
     * @param isTextEnabled true, if text should be inserted
     */
    private void setTextFromTime(boolean isTextEnabled)
        {
        setText(longtime.toString(isTextEnabled));
        }

    /**
     * Pulls longtime from sqlite database (cursor)
     * Called only once, after hint, during the first start, if has rowId
     */
    public void pullData(Cursor cursor)
        {
        longtime.set(cursor.getLong(cursor.getColumnIndexOrThrow(column(columnIndex))));
        setTextFromTime(true);
        clearEdited();
        }

    /**
     * Pushes longtime to sqlite database
     * Called only once, before finish
     */
    public void pushData(ContentValues values)
        {
        if (isEdited())
            {
            getTimeFromText(); // Elvileg felesleges, mert focusváltás nélkül nem lehet ide jutni
            }
        values.put(column(columnIndex), longtime.get()); // Hint lehet nem edited !
        clearEdited(); // Ez viszont felesleges, mert push után befejeződik a szerkesztés
        }

    /**
     * Clears edit after pull, push, hint
     */
    public void clearEdited()
        {
        originalLongtime = longtime.get();
        }

    /**
     * Checks if field was edited. If has focus, than longtime should be updated first
     */
    @Override
    public boolean isEdited()
        {
        if ( hasFocus() )
            {
            getTimeFromText();
            }
        return longtime.get() != originalLongtime;
        }

    /**
     * Saves data, during config changes.
     * If has focus, longtime should be updated first
     * Stores longtime and originallongtime, so edit will be restored, too
     */
    @Override
    public void saveData(Bundle data)
        {
        if ( hasFocus() )
            {
            getTimeFromText();
            }

        data.putLong( "D" + getId(), longtime.get() );
        data.putLong( "O" + getId(), originalLongtime );
        // Nincs rá szükség
        }

    /**
     * Restores data (and edit), and sets text as well.
     */
    @Override
    public void retrieveData(Bundle data)
        {
        longtime.set( data.getLong("D" + getId()));
        originalLongtime = data.getLong("O" + getId());
        setTextFromTime(!hasFocus());
        }
    }
