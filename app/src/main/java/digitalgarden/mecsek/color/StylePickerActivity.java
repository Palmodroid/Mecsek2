package digitalgarden.mecsek.color;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.viewutils.*;

import static digitalgarden.mecsek.viewutils.Longstyle.*;


/**
 * StylePickerActivity picks a style in Longtime format (paper color, ink color, bold and italics attributes)
 * Activity uses the same layout for different tasks:
 * - IndexedStyleView (Fix or Custom) to select an indexed style
 * - ComposeStyleView to compose an indexed or custom style
 * Layout has 3 parts:
 * - titleCellView
 * - colorPickerCheckedLayout
 * - buttons line (IndexedStyleButtonsLayout or ComposeStyleButtonsLayout)
 * Buttons in the different rows:
 * - IndexedStyleButtonsLayout: "Custom" to compose custom style
 * - ComposeStyleButtonsLayout: "Paper/Ink" "Bold" "Italics" "Set" (to show and set currently composed style)
 *
 * setIndexedStyleView( showFixStyle ) -  colorPickerCheckedLayout.isShowingIndexedStyleView() is TRUE
 *  TRUE: shows FixIndexedStyleView
 *  FALSE: shows CustomIndexedView
 * setIndexedStyleView() - refresh last (fix or custom) indexed style view
 * cells contain INDEX (1-128) as int in Tag
 * shows user defined styles from 1-64 (fix) or 65-128 (custom)
 *
 *      SHORT CLICK:    select style and exit
 *      "BACK"          returns to custom from fixed, then exit
 *      LONG CLICK:     compose this style
 *      "CUSTOM"        compose a compound (not indexed) style
 *
 *      The last two will call: showComposeStyleView( false ) to select paper color first
 *          composeIndex = 0 ("CUSTOM") / 1-128 (int)v.getTag();
 *          composeStyle = custom style (0L currently) / not indexed copy of indexed style
 *          inkComposed and paperComposed are FALSE
 *
 *      TITLE ROW CLICK:    changes between FIX and CUSTOM
 *
 * showComposeStyleView( boolean showInkColor ) -  colorPickerCheckedLayout.isShowingIndexedStyleView() is FALSE
 * cells contain color code in a three letter String, as defined in ColorPickerCheckedLayout.PLAN
 * show 64 custom colors: two tables for ink/paper, on 4 levels to show all colors (64^4)
 *
 *      Paper/Ink set by ColorPickerCheckedLayout.showComposeStyleView(,showInkColor), get by .isShowingInkColor()
 *          if both colors are selected, StylePickerActivity will return on short click
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
public class StylePickerActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener
    {

    public static final String LONGSTYLE_KEY = "longstylekey";


    // Layouts
    private BoxAndTextView titleCellView;
    private StylePickerCheckedLayout stylePickerCheckedLayout;
    private ExternalCheckedLayout ComposeStyleButtonsLayout;
    private ExternalCheckedLayout IndexedStyleButtonsLayout;

    private ToggleBoxView paperInkToggle;
    private ToggleBoxView boldToggle;
    private ToggleBoxView italicsToggle;
    private BoxAndTextView setButton;

    // TextPaint to measure text for all buttons
    // Use Longstyle for buttons!!
    private TextPaint textPaint;

    // It is ALWAYS Index, and never Compound style
    // 0 means custom style
    private long composeIndex;

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

        setContentView(R.layout.activity_style_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);

        Longstyle.initDefaults( this );

        // ??? What to do with style that comes from Intent ???
        // Intent intent = getIntent();
        // long intentStyle = intent.getLongExtra( LONGSTYLE_KEY, 0L );
        // and now? ComposeStyle can not contain index!
        composeStyle = new Longstyle(this );

        final TextPaint titlePaint = new TextPaint();
        titlePaint.setTextToMeasure("MMMMMMMMMMMMMMMMMM", "Áy");

        titleCellView = findViewById(R.id.title_cell_view);
        titleCellView.setTextPaint(titlePaint);
        titleCellView.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View v)
                {
                if ( stylePickerCheckedLayout.isShowingIndexedStyleView() )
                    {
                    showIndexedStyleView( !stylePickerCheckedLayout.isShowingFixStyleView() );
                    }
                }
            });

        textPaint = new TextPaint();
        textPaint.setTextToMeasure("MMMMMM", "Áy");

        stylePickerCheckedLayout = findViewById(R.id.color_checked_layout);
        stylePickerCheckedLayout.setOnClickListener(this);
        stylePickerCheckedLayout.setOnLongClickListener(this);

        ComposeStyleButtonsLayout = findViewById(R.id.color_button_checked_layout);
        ComposeStyleButtonsLayout.connect(new ExternalCheckedLayout.ChildViewSource()
            {
            @Override
            public View createChildView(int row, int col)
                {
                switch (col)
                    {
                    case 0:
                        return initPaperInkToggle();
                    case 1:
                        return initItalicsToggle();
                    case 2:
                        return initBoldToggle();
                    case 3:
                        return initSetButton();
                    default:
                        return initEmptyButton();
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
                        return initCustomStyleButton();
                    default:
                        return initEmptyButton();
                    }
                }
            });

        showIndexedStyleView( false );
        }


    private void showIndexedStyleView( boolean showFixStyle )
        {
        stylePickerCheckedLayout.setIndexedStyleView( showFixStyle );
        showIndexedStyleView();
        }

    private void showIndexedStyleView()
        {
        paperComposed = false;
        inkComposed = false;

        titleCellView.setText(stylePickerCheckedLayout.isShowingFixStyleView() ?
                "Predefinied styles" : "Select style!");
        titleCellView.invalidate();
        stylePickerCheckedLayout.showIndexedStyleView();
        ComposeStyleButtonsLayout.setVisibility(View.GONE);
        IndexedStyleButtonsLayout.setVisibility(View.VISIBLE);
        }

    private void showComposeStyleView(boolean showInkColor)
        {
        titleCellView.setText(showInkColor ? "Select INK color!" : "Select PAPER color!");
        titleCellView.invalidate();
        stylePickerCheckedLayout.showComposeStyleView(composeStyle, showInkColor);
        ComposeStyleButtonsLayout.setVisibility(View.VISIBLE);
        IndexedStyleButtonsLayout.setVisibility(View.GONE);

        paperInkToggle.setChecked(showInkColor);
        boldToggle.setChecked(composeStyle.isBoldText());
        italicsToggle.setChecked(composeStyle.isItalicsText());
        setButton.setText( composeIndex == 0L ? "SET" : "#"+composeIndex );
        setButton.invalidate();
        }

    private void refreshComposeStyle()
        {
        stylePickerCheckedLayout.refreshComposeStyleView();
        setButton.invalidate();
        }

    @Override
    public boolean onLongClick(View v)
        {
        if (stylePickerCheckedLayout.isShowingIndexedStyleView())
            {
            composeIndex = (long)v.getTag();
            composeStyle.set ( composeIndex );
            composeStyle.convert2InstanceStyle();
            showComposeStyleView( false );
            }
        else
            {
            stylePickerCheckedLayout.colorsUp((String) v.getTag());
            }
        return true;
        }


    @Override
    public void onBackPressed()
        {
        if ( stylePickerCheckedLayout.isShowingIndexedStyleView() )
            {
            if ( stylePickerCheckedLayout.isShowingFixStyleView() )
                showIndexedStyleView( false );
            else
                super.onBackPressed();
            }
        else if ( !stylePickerCheckedLayout.colorsDown() )
            {
            showIndexedStyleView();
            }
        }


    @Override
    public void onClick(View v)
        {
        if ( stylePickerCheckedLayout.isShowingIndexedStyleView() )
            {
            if ( stylePickerCheckedLayout.isShowingFixStyleView() )
                {
                titleCellView.setText( Longstyle.getMemoTitle( (long)v.getTag() ) );
                titleCellView.invalidate();
                }
            else
                {
                returnSelectedStyle( (long)v.getTag() );
                }
            }
        else // selecting colors
            {
            if ( stylePickerCheckedLayout.isShowingInkColor() )
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
                if ( composeIndex == 0L )
                    {
                    // Only SET button could return
                    // returnSelectedStyle( composeStyle.getCompoundStyle() );
                    showComposeStyleView( !stylePickerCheckedLayout.isShowingInkColor() );
                    // Same behavior like without paperComposed/inkComposed
                    }
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
                showComposeStyleView( !stylePickerCheckedLayout.isShowingInkColor() );
                }
            }
        }

    private View initPaperInkToggle()
        {
        paperInkToggle = new ToggleBoxView(StylePickerActivity.this);
        paperInkToggle.setTextPaint(textPaint);
        paperInkToggle.setStyle(0, "Paper", MEMO_SWITCH_1 );
        paperInkToggle.setStyle(1, "Ink", MEMO_SWITCH_2 );
        paperInkToggle.setOnCheckedChangeListener(new ToggleBoxView.OnCheckedChangeListener()
            {
            @Override
            public void onCheckedChanged(ToggleBoxView toggleBoxView, boolean isChecked)
                {
                paperComposed = false;
                inkComposed = false;
                showComposeStyleView( isChecked );
                }
            });
        return paperInkToggle;
        }

    private View initItalicsToggle()
        {
        italicsToggle = new ToggleBoxView(StylePickerActivity.this);
        italicsToggle.setTextPaint(textPaint);
        italicsToggle.setStyle(0, "italics", MEMO_TOGGLE_OFF);
        italicsToggle.setStyle(1, "ITALICS", MEMO_TOGGLE_ITALICS);
        italicsToggle.setOnCheckedChangeListener(new ToggleBoxView.OnCheckedChangeListener()
            {
            @Override
            public void onCheckedChanged(ToggleBoxView toggleBoxView, boolean isChecked)
                {
                composeStyle.setItalicsText(isChecked);
                refreshComposeStyle();
                }
            });
        return italicsToggle;
        }

    private View initBoldToggle()
        {
        boldToggle = new ToggleBoxView(StylePickerActivity.this);
        boldToggle.setTextPaint(textPaint);
        boldToggle.setStyle(0, "bold", MEMO_TOGGLE_OFF);
        boldToggle.setStyle(1, "BOLD", MEMO_TOGGLE_BOLD);
        boldToggle.setOnCheckedChangeListener(new ToggleBoxView.OnCheckedChangeListener()
            {
            @Override
            public void onCheckedChanged(ToggleBoxView toggleBoxView, boolean isChecked)
                {
                composeStyle.setBoldText(isChecked);
                refreshComposeStyle();
                }
            });
        return boldToggle;
        }

    private View initSetButton()
        {
        setButton = new BoxAndTextView(StylePickerActivity.this);
        setButton.setTextPaint(textPaint);
        // setButton.setText("SET");
        setButton.setLongstyle( composeStyle );
        setButton.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View v)
                {
                if ( composeIndex == 0L )
                    {
                    returnSelectedStyle( composeStyle.getCompoundStyle() );
                    }
                else
                    {
                    Longstyle longstyle = new Longstyle(StylePickerActivity.this, composeIndex);
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
        }

    private View initCustomStyleButton()
        {
        BoxAndTextView customStyleButton = new BoxAndTextView(StylePickerActivity.this);
        customStyleButton.setTextPaint(textPaint);
        customStyleButton.setLongstyle( MEMO_BASIC );
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
        }

    private View initEmptyButton()
        {
        BoxView emptyButton = new BoxView(StylePickerActivity.this);
        emptyButton.setVisibility(View.INVISIBLE);
        return emptyButton;
        }

    private void returnSelectedStyle( long longstyle )
        {
        Intent resultIntent = new Intent();
        // TODO Add extras or a data URI to this intent as appropriate.
        resultIntent.putExtra(LONGSTYLE_KEY, longstyle);
        setResult( Activity.RESULT_OK, resultIntent );
        finish();
        }

    }
