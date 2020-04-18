package digitalgarden.mecsek.fieldtypes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericTable;
import digitalgarden.mecsek.tables.LibraryDatabase;
import digitalgarden.mecsek.utils.BitmapUtils;

import java.util.List;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


/**
 * <em>TUTORIAL: How to create a new field type?</em>
 *
 * <ol>{@link FieldImage} is the extension of ImageView widget. Tapping it will start an "external" selector, to pick
 * a picture from gallery. The picture will be stored in a type BLOB column.
 *
 * <li>If needed a new database column type should be registered above {@link GenericTable#COLUMN_TYPES} Do not
 * forget increase database version! {@link LibraryDatabase#version()} </li>
 *
 * <li>Extend {@link AppCompatImageView} with the necessary constructors. Constructors cannot be changed!</li>
 *
 * <li> An extra {@link #connect(GenericEditFragment, Connection, int)} method is needed, to connect widget to the
 * database. Table is stored by Connection, Column index should be stored here.</li>
 *
 * <li>Implement {@link Connection.Connectable} </li>
 *
 * <li>Implement {@link GenericEditFragment.UsingSelector} {@link #selectorCode} is set (and sent) by
 * {@link #connect(GenericEditFragment, Connection, int)}, is checked by {@link #checkReturningSelector(int, Intent)}
 * Edited flag should be changed, if selector returns.</li>
 *
 * </ol>
 * <p>@see <a href="http://www.coderzheaven.com/2012/12/23/store-image-android-sqlite-retrieve-it/">Link</a></p>
 * <p>@see <a href="https://stackoverflow.com/questions/29803924/android-how-to-set-the-photo-selected-from-gallery-to-a-bitmap/">Link</a></p>
 */
public class FieldImage extends AppCompatImageView implements Connection.Connectable, GenericEditFragment.UsingSelector
    {
    /*
     * *** TUTORIAL - class variables ***
     * editFragment - the root form, contains context, too
     * selectorCode - needed only when selector activities returns through checkReturningSelector()
     * columnIndex  - index of the column represented by this field
     * value        - value of this field (can be part of the original widget!)
     *                value is pulled, than can be changed (edited is set), but will be pushed only on Update or Add
     * edited       - true, if value was changed and should be pushed
     */

    /** Form of the widget */
    private GenericEditFragment editFragment;

    /** Common CODE between widget and selectorActivity Request Code provided by {@link GenericEditFragment#getCode()}
     *  With this common code selectorActivity can identify its calling widget */
    private int selectorCode = -1;

    /** Field's column - field shows/sets data of this column */
    protected int columnIndex;

    /** Field's actual value (not yet stored) */
    private byte[] value = null;

    /** TRUE if value of the field was changed */
    private boolean edited = false;


    public FieldImage(Context context)
        {
        super(context);
        }

    public FieldImage(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public FieldImage(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }


    public void connect(final GenericEditFragment editFragment, Connection connection, int columnIndex )
        {
        this.editFragment = editFragment;
        this.selectorCode = editFragment.getCode();

        // column index (or indices) are stored inside field
        // table index (only one) is stored inside connection
        // so connect will add both data at the same place, connection.add is not needed as a separate row
        this.columnIndex = columnIndex;
        connection.add( this );

        // SELECTOR starts when widget is tapped, and RETURNS in cehckReturningSelector()
        // selectorCode should be used
        setOnClickListener(new View.OnClickListener()
            {
            public void onClick(View view)
                {
                // BitmapUtils.pickImage( FieldImage.this.editFragment, selectorCode );
                BitmapUtils.captureImage( FieldImage.this.editFragment, selectorCode );
                }
            });
        }


    @Override
    public void checkReturningSelector(int requestCode, Intent data)
        {
        if (this.selectorCode == requestCode)
            {
            //data.getData returns the content URI for the selected Image
            //if ( setValue (data.getData()) )
            //    {
            //    edited = true;
            //    }

            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if ( setValue ( imageBitmap ))
                {
                edited = true;
                }
            }
        }


    /** Set a new value means: Setting up value (not pushed to database yet) and
     *  refresh field
     *  <p>IMPORTANT! Some widget stores value (like EditText), but in other cases (like ImageView) data should be
     *  stored outside the original widget</p>
     *  Returns TRUE if data was changed */
    public boolean setValue( Uri imageUri )
        {
        if ( imageUri != null )
            {
            return setValue( BitmapUtils.uriToBitmap( editFragment.getContext(), imageUri, 100, 100) );
            }
        return false;
        }

    public boolean setValue( Bitmap imageBitmap )
        {
        if ( imageBitmap != null )
            {
            setImageAndValue( imageBitmap, BitmapUtils.bitmapToByteArrayPng( imageBitmap ));
            return true;
            }
        return false;
        }

    public boolean setValue( byte[] imageByteArray)
        {
        if ( imageByteArray != null )
            {
            setImageAndValue( BitmapUtils.byteArrayToBitmap( imageByteArray ), imageByteArray);
            return true;
            }
        return false;
        }

    private void setImageAndValue( Bitmap bitmap, byte[] imageByteArray )
        {
        setImageBitmap( bitmap );
        value = imageByteArray;
        }

    /** Column index of image (type BLOB) is added to projection */
    @Override
    public void addColumnToProjection(List<String> projection)
        {
        projection.add( column( columnIndex ));
        }

    /** Pulls value from database AND sets image from value */
    @Override
    public void getDataFromPull(Cursor cursor)
        {
        setValue( cursor.getBlob( cursor.getColumnIndexOrThrow( column( columnIndex ))));
        }

    /** Pushes value from widget to database */
    @Override
    public void addDataToPush(ContentValues values)
        {
        values.put(column( columnIndex ), value );
        }

    @Override
    public void pushSource(int tableIndex, long rowIndex)
        {
        // No source for image fields
        }

    /** Saves value during config changes */
    @Override
    public void saveData(Bundle data)
        {
        data.putByteArray( column(columnIndex), value );
        }

    /** Retrieve value AND sets image from value after config changes */
    @Override
    public void retrieveData(Bundle data)
        {
        setValue( data.getByteArray( column(columnIndex) ));
        }

    @Override
    public boolean isEdited()
        {
        return edited;
        }
    }
