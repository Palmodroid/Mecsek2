package digitalgarden.mecsek.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

/**
 * Action buttons can be automatically hidden by RecycleViews
 * Standard ListView cannot behave the same way.
 *
 * We have to hide Action buttons when listview scrolls OR editview is shown
 * Because these two can happen together, different hiding types should be monitored.
 *
 * HidingActionButton can hide( int cause ) or show ( int cause )
 * Cause-s contains only one - different - positive bit.
 * Now BY_LIST and BY_FRAGMENT causes are in use.
 * BY_LIST will slide right the button to hide it.
 * All other causes will hide() button (standard way)
 */
public class HidingActionButton extends FloatingActionButton
    {
    public HidingActionButton(Context context)
        {
        super(context);
        }

    public HidingActionButton(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public HidingActionButton(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }


    // Each type should have only one unique bit positive (1,2,4,8...)
    public static final int BY_LIST = 1;
    public static final int BY_FRAGMENT = 2;

    // == 0 -> shown ; == BY_LIST -> slided right ; == anything else -> fade out
    private int hidingType = 0;

    // == 0 -> shown ; != 0 binary sum of hidng types
    private int hidingCauses = 0;

    /**
     * This cause allows HidingActionButton to show
     * If no cause left to hide it, HidingActionButton will be shown
     * @param cause binary value (only one bit is 1) of causes
     */
    public void show( int cause )
        {
        if ( hidingCauses != 0 ) // is hidden
            {
            hidingCauses &= ~cause;

            if ( hidingCauses == 0 ) // not hidden any more
                {
                if ( hidingType == BY_LIST )
                    slideBack();
                else
                    show();

                hidingType = 0;
                }
            }

        // Log.d("FAB", "Show - causes: " + hidingCauses + " type: " + hidingType );
        }

    public void hide( int cause )
        {
        if ( hidingCauses == 0 ) // not hidden
            {
            hidingType = cause;
            if ( cause == BY_LIST )
                slideRight();
            else
                hide();
            }
        hidingCauses |= cause;

        // Log.d("FAB", "Hide - causes: " + hidingCauses + " type: " + hidingType );
        }

    /** Slides out HidingActionButton from RIGHT position */
    private void slideRight()
        {
        animate().translationX( getWidth()
                + ((CoordinatorLayout.LayoutParams)getLayoutParams()).rightMargin )
                .setInterpolator(new LinearInterpolator()).start();
        }

    /** Slides back HidingActionButton horizontally */
    private void slideBack()
        {
        animate().translationX( 0F )
                .setInterpolator(new LinearInterpolator()).start();
        }
    }



