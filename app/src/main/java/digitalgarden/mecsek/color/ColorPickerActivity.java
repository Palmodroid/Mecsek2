package digitalgarden.mecsek.color;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.viewutils.*;


/**
 * ColorPickerActivity picks a style in Longtime format (paper color, ink color, bold and italics attributes)
 * Activity uses the same layout for 2 different tasks:
 * - IndexedStyle to select an indexed style
 * - ComposeStyle to compose an indexed or custom style
 * Layout has 3 parts:
 * - titleCellView
 * - colorPickerCheckedLayout
 * - buttons line (IndexedStyleButtonsLayout or ComposeStyleButtonsLayout)
 * Buttons in the different rows:
 * - IndexedStyleButtonsLayout: "Custom" to compose custom style
 * - ComposeStyleButtonsLayout: "Paper/Ink" "Bold" "Italics" "Set" (to show and set currently composed style)
 *
 * showIndexedStyleView() -  colorPickerCheckedLayout.isShowingLongstyles() is TRUE
 * cells contain INDEX (1-64) as int in Tag
 * show user defined styles from 1-64
 *
 *      SHORT CLICK:    select style and exit
 *      "BACK"          exit
 *      LONG CLICK:     compose this style
 *      "CUSTOM"        compose a compound (not indexed) style
 *
 *      The last two will call: showComposeStyleView( false ) to select paper color first
 *          composeIndex = 0 ("CUSTOM") / 1-64 (int)v.getTag();
 *          composeStyle = custom style (0L currently) / not indexed copy of indexed style
 *          inkComposed and paperComposed are FALSE
 *
 * showComposeStyleView( boolean showInkColor ) -  colorPickerCheckedLayout.isShowingLongstyles() is FALSE
 * cells contain color code in a three letter String, as defined in ColorPickerCheckedLayout.PLAN
 * show 64 custom colors: two tables for ink/paper, on 4 levels to show all colors (64^4)
 *
 *      Paper/Ink set by ColorPickerCheckedLayout.showColors(,showInkColor), get by .isShowingInkColor()
 *          if both colors are selected, ColorPickerActivity will return on short click
 *          if "Paper/Ink" button is touched, both colors should be selected, or "Set" will return
 *
 *      Levels set by ColorPickerCheckedLayout.colorsUp(plan area) and .colorsDown
 **
 *      SHORT CLICK:    select color and go to next table. If both papre and ink colors are ready, returns
 *      LONG CLICK:     one color level up
 *      "BACK"          one color level down, than exit to indexedStyle view
 *      BOLD, ITALICS   change bold/italics attribute
 *      PAPER/INK       change table (from this point both paper and ink should be selected to return)
 *      SET             returns
 *
 *      return means:
 *          return to indexedStyle, and composeIndex style will be updated to composeStyle
 *          return with composeStyle if composeIndex is 0
 *
 */
/*
 * NAMES
 *
 *                  Longstyle <= 0L             - Default (not defined) style
 * 1 <=             Longstyle < COMPOUND_MASK   - Indexed style
 * COMPOUND_MASK <= Longstyle                   - Compound style
 * (Default and Compound styles are instance styles)
 *
 * Two views:
 *              Indexed style view
 *              Compose style view
 *
 * 2ND IDEA
 *
 * Loader is not needed (not obligatory) - One style is pulled at once (or maximum 64)
 * All database access is moved to Longstyle
 *
 * 1ST IDEA
 *
 * Implement LoaderManager.LoaderCallbacks<Cursor>
 * initLoader call comes o Activity/Fragment start (onResume)
 * ((initLoader can be callaed several times, while restartLoader will restart Loader expicitly))
 * ((onResume is not necessary, it can be in onCreate - observation is not used in these classes))
 * getLoaderId() comes from table Id (this is similar across lists)
 * onCreateLoader - simplified from GenericCombinedListFragment
 * projection setup is similar to GenericCombinedListFragment
 * onLoaderFinished sets adapter
 * onLoaderReset sets adapter to null
 *
 * Adapter
 *
 * Adapter is used by fillColor()
 * fillColor should be called at start and every time when Loader returns
 *
 * Mi lenne, ha fillColor-t beletennénk a ColorCheckedLayout onDraw metódusába?
 * ViewGroupban ez nem kerül meghívásra.
 * Ki kell ezt tenni innen egyáltalán?
 */
