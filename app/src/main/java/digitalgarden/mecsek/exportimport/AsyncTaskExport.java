package digitalgarden.mecsek.exportimport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.generic.database.GenericTable;

import static digitalgarden.mecsek.database.DatabaseMirror.allTables;
import static digitalgarden.mecsek.database.DatabaseMirror.database;


/**
 * Az exportálási folyamatot egy AsyncTask végzi, melyet az AsyncTaskDialogFragment hív majd meg.
 * A collateRows() rész minden egyes táblát lekérdez, és a lekérdezés eredményét a tábla saját
 * exportImport osztályában tárolja el. Ennek során az is kiderül, hogy hány sort kell exportálnunk.
 * !! Lehet, hogy ez is nagyon hosszú lesz, ha igen, majd gondolkodunk a megjelnítésen.
 * A következő lépésben minden egyes tábla minden egyes - már eltárolt - sorát elkérjük szövegesen
 * (getNextRow()), és kiírjuk a tábla nevével együtt egy file-ba.
 * Végül minden egyes tábla cursor-át be kell zárnunk.
 */
class AsyncTaskExport extends GenericAsyncTask
	{
	// Átadott adatok
	File outputFile;
	
	protected AsyncTaskExport(AsyncTaskDialogFragment asyncTaskDialogFragment, File outputFile)
		{
		super(asyncTaskDialogFragment);
		Scribe.note("AsyncTaskEXPORT to " + outputFile.getName());
		
		this.outputFile = outputFile;
		}
	
	// Indítás előtt elvégzendő előkészítések
	// onPreExecute kimarad, mert:
	// az adatbázislekérdezés (lassú lehet) is háttérszálra került
	// az elemszám kiszámítása is ott történik
    
	// A tényleges, háttérben zajló feladat
	// UI-szál elérése TILOS!
	@Override
	protected Void doInBackground(Void... params) 
		{
		if ( !isRunning() )
			return null;

		// Elkérjük az adatokat, ezt majd a finally-ban zárjuk le
        int count = 0;
        for ( GenericTable table : allTables() )
            {
            count += table.exportImport().collateRows();
            }

		// Itt állítjuk be a progress végértékét a 2. paraméter használatával
		int cnt = 0;
		publishProgress( cnt, count );

    	if ( count == 0 )
    		{
    		// Üres az adatbázis, de végigfutunk, és a feljlécet kiírjuk
   			setReturnedMessage( R.string.msg_error_database_empty);
    		}

		// http://stackoverflow.com/questions/15799157/is-it-overkill-to-use-bufferedwriter-and-bufferedoutputstream-together
		BufferedWriter bufferedWriter = null;
		try
			{
			bufferedWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( outputFile ), "UTF-8" ) );
			
			bufferedWriter.append( database().name() );
			bufferedWriter.append('\t');
			bufferedWriter.append( Integer.toString( database().version() ) );
			bufferedWriter.append('\t');
			
			SimpleDateFormat sdf=new SimpleDateFormat( "yy-MM-dd (EEE) HH:mm", Locale.US );	
			bufferedWriter.append( "exported on " + sdf.format(new Date()) );
			bufferedWriter.append('\n');
			
			String data;
			
			// http://stackoverflow.com/questions/10723770/whats-the-best-way-to-iterate-an-android-cursor
            loopOfTables:
            for ( GenericTable table : allTables() )
                {
                while ( (data=table.exportImport().getNextRow()) != null )
                    {
                    Scribe.note("AsyncTaskEXPORT exporting: " + data);
                    // http://stackoverflow.com/questions/5949926/what-is-the-difference-between-append-and-write-methods-of-java-io-writer
                    bufferedWriter.append( data );

                    publishProgress( ++cnt );

                    if (isCancelled())
                        break loopOfTables;
                    }
                }

            bufferedWriter.flush();
			}
		catch (IOException ioe)
			{
	   		setReturnedMessage( R.string.msg_error_io + ioe.toString());
			}
		finally 
			{
            // Always close the cursor
            for ( GenericTable table : allTables() )
                {
                table.exportImport().close();
                }

			if (bufferedWriter != null) 
				{
				try 
					{
					bufferedWriter.close();
					}
				catch (IOException ioe)
					{
					Scribe.note("ERROR IN CLOSE (AsyncTaskExport) " + ioe.toString());
					}
				}
			}
		return null;
		}      
	}   
