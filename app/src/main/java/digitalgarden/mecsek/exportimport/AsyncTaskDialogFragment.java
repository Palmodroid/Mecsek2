package digitalgarden.mecsek.exportimport;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.selectfile.SelectFileActivity;


public class AsyncTaskDialogFragment extends DialogFragment
	{
	private GenericAsyncTask genericAsyncTask;
	
	private EditText input;

	private ProgressBar progressBar;
	private TextView progressText;

	private TextView message;

	private Button buttonStart;
	private Button buttonCancel;
	private Button buttonReady;

	
	// Static factory method - új AsyncTaskDialogFragmnet készítéséhez ezt használjuk, 
	// setArguments() segítségével átadja a paramétereket is
	public static AsyncTaskDialogFragment newInstance(SelectFileActivity.Mode mode, File file )
		{
		Scribe.note( "AsyncTaskDialogFragment newInstance: Mode " + mode.toString() );
		
    	AsyncTaskDialogFragment asyncTaskDialogFragment = new AsyncTaskDialogFragment();
		asyncTaskDialogFragment.setRetainInstance( true );
    	
		Bundle args = new Bundle();
		args.putSerializable("FILE", file);
		args.putSerializable("MODE", mode);
		asyncTaskDialogFragment.setArguments( args );
		
		return asyncTaskDialogFragment;
		}

	
	// Felület elkészítése
	// Az input mezőt kitölti a getArgument()-ből származó értékkel
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    	{
    	View view = inflater.inflate(R.layout.async_task_dialog_fragment, container);

    	// BackSpace lekezelésére saját módszert fogunk alkalmazni
    	getDialog().setCancelable(false);
    	getDialog().setCanceledOnTouchOutside(false);
    	
    	// http://stackoverflow.com/a/7815342
    	getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
    		{
    		@Override
    		public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
    			{
    			if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) 
    				{
    				if ( checkTaskState() == TaskStatus.RUNNING )
    					cancelTask();
    				else
    					{
    					AsyncTaskDialogFragment.this.dismiss();
    					}
    				return true;
    				}
    			return false;
    			}
    		});
    	
    	// Nincs ellenőrzés, mert a newInstance() mindenképp beteszi a megfelelő értéket
		if ( getArguments().getSerializable("MODE") == SelectFileActivity.Mode.EXPORT )
			{
			getDialog().setTitle(R.string.dialog_export_title);
			}
		else
			{
			getDialog().setTitle(R.string.dialog_import_title);
			}
		
		input = (EditText) view.findViewById(R.id.input);
		input.setText( ((File)getArguments().getSerializable("FILE")).getName() );
		
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		progressText = (TextView) view.findViewById(R.id.progressText);
		
		message = (TextView) view.findViewById(R.id.message);

		buttonStart = (Button) view.findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener()
    		{
    		public void onClick(View view) 
    			{
   				startTask();
    			} 
    		});

        buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener()
			{
			public void onClick(View view) 
				{
				cancelTask();
				} 
			});

        buttonReady = (Button) view.findViewById(R.id.button_ready);
        buttonReady.setOnClickListener(new View.OnClickListener()
			{
			public void onClick(View view) 
				{
				AsyncTaskDialogFragment.this.dismiss();
				} 
			});

        // Didaktikailag itt állítjuk be a felületet indításhoz
        updateLayout();
        
        // Ha nem kell módosítani az input mezőn, akkor a feladatot azonnal is indíthatjuk
        // Ha a task már fut (újraindításkor), ezt a lépést átugorja
        // startTask();
        
        return view;
    	}


    @Override
    public void onStart()
    	{
    	super.onStart();
    	
		startTask();
    	}
    
    // http://code.google.com/p/android/issues/detail?id=17423
    @Override
    public void onDestroyView() 
    	{
    	if (getDialog() != null && getRetainInstance())
    		getDialog().setDismissMessage(null);
    	
     	super.onDestroyView();

     	// Ha véletlenül meg van nyitva a megerősítő kérdés, akkor tüntessük el!
       	if (confirmationDialog != null)
    		{
    		confirmationDialog.dismiss();
    		confirmationDialog = null;
    		}
    	}
    
    @Override
    public void onDismiss(DialogInterface dialog)
    	{
    	super.onDismiss(dialog);
    	Scribe.note("AsyncTaskDialogFragment onDismiss");
    	
    	if (genericAsyncTask != null)
    		{
			// Itt vissza kene menni az activityba
			// if ( genericAsyncTask.isTaskFinished() && getActivity() != null)
			//	{
			//	Scribe.note("Task ready, SelectCsvActivity also finishes");
			//	getActivity().finish();
			//	}
    		finishTaskUnconditionally();
    		genericAsyncTask = null; // lehet, hogy felesleges, de magára hagyjuk az AsyncTask-et
    		}
    	}
    
    
    private static enum TaskStatus 
    	{
    	BEFORE_START, 
    	RUNNING, 
    	READY 
    	};
    
    private TaskStatus checkTaskState()
    	{
    	if ( genericAsyncTask == null )
    		return TaskStatus.BEFORE_START;
    	
    	if ( genericAsyncTask.isRunning() )
    		return TaskStatus.RUNNING;
    	
   		return TaskStatus.READY;
    	}
    
	// Háttérfolyamat indítása
    // CSAK buttonStart lenyomásakor 
	private void startTask()
		{
		Scribe.note("AsyncTaskDialogFragment startTask");
		
		if ( checkTaskState() == TaskStatus.BEFORE_START ) // Ujrainditas nem megengedett!!   != TaskStatus.RUNNING )
			{
			try 
				{
				//int data = Integer.parseInt( input.getText().toString() );
				if ( getArguments().getSerializable("MODE") == SelectFileActivity.Mode.EXPORT )
					{
					genericAsyncTask = new AsyncTaskExport( this, (File)getArguments().getSerializable("FILE") );
					}
				else
					{
					genericAsyncTask = new AsyncTaskImport( this, (File)getArguments().getSerializable("FILE") );
					}
				genericAsyncTask.execute();
				
				Scribe.note("AsyncTaskDialogFragment started genericAsyncTask derivates");
				}
			catch (NumberFormatException e)
				{
				Toast.makeText( getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
				}
			}
		}


	// Háttérfolyamat megszakítása - feltétel és ellenőrzés nélkül
	// közvetlenül csak onDismiss() hívja
	private void finishTaskUnconditionally()
		{
		Scribe.note("AsyncTaskDialogFragment: finishTaskUnconditionally");
		
		// Ha true értéket adunk át, a thread is megszakad, és szemetet hagy hátra by Shane Kirk
        genericAsyncTask.cancel(false);
		}
	
	
	private AlertDialog confirmationDialog;
	
	// Háttérfolyamat megszakítása - felhasználó által 
	// buttonCancel
	// BACK (onKeyListener-ben)
	private void cancelTask()
		{
		Scribe.note("AsyncTaskDialogFragment cancelTask");
		
		if ( checkTaskState() == TaskStatus.RUNNING )
			{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( getActivity() );
			alertDialogBuilder.setTitle( R.string.confirmation_title );
			// alertDialogBuilder.setMessage( "Click yes to exit!" );
			alertDialogBuilder.setCancelable(false);
			alertDialogBuilder.setPositiveButton( R.string.confirmation_yes, new DialogInterface.OnClickListener() 
				{
				public void onClick(DialogInterface dialog,int id) 
					{
					finishTaskUnconditionally();
					}
				});
			alertDialogBuilder.setNegativeButton( R.string.confirmation_no, new DialogInterface.OnClickListener() 
				{
				public void onClick(DialogInterface dialog,int id) 
					{
					dialog.dismiss();
					// kevésbé lényeges, de különben onDestroyView()-ban ismét dismiss-eli
					confirmationDialog = null;
					}
				});
	 
			// create alert dialog
			confirmationDialog = alertDialogBuilder.create();
	 
			// show it
			confirmationDialog.show();
			}
		}
		
	// A teljes felhasználó felület beállítása
	// AsyncTask UI szálon futó részei hívják meg
	protected void updateLayout() 
		{
		switch ( checkTaskState() )
			{
			case BEFORE_START:
				Scribe.note("AsyncTaskDialogFragment updateLayout BEFORE_START");
				
				progressBar.setVisibility( View.VISIBLE );
				progressText.setVisibility( View.VISIBLE );
				
				message.setVisibility( View.VISIBLE );
				
				buttonStart.setVisibility( View.VISIBLE );
				buttonCancel.setVisibility( View.GONE );
				buttonReady.setVisibility( View.GONE );
				break;				

			case RUNNING:
				Scribe.note("AsyncTaskDialogFragment updateLayout RUNNING");

				input.setEnabled( false );
			
				progressBar.setVisibility( View.VISIBLE );
				progressBar.setMax( progressMax );
				progressText.setVisibility( View.VISIBLE );

				message.setVisibility( View.VISIBLE );
				
				buttonStart.setVisibility( View.GONE );
				buttonCancel.setVisibility( View.VISIBLE );
				buttonReady.setVisibility( View.GONE );
				break;				

			case READY:
				Scribe.note("AsyncTaskDialogFragment updateLayout READY");
			
				progressBar.setVisibility( View.VISIBLE );
				progressText.setVisibility( View.VISIBLE );
				
				message.setVisibility( View.VISIBLE );
				message.setText( genericAsyncTask.getReturnedMessage() );
				
				buttonStart.setVisibility( View.GONE );
				// A folyamat ugyan újraindítható, de most ezt nem engedjük meg
				buttonCancel.setVisibility( View.GONE );
				buttonReady.setVisibility( View.VISIBLE );
				break;				
			}
		}
	
	private int progressMax;
	
	protected void setProgressMax(int max)
		{
		progressMax = max;
		}
	
	protected void setProgress(int progress)
		{
		progressText.setText( progress + "/" + progressMax );
		progressBar.setProgress( progress );
		}

	}
