package digitalgarden.mecsek;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;

import digitalgarden.mecsek.MainChooserDialogFragment.Type;
import digitalgarden.mecsek.database.DatabaseContentProvider;
import digitalgarden.mecsek.database.authors.AuthorsControllActivity;
import digitalgarden.mecsek.database.books.BooksControllActivity;
import digitalgarden.mecsek.database.calendar.CalendarControllActivity;
import digitalgarden.mecsek.database.medications.MedicationsControllActivity;
import digitalgarden.mecsek.database.patients.PatientsControllActivity;
import digitalgarden.mecsek.database.pills.PillsControllActivity;
import digitalgarden.mecsek.diary.DiaryActivity;
import digitalgarden.mecsek.exportimport.AsyncTaskDialogFragment;
import digitalgarden.mecsek.permission.PermissionRequestDialog;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.selectfile.SelectFileActivity;
import digitalgarden.mecsek.selectfile.SelectFileActivity.Mode;

import static digitalgarden.mecsek.Debug.initScribe;
import static digitalgarden.mecsek.MainChooserDialogFragment.Type.CONFIRM_IMPORT;
import static digitalgarden.mecsek.MainChooserDialogFragment.Type.CONFIRM_NEW_EXPORT;
import static digitalgarden.mecsek.MainChooserDialogFragment.Type.CONFIRM_OVERWRITING_EXPORT;
import static digitalgarden.mecsek.database.DatabaseMirror.database;

