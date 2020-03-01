package digitalgarden.mecsek.generic;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.viewutils.Longstyle;

public class GenericCombinedPartForList extends GenericCombinedPart
    {
    // A hátteret ezek segítségével állítjuk be. Három kell, mert különben mindig ugyanazt változtatnánk
    // Context miatt ezt constructorban megszerezzük
    private static GradientDrawable backgroundSolid;
    private static GradientDrawable backgroundBorder;
    private static GradientDrawable backgroundBoth;

    // Az eddig kiválasztott elemet mutatja, vagy semmit, ha Selected_none v. Select_disbaled
    // (Konstruktorban mindenképp normálértéket kap)

    // Ez mi? ListPosition vagy CursorPosition?
    // GenericListFragment alapján CursorPosition - a korábban kiválasztott item
    private long selectedItem;

    // Az editálásra kiválasztott elemet mutatja
    // Ez mi? ListPosition vagy CursorPosition?
    // setEditedItem() alapján szintén CursorPosition
    private long editedItem = -1L;



    public GenericCombinedPartForList(GenericCombinedCursorAdapter adapter,
                                      int layout, String[] fromNames, int[] to)
        {
        super( adapter, layout, fromNames, to);
        }


    public void setSelectedItem(long selectedItem)
        {
        this.selectedItem = selectedItem;
        }

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


    @Override
    protected void bindView( View view )
        {
        super.bindView( view );

        if ( getItemId( cursor.getPosition() ) == editedItem &&
                getItemId( cursor.getPosition() ) == selectedItem )
            {
            if (backgroundBoth == null)
                {
                backgroundBoth = (GradientDrawable)adapter.getContext().getResources()
                        .getDrawable(R.drawable.border_translucent);
                backgroundBoth.setColor(0xFF006699);
                backgroundBoth.setStroke(3, 0xFFFFA500);
                }
            view.setBackground( backgroundBoth );
            }
        else if ( getItemId( cursor.getPosition() ) == editedItem )
            {
            if (backgroundSolid == null)
                {
                backgroundSolid = (GradientDrawable)adapter.getContext().getResources()
                        .getDrawable(R.drawable.border_translucent);
                backgroundSolid.setColor(0xFF006699);
                backgroundSolid.setStroke(0, 0);
                }
            view.setBackground( backgroundSolid );
            }
        else if ( getItemId( cursor.getPosition() ) == selectedItem )
            {
            if (backgroundBorder == null)
                {
                backgroundBorder = (GradientDrawable) adapter.getContext().getResources()
                        .getDrawable(R.drawable.border_translucent);
                backgroundBorder.setColor(0);
                backgroundBorder.setStroke(3, 0xFFFFA500);
                }
            view.setBackground( backgroundBorder );
            }

        // Backround set to original (from Tag) at the very beginning !
        }
    }
