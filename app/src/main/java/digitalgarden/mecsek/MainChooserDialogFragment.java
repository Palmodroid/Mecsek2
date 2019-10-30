package digitalgarden.mecsek;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import digitalgarden.mecsek.scribe.Scribe;

public class MainChooserDialogFragment extends DialogFragment
	{
	public static enum Type 
		{
		CONFIRM_IMPORT,
		CONFIRM_OVERWRITING_EXPORT,
		CONFIRM_NEW_EXPORT
		}
	
	public static MainChooserDialogFragment showNewDialog(FragmentActivity activity, Type type, String fileName)
		{
		Scribe.note("MainChooserDialogFragment: " + type.toString() );
		
		MainChooserDialogFragment dialogFragment; 
		FragmentManager fragmentManager = activity.getSupportFragmentManager();

		// Ha volt megnyitott dialogus, azt eltüntetjük
    	dialogFragment = (MainChooserDialogFragment) fragmentManager.findFragmentByTag( "DIALOG" );
    	if ( dialogFragment != null )
    		{
    		dialogFragment.dismiss();
    		Scribe.note("Previous dialog was closed!");
    		}
    	
    	// És nyitunk egy újat, amit be is mutatunk
    	dialogFragment = new MainChooserDialogFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("TYPE", type);
		args.putSerializable("FILE", fileName);
		dialogFragment.setArguments(args);
		
		dialogFragment.show( activity.getSupportFragmentManager(), "DIALOG");
		
		return dialogFragment;
		}
	
	MainChooserActivity activity;
	
	@Override
	public void onAttach(Activity activity)
		{
		super.onAttach(activity);
		
		if ( activity instanceof MainChooserActivity)
			this.activity = (MainChooserActivity) activity;
		else 
            throw new ClassCastException(activity.toString() + " must be instanceof MainChooserActivity!");
		}
	
	@Override
	public void onDetach() 
		{
		super.onDetach();
		
		activity = null;
		}
		
	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
		{
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( getActivity() );

    	// HIBA ELLENŐRZÉS!!
    	Bundle args = getArguments();
		final Type type = (Type) args.getSerializable("TYPE");
		final String fileName = args.getString("FILE");
    	
		int positiveButtonResourceId = 0;
		
		switch ( type )
			{
		case CONFIRM_IMPORT:
			{
			alertDialogBuilder.setTitle( activity.getString(R.string.dialog_import) + " [" + fileName + "]?");
			positiveButtonResourceId = R.string.dialog_button_import;
			break;
			}
			
		case CONFIRM_OVERWRITING_EXPORT:
			{
			alertDialogBuilder.setTitle( activity.getString(R.string.dialog_export) + " [" + fileName + "]? ");
			alertDialogBuilder.setMessage( R.string.dialog_export_warning);
			positiveButtonResourceId = R.string.dialog_button_export_overwrite;
			break;
			}
			
		case CONFIRM_NEW_EXPORT:
			{
			alertDialogBuilder.setTitle( activity.getString(R.string.dialog_export) + " [" + fileName + "]? ");
			positiveButtonResourceId = R.string.dialog_button_export;
			break;
			}
		}

		alertDialogBuilder.setPositiveButton( positiveButtonResourceId, new DialogInterface.OnClickListener()
			{
			public void onClick(DialogInterface dialog, int which) 
				{
				activity.onDialogPositiveResult( type );
				}
			});
		alertDialogBuilder.setNegativeButton( R.string.dialog_button_cancel, new DialogInterface.OnClickListener() 
			{
			public void onClick(DialogInterface dialog, int which) 
				{
				activity.onDialogCancelled();
				}
			});

		return alertDialogBuilder.create();
		}
   
	}
