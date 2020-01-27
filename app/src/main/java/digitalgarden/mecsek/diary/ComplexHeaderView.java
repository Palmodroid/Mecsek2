package digitalgarden.mecsek.diary;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import digitalgarden.mecsek.viewutils.BoxAndTextView;


public class ComplexHeaderView extends BoxAndTextView
    {
    private int backgroundColor = 0;

    public ComplexHeaderView(Context context)
        {
        super(context);
        }

    public ComplexHeaderView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public ComplexHeaderView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    public void setBackgroundColor(int backgroundColor)
        {
        this.backgroundColor = backgroundColor;
        }

    @Override
    protected void onDraw(Canvas canvas)
        {
        getPaperPaint().setColor(backgroundColor);
        super.onDraw(canvas);
        }
    }