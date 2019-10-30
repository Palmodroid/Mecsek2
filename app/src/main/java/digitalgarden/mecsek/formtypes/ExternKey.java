package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


public class ExternKey implements Connection.Connectable
    {
    private static final long EXTERN_INACTIVE = -2L;
    private static final long EXTERN_CREATE = -1L;

    private Connection connection;

    private GenericEditFragment editFragment;
    private int tableIndex;
    private int columnIndex;

    private long externKeyValue = EXTERN_INACTIVE;


    public ExternKey(GenericEditFragment editFragment, int externKeyColumnIndex, int externTableIndex )
        {
        this.tableIndex = externTableIndex;
        this.columnIndex = externKeyColumnIndex;
        this.editFragment = editFragment;

        connection = new Connection( editFragment.getContext(), tableIndex );

        // !!! EZT KI KELL INNEN VENNI, HA NEM AKARJUK, HOGY MINDIG KÉSZÍTSEN EGY EXTERNKEY-T !!!
        createExtern();
        }


    @Override
    public void addColumn( List<String> columns )
        {
        columns.add( column(columnIndex) );
        }



    public EditField addEditField(int editFieldId, int columnIndex )
        {
        EditField editField = (EditField) editFragment.getView().findViewById( editFieldId );
        editField.connect( editFragment, columnIndex );
        connection.add( editField );

        return editField;
        }

    public void createExtern()
        {
        if ( externKeyValue == EXTERN_INACTIVE )
            externKeyValue = EXTERN_CREATE;
        }

    @Override
    public void pullData(Cursor cursor)
        {
        int column = cursor.getColumnIndexOrThrow(column(columnIndex));
        if (cursor.isNull(column))
            {
            if ( externKeyValue != EXTERN_CREATE )
                externKeyValue = EXTERN_INACTIVE;
            }
        else
            {
            externKeyValue = cursor.getLong(column);
            connection.pullData( externKeyValue);
            }
        }


    @Override
    public void pushData(ContentValues values)
        {
        if ( externKeyValue != EXTERN_INACTIVE )
            {
            externKeyValue = connection.pushData(externKeyValue);
            }

        if (externKeyValue >= 0L)
            values.put(column(columnIndex), externKeyValue);
        else
            values.putNull(column(columnIndex));
        }


    /**
     * Ezen a ponton values már tartalmazza a foráás adatait is.
     * Emiatt frissíteni kell az itt lévő adatokat.
     * Erre csak akkor van szükség, ha az adataink léteznek.
     * Mi legyen a hibákkal?
     */
    @Override
    public void pushSource( int sourceTableIndex, long sourceRowIndex )
        {
        if (externKeyValue >= 0L && table( tableIndex ).hasSourceColumns())
            {
            ContentValues values = new ContentValues();

            values.put(
                    column( table(tableIndex).SOURCE_TABLE),
                    (long) (table(sourceTableIndex).id()));
            values.put(
                    column( table(tableIndex).SOURCE_ROW),
                    sourceRowIndex);

            try
                {
                editFragment.getContext().getContentResolver().update(getItemContentUri(externKeyValue), values, null, null);
                }
            catch (Exception e)
                {
                Toast.makeText(editFragment.getContext(), "ERROR: Update source of item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
                }
            }
        }

    // Ez kéne a közösbe!!
    private Uri getItemContentUri( long rowIndex )
        {
        return Uri.parse( table( tableIndex ).contentUri() + "/" + rowIndex );
        }


    @Override
    public void saveData(Bundle data)
        {
        data.putLong( column(columnIndex), externKeyValue );
        connection.saveData( data );
        }

    @Override
    public void retrieveData(Bundle data)
        {
        externKeyValue = data.getLong( column(columnIndex) );
        connection.retrieveData( data );
        }

    @Override
    public boolean isEdited()
        {
        // extern key cannot be changed (created at most)
        return false;
        }
    }
