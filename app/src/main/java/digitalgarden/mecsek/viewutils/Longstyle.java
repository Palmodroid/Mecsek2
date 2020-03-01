package digitalgarden.mecsek.viewutils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import static digitalgarden.mecsek.color.ColorDefsTable.VALUE;
import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.tables.LibraryDatabase.COLOR_DEFS;



/**
 * 4TH IDEA:
 *
 * Separated instance cache (for cacheIndex 0)
 * AND seperated database cache (for all other indexes) are needed
 * So it is better to turn back to LongsytleAdapter SINGELTON pattern :)
 * OR:
 * Separated "static" variables should be used for cache and "instance" variables for instance-cache
 *
 * 3RD IDEA Longstyle:
 * Longstyle stores color data to show sqlite records.
 * It stores mixed data for text (ink color, bold and italics) and for background (paper color),
 * that means it cannot be derived from Paint classes. (And lot of other attributes are missing from compoundStyle)
 *
 * Longstyle <= 0 - not defined style
 * Longstyle 1-256 - indexed style
 * The styles are stored by sqlite in COLOR_DEFS table. Because Longstyle class caches sqlite,
 * COLOR_DEFS table should not be accessed and changed outside Longstyle!
 * Longstyle with (above) COMPOUND_MASK - complex style, containing all info without storing them.
 *
 * Changing (indexed) compoundStyle means, that sqlite data will store the changed data!!
 *
 * How to use compoundStyle?
 *
 * Longstyle is just a "store" for style data. Most View uses Paint to define these styles.
 * Paint parameters should be overwritten before onDraw.
 * It is a good idea to perform overwrite INSIDE onDraw, because it will reflect changes immediately.
 *
 * 2ND IDEA
 * Instance is needed, because styles are stored within these instances.
 * In this case it is better to get context by constructor.
 * Then no singleton, but static puffer array can strore already pulled colors.
 *
 * 1ST IDEA
 * Longstyle uses SINGLETON pattern - because it needs context through its getInstence method()
 * https://stackoverflow.com/questions/519520/difference-between-static-class-and-singleton-pattern
 * https://www.fwd.cloud/commit/post/android-context-on-demand/
 *
 * Colors/Styles could be stored as part of the database record - but in this case standalone colors couldn't be used.
 * SharedPreferences are not easy for arrays (no standard method to store them, and export/import is not implemented.
 * So, a separate database table is used to store these data. Export and import are implemented, as for the other parts.
 * Read data from UI thread is allowed (this is just 256 records, and all records are not read at once)
 * Loader could used, but program should start only after retrieving data. (Like in permissions)
 * Other possibility: WAIT screen hiding the whole surface, and making it GONE when loader is finished.
 */
