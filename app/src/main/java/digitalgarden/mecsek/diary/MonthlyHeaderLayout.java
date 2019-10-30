package digitalgarden.mecsek.diary;

import android.content.Context;
import digitalgarden.mecsek.viewutils.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import digitalgarden.mecsek.viewutils.CheckedLayout;

public class MonthlyHeaderLayout extends CheckedLayout
    {
    // textPaint for ALL views - text size should be calculated only once
    // textPaint cannot be initialized here, because call will come form constructor
    private TextPaint textPaint;

    public MonthlyHeaderLayout(Context context)
        {
        super(context);
        }

    public MonthlyHeaderLayout(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public MonthlyHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    @Override
    public void init()
        {
        textPaint = new TextPaint();
        textPaint.setTextToMeasure("MMMMM", "Áy");
        }

    static String[] nameOfDays = {"MON", "TUE", "WED", "THR", "FRI", "SAT", "SUN"};

    @Override
    public View getChildView( int row, int col )
        {
        ComplexHeaderView child = new ComplexHeaderView(getContext());
        child.setTextPaint(textPaint);
        child.setText(nameOfDays[col]);
        child.setBackgroundColor( DailyData.getColorForDayName(col) );
        return child;
        }
    }
