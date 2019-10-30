package digitalgarden.mecsek.diary;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class MonthlyAdapter extends FragmentStatePagerAdapter
    {
    private static int NUM_MONTHS = 16788;

    public MonthlyAdapter(FragmentManager fragmentManager )
        {
        super( fragmentManager );
        }

    // Returns total number of pages
    @Override
    public int getCount()
        {
        return NUM_MONTHS;
        }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position)
        {
        // position == montsSinceEpoch
        return MonthlyFragment.newInstance( position ); //null;
        }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position)
        {
        return ">" + position + "<";
        //return (1601 + (position / 12)) + "." + (position % 12 + 1);
        }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object)
        {
        super.destroyItem(container, position, object);

/*        for ( Fragment f : fm.getFragments())
            {
            Scribe.debug("Fragment: " + f.getTag());
            }

        Scribe.debug("Item destroyed: " + position);
*/
        }
    }
