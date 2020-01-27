package digitalgarden.mecsek.viewutils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;


/**
 * Draws text in the center of a backgroundview
 */
public class BoxAndTextView extends BoxView
    {
    // text paint to draw text with
    private TextPaint textPaint;

    // text to draw
    private String text ="*";


    public BoxAndTextView(Context context)
        {
        super(context);
        }

    public BoxAndTextView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public BoxAndTextView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    public void setTextPaint(TextPaint textPaint)
        {
        this.textPaint = textPaint;
        }

    public TextPaint getTextPaint()
        {
        return textPaint;
        }

    public void setText(String text)
        {
        this.text = text;
        }

    @Override
    protected void onAttachedToWindow()
        {
        super.onAttachedToWindow();

        if ( textPaint == null )
            {
            textPaint = new TextPaint();
            textPaint.setColor(Color.BLUE);
            textPaint.setTextBold(false);
            textPaint.setTextItalics(false);
            }

        textPaint.setTextAlign( TextPaint.ALIGN_CENTER );
        }

    @Override
    protected void onDraw(Canvas canvas)
        {
        super.onDraw(canvas);

        // longstyle is setStyle by BoxView.setLongstyle() methods
        // text is always written with inkColor
        if ( longstyle != null )
            {
            textPaint.setColor( longstyle.getInkColor() );
            textPaint.setTextItalics( longstyle.isItalicsText() );
            textPaint.setTextBold( longstyle.isBoldText() );
            }

        textPaint.drawText(canvas, text);
        }

    @Override
    protected void onSizeChangedInDraw(int width, int height)
        {
        super.onSizeChangedInDraw(width, height);

        textPaint.calculateTextSizeForBox( width, height );
        textPaint.setTextXY(width/2, height/2);
        }
    }