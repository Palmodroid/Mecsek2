package digitalgarden.mecsek.diary;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import digitalgarden.mecsek.scribe.Scribe;

public class DailyAdapter extends FragmentStatePagerAdapter
    {
    private static int NUM_DAYS = 167880;

    public DailyAdapter(FragmentManager fragmentManager )
        {
        super(fragmentManager);
        }

    // Returns total number of pages
    @Override
    public int getCount()
        {
        return NUM_DAYS;
        }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position)
        {
        // position == daysSinceEpoch
        return DailyListFragment.newInstance( position ); //null;
        }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position)
        {
        return ">" + position + "<";
        }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object)
        {
        super.destroyItem(container, position, object);

        /*
        for ( Fragment f : fm.getFragments())
            {
            Scribe.debug("Fragment: " + f.getTag());
            }

        Scribe.debug("Item destroyed: " + position);
        */
        }
    }
