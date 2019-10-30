package digitalgarden.mecsek.diary;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import digitalgarden.mecsek.R;

import digitalgarden.mecsek.database.calendar.CalendarEditFragment;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.Longtime;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static digitalgarden.mecsek.database.calendar.CalendarEditFragment.DATE_HINT;

/**
 * DiaryActivity shows the ViewPager, which gets its data from the DiaryAdapter.
 * MonthlyFragment shows the days of one month, as ComplexDailyView-s of MonthlyViewerLayout.
 * MonthlyViewerData stores all data on a daily basis.
 *
 * DiaryActivity connects all diary components
 * 1. viewPagerMonthly - loads monthly fragments
 * 2. viewPagerDaily - loads daily fragments
 * 3. editFragment - shows one event
 *
 * viewPager is always present after restart, so savedInstanceState returns its state
 */
public class DiaryActivity extends AppCompatActivity
        implements ConnectionToActivity, GenericEditFragment.OnFinishedListener
    {
    private DataStore dataStore;

    private FragmentStatePagerAdapter monthlyAdapter;
    private FragmentStatePagerAdapter dailyAdapter;

    private ViewPager viewPagerMonthly;
    private ViewPager viewPagerDaily;

    private CoordinatorLayout dailyFrame;

    private FloatingActionButton dailyFab;

    private boolean ViewPagersConnected = true;

    // First option:
    // At start frameMonthly is Shown only
    // Clicking a day frameMonthly is shown (over frame monthly)
    // Editing an item frame edit is shown (over frame monthly and daily)
    // Frame edit will be restored automatically

    // Second option:
    // At start frameMonthly is Shown only
    // Long clicking a day a new item frame edit is shown (over frame monthly, daily is hidden)
    // Frame edit will be restored automatically

    // frameDailyIsShown helps to decide where to return: frame daily or frame monthly
    private boolean frameDailyIsShown = false;
    private static String FRAME_DAILY_IS_SHOWN = "fdis";


    private void showMonthly()
        {
        frameDailyIsShown = false;
        dailyFrame.setVisibility( GONE );
        viewPagerMonthly.setVisibility( VISIBLE );
        }

    private void showDaily()
        {
        frameDailyIsShown = true;
        dailyFrame.setVisibility( VISIBLE );
        viewPagerMonthly.setVisibility( GONE );
        }

    private void showEdit( long date, long id )
        {
        Fragment editFrag = getSupportFragmentManager().findFragmentById(R.id.edit_frame);
        if (editFrag == null)
            {
            editFrag = new CalendarEditFragment();

            Bundle args = new Bundle();
            args.putLong( DATE_HINT, date );
            args.putLong(GenericEditFragment.EDITED_ITEM, id);
            editFrag.setArguments( args );

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            // http://stackoverflow.com/questions/4817900/android-fragments-and-animation és
            // http://daniel-codes.blogspot.hu/2012/06/fragment-transactions-reference.html
            // fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left , android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            fragmentTransaction.add(R.id.edit_frame, editFrag, "EDIT");
            fragmentTransaction.addToBackStack("LIBDB");
            fragmentTransaction.commit();

            findViewById(R.id.edit_frame).setVisibility(VISIBLE);
            }

        }


    // ListFragment kiválasztotta editálásra az id elemet
    public void onItemEditing(long id)
        {
        showEdit( 0L, id );
        }


    @Override
    protected void onSaveInstanceState(Bundle outState)
        {
        super.onSaveInstanceState(outState);

        outState.putBoolean( FRAME_DAILY_IS_SHOWN, frameDailyIsShown );
        }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
        {
        super.onRestoreInstanceState(savedInstanceState);

        if  ( getSupportFragmentManager().findFragmentById(R.id.edit_frame) != null )
            {
            findViewById(R.id.edit_frame).setVisibility(VISIBLE);
            }

        frameDailyIsShown = savedInstanceState.getBoolean( FRAME_DAILY_IS_SHOWN );

        // NOT QUITE SURE IF "GONE" VIEWS ARE STORED OR NOT
        // ViewPager position is stored (Gone is stored, too?)
        }

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);

        dailyFrame = findViewById( R.id.frame_daily );

        dailyFab = findViewById( R.id.fab_daily );
        dailyFab.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {

                int dayIndex = viewPagerDaily.getCurrentItem();

                Longtime longtime = new Longtime();
                longtime.setDayIndex( dayIndex );

                showEdit( longtime.get() , -1L);
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                // .setAction("Action", null).show();
                }
            });

        // Create datastore for both adapters
        dataStore = new DataStore(this);

        monthlyAdapter = new MonthlyAdapter( getSupportFragmentManager() );
        dailyAdapter = new DailyAdapter( getSupportFragmentManager() );

        viewPagerMonthly = (ViewPager) findViewById(R.id.view_pager_monthly);
        viewPagerMonthly.setAdapter(monthlyAdapter);
        viewPagerMonthly.setCurrentItem( dataStore.getToday().getMonthIndex() );
        viewPagerMonthly.setOffscreenPageLimit(1);

        viewPagerDaily = (ViewPager) findViewById(R.id.view_pager_daily);
        viewPagerDaily.setAdapter(dailyAdapter);
        viewPagerDaily.setCurrentItem( dataStore.getToday().getDayIndex() );
        viewPagerDaily.setOffscreenPageLimit(1);

        Log.d("TODAY", "Today: " + dataStore.getToday().toString());

        // Attach the page change listener inside the activity
        viewPagerMonthly.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
            {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position)
                {
                Toast.makeText(DiaryActivity.this,
                        "Selected page position: " + position, Toast.LENGTH_SHORT).show();

                if (ViewPagersConnected)
                    {
                    ViewPagersConnected = false;

                    Longtime longtime = new Longtime();

                    longtime.setMonthIndex(position);
                    longtime.setDayOfMonth(15);

                    viewPagerDaily.setCurrentItem(longtime.getDayIndex());

                    ViewPagersConnected = true;
                    }
                }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
                {
                // Code goes here
                }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state)
                {
                // Code goes here
                }
            });

        // Attach the page change listener inside the activity
        viewPagerDaily.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
            {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position)
                {
                Toast.makeText(DiaryActivity.this,
                        "Selected day position: " + position, Toast.LENGTH_SHORT).show();

                if (ViewPagersConnected)
                    {
                    ViewPagersConnected = false;

                    Longtime longtime = new Longtime();

                    longtime.setDayIndex(position);

                    viewPagerMonthly.setCurrentItem(longtime.getMonthIndex());

                    ViewPagersConnected = true;
                    }
                }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
                {
                // Code goes here
                }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state)
                {
                // Code goes here
                }
            });

        }

    @Override
    protected void onResumeFragments()
        {
        Scribe.locus();
        super.onResumeFragments();

        if ( frameDailyIsShown )
            {
            showDaily();
            }
        else
            {
            showMonthly();
            }
        // Edit is automatically shown (restored), if active
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
        {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_monthly_viewer, menu);
        return true;
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            {
            return true;
            }

        return super.onOptionsItemSelected(item);
        }

    @Override
    public void onReady( DailyData data)
        {
        viewPagerDaily.setCurrentItem( data.dayIndex );
        showDaily();
        }

    @Override
    public void onBackPressed()
        {
        // A BACK Billentyűt visszaadjuk editFragment-nek, ha létezik
        // (uis. nem lehet csak a BackStack-et visszaszedni)
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment editFrag = fragmentManager.findFragmentById(R.id.edit_frame);
        if (editFrag != null)
            {
            Scribe.note("BACK PRESS was forwarded to EDIT Fragment");
            ((GenericEditFragment) editFrag).cancelEdit();
            }
        else if ( frameDailyIsShown )
            {
            showMonthly();
            }
        else
            {
            super.onBackPressed();
            }
        }

    // EditFragment jelzett vissza - befejeztük a szerkesztést.
    public void onFinished( long rowId )
        {
        getSupportFragmentManager().popBackStack();
        findViewById(R.id.edit_frame).setVisibility(View.GONE);
        }


    @Override
    public void onLongClickDetected(DailyData data)
        {

        showEdit( data.longtime, -1L );

        /* Edit-fragment is used now.
         * Other way: call CalendarControllActivity
        Intent i = new Intent();
        i.setClass(this, CalendarControllActivity.class);
        i.putExtra( GenericEditFragment.EDITED_ITEM, -1L );
        startActivity(i);
        */
        }

    @Override
    public DataStore getDataStore()
        {
        return dataStore;
        }

    }
