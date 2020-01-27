package digitalgarden.mecsek.color;

import digitalgarden.mecsek.generic.GenericCombinedListFragment;
import digitalgarden.mecsek.generic.GenericControllActivity;
import digitalgarden.mecsek.generic.GenericEditFragment;

/**
 * 1. Create new ControllActivity extending GenericControllActivity and implement missing methods
 * 2. Add activity to AndroidManifest.xml
 *      <activity
 *          android:theme="@style/AppTheme.NoActionBar"
 *          android:name=".color.ColorDefsControllActivity"
 *          android:label="@string/colordefs_title" >
 *      </activity>
 * 3. Add activity's name to library_strings.xml
 *      <string name="colordefs_title">Color Definitions</string>
 * 4. Add a new Button to main_chooser_activity.xml
 * 		<Button
 * 			android:id="@+id/button_colordefs"
 * 			android:text="Color Definitions"
 * 			android:minEms="10"
 * 			android:layout_width="wrap_content"
 * 			android:layout_height="wrap_content"
 * 			android:layout_below="@id/button_diary"
 * 			android:layout_centerHorizontal="true" />
 * 5. (Button names are not exported to strings!!)
 * 6. Expand MainChooserActivity.java with the new button
 *             findViewById(R.id.button_colordefs).setOnClickListener(new OnClickListener()
 *                 {
 *                 public void onClick(View view)
 *                     {
 *                     Scribe.title("MAINCHOOSER: Colordefs called");
 *                     Intent i = new Intent();
 *                     i.setClass(MainChooserActivity.this, ColorDefsControllActivity.class);
 *                     startActivity(i);
 *                     }
 *                 });
 *
 *  GO TO LIST FRAGMENT !!
 *  GO TO EDIT FRAGMENT !!
 */
public class ColorDefsControllActivity extends GenericControllActivity
    {
    @Override
    protected GenericEditFragment createEditFragment()
        {
        return new ColorDefsEditFragment();
        }

    @Override
    protected GenericCombinedListFragment createListFragment()
        {
        return ColorDefsListFragment.newInstance();
        }
    }