public class Longstyle
    {
    // (be careful! database indices start at 1-, while compoundStyleCache indices start at 0-)
    // 0-63 (1-64 in database) reserved for program / 64-127 (65-128) custom indices for user
    private static final int MAX_INDEX = 128;

    // Indexed styles can have a "memo", to show their names
    // !! This name is NOT stored in database, only initialized default values can have a name
    // (Because these names are fixed to the app. This behavior could be extended to all styles and to the database)
    private static final int MAX_MEMO = 64;

    // MEMO index is databaseIndex (1 is the first) == Indexed longstyles
    public static final long MEMO_BASIC = 1L;
    public static final long MEMO_TOGGLE_OFF = 2L;
    public static final long MEMO_TOGGLE_ON = 3L;
    public static final long MEMO_TOGGLE_ITALICS = 4L;
    public static final long MEMO_TOGGLE_BOLD = 5L;
    public static final long MEMO_SWITCH_1 = 6L;
    public static final long MEMO_SWITCH_2 = 7L;
    public static final long SELECTOR_BLUE = 8L;

    private static String[] memoTitles = new String[MAX_INDEX];


    /* Java long is 64 bit / 8 byte
     * Ts ss PP PP  PP II II II
     *                 Ink color as RR GG BB (AA is always 0xFF)
     *       Paper color as RR GG BB (AA is always 0xFF)
     *  Size not implemented yet
     * Type as: (Signed - not used)
     * (sign) Compound - -   - - Bold  Italics
     *         (40...            02... 01.... hex)
     * COMPOUND BIT - should be the highest bit ! to check compound styles as >= COMPOUND_MASK
     * 0 (or negatíve) : not defined style
     * 1-256 (or < COMPOUND_MASK) : indexed style
     */

    private static final long COMPOUND_MASK = 0x4000000000000000L;

    private static final long BOLD_MASK = 0x0200000000000000L;
    private static final long ITALICS_MASK = 0x0100000000000000L;

    public static final int DEFAULT_INK_COLOR = 0xFF000000;    // Solid balck
    public static final int DEFAULT_PAPER_COLOR = 0x00FFFFFF;  // Transparent white
    public static final boolean DEFAULT_BOLD_TEXT = false;
    public static final boolean DEFAULT_ITALICS_TEXT = false;


    private Context context; // to use database

    // -1             : not indexed color
    // 0 - <MAX_INDEX : indexed color (even if not defined yet)
    private int cacheIndex;

    // Cache for this instance - used if cacheIndex is < 0
    private long compoundStyle;
    private int inkColor;
    private int paperColor;
    private boolean boldText;
    private boolean italicsText;

    private static long[] compoundStyleCache = new long[MAX_INDEX];
    // Value: 0 (default) not yet pulled
    // Value: -1 already pulled, but not exist in database
    // Value: >0 already pulled and pulled value is cached

    private static int[] inkColorCache = new int[MAX_INDEX]; // Contains also AA bits!!
    private static int[] paperColorCache = new int[MAX_INDEX]; // Contains also AA bits!!
    private static boolean[] boldTextCache = new boolean[MAX_INDEX];
    private static boolean[] italicsTextCache = new boolean[MAX_INDEX];


    public Longstyle(Context context)
        {
        this(context, 0L);
        }

    public Longstyle(Context context, long longstyle)
        {
        this.context = context.getApplicationContext(); // Context is needed by database queries
        set(longstyle);
        }

    /**
     * Sets compoundStyle from long parameter >= COMPOUND_MASK : compound data, derived from long 1 - < MAX_INDEX :
     * indexed (pullStyle pulls it) all others : not valid, returns predefined values
     */
    public void set(long longstyle)
        {
        // Compound style
        if (longstyle >= COMPOUND_MASK)
            {
            cacheIndex = -1;

            compoundStyle = longstyle;
            inkColor = calcInkColor(longstyle);
            paperColor = calcPaperColor(longstyle);
            boldText = calcBoldText(longstyle);
            italicsText = calcItalicsText(longstyle);
            }
        // Indexed style
        else
            {
            // Valid index - data comes from LongstyleIndexed (even if not defined)
            cacheIndex = checkIndex(longstyle);

            // Invalid indices - predefined values
            if (isInstanceStyle())
                {
                compoundStyle = 0L; // not defined style
                inkColor = DEFAULT_INK_COLOR;
                paperColor = DEFAULT_PAPER_COLOR;
                boldText = DEFAULT_BOLD_TEXT;
                italicsText = DEFAULT_ITALICS_TEXT;
                }
            }
        }


    private boolean isInstanceStyle()
        {
        return cacheIndex < 0;
        }


    /**
     * Check if index is valid.
     *
     * @param databaseIndex
     * @return cache index (if valid) or -1 if index is invalid
     */
    private static int checkIndex(long databaseIndex)
        {
        return (databaseIndex < 1L || databaseIndex > MAX_INDEX) ? -1 : (int) databaseIndex - 1;
        }

    private static void setNotDefinedStyleCache(int index)
        {
        compoundStyleCache[index] = -1L; // not defined style
        inkColorCache[index] = DEFAULT_INK_COLOR;
        paperColorCache[index] = DEFAULT_PAPER_COLOR;
        boldTextCache[index] = DEFAULT_BOLD_TEXT;
        italicsTextCache[index] = DEFAULT_ITALICS_TEXT;
        }

    public static void pullAllStyles(Context context)
        {
        // Clear all previous style first
        for (int index = 0; index < MAX_INDEX; index++)
            {
            setNotDefinedStyleCache(index);
            }

        // Overwrite all existing styles next        
        String[] projection = {
                column_id(),
                column(VALUE)}; // One column is enough, id_ is the database index (1-256)

        Cursor cursor = context.getContentResolver().query(
                table(COLOR_DEFS).contentUri(),
                projection, null, null, null);

        // Queries are never NULL!!!
        while (cursor.moveToNext())
            {
            // cacheIndex = databaseId - 1
            int cacheIndex = (int) cursor.getLong(cursor.getColumnIndex(column_id())) - 1;

            if (cacheIndex >= 0 && cacheIndex < MAX_INDEX)
                {
                // value is pulled
                // store COMPOUND VALUE of this indexed longstyle
                compoundStyleCache[cacheIndex] = cursor.getLong(cursor.getColumnIndex(column(VALUE)));

                inkColorCache[cacheIndex] = calcInkColor(compoundStyleCache[cacheIndex]);
                paperColorCache[cacheIndex] = calcPaperColor(compoundStyleCache[cacheIndex]);
                boldTextCache[cacheIndex] = calcBoldText(compoundStyleCache[cacheIndex]);
                italicsTextCache[cacheIndex] = calcItalicsText(compoundStyleCache[cacheIndex]);
                }
            }
        cursor.close();
        }


    /**
     * Pull indexed longstyle from database - INDEX IS NOT VERIFIED!!!
     *
     * @return true  - if index is valid, and indexed longstyle exists or       false - indexed longstyle is not yet
     * stored
     */
    private boolean pullStyle()
        {
        // Value not yet pulled - try to pull it
        if (compoundStyleCache[cacheIndex] == 0L)
            {
            String[] projection = {column(VALUE)}; // One column is enough, id_ is the database cacheIndex (1-256)

            Cursor cursor = context.getContentResolver().query(
                    table(COLOR_DEFS).itemContentUri(cacheIndex + 1),    // longstyle == database cacheIndex
                    projection, null, null, null);

            // Queries are never NULL!!!
            if (cursor.moveToFirst())
                {
                // value is pulled
                // store COMPOUND VALUE of this indexed longstyle
                compoundStyleCache[cacheIndex] = cursor.getLong(cursor.getColumnIndex(column(VALUE)));

                inkColorCache[cacheIndex] = calcInkColor(compoundStyleCache[cacheIndex]);
                paperColorCache[cacheIndex] = calcPaperColor(compoundStyleCache[cacheIndex]);
                boldTextCache[cacheIndex] = calcBoldText(compoundStyleCache[cacheIndex]);
                italicsTextCache[cacheIndex] = calcItalicsText(compoundStyleCache[cacheIndex]);
                } else
                {
                // This indexed longstyle does not exist,
                // Storing -1L means, that pull was already tried;
                setNotDefinedStyleCache(cacheIndex);
                }
            cursor.close();
            }

        // Pull already tried - but without success. Previous pull filled up compoundStyleCache with values.
        return (compoundStyleCache[cacheIndex] >= 0L);
        }


    /**
     * Generates longstyle from parameters, and stores it INDEX IS NOT VERIFIED!!! in cache[cacheIndex] and in database,
     * too
     */
    private boolean calcAndPushStyle()
        {
        compoundStyleCache[cacheIndex] = calcCompoundStyle(
                inkColorCache[cacheIndex],
                paperColorCache[cacheIndex],
                boldTextCache[cacheIndex],
                italicsTextCache[cacheIndex]);

        ContentValues values = new ContentValues();
        values.put(column(VALUE), compoundStyleCache[cacheIndex]);

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // Ez még nem működik tökéletesen
        // most az insert és update is REPLACE-ként működik.
        // De inkább UPSERT kéne, vagyis INSERT, ha nincs és UPDATE, ha van
        // (Merthogy a relációkat is törli, ha REPLACE)
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        // context.getContentResolver().insert(
        //        table( COLOR_DEFS ).itemContentUri(cacheIndex + 1), values );

        values.put(column_id(), (long) (cacheIndex + 1));

        if (context.getContentResolver().update(
                table(COLOR_DEFS).contentUri(), values, null, null) != 1)
            {
            return false;
            }

        // if successful (one row was updated) store longstyle in the puffer, too
        return true;
        }


    public void setPaperColor(int paperColor)
        {
        paperColor |= 0xff000000;
        if (isInstanceStyle())
            {
            this.paperColor = paperColor;
            calcStyle();
            } else
            {
            paperColorCache[cacheIndex] = paperColor;
            calcAndPushStyle();
            }
        }

    public void setInkColor(int inkColor)
        {
        inkColor |= 0xff000000;
        if (isInstanceStyle())
            {
            this.inkColor = inkColor;
            calcStyle();
            } else
            {
            inkColorCache[cacheIndex] = inkColor;
            calcAndPushStyle();
            }
        }

    public void setBoldText(boolean boldText)
        {
        if (isInstanceStyle())
            {
            this.boldText = boldText;
            calcStyle();
            } else
            {
            boldTextCache[cacheIndex] = boldText;
            calcAndPushStyle();
            }
        }

    public void setItalicsText(boolean italicsText)
        {
        if (isInstanceStyle())
            {
            this.italicsText = italicsText;
            calcStyle();
            } else
            {
            italicsTextCache[cacheIndex] = italicsText;
            calcAndPushStyle();
            }
        }


    /**
     * Sets style parameters - retaining cacheIndex If it is an indexed style, than corresponding database compoundStyle
     * will be updated
     *
     * @param inkColor
     * @param paperColor
     * @param boldText
     * @param italicsText
     */
    public void set(int inkColor, int paperColor, boolean boldText, boolean italicsText)
        {
        inkColor |= 0xff000000;
        paperColor |= 0xff000000;
        if (isInstanceStyle())
            {
            this.inkColor = inkColor;
            this.paperColor = paperColor;
            this.boldText = boldText;
            this.italicsText = italicsText;
            calcStyle();
            } else
            {
            inkColorCache[cacheIndex] = inkColor;
            paperColorCache[cacheIndex] = paperColor;
            boldTextCache[cacheIndex] = boldText;
            italicsTextCache[cacheIndex] = italicsText;
            calcAndPushStyle();
            }
        }

    public void convert2InstanceStyle()
        {
        if (!isInstanceStyle())
            {
            if (pullStyle())
                {
                this.inkColor = inkColorCache[cacheIndex];
                this.paperColor = paperColorCache[cacheIndex];
                this.boldText = boldTextCache[cacheIndex];
                this.italicsText = italicsTextCache[cacheIndex];
                calcStyle();
                } else
                {
                inkColor = DEFAULT_INK_COLOR;
                paperColor = DEFAULT_PAPER_COLOR;
                boldText = DEFAULT_BOLD_TEXT;
                italicsText = DEFAULT_ITALICS_TEXT;
                compoundStyle = 0L; // not defined style
                }
            cacheIndex = -1;
            }
        }

    /**
     * Generates compoundStyle from parameters, and stores it in cache[cacheIndex] and in database, too
     */
    private void calcStyle()
        {
        compoundStyle = calcCompoundStyle(
                inkColor,
                paperColor,
                boldText,
                italicsText);
        }

    /**
     * Always returns compund style, and NOT index!!
     *
     * @return
     */
    public long getCompoundStyle()
        {
        if (isInstanceStyle())
            {
            return compoundStyle;
            }
        pullStyle();
        return compoundStyleCache[cacheIndex];
        }

    public int getInkColor()
        {
        if (isInstanceStyle())
            return inkColor;

        pullStyle();
        return inkColorCache[cacheIndex];
        }

    public int getPaperColor()
        {
        if (isInstanceStyle())
            return paperColor;

        pullStyle();
        return paperColorCache[cacheIndex];
        }

    public boolean isBoldText()
        {
        if (isInstanceStyle())
            return boldText;

        pullStyle();
        return boldTextCache[cacheIndex];
        }

    public boolean isItalicsText()
        {
        if (isInstanceStyle())
            return italicsText;

        pullStyle();
        return italicsTextCache[cacheIndex];
        }


    private static int calcInkColor(long longstyle)
        {
        return (int) longstyle | 0xFF000000;           // AA is explicitly solid (0xFF)
        }

    private static int calcPaperColor(long longstyle)
        {
        return (int) (longstyle >> 24) | 0xFF000000; // AA is explicitly solid (0xFF)
        }

    private static boolean calcBoldText(long longstyle)
        {
        return (longstyle & BOLD_MASK) != 0L;
        }

    private static boolean calcItalicsText(long longstyle)
        {
        return (longstyle & ITALICS_MASK) != 0L;
        }

    private static long calcCompoundStyle(int inkColor, int paperColor, boolean boldText, boolean italicsText)
        {
        //        long longstyle = COMPOUND_MASK |
        //                inkColor | (paperColor << 24) | (boldText ? BOLD_MASK : 0) | (italicsText ? ITALICS_MASK : 0);

        long longstyle = (paperColor & 0x00FFFFFF);
        longstyle = (longstyle << 24);
        longstyle |= COMPOUND_MASK;
        longstyle |= (inkColor & 0x00FFFFFF);
        longstyle |= (boldText ? BOLD_MASK : 0);
        longstyle |= (italicsText ? ITALICS_MASK : 0);

        return longstyle;
        }


    static public void override( Long style, View... views )
        {
        if ( style != null && views.length > 0 )
            {
            // Views should contain context !
            Longstyle longstyle = new Longstyle(views[0].getContext(), style);

            for (View view : views)
                {
                longstyle.overrideViewStyle( view );
                }
            }
        }

    public void overrideViewStyle( View view )
        {
        Drawable background = view.getBackground();

        if ( background instanceof GradientDrawable ) // returns false on null, too
            {
            // shape drawable is gradientbackground!!
            ((GradientDrawable)background).setColor( getPaperColor() );
            ((GradientDrawable)background).setStroke(2, getInkColor() );
            }

        if ( view instanceof TextView )
            {
            // !!! TODO Paint should be changed by setTypeface and NOT DIRECTLY !!!
            Paint paint = ((TextView) view).getPaint();

            if ( isBoldText() )
                {
                paint.setFlags(paint.getFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                }
            else
                {
                paint.setFlags( paint.getFlags() & (~Paint.FAKE_BOLD_TEXT_FLAG));
                }

            paint.setTextSkewX( isItalicsText() ? -0.25f : 0);
            ((TextView)view).setTextColor( getInkColor() );
            }
        }

/*
    static boolean setDefault(Context context, long databaseIndex,
                              int inkColor, int paperColor, boolean boldText, boolean italicsText)
        {
        Longstyle longstyle = new Longstyle( context, databaseIndex );
        if ( longstyle.isInstanceStyle() )
            return false;
        if ( longstyle.getCompoundStyle() <= 0L )
            {
            longstyle.set(inkColor, paperColor, boldText, italicsText);
            }
        return true;
        }
*/


    public static class defaultStyleCreator
        {
        private Context context;
        private boolean overwrite;

        private defaultStyleCreator(Context context, boolean overwrite)
            {
            this.context = context.getApplicationContext();
            this.overwrite = overwrite;
            }

        public defaultStyleCreator style(long databaseIndex,
                                         int inkColor, int paperColor, boolean boldText, boolean italicsText,
                                         String memoTitle )
            {
            // memo titles are always overwritten - because they are NOT stored inside database!
            if (databaseIndex >= 1L && databaseIndex <= MAX_MEMO)
                {
                memoTitles[(int) databaseIndex - 1] = memoTitle;
                }

            return style(databaseIndex, inkColor, paperColor, italicsText, boldText);
            }

        public defaultStyleCreator style(long databaseIndex,
                                         int inkColor, int paperColor, boolean boldText, boolean italicsText)
            {
            Longstyle longstyle = new Longstyle(context, databaseIndex);
            if (!longstyle.isInstanceStyle() && (longstyle.getCompoundStyle() <= 0L || overwrite))
                {
                longstyle.set(inkColor, paperColor, boldText, italicsText);
                }
            return this;
            }
        }


    public static String getMemoTitle(long databaseIndex)
        {
        if (databaseIndex < 1L || databaseIndex > MAX_MEMO)
            return "N/A";

        String title = memoTitles[(int) databaseIndex - 1];
        if (title == null)
            {
            title = "#" + databaseIndex;
            }
        return title;
        }


    public static defaultStyleCreator setDefaults(Context context, boolean overwrite )
        {
        return new defaultStyleCreator( context, overwrite);
        }

    /**
     * This could go anywhere inside app !!!
     */
    public static void initDefaults(Context context)
        {
        pullAllStyles( context );
        setDefaults( context, false )
                .style(MEMO_BASIC, 0x000000, 0xefefef, false, false )
                .style(MEMO_TOGGLE_OFF, 0xc0c0c0, 0xeaeaea, false, false,
                        "Toggle button OFF" )
                .style(MEMO_TOGGLE_ON, 0xff0000, 0x3fff3f, false, false,
                        "Toggle button ON" )
                .style(MEMO_TOGGLE_ITALICS, 0xff0000, 0x3fff3f, false, true,
                        "Toggle button ITALICS" )
                .style(MEMO_TOGGLE_BOLD, 0xff0000, 0x3fff3f, true, false,
                        "Toggle button BOLD" )
                .style(MEMO_SWITCH_1, 0x000055, 0x55aaff, true, true,
                        "Switch button 1" )
                .style(MEMO_SWITCH_2, 0xff0000, 0xffffaa, true, true,
                        "Switch button 2" )
                .style(SELECTOR_BLUE, 0x16c1ff, 0xd6f8ff, false, true,
                "Selector" );
        }

    }
