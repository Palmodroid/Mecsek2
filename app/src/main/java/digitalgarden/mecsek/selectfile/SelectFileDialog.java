package digitalgarden.mecsek.selectfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.EditText;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;


// Dialogusokért felelős fragment-rész
public class SelectFileDialog extends DialogFragment
	{
	public static enum Type 
		{
		SD_CARD_ERROR,
		CREATE_DIRECTORY,
		CREATE_FILE,
		}
	
	public static SelectFileDialog showNewDialog(FragmentActivity activity, Type type )
		{
		return showNewDialog( activity, type, null );
		}

	public static SelectFileDialog showNewDialog(FragmentActivity activity, Type type, String text)
		{
		Scribe.note("SelectFileDialog: " + type.toString() );
		
		SelectFileDialog selectFileDialog; 
		FragmentManager fragmentManager = activity.getSupportFragmentManager();

		// Ha volt megnyitott dialogus, azt eltüntetjük
    	selectFileDialog = (SelectFileDialog) fragmentManager.findFragmentByTag( "DIALOG" );
    	if ( selectFileDialog != null )
    		{
    		selectFileDialog.dismiss();
    		Scribe.note("Previous dialog was closed!");
    		}
    	
    	// És nyitunk egy újat, amit be is mutatunk
    	selectFileDialog = new SelectFileDialog();
		
		Bundle args = new Bundle();
		args.putSerializable("TYPE", type);
		args.putString("TEXT", text); // vigyázz, ez lehet null!!
		selectFileDialog.setArguments(args);
		
		selectFileDialog.show( activity.getSupportFragmentManager(), "DIALOG");
		
		return selectFileDialog;
		}
	
	SelectFileActivity selectFileActivity;
	
	@Override
	public void onAttach(Activity activity) 
		{
		super.onAttach(activity);
		
		if ( activity instanceof SelectFileActivity)
			selectFileActivity = (SelectFileActivity) activity;
		else 
            throw new ClassCastException(activity.toString() + " must be instanceof SelectFileActivity!");
		}
	
	@Override
	public void onDetach() 
		{
		super.onDetach();
		
		selectFileActivity = null;
		}
		
	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
		{
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( getActivity() );

    	// HIBA ELLENŐRZÉS!!
    	Bundle args = getArguments();
		final Type type = (Type) args.getSerializable("TYPE");
		final String text = args.getString("TEXT");
    	
		switch ( type )
			{
			case SD_CARD_ERROR:
				{
				alertDialogBuilder.setMessage( R.string.dialog_sd_card_error );
				alertDialogBuilder.setPositiveButton( R.string.dialog_button_ok, new DialogInterface.OnClickListener() 
					{
					public void onClick(DialogInterface dialog, int which) 
						{
						selectFileActivity.onDialogPositiveResult( type, null );
						}
		           });

				SelectFileDialog.this.setCancelable( false );
				break;
				}
				
			case CREATE_DIRECTORY:
				{
				alertDialogBuilder.setTitle( R.string.dialog_create_directory );

				final EditText name = new EditText( getActivity() );
				// Ezzel megőrzi elforgatásnál az értékét - alacsony ID értékek használhatóak
				// Ez érdekes: http://stackoverflow.com/questions/1714297/android-view-setidint-id-programmatically-how-to-avoid-id-conflicts
				// Gond: csak azokat a konfliktusokat ellenőrizzük, amik már benne vannak a View-Tree-ben, egyébként meg használhatunk alacsony értékeket
				// Vagy: használjuk a Res:id megadásának lehetőségét
				name.setId( 1 );
				name.setText( text );
				alertDialogBuilder.setView( name );
 				
				alertDialogBuilder.setPositiveButton( R.string.dialog_button_create, new DialogInterface.OnClickListener() 
					{
					public void onClick(DialogInterface dialog, int which) 
						{
						selectFileActivity.onDialogPositiveResult( type, name.getText().toString() );
						}
		           });
		
				alertDialogBuilder.setNegativeButton( R.string.dialog_button_cancel, null );
		
				break;
				}
				
			case CREATE_FILE:
				{
				alertDialogBuilder.setTitle( R.string.dialog_create_file);

				final EditText name = new EditText( getActivity() );
				// Ezzel megőrzi elforgatásnál az értékét - alacsony ID értékek használhatóak
				name.setId( 2 );
				name.setText( text );
				alertDialogBuilder.setView( name );

				alertDialogBuilder.setPositiveButton( R.string.dialog_button_create, new DialogInterface.OnClickListener() 
					{
					public void onClick(DialogInterface dialog, int which) 
						{	
						selectFileActivity.onDialogPositiveResult( type, name.getText().toString() );
						}
					});

				alertDialogBuilder.setNegativeButton( R.string.dialog_button_cancel, null );

				break;
				}
								
			}
		
		return alertDialogBuilder.create();
		}
   
	}
