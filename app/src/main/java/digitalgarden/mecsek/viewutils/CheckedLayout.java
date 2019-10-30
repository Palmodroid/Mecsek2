package digitalgarden.mecsek.viewutils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import digitalgarden.mecsek.R;


/**
 * Ehhez kell az attr.xml
 */

public abstract class CheckedLayout extends ViewGroup
    {
    protected int columns = 7;
    protected int rows = 5;
    private int verticalSpacing = 0;
    private int horizontalSpacing = 0;


    public CheckedLayout(Context context)
        {
        super(context);
        init();
        initChildViews(context);
        }

    public CheckedLayout(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        initAttributes(context, attrs);
        init();
        initChildViews(context);
        }

    public CheckedLayout(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
        init();
        initChildViews(context);
        }

    private void initAttributes(Context context, AttributeSet attributeSet)
        {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CheckedLayout);
        verticalSpacing =
                typedArray.getDimensionPixelOffset(R.styleable.CheckedLayout_android_verticalSpacing, verticalSpacing);
        horizontalSpacing = typedArray.getDimensionPixelOffset(R.styleable.CheckedLayout_android_horizontalSpacing, horizontalSpacing);
        columns = typedArray.getInteger(R.styleable.CheckedLayout_columns, columns);
        rows = typedArray.getInteger(R.styleable.CheckedLayout_rows, rows);
        typedArray.recycle();
        }

    private void initChildViews(Context context)
        {
        for (int row = 0; row < rows; row++)
            {
            for (int col = 0; col < columns; col++)
                {
                addView(getChildView( row, col ));
                }
            }
        }

    public void init()
        {
        }

    /*
     * OnClickListener should check childviews
     */
    @Override
    public void setOnClickListener(View.OnClickListener listener )
        {
        // super.setOnClickListener(l);
        for (int index = 0; index < getChildCount(); index++)
            {
            getChildAt( index ).setOnClickListener( listener );
            }
        }

    /*
     * OnLongClickListener should check childviews
     */
    @Override
    public void setOnLongClickListener( View.OnLongClickListener listener )
        {
        // super.setOnLongClickListener(l);
        for (int index = 0; index < getChildCount(); index++)
            {
            getChildAt( index ).setOnLongClickListener( listener );
            }
        }

    public abstract View getChildView(int row, int col );

    /**
     * Return child view at the specified row and column coordinate pair.
     *
     * @param row child view's row (starts from 0)
     * @param col child view's column (starts from 0)
     * @return child view
     */
    public View getChildAt(int row, int col)
        {
        int index = rows * row + col;
        return getChildAt(index);
        }

    /**
     * Any layout manager that doesn't scroll will want this.
     */
    @Override
    public boolean shouldDelayChildPressedState()
        {
        return false;
        }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
        {
        // left | paddingLeft Child horizontalSpacing Child horizontalSpacing Child paddingRight
        // right and bottom values seems to be right and bottom pixel behind the view!!
        // let's assume, that this is NOT true, right and bottom values are the last pixel of the view

        int width = right - left - getPaddingLeft() - getPaddingRight() - horizontalSpacing * (columns - 1);
        int height = bottom - top - getPaddingTop() - getPaddingBottom() - verticalSpacing * (rows - 1);

        for (int row = 0; row < rows; row++)
            {
            for (int col = 0; col < columns; col++)
                {
                int index = row * columns + col;

                int childLeft = getPaddingLeft() + col * width / columns + col * horizontalSpacing;
                int childTop = getPaddingTop() + row * height / rows + row * verticalSpacing;
                int childRight = getPaddingLeft() + (col + 1) * width / columns + col * horizontalSpacing;
                int childBottom = getPaddingTop() + (row + 1) * height / rows + row * verticalSpacing;

                getChildAt(index).layout(
                        childLeft,
                        childTop,
                        childRight,
                        childBottom);
                }
            }
        }
    }
