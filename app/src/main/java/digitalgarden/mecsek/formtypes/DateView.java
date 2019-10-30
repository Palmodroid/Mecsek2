package digitalgarden.mecsek.formtypes;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;


import digitalgarden.mecsek.utils.Longtime;

public class DateView extends AppCompatTextView
    {
    public DateView(Context context)
        {
        super(context);
        }

    public DateView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public DateView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    public void setDate( long date )
        {
        Longtime lt = new Longtime( date );
        setText( lt.toString() );
        }
    }