public class MainChooserActivity extends FragmentActivity implements PermissionRequestDialog.OnPermissionRequestFinished
	{
	@Override
	protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        // Longtime longtime = new Longtime();

        /*
        for ( int y = 1970; y < 2020; y++ )
            {
            longtime.set( y, 1, 1 );

            int di = longtime.getDayIndex();
            int mi = longtime.getMonthIndex();
            int dn = longtime.getDayName();

            Scribe.debug("LONGTIME : " + longtime.toString(true) +
                    " dayIndex: " + di +
                    " monthIndex: " + mi +
                    " day name: " + dn + ( dn == 0 ? "<!!!!!!!!!!!!" : ""));
            }
        */

        /*
        longtime.set(2000, 3, 17);

        int di = longtime.getDayIndex();
        int mi = longtime.getMonthIndex();

        Scribe.debug("LONGTIME : " + longtime.toString(true) +
                " dayIndex: " + di +
                " monthIndex: " + mi);

        longtime.setDayIndex( di );
        Scribe.debug("LONGTIME from DI: " + longtime.toString(true));

        longtime.setMonthIndex( mi );
        Scribe.debug("LONGTIME from MI: " + longtime.toString(true));
        */

        /*
        longtime.set(1970, 1, 1);
        boolean error = false;
        for (int n = 0; n < 500; n++)
            {
            Scribe.debug("LONGTIME : " + longtime.toString(true) +
                    " leap1: " + longtime.isLeapYear() +
                    " leap2: " + longtime.isLeapYear2());

            if ( longtime.isLeapYear() != longtime.isLeapYear2() )
                {
                error = true;
                break;
                }

            longtime.addDays( 365 );
            }

        if ( error )
            Scribe.debug("LONGTIME : LEAPYEAR ERROR !!" );

        else
            Scribe.debug("LONGTIME : LEAPYEAR O.K. !!" );
        */

        /*
        longtime.set(1970, 1, 1);
        Scribe.debug("LONGTIME: " + longtime.toString(true) +
                " index: " + longtime.getDayIndex() );

        for (int n = 0; n < 80000; n++)
            {
            longtime.addDays(1);

            if (longtime.daysSinceEpoch() - 134774 != longtime.getDayIndex())
                {
                Scribe.debug("LONGTIME ERROR: " + longtime.toString(true) +
                        " epoch: " + longtime.daysSinceEpoch() +
                        " index: " + longtime.getDayIndex());

                break;
                }
            }

        Scribe.debug( "LONGTIME READY: " + longtime.toString(true) +
                " epoch: " + longtime.daysSinceEpoch() +
                " index: " + longtime.getDayIndex() );
        */


        PermissionRequestDialog permissionRequestDialog =
                (PermissionRequestDialog)getFragmentManager().findFragmentByTag("dialog");
        if ( permissionRequestDialog == null )
            {
            //Scribe.debug(Debug.PERMISSION, "Permission dialog is not found, it should be recreated!");
            permissionRequestDialog = PermissionRequestDialog.newInstance();
            // permissionRequestDialog.setRetainInstance(true); - do not need to retain
            // testDialog.setCancelable(false);
            permissionRequestDialog.show( getFragmentManager(), "dialog");
            }

		}


    @Override
    public void onPermissionRequestFinish(boolean permissionsGranted)
        {
        if (permissionsGranted)
            {
            initScribe(this);
            Scribe.title("Database started");

            setContentView(R.layout.main_chooser_activity);

            findViewById(R.id.button_authors_table).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.title("MAINCHOOSER: Authors table called");

                    Intent i = new Intent();

                    i.setClass(MainChooserActivity.this, AuthorsControllActivity.class);
                    startActivity(i);
                    }
                });

            findViewById(R.id.button_books_table).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.title("MAINCHOOSER: Books table called");

                    Intent i = new Intent();

                    i.setClass(MainChooserActivity.this, BooksControllActivity.class);
                    startActivity(i);
                    }
                });

            findViewById(R.id.button_patients_table).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.title("MAINCHOOSER: Patients table called");

                    Intent i = new Intent();

                    i.setClass(MainChooserActivity.this, PatientsControllActivity.class);
                    startActivity(i);
                    }
                });

            findViewById(R.id.button_pills_table).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.title("MAINCHOOSER: Pills table called");

                    Intent i = new Intent();

                    i.setClass(MainChooserActivity.this, PillsControllActivity.class);
                    startActivity(i);
                    }
                });

            findViewById(R.id.button_medications_table).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.title("MAINCHOOSER: Medications table called");

                    Intent i = new Intent();

                    i.setClass(MainChooserActivity.this, MedicationsControllActivity.class);
                    startActivity(i);
                    }
                });

            findViewById(R.id.button_calendar_table).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.title("MAINCHOOSER: Calendar table called");

                    Intent i = new Intent();

                    i.setClass(MainChooserActivity.this, CalendarControllActivity.class);
                    startActivity(i);
                    }
                });

			findViewById(R.id.button_proba).setOnClickListener(new OnClickListener()
				{
				public void onClick(View view)
					{
					Scribe.title("MAINCHOOSER: Proba called");

					Intent i = new Intent();

					i.setClass(MainChooserActivity.this, DiaryActivity.class);
					startActivity(i);
					}
				});

			findViewById(R.id.button_export).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.title("MAINCHOOSER: Export called");
                    startPorting(PortingType.EXPORT);
                    }
                });

            findViewById(R.id.button_import).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.title("MAINCHOOSER: Import called");
                    startPorting(PortingType.IMPORT);
                    }
                });

            findViewById(R.id.button_drop).setOnClickListener(new OnClickListener()
                {
                public void onClick(View view)
                    {
                    Scribe.note("MainActivity Menu: DROP");
                    getContentResolver().call(
                            Uri.parse("content://" + database().authority()),
                            DatabaseContentProvider.DROP_METHOD,
                            null, null);
                    }
                });
            }
        else
            finish();
        }


    static enum PortingType
		{
		EXPORT,
		IMPORT
		};
	
	static enum PortingState
		{
		NO_PORTING,
		SELECT_FILE_FOR_PORTING,	// Ez pl. tök felesleges
		CONFIRM_PORTING
		};
	
	private PortingType portingType; 
	private PortingState portingState = PortingState.NO_PORTING;	
		
	private final static int SELECT_FILE_REQUEST = 1;
	
	protected File portingFile;
	protected String directorySubPath;

	protected void onSaveInstanceState( Bundle state ) 
		{
		super.onSaveInstanceState( state );
		Scribe.note("MainChooser: onSaveInstanceState, State: " + portingState.toString() );
		
		state.putSerializable( "PORTING_TYPE", portingType );
		state.putSerializable( "PORTING_STATE", portingState );
		state.putSerializable( "PORTING_FILE", portingFile );
		state.putString( "DIRECTORY_SUB_PATH", directorySubPath );
		}

	public void onRestoreInstanceState( Bundle state ) 
		{
		super.onRestoreInstanceState( state );
		
		if (state != null)
			{
			portingType = (PortingType) state.getSerializable( "PORTING_TYPE" );
			portingState = (PortingState) state.getSerializable( "PORTING_STATE" );
			portingFile = (File) state.getSerializable( "PORTING_FILE" );
			directorySubPath = state.getString( "DIRECTORY_SUB_PATH" );

			Scribe.note("MainChooser: onRestoreInstanceState, State: " + portingState.toString() );
			}
		else
			Scribe.note("MainChooser: onRestoreInstanceState is NULL!");
		}
	
	protected void startPorting( PortingType type )
		{
		Scribe.note("MainChooser: startPorting: " + type.toString() );
		
		portingType = type;
		portingState = PortingState.SELECT_FILE_FOR_PORTING;
		
		Intent i = new Intent();
		
		i.setClass( MainChooserActivity.this, SelectFileActivity.class );
		
		i.putExtra( SelectFileActivity.FILE_ENDING, getString( R.string.extension ) );
		
		if ( directorySubPath == null )
			i.putExtra( SelectFileActivity.DIRECTORY_SUB_PATH, getString( R.string.directory ) );
		else
			i.putExtra( SelectFileActivity.DIRECTORY_SUB_PATH, directorySubPath );
		
		if ( portingType == PortingType.IMPORT )
			{
			i.putExtra( SelectFileActivity.CUSTOM_TITLE, "Select File for Import!");
			i.putExtra( SelectFileActivity.CREATE_ALLOWED, false);
			}
		else // portingType == EXPORT
			{
			i.putExtra( SelectFileActivity.CUSTOM_TITLE, "Select File for Export!");
			i.putExtra( SelectFileActivity.CREATE_ALLOWED, true);
			}

		startActivityForResult( i, SELECT_FILE_REQUEST );
		}
	
	// http://stackoverflow.com/a/18345899
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
		{
		Scribe.note("MainChooser: onActivityResult" );

		if ( requestCode == SELECT_FILE_REQUEST )
			{
			if ( resultCode == RESULT_OK )
				{
				// Az a baj, hogy android.net.uri != java.net.uri
				portingFile = new File ( data.getStringExtra( SelectFileActivity.SELECTED_FILE ) );
				directorySubPath = data.getStringExtra( SelectFileActivity.DIRECTORY_SUB_PATH );
				
				portingState = PortingState.CONFIRM_PORTING;
				}
			else
				portingState = PortingState.NO_PORTING;
			}
		}
	
	// Ha Activity visszatérése miatt jöttünk...
	@Override
	protected void onResumeFragments() 
		{
	    super.onResumeFragments();
		Scribe.note("MainChooser: onResumeFragments" );

	    if ( portingState == PortingState.CONFIRM_PORTING )
	    	{
			Scribe.note("    Confirm Porting part started" );

			if ( portingType == PortingType.IMPORT )
				{
				MainChooserDialogFragment.showNewDialog(this, 
					CONFIRM_IMPORT, portingFile.getName() );
				}
			else if ( portingType == PortingType.EXPORT )
				{
				if ( portingFile.exists() )
					{
					MainChooserDialogFragment.showNewDialog(this, 
						CONFIRM_OVERWRITING_EXPORT, portingFile.getName() );
					}
				else
					{
					MainChooserDialogFragment.showNewDialog(this, 
						CONFIRM_NEW_EXPORT, portingFile.getName() );
					}
				}
	    	}
	    else
			Scribe.note("    Normal start, Confirm Porting not yet started" );

		}
	
	public void onDialogPositiveResult( Type type )
		{
		Scribe.note("MainChooser: Return from Dialogs: POSITIVE");
		
		portingState = PortingState.NO_PORTING;
		
		switch (type)
			{
			case CONFIRM_IMPORT:
				{
				Scribe.note("Confirm Import - Import started");
				
				AsyncTaskDialogFragment asyncTaskDialogFragment = AsyncTaskDialogFragment.newInstance( Mode.IMPORT, portingFile );
				asyncTaskDialogFragment.show(getSupportFragmentManager(), "DIALOG");
				break;
				}
			case CONFIRM_NEW_EXPORT:
			case CONFIRM_OVERWRITING_EXPORT:
				{
				Scribe.note("Confirm NEW/OVERWRITING Export - Export Started");
				
				AsyncTaskDialogFragment asyncTaskDialogFragment = AsyncTaskDialogFragment.newInstance( Mode.EXPORT, portingFile );
				asyncTaskDialogFragment.show(getSupportFragmentManager(), "DIALOG");
				break;
				}
			}
		}
	
	public void onDialogCancelled()
		{
		Scribe.note("MainChooser: Return from Dialogs: CANCEL");

		startPorting( portingType );
		}

    }
