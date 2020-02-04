package digitalgarden.mecsek.color;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import digitalgarden.mecsek.viewutils.*;

public class StylePickerCheckedLayout extends CheckedLayout
    {
    private static final int CUSTOM_STYLE_OFFSET = 64;

    private static final String[][] PLAN = {
            {"322", "311", "321", "320", "230", "231", "131", "232"},
            {"330", "300", "310", "211", "221", "121", "130", "030"},
            {"331", "301", "200", "210", "220", "120", "020", "031"},
            {"332", "302", "201", "100", "110", "010", "021", "032"},
            {"333", "312", "202", "101", "001", "011", "022", "132"},
            {"222", "303", "212", "102", "002", "012", "122", "033"},
            {"111", "313", "203", "103", "003", "013", "123", "133"},
            {"000", "323", "213", "223", "112", "113", "023", "233"}};


    // true  - show indexed styles (fix or custom)
    // false - show compose style chart
    private boolean showingIndexedStyleView;
    // true - show (or use) fix styles (1-64)
    // false - show (or use) custom styles (64-128)
    private boolean showingFixStyleView;
    // true - choose ink color (same paper)
    // false - choose paper color (same ink)
    private boolean showingInkColor;


    // Range can be 256 64 16 4
    private int range;
    // Where to start range (which color part is active)
    private int[] start = new int[3];

    // textPaint for ALL views - text size should be calculated only once
    // textPaint cannot be initialized here, because call will come form constructor
    private TextPaint textPaint;


    public StylePickerCheckedLayout(Context context)
        {
        super(context);
        }

    public StylePickerCheckedLayout(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public StylePickerCheckedLayout(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    @Override
    protected void init()
        {
        // Rows and columns are setStyle programatically
        rows = 8;
        columns = 8;

        textPaint = new TextPaint();
        textPaint.setTextToMeasure("MMM", "Áy");
        }

    @Override
    protected View createChildView(int row, int col)
        {
        BoxAndTextView child = new BoxAndTextView(getContext());
        child.setTextPaint(textPaint);
        return child;
        }

    public void refreshComposeStyleView()
        {
        showingIndexedStyleView = false;

        BoxAndTextView view;

        for (int r = 0; r < 8; r++)
            {
            for (int c = 0; c < 8; c++)
                {
                int color = 0xFF;

                for (int i = 0; i < 3; i++)
                    {
                    int chr = PLAN[r][c].charAt(i);
                    if (chr == ' ') chr = '3';
                    int index = chr - '0';
                    color = color * 256 + index * (range - 1) / 3 + start[i]; // int res = (range-1) / 3;
                    }

                view = ((BoxAndTextView) getChildAt(r, c));

                view.setLongstyle(longstyle.getCompoundStyle());
                if (showingInkColor)
                    {
                    view.getLongstyle().setInkColor(color);
                    } else
                    {
                    view.getLongstyle().setPaperColor(color);
                    }
                view.setText(PLAN[r][c]);
                view.setTag(PLAN[r][c]);
                }
            }
        invalidate();
        }

    public void showIndexedStyleView()
        {
        showingIndexedStyleView = true;

        BoxAndTextView view;

        for (int index = 0, databaseIndex = 1 + (showingFixStyleView ? 0 : CUSTOM_STYLE_OFFSET);
             index < 64; index++, databaseIndex++)
            {
            view = (BoxAndTextView) getChildAt(index);
            view.setLongstyle(databaseIndex);

            view.setText("Xx");
            view.setTag( (long)databaseIndex );
            }
        invalidate();
        }

    // 1titok

    private Longstyle longstyle;

    public void showComposeStyleView(Longstyle longstyle, boolean showInkColor)
        {
        this.longstyle = longstyle;
        this.showingInkColor = showInkColor;

        range = 256;

        start[0] = 0;
        start[1] = 0;
        start[2] = 0;

        refreshComposeStyleView();
        }

    public boolean colorsUp(String area)
        {
        if (showingIndexedStyleView || range <= 4)
            return false;

        range /= 4;

        for (int i = 0; i < 3; i++) // !!!!!!!!!!!!!!!
            {
            start[i] += (area.charAt(i) - '0') * range;
            }

        refreshComposeStyleView();

        return true;
        }

    public boolean colorsDown()
        {
        if (showingIndexedStyleView || range >= 256)
            return false;

        range = range * 4;

        for (int i = 0; i < 3; i++) // !!!!!!!!!!!!!!!!
            {
            start[i] /= range;
            start[i] *= range;
            }

        refreshComposeStyleView();

        return true;
        }

    public void setIndexedStyleView(boolean showFixStyle)
        {
        showingFixStyleView = showFixStyle;
        }

    public boolean isShowingIndexedStyleView()
        {
        return showingIndexedStyleView;
        }

    public boolean isShowingFixStyleView()
        {
        return showingFixStyleView;
        }

    public boolean isShowingInkColor()
        {
        return showingInkColor;
        }
    }
