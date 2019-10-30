package digitalgarden.mecsek.utils;

import java.util.Calendar;


/**
 * Timestamp
 * Timestamp is stored as
 * - long (used by sqlite)
 * - different "time"-parts as YEAR MONTH DAY (DAY_NAME) HOUR MIN SEC and MILL
 * Set... methods always set both data, they are always synchronized.
 *
 * IMPORTANT!! HOW TO SET TIME?
 * Set - as long - no error check is performed!
 * ALWAYS SET! auto - always false
 * ALWAYS CALL! convertLong2Parts() - (no error check) - error is always set to false
 *
 * Set (or modify) parts
 * ALWAYS SET! auto - set if any value is set automatically (ex. from calendar)
 * ALWAYS CALL! convertParts2Long() - calls checkParts() - error is set,
 * if time value is invalid - (clearAbove() is called on and above invalid part)
 *
 * Set methods (mostly) return true if error OR auto is set.
 * isAuto() and isError() can help to decide
 * ?? returning first part where auto or error was set ??
 */

public class Longtime
    {
    /** Timestamp as long - currently 0L (is it ok??) means - no time is set */
    private long time = 0L;

    /** Timestamp divided into parts as YEAR MONTH DAY (DAY_NAME) HOUR MIN SEC and MILL */
    private int[] part = new int[TIME_PARTS];

    /** START value means, that this (and more precise) parts are NOT given */
    private static final int[] START =
            {1600, 0, 0, -1, -1, -1, -1, -1};

    /** RANGE (including START and EXTRA values) - available range for this part */
    private static final int[] RANGE =
            {1400, 14, 33, 8, 25, 61, 61, 1001};
    // Example: MONTH - range 14: 0 - month is not given 1-12 valid months 13 "next" month

    /** EXTRA (included in RANGE!!) data for this part, such as "next" */
    private static final int[] EXTRA =
            {0, 1, 1, 0, 0, 0, 0, 0};

    /** Where to split century if only two digits are given as year */
    private static int TWO_DIGIT_YEAR_START = Calendar.getInstance().get(Calendar.YEAR) - 80;

    public static final int YEAR = 0;      // 1601 - 2999
    public static final int MONTH = 1;     // 1 - 12 13 - next month
    public static final int DAY = 2;       // 1 - 31 32 - next day

    public static final int DAY_NAME = 3;  // 0 - 6

    public static final int HOUR = 4;      // 0 - 23
    public static final int MIN = 5;       // 0 - 59
    public static final int SEC = 6;       // 0 - 59
    public static final int MILL = 7;      // 0 - 999

    private static final int TIME_PARTS = 8;

    private static String[] dayString =
            {"Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat", "Vasárnap"};

    private static String[] monthString =
            {"január", "február", "március", "április", "május", "június",
                    "július", "augusztus", "szeptember", "október", "november", "december", "NEXT MONTH"};

    /** checkParts() sets it true if any of the time-values is invalid */
    private boolean error = false;

    /** Parsing sets it true if any of the time-values is set automatically */
    private boolean auto = false;

    /** Constructor without setting any value */
    public Longtime()
        {
        // all parts are 0 - '0' YEAR means - no YEAR is set
        }

    /** Constructor set longtime by long value */
    public Longtime(long time)
        {
        set(time);
        }

    /** Returns duplicate of this longtime */
    public Longtime duplicate()
        {
        return new Longtime( time );
        }

    /** Returns longtime as long */
    public long get()
        {
        return time;
        }

    /**
     * Returns the given part
     * @param part constants as YEAR MONTH DAY (DAY_NAME) HOUR MIN SEC and MILL
     * @return value of the asked part
     */
    public int get( int part )
        {
        return this.part[ part ];
        }

    /** TRUE if error was set during the last set method */
    public boolean isError()
        {
        return error;
        }

    /** TRUE if any value was set automatically during the last set method */
    public boolean isAuto()
        {
        return auto;
        }

    /** Sets longtime as long - Value is NOT CHECKED !! */
    public void set(long time)
        {
        // long is not checked !!

        this.time = time;
        auto = false;
        convertLong2Parts();
        }

    /** Sets parts as ints. Missing values set to zero */
    public boolean set(int... parts)
        {
        int size;

        if (parts.length < part.length)
            size = parts.length;
        else
            size = part.length;

        int n;
        for (n = 0; n < size; n++)
            {
            part[n] = parts[n];
            }
        clearAbove(n);

        auto = false;
        convertParts2Long();
        return error;
        }

    /** Set current timstamp */
    public void set() // Timestamp
        {
        Calendar now = Calendar.getInstance();

        part[YEAR] = now.get(Calendar.YEAR);
        part[MONTH] = now.get(Calendar.MONTH) + 1;
        part[DAY] = now.get(Calendar.DAY_OF_MONTH);

        part[DAY_NAME] = -1;
        // Calendar Vasárnappal kezdődik, nem hétfővel,
        // egyszerűbb az ellenőrzést elvégezni

        part[HOUR] = now.get(Calendar.HOUR_OF_DAY);
        part[MIN] = now.get(Calendar.MINUTE);
        part[SEC] = now.get(Calendar.SECOND);
        part[MILL] = now.get(Calendar.MILLISECOND);

        auto = false;
        convertParts2Long();
        }

    /** Parses date (only YEAR, MONTH and DAY as integers) from String */
    public boolean setDate(String string, int twoDigitYearStart)
        {
        int[] ints = Utils.splitInts(string);
        Calendar now = null;
        auto = false;

        if (ints.length >= 3)
            {
            if (ints[0] < 100)
                {
                auto = true;
                ints[0] += twoDigitYearStart - twoDigitYearStart % 100;
                if (ints[0] <= twoDigitYearStart)
                    {
                    ints[0] += 100;
                    }
                }

            part[YEAR] = ints[0];
            part[MONTH] = ints[1];
            part[DAY] = ints[2];
            }
        else if (ints.length != 0) // YEAR, MONTH, DAY mind 0
            {
            auto = true;
            now = Calendar.getInstance();
            part[YEAR] = now.get(Calendar.YEAR);

            if (ints.length == 2)
                {
                part[MONTH] = ints[0];
                part[DAY] = ints[1];
                }
            else
                {
                part[MONTH] = now.get(Calendar.MONTH);

                if (ints.length == 1)
                    {
                    part[DAY] = ints[0];
                    }
                // else // ints.length = 0
                //    {
                //    part[DAY] = now.get(Calendar.DAY_OF_MONTH);
                //    }
                }
            }

        clearAbove(DAY_NAME);

        convertParts2Long();
        return error || auto;
        }

    /** Parses date (only YEAR, MONTH and DAY as integers) from String */
    public boolean setDate(String string)
        {
        return setDate(string, TWO_DIGIT_YEAR_START);
        }

    /** Changes ONLY DAY part */
    public boolean setDayOfMonth(int dayOfMonth)
        {
        // NOT NEEDED, check will do the job
        // if (part[MONTH] > 0 && dayOfMonth > 0 && dayOfMonth < lengthOfMonth() )

        part[DAY] = dayOfMonth;

        auto = false;
        convertParts2Long();
        return error;
        }

    /** Clears all value on and above index-part */
    private void clearAbove(int index)
        {
        for (int n = index; n < part.length; n++)
            {
            part[n] = START[n];
            }
        auto = false;
        convertParts2Long();
        }


    /** Clears TIME-PARTS - HOUR MIN SEC and MILL */
    public void clearDate()
        {
        clearAbove( HOUR );
        }


    /** Checks parts, and then converts them to long value */
    private void convertParts2Long()
        {
        checkParts();
        time = 0L;
        for (int n = 0; n < TIME_PARTS; n++)
            {
            time *= RANGE[n];
            time += (part[n] - START[n]);
            // Scribe.debug("Time: " + time);
            }
        }

    /** Converts long value to different date- and time-parts - NO CHECK IS PERFORMED !! */
    private void convertLong2Parts()
        {
        error = false; // megegyezés alapján nincs check
        long time = this.time;

        for (int n = TIME_PARTS - 1; n >= 0; n--)
            {
            part[n] = (int) (time % RANGE[n]) + START[n];
            time /= RANGE[n];
            // Scribe.debug("Time: " + time );
            }
        }

    /**
     * Ellenőrzi, hogy az idő részeinek érvényességét.
     * A hónap ill. napvégét jelentő értékeket nem fogadja el, csak a valós idő-értékeket.
     * A nap nevét beállítja.
     * Ha hibát talál, akkor a hibás értékre csökkenti a pontosságot, és az 'error' flag-et
     * beállítja.
     * convertParts2Long() mindig meghívja!
     * convertLong2Parts() esetén - megegyezés szerint - nem kell meghívni.
     *
     * @return true, ha nem talált hibát, false, ha az időérték hibás volt, és javítani kellett
     */
    private boolean checkParts()
        {
        error = false;
        boolean zero = false;

        for (int n = 0; n < TIME_PARTS; n++)
            {
            if (zero)
                {
                if (part[n] > START[n])
                    {
                    error = true;
                    }
                part[n] = START[n];
                }
            else // non-zero
                {
                if (n == DAY_NAME) // mire ide értünk, year, month és day már ellenőrzött
                    {
                    part[n] = dayName();
                    }
                else if (part[n] <= START[n])  // most válik zero-vá, ez nem hiba
                    {
                    zero = true;
                    part[n] = START[n];
                    }
                else if ((n == DAY && part[n] > lengthOfMonth())
                        || part[n] > RANGE[n] - EXTRA[n] + START[n] - 1) // túl nagy
                    {
                    error = true;
                    zero = true;
                    part[n] = START[n];
                    }
                }
            }

        return error;
        }

    /** TRUE if leap-year
     *  (4-gyel osztható, kivéve, ha 100-zal osztható, de mégis, ha 400-zal osztható)
     *
     *  non-leap-year
     *  (nem osztható néggyel,
     *   a néggyel oszthatók közül a százzal oszthatók, de a 400-zal osztható nem)
     **/
    public boolean isLeapYear()
        {
        return !(part[YEAR] % 4 != 0 || (part[YEAR] % 100 == 0 && part[YEAR] % 400 != 0));

        /*
        if (part[YEAR] % 4 != 0) // 4-gyel NEM osztható -> nem leap
            return false;

        if (part[YEAR] % 100 != 0) // 4-gyel osztható, de százzal NEM osztható -> leap
            return true;

        return part[YEAR] % 400 == 0; // (4-gyel), 100-zal osztható - ha 400-zal, akkor -> leap
        */
        }

    public boolean isLeapYear2()
        {
        return part[YEAR] % 400 == 0 || part[YEAR] % 100 != 0 && part[YEAR] % 4 == 0;
        }

    /** Returns length of this month */
    private int lengthOfMonth()
        {
        if (part[MONTH] == 2)
            return isLeapYear() ? 29 : 28;
        return part[MONTH] <= 7 ? 30 + part[MONTH] % 2 : 31 - part[MONTH] % 2;
        }

    /* public int daysSinceEpoch()
        {
        // Epoch 1601.01.01 - Csak 400-zal osztható év utáni lehet !!
        // És ez pont hétfő! 2001 is.
        // Epoch előtt nem tud számolni!!!

        int year = part[YEAR];
        int month = part[MONTH];
        int day = part[DAY];

        if (month > 2)
            day -= isLeapYear() ? 1 : 2;

        day += month * 30; // A mostani hónapot vegyük majd ki!

        if (month >= 8) month++;

        //  1  2  3  4  5  6  7  8  9 10 11 12 month
        //  0  1  1  2  2  3  3  4  5  5  6  6

        //  1  2  3  4  5  6  7  x  9 10 11 12 13month
        //  0  1  1  2  2  3  3  4  4  5  5  6  6

        day += month / 2;

        year -= 1601;

        day += year * 365 + year / 4 - year / 100 + year / 400;

        return day - 31;
        } */

    // http://howardhinnant.github.io/date_algorithms.html
    // Index 0 starts at 1973.01.01
    public int getDayIndex()
        {
        int y = part[YEAR];
        int m = part[MONTH];
        int d = part[DAY];

        if (m <= 2) y--;
        int era = (y >= 0 ? y : y-399) / 400;
        int yoe = y - era * 400;                             // [0, 399]
        int doy = (153*(m + (m > 2 ? -3 : 9)) + 2)/5 + d-1;  // [0, 365]
        int doe = yoe * 365 + yoe/4 - yoe/100 + doy;         // [0, 146096]
        return era * 146097 + doe - 719468 - 1096; // // 1096 = 1973.01.01. 3652 == 1980.01.01
        }

    // http://howardhinnant.github.io/date_algorithms.html
    // Index 0 starts at 1973.01.01
    public boolean setDayIndex(int dayIndex)
        {
        dayIndex += 719468 + 1096; // 1096 = 1973.01.01. 3652 == 1980.01.01
        int era = (dayIndex >= 0 ? dayIndex : dayIndex - 146096) / 146097;
        int doe = dayIndex - era * 146097;                          // [0, 146096]
        int yoe = (doe - doe/1460 + doe/36524 - doe/146096) / 365;  // [0, 399]
        int y = (yoe) + era * 400;
        int doy = doe - (365*yoe + yoe/4 - yoe/100);                // [0, 365]
        int mp = (5*doy + 2)/153;                                   // [0, 11]
        int d = doy - (153*mp+2)/5 + 1;                             // [1, 31]
        int m = mp + (mp < 10 ? 3 : -9);                            // [1, 12]

        part[YEAR] = m <= 2 ? y+1 : y;
        part[MONTH] = m;
        part[DAY] = d;

        clearAbove( HOUR );
        convertParts2Long();

        return error;
        }

    // Index 0 starts at 1973.01.01
    // !! Negative monthIndex is not working yet !!
    public int getMonthIndex()
        {
        // Epoch 1601.01.01 - Csak 400-zal osztható év utáni lehet !!
        // És ez pont hétfő! 2001 is.
        // Epoch előtt nem tud számolni!!!

        int year = part[YEAR];
        int month = part[MONTH];

        year -= 1973; // 1601;
        month --;

        return year * 12 + month; // - 4548 == 1980.01.01
        }

    // Index 0 starts at 1973.01.01
    // !! Negative monthIndex is not working yet !!
    public boolean setMonthIndex(int monthIndex)
        {
        part[YEAR] = monthIndex / 12 + 1973; //1601;
        part[MONTH] = monthIndex % 12 + 1;

        clearAbove( DAY );
        convertParts2Long();

        return error;
        }

    // Index 0 starts at 1973.01.01 which is monday
    private int dayName()
        {
        return getDayIndex() % 7;
        }

    public int getDayName()
        {
        return part[DAY_NAME];
        }

    public boolean addDays(int days)
        {
        part[DAY]+=days;

        int lm;
        while ( part[DAY] > (lm = lengthOfMonth() ))
            {
            part[DAY]-=lm;
            part[MONTH]++;
            if ( part[MONTH] > 12 )
                {
                part[MONTH] = 1;
                part[YEAR]++;
                }
            }

        while ( part[DAY] < 1 )
            {
            part[MONTH]--;
            if ( part[MONTH] < 1 )
                {
                part[MONTH] = 12;
                part[YEAR]--;
                // !! CHECK IF YEAR IS BELOW LOWER BOUND !!
                }
            part[DAY]+= lengthOfMonth();
            }

        auto = false;
        convertParts2Long();
        return error;
        }

    @Override
    public String toString()
        {
        return toString( true );
        }

    public String toString( boolean isTextEnabled )
        {
        StringBuilder builder = new StringBuilder( 24 );

        if ( part[YEAR] > START[YEAR] )
            {
            builder.append(part[YEAR]).append('.');

            if (part[MONTH] > START[MONTH])
                {
                builder.append(Integer.toString(part[MONTH] + 100).substring(1)).append('.');

                if ( isTextEnabled )
                    {
                    builder.append(" (").append( monthString[part[MONTH]-1]).append(") ");
                    }

                if (part[DAY] > START[DAY])
                    {
                    builder.append(Integer.toString(part[DAY] + 100).substring(1)).append('.');

                    if (isTextEnabled && part[DAY_NAME] > START[DAY_NAME])
                        {
                        builder.append(' ').append(dayString[part[DAY_NAME]]);
                        }

                    if (part[HOUR] > START[HOUR])
                        {
                        builder.append(' ').append(Integer.toString(part[HOUR] + 100).substring(1));

                        if (part[MIN] > START[MIN])
                            {
                            builder.append(':').append(Integer.toString(part[MIN] + 100).substring(1));

                            if (part[SEC] > START[SEC])
                                {
                                builder.append(':').append(Integer.toString(part[SEC] + 100).substring(1));

                                if (part[MILL] > START[MILL])
                                    {
                                    builder.append('.').append(Integer.toString(part[MILL] + 1000).substring(1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return builder.toString();
        }

    public String toStringYearMonth( boolean isTextEnabled )
        {
        StringBuilder builder = new StringBuilder( 24 );

        if ( part[YEAR] > START[YEAR] )
            {
            builder.append(part[YEAR]).append('.');

            if (part[MONTH] > START[MONTH])
                {
                builder.append(Integer.toString(part[MONTH] + 100).substring(1)).append('.');

                if ( isTextEnabled )
                    {
                    builder.append(" (").append( monthString[part[MONTH]-1]).append(") ");
                    }
                }
            }
        return builder.toString();
        }

    public String toStringDayOfMonth()
        {
        if ( part[DAY] > 0 && part[DAY] < 32 )
            {
            return Integer.toString(part[DAY]);
            }
        return "";
        }
    }
