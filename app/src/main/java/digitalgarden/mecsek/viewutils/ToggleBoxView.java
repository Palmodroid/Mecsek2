package digitalgarden.mecsek.viewutils;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class ToggleBoxView extends BoxAndTextView
    {
    public interface OnCheckedChangeListener
        {
        public void onCheckedChanged(ToggleBoxView toggleBoxView, boolean isChecked);
        }

    private OnCheckedChangeListener onCheckedChangeListener;


    private int state = 0;

    private String[] title;
    private Longstyle[] longstyle;

    public ToggleBoxView(Context context)
        {
        super(context);
        init();
        }

    public ToggleBoxView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        init();
        }

    public ToggleBoxView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        init();
        }

    private void init()
        {
        title = new String[2];
        title[0] = "OFF";
        title[1] = "ON";

        longstyle = new Longstyle[2];
        longstyle[0] = new Longstyle( getContext(), 1L);
        longstyle[1] = new Longstyle( getContext(), 2L);

        setOnClickListener(new OnClickListener()
            {
            @Override
            public void onClick(View v)
                {
                state++;
                state %= 2;
                invalidate();

                if (onCheckedChangeListener != null)
                    {
                    onCheckedChangeListener.onCheckedChanged(ToggleBoxView.this, state == 1);
                    }
                }
            });
        }

    public void setOnCheckedChangeListener( OnCheckedChangeListener onCheckedChangeListener)
        {
        this.onCheckedChangeListener = onCheckedChangeListener;
        }


    public void setStyle(int index, String title, long longstyle )
        {
        index %=2;
        this.title[index] = title;
        this.longstyle[index].set( longstyle );
        invalidate(); // Normally these data arrive before showing view, so invalidate is not necessary
        }

    public void setChecked( boolean checked )
        {
        state = checked ? 1 : 0;
        invalidate();
        }

    public boolean isChecked()
        {
        return state == 1;
        }

    @Override
    protected void onDraw(Canvas canvas)
        {
        setText( title[state] );
        setLongstyle( longstyle[state] );

        super.onDraw(canvas);
        }
    }
