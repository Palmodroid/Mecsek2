package digitalgarden.mecsek.diary;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import digitalgarden.mecsek.viewutils.BackgroundAndTextView;



public class ComplexHeaderView extends BackgroundAndTextView
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

    public void setBackgroundColor( int backgroundColor )
        {
        this.backgroundColor = backgroundColor;
        }

    @Override
    protected void onDraw(Canvas canvas)
        {
        getBackgroundPaint().setColor( backgroundColor );
        super.onDraw(canvas);
        }
    }
