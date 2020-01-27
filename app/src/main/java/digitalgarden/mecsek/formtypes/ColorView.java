package digitalgarden.mecsek.formtypes;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import digitalgarden.mecsek.utils.Longtime;

/**
 * ColorView shows colors as their hexadecimal values
 * It is a simple (AppCompat)TextView which setColor() method sets its text from long value
 */
public class ColorView extends AppCompatTextView
    {
    public ColorView(Context context)
        {
        super(context);
        }

    public ColorView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public ColorView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    public void setColor( long color )
        {
        setText( Long.toHexString( color ) );
        }
    }
