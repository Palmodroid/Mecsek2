package digitalgarden.mecsek.selectfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.Keyboard;


/*	
	Hogyan történik az export/import folyamata?
	
	LibraryDbMainActivity.onOptionsMenuItemSelected()
		startActivityForResult
		
	FileChooserActivity
		onDialogPositiveResult
		
	AsyncTaskDialogFragment
	
	ImportingAsyncTask extends GenericAsyncTask
	ExportingAsyncTask extends GenericAsyncTask
 */
 
/* 
 * A file-chooser activity az sd-kártya könyvtáraiból segít kiválasztani egy file-t
 * A kiválasztás standard módjáról az AndroidManifest.xml gondoskodik:
	<intent-filter>
		<action android:name="android.intent.action.GET_CONTENT" />
		<category android:name="android.intent.category.DEFAULT" />
		<data android:mimeType="file/*" />
	</intent-filter>
 */

public class SelectFileActivity extends FragmentActivity
	{

	
	
	public static enum Mode
		{
		EXPORT,
		IMPORT
		}
	
	
	
	// Intent-ben extra String jelölésére: SD-kártyán belüli indító könyvtár 
	public static String DIRECTORY_SUB_PATH = "DIRECTORY_SUB_PATH";

	// Intent-ben extra String jelölésére: csak az adott végződésekkel (kiterjesztéssel) 
	// rendelkező file-ok kerülnek megjelenítésre 
	public static String FILE_ENDING = "FILE_ENDING";
	
	// Intent-ben extra Boolean jelölésére: új file/új könyvtár létrehozása engedélyezett-e
	public static String CREATE_ALLOWED = "CREATE_ALLOWED";
	
	// Intent-ben extra String jelölésére: egyedi címsor létrehozása
	public static String CUSTOM_TITLE = "CUSTOM_TITLE";
	
	
	// VISSZATÉRÉSI ÉRTÉKEK
	// Data: a kiválasztott file URL címe (nem biztos, hogy létezik!!)
	// DIRECTORY_SUB_PATH: a kiválasztott file könyvtára az SD-kártyán belül (vagy teljes, ha ezt nem tudja leválasztani)
	// A kiválasztott file neve
	public static String SELECTED_FILE_NAME = "SELECTED_FILE_NAME";
	// Mivel a data az URI-Uri átalakítás miatt nehezen használható, megadjuk a teljes path-ot is
	public static String SELECTED_FILE = "SELECTED_FILE";
	
	
	// Ezek az értékek nem változnak - https://groups.google.com/d/msg/android-developers/9Hrya2lq8uI/aI4jBQhjGzUJ és előtte
	private boolean isCreateAllowed()
		{
		return getIntent().getBooleanExtra( CREATE_ALLOWED, true );
		}
	
	private String getCustomTitle()
		{
		return getIntent().getStringExtra( CUSTOM_TITLE );
		}
	
	private String getFileEnding()
		{
		String fileEnding = getIntent().getStringExtra( FILE_ENDING );
		if (fileEnding == null)
			fileEnding = "";
		return fileEnding;
		}
	
	
	// Újraindítások között megtartott globális változók egy Fragmentben
	// setRetainInstance(true) kell legyen
	public static class RetainedVariables extends Fragment
		{
		// Root könyvtár, mely alatt a program nem keres
		// (Alapértelmezetten az SD-kártya)
		File rootDir;
		// EBBEN A MEGVALÓSÍTÁSBAN EZ SEM VÁLTOZIK!
		
		// Aktuálisan megjelenített könyvtár
		File currentDir;

		// A KÖVETKEZŐ HÁROM MEGFELEL AZ INTENTNEK ÉS NEM VÁLTOZIK:
		
		// A lista első látható elemének helyét megőrizzük az elforgatáskor
		// http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
		int positionOfFirstItem = -1;
		int topOfFirstItem;
		
		// Az elsõ file a listában. Erre is pozícionálhatunk.
		int positionOfFileSection;
		}
	
	private RetainedVariables variables;

	
	// UI elemek
	private ListView list;
	private EditText filter;
	private TextView ending;
	
	
	// onCreate kizárólag az UI elemek előkészítésére szolgál
	@Override
    public void onCreate(Bundle savedInstanceState) 
		{
        super.onCreate(savedInstanceState);
        setContentView( R.layout.select_file_activity );

        Scribe.note("SelectFileActivity onCreate");
        
        list = (ListView)findViewById( R.id.list );

        // A lista megérintésekor eltűnik a billentyűzet
	    list.setOnTouchListener(new OnTouchListener()
	    	{
			@Override
			public boolean onTouch(View v, MotionEvent event)
				{
				if (event.getAction() == MotionEvent.ACTION_DOWN) 
					Keyboard.hide( SelectFileActivity.this );
				return false;
				}
	    	} );
	    
	    // Elem kiválasztásakor 
	    // DIR - továbblépünk a könyvtárra
	    // PARENT_DIR - eggyel vissza - ilyenkor a jelenlegi könyvtár lesz a lista első eleme
	    // FILE (minden más) - a kiválasztott file adataival (setMonthlyData) visszatérünk
	    list.setOnItemClickListener( new OnItemClickListener()
	    	{
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	    		{
	    	    SelectFileEntry entry = (SelectFileEntry) list.getItemAtPosition( position );

	    	    if ( entry.getType() == SelectFileEntry.Type.DIR )
	    			{
	    			Scribe.note("SelectFileActivity: DIR was selected");
	    	        populateList( entry.getFile(), null );
	    	    	}
	    	    else if ( entry.getType() == SelectFileEntry.Type.PARENT_DIR ) 
	    			{
	    			Scribe.note("SelectFileActivity: PARENT DIR was selected");
	    	        populateList( variables.currentDir.getParentFile(), variables.currentDir  );
	    	    	}
	    	    else if ( entry.getType() == SelectFileEntry.Type.NEW )
	    	    	{
					Scribe.note("SelectFileActivity: NEW item selected, CREATE FILE started");
					SelectFileDialog.showNewDialog( SelectFileActivity.this, SelectFileDialog.Type.CREATE_FILE, filter.getText().toString());		
	    	    	}
				else // existing file SELECTED
					{
					returnSelectedFile( entry.getFile() );
					}
				}
	    	} );
			
		// Nem használhatunk headerView-t, mert akkor HeaderViewAdapter-t is kellene késziteni, hogy filter-t legyen kitől elkérni
		//header = new TextView( this );
		//list.addHeaderView( header, null, false );
	    
	    // szöveg beírásakor szűrjük a listát
	    // erre két lehetőség lenne
	    // 1. újra leválogatjuk a könyvtárat (lassú)
	    // 2. adapter saját filterével a teljes leválogatott könyvtárat szűkítjük tovább (ez a megvalósított)
        filter = (EditText)findViewById( R.id.filter );
        filter.addTextChangedListener( new TextWatcher() 
    		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
				{
				SelectFileAdapter adapter = ((SelectFileAdapter)list.getAdapter());
				if (adapter != null)
					{
					adapter.getFilter().filter(s);
					if ( s.length() > 0 )
						list.setSelectionFromTop( variables.positionOfFileSection, 0 );
					}
				}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
				// TODO Auto-generated method stub
				}

			@Override
			public void afterTextChanged(Editable s)
				{
				// TODO Auto-generated method stub
				}
        	} );

        ending = (TextView)findViewById( R.id.ending );
		}
	
	
	// A Fragmentek az onResumeFragments-től elérhetőek, 
	// ezért itt történik az UI elemek feltöltése a megőrzött Fragment alapján
	// Ha még nem létezik ez a Fragment, akkor új indítás történt
	@Override
	public void onResumeFragments()
		{
		super.onResumeFragments();
		
		Scribe.note("SelectFileActivity: onResumeFragments");

		FragmentManager fragmentManager = getSupportFragmentManager();

		variables = (RetainedVariables)fragmentManager.findFragmentByTag("VAR");
		if (variables == null)
			{
			Scribe.note("SelectFileActivity: new VAR fragment created, added");
			
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			variables = new RetainedVariables();
			variables.setRetainInstance( true );
			fragmentTransaction.add(variables, "VAR");
			fragmentTransaction.commit();
			
			// Van egyaltalan SD kartya?	
			variables.rootDir = Environment.getExternalStorageDirectory();
			Scribe.note("  rootDir: " + variables.rootDir.getPath());

			Intent intent = getIntent();	
			
			variables.currentDir = variables.rootDir;
			String subPath = intent.getStringExtra(DIRECTORY_SUB_PATH);
			if (subPath != null)
				{
				File temp = new File( variables.currentDir, subPath);
				// ha nincs ilyen konyvtar, akkor letrehozzuk
				if ( !temp.exists() )
					{
					temp.mkdirs();
					}
				// de csak akkor valasztjuk ki, ha ez egy konyvtar
				if ( temp.isDirectory() )
					{
					variables.currentDir = temp;					
					}
					
				/*
				// Altalanosan ez a megoldas jobb, de itt eleg volt egy egyszerubb	
				String[] subPathItems = subPath.split( System.getProperty( "file.separator" ));
				
				for (String subPathItem : subPathItems)
					{
					if (subPathItem.length() > 0)
						{
						File temp = new File( variables.currentDir, subPathItem);
						if (temp.isDirectory())
							{
							variables.currentDir = temp;
							}
						else
							break;
						}
					}
				*/
				}
			Scribe.note("  currentDir: " + variables.currentDir.getPath() );
			
			}
		else
			Scribe.note("SelectFileActivity: VAR Fragment found");
			
		if ( getCustomTitle() != null )
			{
			this.setTitle( getCustomTitle() );
			}
	    
		ending.setText( getFileEnding() );
		
        populateList( variables.currentDir, null );

        Keyboard.hide( this );
        }

		
	// Lista feltöltése
	// - dir - az aktuálisan feltöltendő könyvtár
	// - previousDir - a könyvtár, ahonnan érkeztünk (vagy null)
	// Ha previousDir található dir elemei között, akkor az lesz a lista első eleme
	// Ennek a könyvtárak közötti visszalépéskor van jelentősége. 
	// Ilyenkor a previousDir értéke a currentDir lesz, majd - felhasználás után! - kerül a currentDir ténylegesen átállításra
    private void populateList(File dir, File previousDir) 
		{
		Scribe.note( "SelectFileActivity populating " + dir.getPath() );

		List<SelectFileEntry>dirEntries = new ArrayList<SelectFileEntry>();
		List<SelectFileEntry>fileEntries = new ArrayList<SelectFileEntry>();
		
		File[] filesInDir = dir.listFiles();	// null ellenorzes: pl. leválasztották az sd-t?	
		if (filesInDir == null)
			{
			SelectFileDialog.showNewDialog( this , SelectFileDialog.Type.SD_CARD_ERROR );
			return;
			}
			
		for(File file: filesInDir) 
			{
			if ( !file.isHidden() )
				{
				if( file.isDirectory() )
					dirEntries.add( new SelectFileEntry( file, SelectFileEntry.Type.DIR ) );
				else if ( file.getName().endsWith( getFileEnding() ) )
					{
					fileEntries.add( new SelectFileEntry( file, SelectFileEntry.Type.FILE ) );
					} 
				}
			}

		Collections.sort(dirEntries);

		// Parent dir - ha van - akkor megy az elejere
		if(!dir.equals(variables.rootDir))
			dirEntries.add( 0, new SelectFileEntry( dir.getParentFile(), SelectFileEntry.Type.PARENT_DIR ));

		// Header-t is hozzáadjuk - legelejére kerül
		if ( dir.equals( variables.rootDir) )
			dirEntries.add( 0, new SelectFileEntry( null, SelectFileEntry.Type.HEADER ));
		else
			dirEntries.add( 0, new SelectFileEntry( dir, SelectFileEntry.Type.HEADER ));
		
		variables.positionOfFileSection = dirEntries.size();
		
		// Exportnál NEW File, megy a file-ok elé, directory-k után
		if ( isCreateAllowed() )
			dirEntries.add( new SelectFileEntry( null, SelectFileEntry.Type.NEW ));
		
		Collections.sort(fileEntries);
		dirEntries.addAll(fileEntries);
		
    	SelectFileAdapter adapter = new SelectFileAdapter(SelectFileActivity.this, dirEntries);
    	list.setAdapter(adapter);
    	
		adapter.getFilter().filter( filter.getText().toString() );
		Scribe.note("SelectFileActivity: FILTERED with " + filter.getText().toString() );

		// Ha nincs elmentett érték
    	if ( previousDir == null )
    		{
    		if ( variables.positionOfFirstItem != -1 )
	    		{
				list.setSelectionFromTop(variables.positionOfFirstItem, variables.topOfFirstItem);
				variables.positionOfFirstItem = -1;
	    		}
			else if ( filter.length() > 0 )
				{
				list.setSelectionFromTop( variables.positionOfFileSection, 0 );
				}
    		}
    	else // if (previousDir != null)
    		{
	    	for (int i=0; i < adapter.getCount(); i++)
	    		{
	    		if ( adapter.getItem(i).getType() == SelectFileEntry.Type.DIR && adapter.getItem(i).getFile().equals( previousDir ) )
	    			{
	    			list.setSelectionFromTop(i, 0);
	    			break;
	    			}
	    		if ( adapter.getItem(i).getType() == SelectFileEntry.Type.FILE )
	    			break;
	    		}
    		}
    	
    	variables.currentDir = dir;
    	}       	
    
	
    // Leálláskor elmentjük a lista pozícióját
    @Override
    public void onPause()
    	{
    	super.onPause();
    	
    	variables.positionOfFirstItem = list.getFirstVisiblePosition();
    	View v = list.getChildAt(0);
    	variables.topOfFirstItem = (v == null) ? 0 : v.getTop();
    	}
  
    // A root-könyvtárig (SD-card) ugyanaz, mint az előző könyvtár, ott viszont Cancel-ként üzemel 
	@Override
	public void onBackPressed() 
		{
		// igy vissza tudunk menni a root-dir-ig, de most nem akarunk
	   	//if ( !variables.currentDir.equals( variables.rootDir ) )
		//	{
		//    populateList( variables.currentDir.getParentFile(), variables.currentDir );
		//	}
		//else
			{
			//Intent returnIntent = new Intent();
			//setResult(RESULT_CANCELED, returnIntent);        
			finish();
			}
		}
	
	// Menü vezérlése
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
		{
		// Kihasználjuk, hogy CSAK új elemek hozzáadása van a menüben.
		// Ha ez nem engedélyezett, hozzá se adjuk
		if ( isCreateAllowed() )
			{
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.select_file_menu, menu);
			return true;
			}
		
		return false;
		}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) 	
		{
		switch (item.getItemId())
			{ 	
			case R.id.menu_new_file:
				{
				Scribe.note("SelectFileActivity: Menu CREATE FILE started");
				SelectFileDialog.showNewDialog( this, SelectFileDialog.Type.CREATE_FILE, filter.getText().toString());		
				return true; 	
				}
			case R.id.menu_new_directory:
				{
				Scribe.note("SelectFileActivity: Menu CREATE DIRECTORY started");
				SelectFileDialog.showNewDialog( this, SelectFileDialog.Type.CREATE_DIRECTORY, filter.getText().toString());
				return true; 	
				}
			default: 	
				return super.onOptionsItemSelected(item); 	 
			}
		}
	
	// Dialogusokhoz szükséges kommunikáció
	public void onDialogPositiveResult( SelectFileDialog.Type type, String text)
		{
		Scribe.note("Return from SelectFileActivity Dialogs:");
		switch (type)
			{
			case SD_CARD_ERROR:
				{
				Scribe.note("SD Card Error!");

				//Intent returnIntent = new Intent();
				//setResult( RESULT_CANCELED, returnIntent);        
				finish();
				break;
				}
			case CREATE_DIRECTORY:
				{
				Scribe.note("Create Directory");

				// write-permission szukseges
				File newDir = new File( variables.currentDir, text );
				if ( newDir.mkdirs() ) // akár alkönyvtárral legyártja
					{
					populateList( newDir, null);
					}
				else
					{
					Toast.makeText( this , this.getString(R.string.dialog_error_cannot_create) + " [" + text + "]", Toast.LENGTH_SHORT).show();
					SelectFileDialog.showNewDialog( this, SelectFileDialog.Type.CREATE_DIRECTORY, text);
					}
				break;
				}
			case CREATE_FILE: // Export to non-exsisting file
				{
				Scribe.note("Create File");
				
				File newFile = new File( variables.currentDir, text + getString( R.string.extension ) );
				returnSelectedFile( newFile );

				break;
				}
			}
		}

	protected void returnSelectedFile( File selectedFile )
		{
		String selectedDir;
		
		if ( selectedFile.getParent().startsWith( variables.rootDir.getParent()) );
			{
			selectedDir = selectedFile.getParent().substring( variables.rootDir.getAbsolutePath().length() );
			}
		
		Scribe.note("SelectFileActivity: selected FILE: " + selectedFile.getName() );
		Scribe.note("SelectFileActivity: selected DIR:  " + selectedDir );

		Intent returnIntent = new Intent();
		
		returnIntent.setData( Uri.fromFile(selectedFile) );
		
		returnIntent.putExtra( SELECTED_FILE, selectedFile.getAbsolutePath() );
		returnIntent.putExtra( SELECTED_FILE_NAME, selectedFile.getName() );
		returnIntent.putExtra( DIRECTORY_SUB_PATH, selectedDir );
		
		setResult(RESULT_OK, returnIntent);     
		finish();
		}

	}
