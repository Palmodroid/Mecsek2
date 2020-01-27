package digitalgarden.mecsek.viewutils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


/**
 * BoxView draws a box with background (defined by paperPaint) and border
 * (defined by framePaint.
 * Paints can be definied previosly, and used for more Views.
 * Extend it to draw more.
 */
public class BoxView extends View
    {
    // Temporary store box coords in onDraw
    private RectF tempRectF = new RectF();

    // fill paint of paper
    private Paint paperPaint;

    // stroke paint of frame
    private Paint framePaint;

    // Longstyle to overwrite color values
    protected Longstyle longstyle = null;
    // (BoxAndTextView uses it!)

    // Draws only paper with paperPaint overwritten by longstyle's paper-color (if defined)
    public static final int PAPER = 2;
    // Draws only frame with framePaint overwritten by longstyle's paper-color (if defined)
    public static final int FRAME = 1;
    // DEFAULT: Draws frame/framePaint/inkColor AND paper/paperPaint/paperColor
    public static final int PAPER_AND_FRAME = PAPER | FRAME;
    // if PAPER and FRAME bits are 0 - nothing is drawn

    // How to overwrite colors if Longstyle is setStyle
    private int overwriteMode = PAPER_AND_FRAME;

    // last size of this view
    private int lastWidth = -1;
    private int lastHeight = -1;

    public BoxView(Context context)
        {
        super(context);
        }

    public BoxView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public BoxView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }


    public void setPaperPaint(Paint paperPaint)
        {
        this.paperPaint = paperPaint;
        }

    public Paint getPaperPaint()
        {
        return paperPaint;
        }

    public void setFramePaint(Paint framePaint)
        {
        this.framePaint = framePaint;
        }

    public Paint getFramePaint()
        {
        return framePaint;
        }


    public void setLongstyle( Longstyle longstyle )
        {
        setLongstyle( longstyle, PAPER_AND_FRAME );
        }

    public void setLongstyle( long longstyle )
        {
        setLongstyle( longstyle, PAPER_AND_FRAME );
        }

    public void setLongstyle( Longstyle longstyle, int overwriteMode )
        {
        this.longstyle = longstyle;
        this.overwriteMode = overwriteMode;
        }

    public void setLongstyle( long longstyle, int overwriteMode )
        {
        if ( this.longstyle == null )
            this.longstyle = new Longstyle( getContext(), longstyle );
        else
            this.longstyle.set( longstyle );
        this.overwriteMode = overwriteMode;
        }

    public Longstyle getLongstyle()
        {
        return longstyle;
        }

    /**
     * onAttachedToWindow() - all attributes should be ready
     * Before this point external Paints can be joined.
     */
    @Override
    protected void onAttachedToWindow()
        {
        super.onAttachedToWindow();

        if ( paperPaint == null )
            {
            paperPaint = new Paint();
            paperPaint.setColor(Color.WHITE);
            }
        paperPaint.setStyle(Paint.Style.FILL);

        if ( framePaint == null )
            {
            framePaint = new Paint();
            framePaint.setStrokeWidth(2f);
            framePaint.setColor(Color.RED);
            }
        framePaint.setStyle(Paint.Style.STROKE);
        }

    /*
     * onSizeChanged() - all measurements come here
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
        {
        super.onSizeChanged(w, h, oldw, oldh);

        }
     */

    /**
     * Draw using attributes setStyle by onAttachedToWindow and coordinates measured by onSizeChanged
     */
    @Override
    protected void onDraw(Canvas canvas)
        {
        super.onDraw(canvas);

        if ( getWidth() != lastWidth || getHeight() != lastHeight )
            {
            lastWidth = getWidth();
            lastHeight = getHeight();

            onSizeChangedInDraw( lastWidth, lastHeight );
            }

        if ( longstyle != null )
            {
            if ( overwriteMode == PAPER )
                {
                paperPaint.setColor( longstyle.getPaperColor() );
                }
            else if ( overwriteMode == FRAME )
                {
                framePaint.setColor( longstyle.getPaperColor() );
                }
            else // overwriteMode == PAPER_AND_FRAME
                {
                paperPaint.setColor( longstyle.getPaperColor() );
                framePaint.setColor( longstyle.getInkColor() );
                }
            }

        // if longstyle is null, then overwriteMode is always PAPER | FRAME - both is drawn
        if ( (overwriteMode & PAPER) != 0 )
            {
            canvas.drawRoundRect(tempRectF, 10f, 10f, paperPaint);
            }
        if ( (overwriteMode & FRAME) != 0 )
            {
            canvas.drawRoundRect(tempRectF, 10f, 10f, framePaint);
            }
        }

    protected void onSizeChangedInDraw(int width, int height)
        {
        float left = 1f + getPaddingLeft();
        float top = 1f + getPaddingTop();
        float right = width - getPaddingRight();
        float bottom = height - getPaddingBottom();

        tempRectF.set( left, top, right, bottom );
        }
    }
