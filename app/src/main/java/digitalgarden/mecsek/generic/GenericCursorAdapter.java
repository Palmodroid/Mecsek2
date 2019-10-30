package digitalgarden.mecsek.generic;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.formtypes.DateView;


public class GenericCursorAdapter extends SimpleCursorAdapter
	{
	// Az eddig kiválasztott elemet mutatja, vagy semmit, ha Selected_none v. Select_disbaled
	// (Konstruktorban mindenképp normálértéket kap)
	private long selectedItem;
	
	// Az editálásra kiválasztott elemet mutatja
	private long editedItem = -1L;
	
	// A hátteret ezek segítségével állítjuk be. Három kell, mert különben mindig ugyanazt változtatnánk
	// Context miatt ezt constructorban megszerezzük
	private GradientDrawable backgroundSolid;
	private GradientDrawable backgroundBorder;
	private GradientDrawable backgroundBoth;
	
	// editedItem változása után újra kell rajzolni a listát 
	// ez kikényszeríthető pl. a notifyDataSetChanged paranccsal, 
	// de esetünkben nem kell, mert az editFragment változása miatt listFragment is változik, és újrarajzolja a listát.
	public void setEditedItem(long editedItem)
		{
		this.editedItem = editedItem;
		}
	
	public void clearEditedItem()
		{
		this.editedItem = -1L;
		}
	
	public GenericCursorAdapter(Context context, int layout, Cursor c,
                                String[] from, int[] to, int flags, long selectedItem)
		{
		super(context, layout, c, from, to, flags);

		this.selectedItem = selectedItem; 
		}

	// http://stackoverflow.com/questions/12310836/custom-simplecursoradapter-with-background-color-for-even-rows
    /*
    A ViewBinder lehetőséget ad arra, hogy az egyes adatokhoz speciális field-et rendeljünk.
    Ugyanakkor a teljes View megváltoztatása csak a bindView metódus kibővítésével lehetséges.
    Ha már ezt megtettem, akkor egyszerűbb volt az egészet átírni.
    Itt egy leírás:
    https://stackoverflow.com/questions/10396345/how-to-use-simpleadapter-viewbinder
     */
	@Override
	public void bindView(View view, Context context, Cursor cursor) 
		{
	    //super.bindView(view, context, cursor);
        // Ez igazából a super-methodból kimásolt kód

        //final ViewBinder binder = getViewBinder();
        final int count = mTo.length;
        final int[] from = mFrom;
        final int[] to = mTo;

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
                if (v instanceof TextView)
                    {
                    if ( v instanceof DateView)
                        {
                        long time = cursor.getLong(from[i]);
                        ((DateView)v).setDate( time );
                        }
                    else
                        {
                        String text = cursor.getString(from[i]);
                        if (text == null)
                            {
                            text = "";
                            }

                        setViewText((TextView) v, text);
                        }
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

	    if ( getItemId( cursor.getPosition() ) == editedItem && getItemId( cursor.getPosition() ) == selectedItem )
	    	{
	    	if (backgroundBoth == null)
	    		{
	    		backgroundBoth = (GradientDrawable)context.getResources().getDrawable(R.drawable.border_translucent);
	    		backgroundBoth.setColor(0xFF006699);
		    	backgroundBoth.setStroke(3, 0xFFFFA500);
	    		}
	    	view.setBackground( backgroundBoth );
	    	}
	    else if ( getItemId( cursor.getPosition() ) == editedItem )
	    	{
	    	if (backgroundSolid == null)
	    		{
	    		backgroundSolid = (GradientDrawable)context.getResources().getDrawable(R.drawable.border_translucent);
	    		backgroundSolid.setColor(0xFF006699);
		    	backgroundSolid.setStroke(0, 0);
	    		}
	    	view.setBackground( backgroundSolid );
	    	}
	    else if ( getItemId( cursor.getPosition() ) == selectedItem )
	    	{
	    	if (backgroundBorder == null)
	    		{
	    		backgroundBorder = (GradientDrawable)context.getResources().getDrawable(R.drawable.border_translucent);
	    		backgroundBorder.setColor(0);
		    	backgroundBorder.setStroke(3, 0xFFFFA500);
	    		}
	    	view.setBackground( backgroundBorder );
	    	}
	    else
	    	view.setBackgroundResource( 0 );
		}
	}
