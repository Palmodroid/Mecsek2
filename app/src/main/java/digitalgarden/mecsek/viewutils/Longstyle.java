package digitalgarden.mecsek.viewutils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

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

    public static final long COMPOUND_MASK =    0x4000000000000000L;

    private static final long BOLD_MASK =       0x0200000000000000L;
    private static final long ITALICS_MASK =    0x0100000000000000L;

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

    // (be careful! database indices start at 1-, while compoundStyleCache indices start at 0-)
    private static final int MAX_INDEX = 64;

    private static long[] compoundStyleCache = new long[MAX_INDEX];
    // Value: 0 (default) not yet pulled
    // Value: -1 already pulled, but not exist in database
    // Value: >0 already pulled and pulled value is cached

    private static int[] inkColorCache = new int[MAX_INDEX]; // Contains also AA bits!!
    private static int[] paperColorCache = new int[MAX_INDEX]; // Contains also AA bits!!
    private static boolean[] boldTextCache = new boolean[MAX_INDEX];
    private static boolean[] italicsTextCache = new boolean[MAX_INDEX];


    public Longstyle( Context context )
        {
        this( context, 0L );
        }

    public Longstyle( Context context, long longstyle )
        {
        this.context = context.getApplicationContext(); // Context is needed by database queries
        set( longstyle );
        }

    /**
     * Sets compoundStyle from long parameter
     * >= COMPOUND_MASK : compound data, derived from long
     * 1 - < MAX_INDEX : indexed (pullStyle pulls it)
     * all others : not valid, returns predefined values
     */
    public void set( long longstyle )
        {
        // Compound style
        if ( longstyle >= COMPOUND_MASK )
            {
            cacheIndex = -1;

            compoundStyle = longstyle;
            inkColor = calcInkColor( longstyle );
            paperColor = calcPaperColor( longstyle );
            boldText = calcBoldText( longstyle );
            italicsText = calcItalicsText( longstyle );
            }
        // Indexed style
        else
            {
            // Valid index - data comes from LongstyleIndexed (even if not defined)
            cacheIndex = checkIndex(longstyle);

            // Invalid indices - predefined values
            if ( isInstanceStyle() )
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
     * @param databaseIndex
     * @return cache index (if valid) or -1 if index is invalid
     */
    public int checkIndex(long databaseIndex)
        {
        return ( databaseIndex < 1L || databaseIndex > MAX_INDEX ) ? -1 : (int) databaseIndex-1;
        }


    /**
     * Pull indexed longstyle from database - INDEX IS NOT VERIFIED!!!
     * @return  true  - if index is valid, and indexed longstyle exists
     * or       false - indexed longstyle is not yet stored
     */
    private boolean pullStyle( )
        {
        // Value not yet pulled - try to pull it
        if ( compoundStyleCache[cacheIndex] == 0L )
            {
            String[] projection = {column(VALUE)}; // One column is enough, id_ is the database cacheIndex (1-256)

            Cursor cursor = context.getContentResolver().query(
                    table(COLOR_DEFS).itemContentUri( cacheIndex + 1 ),    // longstyle == database cacheIndex
                    projection, null, null, null);

            // Queries are never NULL!!!
            if (cursor.moveToFirst())
                {
                // value is pulled
                // store COMPOUND VALUE of this indexed longstyle
                compoundStyleCache[cacheIndex] = cursor.getLong(cursor.getColumnIndex(column(VALUE)));

                inkColorCache[cacheIndex] = calcInkColor( compoundStyleCache[cacheIndex] );
                paperColorCache[cacheIndex] = calcPaperColor( compoundStyleCache[cacheIndex] );
                boldTextCache[cacheIndex] = calcBoldText( compoundStyleCache[cacheIndex] );
                italicsTextCache[cacheIndex] = calcItalicsText( compoundStyleCache[cacheIndex] );
                }
            else
                {
                // This indexed longstyle does not exist,
                // Storing -1L means, that pull was already tried;
                compoundStyleCache[cacheIndex] = -1L;
                }
            cursor.close();
            }

        // Pull already tried - but without success. Previous pull filled up compoundStyleCache with values.
        return ( compoundStyleCache[cacheIndex] >= 0L );
        }


    /**
     * Generates longstyle from parameters, and stores it INDEX IS NOT VERIFIED!!!
     * in cache[cacheIndex]
     * and in database, too
     */
    private boolean calcAndPushStyle( )
        {
        compoundStyleCache[cacheIndex] = calcCompoundStyle(
                inkColorCache[cacheIndex],
                paperColorCache[cacheIndex],
                boldTextCache[cacheIndex],
                italicsTextCache[cacheIndex]);

        ContentValues values = new ContentValues();
        values.put( column( VALUE ), compoundStyleCache[cacheIndex] );

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // Ez még nem működik tökéletesen
        // most az insert és update is REPLACE-ként működik.
        // De inkább UPSERT kéne, vagyis INSERT, ha nincs és UPDATE, ha van
        // (Merthogy a relációkat is törli, ha REPLACE)
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        // context.getContentResolver().insert(
        //        table( COLOR_DEFS ).itemContentUri(cacheIndex + 1), values );

        values.put( column_id(), (long)(cacheIndex + 1) );

        if ( context.getContentResolver().update(
                table( COLOR_DEFS ).contentUri(), values, null, null) != 1 )
            {
            return false;
            }

        // if successful (one row was updated) store longstyle in the puffer, too
        return true;
        }


    public void setPaperColor( int paperColor )
        {
        if ( isInstanceStyle() )
            {
            this.paperColor = paperColor;
            calcStyle();
            }
        else
            {
            paperColorCache[cacheIndex] = paperColor;
            calcAndPushStyle( );
            }
        }

    public void setInkColor( int inkColor )
        {
        if ( isInstanceStyle() )
            {
            this.inkColor = inkColor;
            calcStyle();
            }
        else
            {
            inkColorCache[cacheIndex] = inkColor;
            calcAndPushStyle( );
            }
        }

    public void setBoldText( boolean boldText )
        {
        if ( isInstanceStyle() )
            {
            this.boldText = boldText;
            calcStyle();
            }
        else
            {
            boldTextCache[cacheIndex] = boldText;
            calcAndPushStyle( );
            }
        }

    public void setItalicsText( boolean italicsText )
        {
        if ( isInstanceStyle() )
            {
            this.italicsText = italicsText;
            calcStyle();
            }
        else
            {
            italicsTextCache[cacheIndex] = italicsText;
            calcAndPushStyle( );
            }
        }


    /**
     * Set all parameters, clearing cacheIndex (seting it to 0)
     * @param inkColor
     * @param paperColor
     * @param boldText
     * @param italicsText
     */
    public void setInstanceStyle( int inkColor, int paperColor, boolean boldText, boolean italicsText )
        {
        cacheIndex = -1;
        set( inkColor, paperColor, boldText, italicsText );
        }


    /**
     * Sets style parameters - retaining cacheIndex
     * If it is an indexed style, than corresponding database compoundStyle will be updated
     * @param inkColor
     * @param paperColor
     * @param boldText
     * @param italicsText
     */
    public void set( int inkColor, int paperColor, boolean boldText, boolean italicsText )
        {
        if ( isInstanceStyle() )
            {
            this.inkColor = inkColor;
            this.paperColor = paperColor;
            this.boldText = boldText;
            this.italicsText = italicsText;
            calcStyle();
            }
        else
            {
            inkColorCache[cacheIndex] = inkColor;
            paperColorCache[cacheIndex] = paperColor;
            boldTextCache[cacheIndex] = boldText;
            italicsTextCache[cacheIndex] = italicsText;
            calcAndPushStyle( );
            }
        }

    public void clearIndex()
        {
        if ( !isInstanceStyle() )
            {
            this.inkColor = inkColorCache[cacheIndex];
            this.paperColor = paperColorCache[cacheIndex];
            this.boldText = boldTextCache[cacheIndex];
            this.italicsText = italicsTextCache[cacheIndex];
            cacheIndex = -1;
            calcStyle();
            }
        }

    /**
     * Generates compoundStyle from parameters, and stores it
     * in cache[cacheIndex]
     * and in database, too
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
     * @return
     */
    public long getCompoundStyle()
        {
        if ( isInstanceStyle() )
            {
            return compoundStyle;
            }

        return pullStyle() ? compoundStyleCache[cacheIndex] : 0L;
        }

    public int getInkColor()
        {
        if ( isInstanceStyle() )
            return inkColor;

        return pullStyle() ? inkColorCache[cacheIndex] : DEFAULT_INK_COLOR;
        }

    public int getPaperColor()
        {
        if ( isInstanceStyle() )
            return paperColor;

        return pullStyle() ? paperColorCache[cacheIndex] : DEFAULT_PAPER_COLOR;
        }

    public boolean isBoldText()
        {
        if ( isInstanceStyle() )
            return boldText;

        return pullStyle() ? boldTextCache[cacheIndex] : DEFAULT_BOLD_TEXT;
        }

    public boolean isItalicsText()
        {
        if ( isInstanceStyle() )
            return italicsText;

        return pullStyle() ? italicsTextCache[cacheIndex] : DEFAULT_ITALICS_TEXT;
        }


    private static int calcInkColor( long longstyle )
        {
        return (int) longstyle | 0xFF000000;           // AA is explicitly solid (0xFF)
        }

    private static int calcPaperColor( long longstyle )
        {
        return (int) (longstyle >> 24) | 0xFF000000; // AA is explicitly solid (0xFF)
        }

    private static boolean calcBoldText( long longstyle )
        {
        return (longstyle & BOLD_MASK) != 0L;
        }

    private static boolean calcItalicsText( long longstyle )
        {
        return (longstyle & ITALICS_MASK) != 0L;
        }

    private static long calcCompoundStyle(int inkColor, int paperColor, boolean boldText, boolean italicsText )
        {
        //        long longstyle = COMPOUND_MASK |
        //                inkColor | (paperColor << 24) | (boldText ? BOLD_MASK : 0) | (italicsText ? ITALICS_MASK : 0);

        long longstyle =  (paperColor & 0x00FFFFFF);
        longstyle = (longstyle << 24);
        longstyle |= COMPOUND_MASK;
        longstyle |= (inkColor & 0x00FFFFFF);
        longstyle |= (boldText ? BOLD_MASK : 0);
        longstyle |= (italicsText ? ITALICS_MASK : 0);

        return longstyle;
        }

    }
