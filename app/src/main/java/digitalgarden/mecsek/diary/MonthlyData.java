package digitalgarden.mecsek.diary;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import digitalgarden.mecsek.database.calendar.CalendarTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.Longtime;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.CALENDAR;


/**
 * MonthlyData loads and stores all data for each day (As DailyData-s)
 */
public class MonthlyData implements LoaderManager.LoaderCallbacks<Cursor>
    {
    private DataStore dataStore;

    private MonthlyFragment monthlyFragment;

    // Data for each daily view
    // There are 42 days in one monthly view (some of them from the previous and next month
    private DailyData[] dailyDataArray = new DailyData[42];

    // monthIndex == index of the ViewPager
public int monthIndex;

    // "Name" of this month as string (Year, Month and Month as string)
    private String yearMonthString;

    // First day of this view - Longtime without timeinfo
    private Longtime longtimeFirst;

    // Days since epoch - used for indexing complexDayilyDataArray
    private int dayIndexOfFirstDay;

    // First day of next view - Longtime without timeinfo
    private Longtime longtimeLast;

    // First day of the month is on this index in array
    private int offset;


    /**
     * Constructor sets parameters for this month
     *
     * @param monthIndex ViewPager index, which is months since epoch
     * @return year and month as string
     */
    public MonthlyData(DataStore dataStore, int monthIndex )
        {
        this.dataStore = dataStore;
        this.monthIndex = monthIndex;

        // START DAY of this monthly view - FIRST DAY of the month is set first
        longtimeFirst = new Longtime();
        longtimeFirst.setMonthIndex(monthIndex);
        longtimeFirst.setDayOfMonth(1);
        // YEAR MONTH and DAY (DAY_NAME) is set, but TIME is not

        // Data from FIRST DAY of this month
        yearMonthString = longtimeFirst.toStringYearMonth(true);
        int month = longtimeFirst.get(Longtime.MONTH);

        // First day of the month is on this offset inside array
        offset = longtimeFirst.getDayName();

        // Roll back to START DAY
        longtimeFirst.addDays(-offset);
        dayIndexOfFirstDay = longtimeFirst.getDayIndex();

        // ENDING DAY - FIRST DAY OF THE NEXT MONTH
        longtimeLast = longtimeFirst.duplicate();
        longtimeLast.addDays(42);

        Longtime longtime = longtimeFirst.duplicate();
        for (int n = 0; n < 42; n++)
            {
            dailyDataArray[n] = new DailyData(this, longtime, month, dataStore.getToday().get());
            // ((ComplexDailyView) monthlyViewerLayout.getChildAt(n)).setMonthlyData(dailyDataArray[n]);
            longtime.addDays(1); // 86350
            }
        }


    public void setMonthlyFragment(MonthlyFragment monthlyFragment)
        {
        this.monthlyFragment = monthlyFragment;
        }

    /**
     * "Name" of this month as string (Year, Month and Month as string)
     */
    public String getYearMonthString()
        {
        return yearMonthString;
        }

    public void createLoader()
        {
        Scribe.note("LOADER: CreateLoader - id: " + monthIndex);
        LoaderManager.getInstance( dataStore.getAppCompatActivity()).
                initLoader(monthIndex, null, this);
        }

    /* !! Ez elfordításkor is mindent újratölt !! */
    public void destroyLoader()
        {
        Scribe.note("LOADER: DestroyLoader - id: " + monthIndex);
        LoaderManager.getInstance(dataStore.getAppCompatActivity()).
                destroyLoader(monthIndex);
        }

    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle)
        {
        Scribe.note("LOADER: onCreateLoader (Query) started - id: " + monthIndex);

        if (id != monthIndex)
            {
            return null;
            }

        String[] projection = {
                column_id(),
                column(CalendarTable.DATE),
                column(CalendarTable.NOTE)};

        String selection =
                column(CalendarTable.DATE) + " >=  ? AND " + column(CalendarTable.DATE) + " < ?";
        String[] selectionArgs = {
                Long.toString(longtimeFirst.get()),
                Long.toString(longtimeLast.get())};

        // http://code.google.com/p/android/issues/detail?id=3153
        CursorLoader cursorLoader = new CursorLoader(
                dataStore.getAppCompatActivity(),
                table(CALENDAR).contentUri(),
                projection,
                selection,
                selectionArgs,
                //new String[] { "%"+filterString+"%" },
                // ha nincs filterClause, akkor nem használja fel
                null);

        return cursorLoader;
        }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor)
        {
        Scribe.note("LOADER: onLoadFinished - id: " + monthIndex);

        // !! VOLT EGY CURSOR.CLOSE() AZ EREDETI KÓDBAN. EZ SZTEM. MŰKÖDIK !!
        // https://stackoverflow.com/questions/49524029/support-library-27-1-0-onloaderfinished
        // -returns-a-closed-cursor-when-starting-a
        // if ( cursor.isClosed() )
        //    {
        //    LoaderManager.getInstance(monthlyViewerFragment.getActivity()).
        //            restartLoader(monthIndex, null, this);
        //    return;
        //    }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
            Longtime longtime = new Longtime
                    (cursor.getLong(cursor.getColumnIndexOrThrow(column(CalendarTable.DATE))));
            dailyDataArray[longtime.getDayIndex() - dayIndexOfFirstDay]
                    .addEntryData(
                            cursor.getLong( cursor.getColumnIndexOrThrow( column_id() ) ),
                            // longtime.getDayIndex(), // ?????????????????????????????????????????
                            longtime,
                            cursor.getString(cursor.getColumnIndexOrThrow(column(CalendarTable.NOTE))));
            }

        for (DailyData data : dailyDataArray)
            {
            data.onLoadFinished();
            }

        monthlyFragment.onLoadFinished();
        }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader)
        {
        }

    public DailyData getDailyData(int index )
        {
        return dailyDataArray[index];
        }

    public DailyData getDailyDataWithOffset(int index )
        {
        return dailyDataArray[index + offset];
        }
    }
