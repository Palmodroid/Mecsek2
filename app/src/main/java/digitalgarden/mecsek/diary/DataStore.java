package digitalgarden.mecsek.diary;



import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import digitalgarden.mecsek.utils.Longtime;

/**
 * DataStore organizes data for diary.
 * DataMonthlyViewer contains(6x7 = 42 days), and it loads calendar data for its each day.
 * DailyData contains data for only one day, and this data originates from its DataMonthlyViewer.
 * DataMonthlyViewers overlap each other, but - because views start from monthly view - it is
 * easier to load the whole view, not only one month.
 * Touching one day on monthly view gets data from this MonthlyData (and not from its month).
 * Scrolling to an other day gets data from this month's MonthlyData
 */
public class DataStore
    {
    Map<Integer, MonthlyData> DataMonthlyViewList = new HashMap<>();

    Longtime today;

    AppCompatActivity appCompatActivity;

    public DataStore( AppCompatActivity appCompatActivity )
        {
        this.appCompatActivity = appCompatActivity;

        today = new Longtime();
        today.set();
        today.clearDate();
        }


    /**
     * Gets data for monthly view
     * @param indexMonth
     * @return
     */
    public MonthlyData getMonthlyData(int indexMonth )
        {
        MonthlyData monthlyData = DataMonthlyViewList.get( indexMonth );

        if ( monthlyData == null )
            {
            monthlyData = new MonthlyData( this, indexMonth );

            DataMonthlyViewList.put( indexMonth, monthlyData);
            }

        return monthlyData;
        }

    /**
     * Gets data for one day
     * @param indexDay
     * @return
     */
    public DailyData getDailyData(int indexDay )
        {
        Longtime longtime = new Longtime();

        longtime.setDayIndex( indexDay );

        MonthlyData monthlyData = getMonthlyData( longtime.getMonthIndex() );

        int day = longtime.get( Longtime.DAY )-1; // !!!!!!!!!!!!!!!!!! -1 kell, vagy itt kell??

        DailyData dailyData = monthlyData.getDailyDataWithOffset( day );

        return dailyData;
        }

    public AppCompatActivity getAppCompatActivity()
        {
        return appCompatActivity;
        }

    public Longtime getToday()
        {
        return today;
        }
    }
