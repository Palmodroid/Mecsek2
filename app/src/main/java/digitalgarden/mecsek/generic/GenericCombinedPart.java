package digitalgarden.mecsek.generic;

import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import digitalgarden.mecsek.formtypes.ColorView;
import digitalgarden.mecsek.formtypes.DateView;

public class GenericCombinedPart
    {
    protected GenericCombinedCursorAdapter adapter;

    private int layout;
    private int[] to;
    private String[] fromNames;
    private int[] from; // value inpredectible if cursor is null!!

    protected Cursor cursor = null;
    private int rowIDColumn = -1;


    public GenericCombinedPart(GenericCombinedCursorAdapter adapter,
                               int layout, String[] fromNames, int[] to)
        {
        this.adapter = adapter;

        this.layout = layout;
        this.to = to;
        this.fromNames = fromNames;
        }


    public void setCursor( Cursor cursor )
        {
        if ( cursor == this.cursor)
            {
            return;
            }

        this.cursor = cursor;
        if (this.cursor == null)
            {
            this.rowIDColumn = -1;
            adapter.notifyDataSetInvalidated();
            return;
            }

        this.rowIDColumn = this.cursor.getColumnIndexOrThrow("_id");
        adapter.notifyDataSetChanged();

        int count = fromNames.length;
        if (this.from == null || this.from.length != count)
            {
            this.from = new int[count];
            }

        for (int i = 0; i < count; ++i)
            {
            this.from[i] = cursor.getColumnIndexOrThrow(fromNames[i]);
            }
        }


    public int getCount()
        {
        return this.cursor != null ? this.cursor.getCount() : 0;
        }


    public Cursor getItem(int cursorPosition)
        {
        if (cursor != null)
            {
            cursor.moveToPosition( cursorPosition );
            return cursor;
            }
        else
            {
            return null;
            }
        }


    public long getItemId(int cursorPosition)
        {
        if (cursor != null)
            {
            return cursor.moveToPosition( cursorPosition ) ?
                    cursor.getLong( rowIDColumn ) : 0L;
            }
        else
            {
            return 0L;
            }
        }


    public View getView(int cursorPosition, View convertView, ViewGroup parent)
        {
        if (cursor == null) // Hoppá!! Ilyen lehet az elején, ha nincs feltöltve a cursor.
            // Vagy
            // mégsem? Count nem fog ilyen pozíciót adni!
            {
            throw new IllegalStateException("this should only be called when the cursor is valid");
            }
        else if (!cursor.moveToPosition( cursorPosition ))
            {
            throw new IllegalStateException("couldn't move cursor to position " + cursorPosition);
            }
        else
            {
            View view;
            if (convertView == null)
                {
                view = adapter.getLayoutInflater().inflate(this.layout, parent, false);
                }
            else
                {
                view = convertView;
                }
            bindView( view );
            return view;
            }
        }


    // http://stackoverflow.com/questions/12310836/custom-simplecursoradapter-with-background-color-for-even-rows
    /*
    A ViewBinder lehetőséget ad arra, hogy az egyes adatokhoz speciális field-et rendeljünk.
    Ugyanakkor a teljes View megváltoztatása csak a bindView metódus kibővítésével lehetséges.
    Ha már ezt megtettem, akkor egyszerűbb volt az egészet átírni.
    Itt egy leírás:
    https://stackoverflow.com/questions/10396345/how-to-use-simpleadapter-viewbinder
     */
    protected void bindView(View view)
        {
        //super.bindView(view, context, cursor);
        // Ez igazából a super-methodból kimásolt kód

        //final ViewBinder binder = getViewBinder();
        final int count = this.to.length;
        final int[] from = this.from;
        final int[] to = this.to;

        for (int i = 0; i < count; i++)
            {
            final View v = view.findViewById(to[i]);
            if (v != null)
                {
                // boolean bound = false;
                // if (binder != null)
                //    {
                //    bound = binder.setViewValue(v, cursor, from[i]);
                //    }

                //if (!bound)
                //    {
                if ( v instanceof DateView)
                    {
                    long time = cursor.getLong(from[i]);
                    ((DateView)v).setDate( time );
                    }
                else if ( v instanceof ColorView)
                    {
                    long color = cursor.getLong(from[i]);
                    ((ColorView)v).setColor( color );
                    }
                else  if (v instanceof TextView) // all previous were descendants of textView
                    {
                    String text = cursor.getString(from[i]);
                    if (text == null)
                        {
                        text = "";
                        }

                    setViewText((TextView) v, text);
                    }
                // else if (v instanceof ImageView)
                //    {
                //    setViewImage((ImageView) v, text);
                //    }
                else
                    {
                    throw new IllegalStateException(v.getClass().getName() + " is not a "
                            + " view that can be bounds by this SimpleCursorAdapter");
                    }
                //    }
                }
            }

        }


    protected void setViewImage(ImageView v, String value)
        {
        try
            {
            v.setImageResource(Integer.parseInt(value));
            }
        catch (NumberFormatException var4)
            {
            v.setImageURI(Uri.parse(value));
            }

        }


    protected void setViewText(TextView v, String text)
        {
        v.setText(text);
        }

    }
