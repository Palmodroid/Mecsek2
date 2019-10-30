package digitalgarden.mecsek.diary;

import java.util.ArrayList;
import java.util.List;

import digitalgarden.mecsek.utils.Longtime;

/**
 * DailyData stores all data for one day.
 * The list of the entries is filled during load, but when loaded, will be ready for showing
 */
public class DailyData
    {
    // Mi van a LOADER reset stb. beállításaal?????????????????????


    // List for loading data
    private List<DataEntry> dataEntryListToLoad = new ArrayList<>();

    // List for data to show (loaded data is be moved here after loading finished)
    private List<DataEntry> dataEntryListToUse = null;

public long longtime;

public String longtimeString;

    // Day of the month as string (to show as header)
public String dayOfMonth;

public int dayIndex;

    // Basic background color of the day - can be altered by loaded entries
    private int dayColor;


public MonthlyData monthlyData;

    /**
     * These parameters are needed first to set up a day.
     * Second, loader fill up data for the whole monthly view, and fills data for the days
     * @param longtime time of this day (no timeinfo)
     * @param month month of this monthly view
     * @param today datestamp of today, comes from OLDDiaryActivity without timeinfo
     */
    public DailyData(MonthlyData monthlyData, Longtime longtime, int month, long today)
        {
        this.monthlyData = monthlyData;

        this.longtime = longtime.get();

        this.longtimeString = longtime.toString( true );

        dayOfMonth = longtime.toStringDayOfMonth();
        dayIndex = longtime.getDayIndex();

        if ( today == longtime.get() )
            dayColor = 0xFFCD3925;
        else
            dayColor = getColorForDayName( longtime.getDayName() );

        if ( month != longtime.get(Longtime.MONTH) )
            dayColor &= 0x40FFFFFF;
        }

    /**
     * Returns background color for day-name
     * Static, to be called by MonthlyHeaderLayout
     */
    static int getColorForDayName( int dayName )
        {
        if (dayName < 5) // Hétköznap
            return 0xFFE3D26F;
        if (dayName < 6) // Szombat
            return 0xFFCDA642;
        // Vasárnap
        return 0xFFAD6519;
        }

    public void addEntryData(long id, Longtime date, String note )
        {
        dataEntryListToLoad.add( new DataEntry(id, note, date));
        }

    private DailyListFragment dailyFragment = null;

    public void setDailyFragment( DailyListFragment dailyFragment )
        {
        this.dailyFragment = dailyFragment;
        }

    public void createLoader()
        {
        monthlyData.createLoader();
        }

    public void onLoadFinished()
        {
        dataEntryListToUse = dataEntryListToLoad;
        dataEntryListToLoad = new ArrayList<>();

        if ( dailyFragment != null )
            dailyFragment.onLoadFinished( dataEntryListToUse );
        }

    public List<DataEntry> getEntryDataList()
        {
        return dataEntryListToUse;
        }

    public String getDayOfMonth()
        {
        return dayOfMonth;
        }

    public int getDayColor()
        {
        return dayColor;
        }

    }
