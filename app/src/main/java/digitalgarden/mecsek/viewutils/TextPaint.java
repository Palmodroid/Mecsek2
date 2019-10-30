package digitalgarden.mecsek.viewutils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * TextPaint class extends Paint to draw text with these attributes. Text size is adjusted to
 * fit inside window (or box).
 * Text parameters should be set first:
 * setTextFlags() - set by constructor,
 * setTextItalics() and setTextBold() - if needed
 * setTextBox() - set box inside text should fit
 * setTextToMeasure() - set text to measure (text for width and height can be set separately)
 * (it will set paint's own textSize!!)
 * Second step:
 * calculateTextSizeForBox() - calculates size from the previously set parameters
 * Last step:
 * drawText() or drawEllipsizedText() - draw text using text size set by calculateTextSizeForBox()
 * (Coordinates and alignment can be set previously for drawText(), but not for
 * drawEllipsizedText())
 */
public class TextPaint extends Paint
    {
    /** Text should be draw in the middle */
    public static int ALIGN_CENTER = 0;

    /** Text should be draw to the left of x */
    public static int ALIGN_LEFT = 1;

    /** Text should be draw to the right of x */
    static int ALIGN_RIGHT = 2;

    /** Text should be draw above y */
    static int ALIGN_ABOVE = 4;

    /** Text should be draw below y */
    static int ALIGN_BELOW = 8;

    // Width of box where text should fit
    private int boxWidth = -1;

    // Height of box where text should fit
    private int boxHeight = -1;

    // Text to fit in box width
    private String textToMeasureX = "MMM";

    // Text to fit in box height
    private String textToMeasureY = "y";

    // adjust x coord to clear leading place (if any)
    private int xAdjust;

    // adjust y coord to get upper corner instead of baseline
    private int yAdjust;

    // measured text height is needed later to get line height
    private int measuredTextHeight;

    // string to use as ellipsis
    private String ELLIPSIS = "\u2026";

    // width of ellipsis character, negative if not yet measured
    private float ellipsisWidth = -1f;

    // X coord to draw text (not used by ellipsized text)
    private int textX = 0;

    // Y coord to draw text (not used by ellipsized text)
    private int textY = 0;

    // Text alignment to draw text (not used by ellipsized text)
    private int textAlign = ALIGN_CENTER;

    // Calculated text size. Real text size can be changed.
    private int calculatedTextSize = 0;


    /**
     * Constructor sets default flags
     */
    public TextPaint()
        {
        super();
        setTextFlags();
        }

    /**
     * Helper util to calculate value's millis
     * @param value value to get millis of
     * @param millis millis
     * @return value's millis
     */
    public static int millis(int value, int millis)
        {
        return value * millis / 1000;
        }

    /**
     * Paint flags to get nice text. I don't remember why exactly these flags.
     */
    void setTextFlags()
        {
        setFlags(
                Paint.ANTI_ALIAS_FLAG |
                        Paint.DITHER_FLAG |
                        Paint.SUBPIXEL_TEXT_FLAG |
                        Paint.LINEAR_TEXT_FLAG);
        }

    /**
     * Simulate italix with text skew
     * @param italics true for italics
     */
    public void setTextItalics(boolean italics)
        {
        setTextSkewX(italics ? -0.25f : 0);
        }

    /**
     * Simulate (fake) bold
     * @param bold true for bold
     */
    public void setTextBold(boolean bold)
        {
        if (bold)
            {
            setFlags(getFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            }
        else
            {
            setFlags(getFlags() & (~Paint.FAKE_BOLD_TEXT_FLAG));
            }
        }

    /**
     * Get calculated (by calculateTextSizeForBox) text size
     * @return calculated text size
     */
    public int getCalculatedTextSize()
        {
        return calculatedTextSize;
        }

    /**
     * Set text to fit inside box (box size is set by setTextBox())
     * This text is used to measure size.
     * Other text could be draw whit this size later
     * @param textToMeasure text
     */
    public void setTextToMeasure(String textToMeasure)
        {
        textToMeasureX = textToMeasure;
        textToMeasureY = null;
        }

    /**
     * Set text to fit inside box (box size is set by setTextBox())
     * Text for width and text for height can be set separately.
     * These texts are used to measure size. Both width and height should fit.
     * Other text could be draw whit this size later
     * @param textToMeasureX text to measure width (Default "MMM")
     * @param textToMeasureY text to measure height (Default "y")
     */
    public void setTextToMeasure(String textToMeasureX, String textToMeasureY)
        {
        this.textToMeasureX = textToMeasureX;
        this.textToMeasureY = textToMeasureY;
        }

    String getTextMeasured()
        {
        return textToMeasureX;
        }


    /**
     * When all parameters are set, this method will calculate actual text size.
     * @param width box width
     * @param height box height
     */
    public void calculateTextSizeForBox(int width, int height)
        {
        if ( boxWidth == width && boxHeight== height )
            {
            return;
            }

        boxWidth = width;
        boxHeight = height;

        Rect bounds = new Rect(); // to store text dimension data
        setTextSize(1000f);

        getTextBounds(textToMeasureX, 0, textToMeasureX.length(), bounds);

        if (textToMeasureY != null) // separate text is used for measure height
            {
            Rect boundsY = new Rect();
            getTextBounds(textToMeasureY, 0, textToMeasureY.length(), boundsY);
            bounds.top = boundsY.top;
            bounds.bottom = boundsY.bottom; // height dimensions are copied into original "bounds"
            }

        // Place for accents are added (the lower dimension of 'y' is added to the top)
        bounds.top -= bounds.bottom;

        // textWindth : boxWidth  vs  textHeight : boxHeight
        // egységes box méretre számolja a textméretet. Amelyik nagyobb, annak kell elférnie
        if (1000 * bounds.width() / boxWidth > 1000 * bounds.height() / boxHeight)
            {
            /* Érdekes, számoknál a bounds.left is pozitív, vagyis van egy kis bevezető üres rész.
             * Vagy eltoljuk a szöveget az y irányban (és ezzel pont a határon tudjuk kiírni, vagy
             * ezt az üres részt is beszámítjuk.*/
            // Aránypár:  textSizeNew : 1000 = widthNew : width1000

            calculatedTextSize = 1000 * boxWidth / bounds.width();
            }
        else
            {
            // Aránypár:  textSizeNew : 1000 = heightNew : height1000
            // A bottom értéket 2x kell venni, mert az ékezet ugyanannyit növelhet felfelé
            calculatedTextSize = 1000 * boxHeight / bounds.height();
            }

        // A drawtext az alapvonalra ír. yAdjust értékkel kell módosítani az alapvonalat,
        // hogy a bal felső sarkot kapjuk meg.
        // Aránypár: textSize : 1000 = yAdjust : top
        yAdjust = calculatedTextSize * (-bounds.top) / 1000;

        // Ha 'left' érték nem nulla, akkor ezzel törölhetjük az előtte lévő üres részt
        // Aránypár: textSize : 1000 = xAdjust : left
        xAdjust = calculatedTextSize * (-bounds.left) / 1000;

        // További számításokhoz szükség lehet a példaszöveg magasságára
        measuredTextHeight = calculatedTextSize * bounds.height() / 1000;

        setTextSize(calculatedTextSize);

        // width of ellipsis char should be measured newly
        ellipsisWidth = -1f;
        }

    /**
     * Sets coordinates to draw text
     * @param x x coord
     * @param y y coord
     */
    public void setTextXY(int x, int y)
        {
        this.textX = x;
        this.textY = y;
        }

    /**
     * Sets text align to draw text
     * @param align text alignment
     */
    public void setTextAlign(int align)
        {
        this.textAlign = align;
        }

    /**
     * Draw text with the size, coordinates and text-align previously set
     * @param canvas canvas to draw on
     * @param text text to draw
     *              Text size comes from measured text, but aligned width is calculated
     *              from drawn text.
     */
    public void drawText(Canvas canvas, String text)
        {
        // A sor értékeit az alapbeállításból indul
        int adjustedY = textY + yAdjust;

        if ((textAlign & ALIGN_ABOVE) != 0) // ABOVE
            {
            adjustedY -= measuredTextHeight;
            }
        else if ((textAlign & ALIGN_BELOW) == 0) // CENTER (nem ABOVE és nem BELOW)
            {
            adjustedY -= (measuredTextHeight / 2);
            }

        // Az x értéket viszont a tényleges szövegből választja
        int adjustedX = textX + xAdjust;

        if ((textAlign & ALIGN_RIGHT) == 0) // nem RIGHT (CENTER vagy LEFT)
            {
            Rect bounds = new Rect();
            getTextBounds(text, 0, text.length(), bounds);

            if ((textAlign & ALIGN_LEFT) != 0) // LEFT
                {
                adjustedX -= bounds.width();
                }
            else                            // CENTER
                {
                adjustedX -= (bounds.width() / 2);
                }
            }

        canvas.drawText(text,
                (float) adjustedX,
                (float) adjustedY,
                this);

        }

    /**
     * Draw text with the size previously set
     * @param canvas canvas to draw on
     * @param text text to draw
     * @param x x coordinate
     * @param y y coordinate
     * @param align how to align text to coordinates (center, left, right, above, below.
     *              Text size comes from measured text, but aligned width is calculated
     *              from drawn text.
     */
    void drawText(Canvas canvas, String text, int x, int y, int align)
        {
        setTextXY( x, y );
        setTextAlign( align );
        drawText( canvas, text );
        }

    /**
     * Draw text break and ellipsized at max width.
     * Text is aligned BELOW and RIGHT from the coord.
     * LEFT-TOP corner of the text will be at coord.
     * @param canvas canvas to draw on
     * @param text text to draw
     * @param x x coordinate
     * @param y y coordinate
     * @param maxWidth where to cut text
     * @param highlighted Paint of background, or null, if no background is needed
     */
    void drawEllipsizedText(Canvas canvas, String text, int x, int y, int maxWidth,
                            Paint highlighted)
        {
        if ( highlighted != null )
            {
            float left = x - 2;
            float top = y - 2;
            float right = x + maxWidth + 2;
            float bottom = y + measuredTextHeight + 2;

            RectF rectF = new RectF(left, top, right, bottom);

            canvas.drawRoundRect(rectF, 5f, 5f, highlighted);
            }

        int adjustedY = y + yAdjust;

        int adjustedX = x + xAdjust;

        if (ellipsisWidth < 0f)
            {
            ellipsisWidth = measureText(ELLIPSIS);
            }

        // There is a small issue: text will be truncated,
        // if length is longer than width-ellipsiswidth (instead of width)
        // It can not be recognized by the user btw.
        int end = breakText(text, 0, text.length(),
                true, maxWidth - ellipsisWidth, null);
        canvas.drawText(text, 0, end, adjustedX, adjustedY, this);
        if (end < text.length())
            {
            canvas.drawText("\u2026", adjustedX + measureText(text, 0, end), adjustedY, this);
            }
        }


    /**
     * Same as drawEllipsizedText() with no highlighted background
     */
    public void drawEllipsizedText(Canvas canvas, String text, int x, int y, int maxWidth)
        {
        drawEllipsizedText(canvas, text, x, y, maxWidth, null);
        }


    /**
     * Returns real height of measured text. It will be smaller than box height.
     * This Height can be used as line height for multiple lines.
     * @return Measured height of text
     */
    public int getMeasuredTextHeight()
        {
        return measuredTextHeight;
        }
    }
