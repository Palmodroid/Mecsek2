package digitalgarden.mecsek.generic;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class GenericCombinedCursorAdapter extends BaseAdapter
    {
    private Context context;
    private LayoutInflater layoutInflater;

    private GenericCombinedPartForList listPart;
    private GenericCombinedPart headerPart = null;

    private static final int HEADER_ROWS_COUNT = 1;


    public GenericCombinedCursorAdapter(Context context, int layout, String[] from, int[] to, long selectedItem)
        {
        this.context = context;

        // https://stackoverflow.com/a/18942760
        // better than inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.layoutInflater = LayoutInflater.from(context);

        listPart = new GenericCombinedPartForList(this, layout, from, to);
        listPart.setSelectedItem(selectedItem);
        }


    public void setStyleColumnName( String styleColumnName )
        {
        listPart.setStyleColumnName( styleColumnName );
        }


    public Context getContext()
        {
        return context;
        }

    public LayoutInflater getLayoutInflater()
        {
        return layoutInflater;
        }

    public void setListCursor(Cursor cursor)
        {
        listPart.setCursor(cursor);
        }

    public boolean isHeaderDefinied()
        {
        return headerPart != null;
        }

    public void setHeaderRow( int layout, String[] from, int[] to )
        {
        headerPart = new GenericCombinedPart( this, layout, from, to);
        }

    public void setHeaderCursor( Cursor cursor )
        {
        // If headerPart is null, it will stop immediately
        headerPart.setCursor( cursor );
        }

    @Override
    public int getViewTypeCount()
        {
        return isHeaderDefinied() ? 2 : 1;
        }

    @Override
    public int getItemViewType(int position)
        {
        if ( isHeaderDefinied() )
            return ( position < HEADER_ROWS_COUNT ) ? 0 : 1;
        else
            return 0;
        }

    public int getCount()
        {
        // Header part has got fixed size. One row and can have row separators
        int length = listPart.getCount() +
                (isHeaderDefinied() && headerPart.getCount() > 0 ? HEADER_ROWS_COUNT : 0);

        return length;
        }

    public Object getItem(int listPosition)
        {
        int cursorPosition = listPosition;

        if ( isHeaderDefinied() )
            {
            if (listPosition < HEADER_ROWS_COUNT)
                return headerPart.getItem(listPosition);

            cursorPosition -= HEADER_ROWS_COUNT;
            }

        return listPart.getItem( cursorPosition );
        }

    public long getItemId(int listPosition)
        {
        int cursorPosition = listPosition;

        if ( isHeaderDefinied() )
            {
            if (listPosition < HEADER_ROWS_COUNT)
                return -headerPart.getItemId(listPosition);

            cursorPosition -= HEADER_ROWS_COUNT;
            }

        return listPart.getItemId( cursorPosition );
        }

    public boolean hasStableIds()
        {
        return true;
        }

    public View getView(int listPosition, View convertView, ViewGroup parent)
        {
        int cursorPosition = listPosition;

        if ( isHeaderDefinied() )
            {
            if (listPosition < HEADER_ROWS_COUNT)
                {
                // View view = headerPart.getView( cursorPosition, convertView, parent );
                // view.setPadding(0,0,0,0);
                // view.setBackgroundColor( 0xFFE8E8E9 ); // Not the best, but can paint background

                // return view;

                return headerPart.getView( cursorPosition, convertView, parent );
                }

            cursorPosition -= HEADER_ROWS_COUNT;
            }

        return listPart.getView( cursorPosition, convertView, parent );
        }

    public void setEditedItem(long id)
        {
        listPart.setEditedItem( id );
        }

    public void clearEditedItem()
        {
        listPart.clearEditedItem();
        }
    }
