package digitalgarden.mecsek.viewutils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


/**
 * BackgroundView draws a box with background (defined by backgroundPaint) and border
 * (defined by borderPaint.
 * Paints can be definied previosly, and used for more Views.
 * Extend it to draw more.
 */
public class BackgroundView extends View
    {
    // Temporary store box coords in onDraw
    private RectF tempRectF = new RectF();

    // fill paint of background
    private Paint backgroundPaint;

    // stroke paint of border
    private Paint borderPaint;

    // last size of this view
    private int lastWidth = -1;
    private int lastHeight = -1;

    public BackgroundView(Context context)
        {
        super(context);
        }

    public BackgroundView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public BackgroundView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }


    public void setBackgroundPaint(Paint backgroundPaint)
        {
        this.backgroundPaint = backgroundPaint;
        }

    public Paint getBackgroundPaint()
        {
        return backgroundPaint;
        }

    public void setBorderPaint(Paint borderPaint)
        {
        this.borderPaint = borderPaint;
        }

    public Paint getBorderPaint()
        {
        return borderPaint;
        }


    /**
     * onAttachedToWindow() - all attributes should be ready
     * Before this point external Paints can be joined.
     */
    @Override
    protected void onAttachedToWindow()
        {
        super.onAttachedToWindow();

        if ( backgroundPaint == null )
            {
            backgroundPaint = new Paint();
            backgroundPaint.setColor(Color.WHITE);
            }
        backgroundPaint.setStyle(Paint.Style.FILL);

        if ( borderPaint == null )
            {
            borderPaint = new Paint();
            borderPaint.setStrokeWidth(2f);
            borderPaint.setColor(Color.RED);
            }
        borderPaint.setStyle(Paint.Style.STROKE);
        }

    /**
     * onSizeChanged() - all measurements come here
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
        {
        super.onSizeChanged(w, h, oldw, oldh);

        }
     */

    /**
     * Draw using attributes set by onAttachedToWindow and coordinates measured by onSizeChanged
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

        canvas.drawRoundRect( tempRectF, 10f, 10f, backgroundPaint );
        canvas.drawRoundRect( tempRectF, 10f, 10f, borderPaint );
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
