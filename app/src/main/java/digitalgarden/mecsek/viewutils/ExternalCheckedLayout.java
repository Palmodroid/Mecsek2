package digitalgarden.mecsek.viewutils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import digitalgarden.mecsek.viewutils.CheckedLayout;

public class ExternalCheckedLayout extends CheckedLayout
    {
    private ChildViewSource childViewSource;

    public interface ChildViewSource
        {
        public View createChildView(int row, int col);
        }

    public ExternalCheckedLayout(Context context)
        {
        super(context);
        }

    public ExternalCheckedLayout(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public ExternalCheckedLayout(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    @Override
    protected void initChildViews(Context context)
        {
        }

    public void connect(ChildViewSource childViewSource )
        {
        this.childViewSource = childViewSource;
        super.initChildViews( getContext() );
        }

    @Override
    protected View createChildView(int row, int col)
        {
        return childViewSource.createChildView( row, col );
        }
    }
