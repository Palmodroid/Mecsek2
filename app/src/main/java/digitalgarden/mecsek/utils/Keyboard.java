package digitalgarden.mecsek.utils;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import digitalgarden.mecsek.scribe.Scribe;


public class Keyboard
	{
	// http://stackoverflow.com/a/17789187; többi is jó lehet, ez elsőre is működött 
    public static void hide(Activity activity) 
		{
		if (activity == null)
			{	
			Scribe.note("Hide-keyboard: activity is missing!!");
			return;
			}

		//start with an 'always hidden' command for the activity's window
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
		| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		Scribe.note("Hide-keyboard: SOFT_INPUT_STATE_ALWAYS_HIDDEN");

		//now tell the IMM to hide the keyboard FROM whatever has focus in the activity
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		View cur_focus = activity.getCurrentFocus();
		if(cur_focus != null)
			{
			inputMethodManager.hideSoftInputFromWindow(cur_focus.getWindowToken(), 0);
			Scribe.note("Hide-keyboard: hideSoftInputFromWindow");
			}
		}     
	}
