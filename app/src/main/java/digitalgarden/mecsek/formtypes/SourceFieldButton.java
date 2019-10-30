package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;


import java.util.List;

import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


public class SourceFieldButton extends AppCompatButton implements Connection.Connectable
    {
    private GenericEditFragment editFragment;

    private int sourceTableIndex;
    private long sourceRowIndex;


    public SourceFieldButton(Context context)
        {
        super(context);
        }

    public SourceFieldButton(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public SourceFieldButton(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }


    /**
     *
     * @param editFragment
     * tableIndex Saját tábla indexe!!
     *                   Azért kell a tábla indexe, mert ebből tudja meg a column adatokat
     *                   A table index lekérdezhető az editFRagmentben
     */
    public void connect(final GenericEditFragment editFragment)
        {
        this.editFragment = editFragment;

        // Ha még csak most hozzuk létre, akkor nem lehet source!
        if ( editFragment.getRowIndex() < 0 )
            {
            setVisibility(View.GONE);
            }
        else
            {
            setOnClickListener(new View.OnClickListener()
                {
                public void onClick(View view)
                    {
                    Intent i = new Intent();

                    if (sourceTableIndex >= 0L) // getControllACtivity-t kell vajon ellenőrizni?
                        {
                        i.setClass(
                                SourceFieldButton.this.editFragment.getContext(),
                                table(sourceTableIndex).getControllActivity());
                        i.putExtra(
                                GenericEditFragment.EDITED_ITEM, sourceRowIndex);
                        SourceFieldButton.this.editFragment.startActivity(i);
                        }
                    }
                });
            }
        }


    @Override
    public void addColumn( List<String> columns )
        {
        columns.add( column(table(editFragment.defineTableIndex()).SOURCE_TABLE));
        columns.add( column(table(editFragment.defineTableIndex()).SOURCE_ROW));
        }


    @Override
    public void pullData(Cursor cursor)
        {
        int column;

        column = cursor.getColumnIndexOrThrow(column(table(editFragment.defineTableIndex()).SOURCE_TABLE));
        sourceTableIndex = cursor.isNull(column) ? -1 : cursor.getInt(column);

        column = cursor.getColumnIndexOrThrow(column(table(editFragment.defineTableIndex()).SOURCE_ROW));
        sourceRowIndex = cursor.isNull(column) ? -1L : cursor.getLong(column);

        if ( sourceRowIndex == -1L || sourceTableIndex == -1 )
            {
            setVisibility( View.GONE );
            }
        }

    @Override
    public void pushData(ContentValues values)
        {
        // These data cannot be pushed; source is set by the source table
        }

    @Override
    public void pushSource(int tableIndex, long rowIndex)
        {
        // Not needed here
        }

    @Override
    public void saveData(Bundle data)
        {
        data.putInt( column(table(editFragment.defineTableIndex()).SOURCE_TABLE),
                sourceTableIndex );
        data.putLong( column(table(editFragment.defineTableIndex()).SOURCE_ROW),
                sourceRowIndex );
        }

    @Override
    public void retrieveData(Bundle data)
        {
        sourceTableIndex = data.getInt( column(table(editFragment.defineTableIndex()).SOURCE_TABLE) );
        sourceRowIndex = data.getLong( column(table(editFragment.defineTableIndex()).SOURCE_ROW) );
        }

    @Override
    public boolean isEdited()
        {
        // source button cannot be changed
        return false;
        }
    }