public class ColorPickerActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener
    {
    // Layouts
    private BoxAndTextView titleCellView;
    private ColorPickerCheckedLayout colorPickerCheckedLayout;
    private ExternalCheckedLayout ComposeStyleButtonsLayout;
    private ExternalCheckedLayout IndexedStyleButtonsLayout;

    private ToggleBoxView paperInkToggle;
    private ToggleBoxView boldToggle;
    private ToggleBoxView italicsToggle;
    private BoxAndTextView setButton;

    // It is ALWAYS Index, and never Compound style
    // 0 means custom style
    private int composeIndex;

    // Longstyle to compose - the same instance is used by colorPickerCheckedLayout
    // It is ALWAYS Compound style, and never indexed
    private Longstyle composeStyle;

    // true - if color was selected
    private boolean inkComposed;
    private boolean paperComposed;


    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_color_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);

        composeStyle = new Longstyle(this);

        final TextPaint titlePaint = new TextPaint();
        titlePaint.setTextToMeasure("MMMMMMMMMMMMMMMMMM", "Áy");

        titleCellView = findViewById(R.id.title_cell_view);
        titleCellView.setTextPaint( titlePaint );

        final TextPaint textPaint = new TextPaint();
        textPaint.setTextToMeasure("MMMMMM", "Áy");

        colorPickerCheckedLayout = findViewById(R.id.color_checked_layout);
        colorPickerCheckedLayout.setOnClickListener(this);
        colorPickerCheckedLayout.setOnLongClickListener(this);

        ComposeStyleButtonsLayout = findViewById(R.id.color_button_checked_layout);
        ComposeStyleButtonsLayout.connect(new ExternalCheckedLayout.ChildViewSource()
            {
            @Override
            public View createChildView(int row, int col)
                {
                switch (col)
                    {
                    case 0:
                        paperComposed = false;
                        inkComposed = false;
                        paperInkToggle = new ToggleBoxView(ColorPickerActivity.this);
                        paperInkToggle.setTextPaint(textPaint);
                        paperInkToggle.setStyle(0, "Paper", 0x430055aaff000055L);
                        paperInkToggle.setStyle(1, "Ink", 0x4300ffffaa000055L);
                        paperInkToggle.setOnCheckedChangeListener(new ToggleBoxView.OnCheckedChangeListener()
                            {
                            @Override
                            public void onCheckedChanged(ToggleBoxView toggleBoxView, boolean isChecked)
                                {
                                showComposeStyleView( isChecked );
                                }
                            });
                        return paperInkToggle;
                    case 1:
                        italicsToggle = new ToggleBoxView(ColorPickerActivity.this);
                        italicsToggle.setTextPaint(textPaint);
                        italicsToggle.setStyle(0, "italics", 0x4000eaeaeac0c0c0L);
                        italicsToggle.setStyle(1, "ITALICS", 0x41003fff3fff0000L);
                        italicsToggle.setOnCheckedChangeListener(new ToggleBoxView.OnCheckedChangeListener()
                            {
                            @Override
                            public void onCheckedChanged(ToggleBoxView toggleBoxView, boolean isChecked)
                                {
                                composeStyle.setItalicsText(isChecked);
                                colorPickerCheckedLayout.fillColors();
                                setButton.invalidate();

                                }
                            });
                        return italicsToggle;
                    case 2:
                        boldToggle = new ToggleBoxView(ColorPickerActivity.this);
                        boldToggle.setTextPaint(textPaint);
                        boldToggle.setStyle(0, "bold", 0x4000eaeaeac0c0c0L);
                        boldToggle.setStyle(1, "BOLD", 0x42003fff3fff0000L);
                        boldToggle.setOnCheckedChangeListener(new ToggleBoxView.OnCheckedChangeListener()
                            {
                            @Override
                            public void onCheckedChanged(ToggleBoxView toggleBoxView, boolean isChecked)
                                {
                                composeStyle.setBoldText(isChecked);
                                colorPickerCheckedLayout.fillColors();
                                setButton.invalidate();
                                }
                            });
                        return boldToggle;
                    case 3:
                        setButton = new BoxAndTextView(ColorPickerActivity.this);
                        setButton.setTextPaint(textPaint);
                        setButton.setText("SET");
                        setButton.setLongstyle( composeStyle );
                        setButton.setOnClickListener(new View.OnClickListener()
                            {
                            @Override
                            public void onClick(View v)
                                {
                                if ( colorPickerCheckedLayout.isShowingLongstyles() )
                                    {
                                    return;
                                    }
                                else
                                    {
                                    Longstyle longstyle = new Longstyle(ColorPickerActivity.this, composeIndex);
                                    longstyle.set(
                                            composeStyle.getInkColor(),
                                            composeStyle.getPaperColor(),
                                            composeStyle.isBoldText(),
                                            composeStyle.isItalicsText());
                                    showIndexedStyleView();
                                    }
                                }
                            });
                        return setButton;
                    default:
                        BoxView emptyButton = new BoxView(ColorPickerActivity.this);
                        emptyButton.setVisibility(View.INVISIBLE);
                        return emptyButton;
                    }
                }
            });

        IndexedStyleButtonsLayout = findViewById(R.id.longstyle_button_checked_layout);
        IndexedStyleButtonsLayout.connect(new ExternalCheckedLayout.ChildViewSource()
            {
            @Override
            public View createChildView(int row, int col)
                {
                switch (col)
                    {
                    case 3:
                        BoxAndTextView customStyleButton = new BoxAndTextView(ColorPickerActivity.this);
                        customStyleButton.setTextPaint(textPaint);
                        customStyleButton.setText("Custom");
                        customStyleButton.setOnClickListener(new View.OnClickListener()
                            {
                            @Override
                            public void onClick(View v)
                                {
                                composeIndex = 0;
                                composeStyle.set( 0L );
                                showComposeStyleView( false );
                                }
                            });
                        return customStyleButton;
                    default:
                        BoxView emptyButton = new BoxView(ColorPickerActivity.this);
                        emptyButton.setVisibility(View.INVISIBLE);
                        return emptyButton;
                    }
                }
            });

        showIndexedStyleView();
        }


    private void showIndexedStyleView()
        {
        paperComposed = false;
        inkComposed = false;

        titleCellView.setText("Select style!");
        titleCellView.invalidate();
        colorPickerCheckedLayout.showLongstyles();
        ComposeStyleButtonsLayout.setVisibility( View.GONE );
        IndexedStyleButtonsLayout.setVisibility( View.VISIBLE );
        }

    private void showComposeStyleView(boolean showInkColor )
        {
        titleCellView.setText( showInkColor ? "Select INK color!" : "Select PAPER color!");
        titleCellView.invalidate();
        colorPickerCheckedLayout.showColors( composeStyle, showInkColor );
        ComposeStyleButtonsLayout.setVisibility( View.VISIBLE );
        IndexedStyleButtonsLayout.setVisibility( View.GONE );

        paperInkToggle.setChecked( showInkColor );
        boldToggle.setChecked( composeStyle.isBoldText());
        italicsToggle.setChecked( composeStyle.isItalicsText());
        }

    @Override
    public boolean onLongClick(View v)
        {
        if (colorPickerCheckedLayout.isShowingLongstyles())
            {
            composeIndex = (int)v.getTag();
            composeStyle.set ((long) ((int)v.getTag()));
            composeStyle.clearIndex();
            showComposeStyleView( false );
            }
        else
            {
            colorPickerCheckedLayout.colorsUp((String) v.getTag());
            }
        return true;
        }


    @Override
    public void onBackPressed()
        {
        if ( colorPickerCheckedLayout.isShowingLongstyles() )
            {
            super.onBackPressed();
            }
        else if ( !colorPickerCheckedLayout.colorsDown() )
            {
            showIndexedStyleView();
            }
        }


    @Override
    public void onClick(View v)
        {
        if ( colorPickerCheckedLayout.isShowingLongstyles() )
            {
            return;
            }
        else // selecting colors
            {
            if ( colorPickerCheckedLayout.isShowingInkColor() )
                {
                int inkColor = ((BoxAndTextView) v).getLongstyle().getInkColor();
                composeStyle.setInkColor(inkColor);
                inkComposed = true;
                }
            else
                {
                int paperColor = ((BoxAndTextView) v).getLongstyle().getPaperColor();
                composeStyle.setPaperColor(paperColor);
                paperComposed = true;
                }


            if ( inkComposed && paperComposed )
                {
                if ( composeIndex == 0 )
                    return;
                else
                    {
                    Longstyle longstyle = new Longstyle( this, composeIndex );
                    longstyle.set(
                            composeStyle.getInkColor(),
                            composeStyle.getPaperColor(),
                            composeStyle.isBoldText(),
                            composeStyle.isItalicsText());
                    showIndexedStyleView();
                    }
                }
            else
                {
                setButton.invalidate();
                showComposeStyleView( !colorPickerCheckedLayout.isShowingInkColor() );
                }
            }
        }

    }
