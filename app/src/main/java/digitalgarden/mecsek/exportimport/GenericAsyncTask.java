package digitalgarden.mecsek.exportimport;

import android.content.Context;
import android.os.AsyncTask;

import digitalgarden.mecsek.R;

class GenericAsyncTask extends AsyncTask<Void, Integer, Void>
	{
	// átadott adatok - majd a leszármazottakban lesznek
	
	// callerFragment-en keresztül férünk hozzá a felhasználói felülethez
	// a setRetainInstance(true) miatt ennek mindig van értéke
	protected AsyncTaskDialogFragment callerFragment;
	
	// Az applicationContext NEM VÁLTOZIK a program futása alatt, 
	// (a resource-ok eléréséhez kell)
	// de lekérdezni csak az elején biztonságos
	protected Context applicationContext;

	protected GenericAsyncTask(AsyncTaskDialogFragment asyncTaskDialogFragment )
		{
		callerFragment = asyncTaskDialogFragment;
		applicationContext = asyncTaskDialogFragment.getActivity().getApplicationContext();
		}
	
	
	// returnedMessage == null a végrehajtás alatt
	// hiba vagy befejezés esetén értéket kap
	private String returnedMessage = null;

	protected boolean isRunning()
		{
		return returnedMessage == null;
		}
	
	protected String getReturnedMessage()
		{
		return returnedMessage;
		}
	
	protected void setReturnedMessage(String message)
		{
		returnedMessage = message;
		}
	
	protected void setReturnedMessage(int resId)
		{
		returnedMessage = applicationContext.getString( resId );
		}
	
	// Ez akkor lesz pozitív, ha a felhasználó szempontjából vége:
	// felhasználói megszakítás v. sikeres befejezés
	private boolean taskFinished = false;
	
	protected boolean isTaskFinished()
		{
		return taskFinished;
		}
	
	
	// Indítás előtt elvégzendő előkészítések
    @Override
    protected void onPreExecute() 
    	{
    	super.onPreExecute();

    	//callerFragment.setProgressMax( 100 );
   		//callerFragment.updateLayout();
  	     	
    	/* Hiba esetén:
    	if (error)
    		{
   			setReturnedMessage( R.string.msg_error );
     		return;
    		}
    	*/
    	}

    
	// A tényleges, háttérben zajló feladat
	// UI-szál elérése TILOS!
	@Override
	protected Void doInBackground(Void... params) 
		{
		if ( !isRunning() )
			return null;
		
		for (int i=1; i <= 100; i++)
			{
			try
				{
				Thread.sleep( 100L );
				} 
			catch (InterruptedException e)
				{
	   			setReturnedMessage( R.string.msg_error );
				break;
				}

			publishProgress(i);

			if (isCancelled())
				break;
			}
		
		return null;
		}      
	
    // Végrehajtás után - a háttérszálat megtartjuk, de felajánljuk az újraindítás lehetőségét
    @Override
    protected void onPostExecute(Void result) 
    	{
    	super.onPostExecute(result);
    	
    	if ( isRunning() )
			{
    		setReturnedMessage( R.string.msg_ready );
			taskFinished = true;
			}
   		callerFragment.updateLayout();
    	}
    
    // Megszakítás után - felállunk újraindításhoz. Ha már nincs meg a hívó Activity, akkor az új példány már így indul
	@Override
	protected void onCancelled()
		{
		super.onCancelled();
		
		setReturnedMessage( R.string.msg_cancel );
		taskFinished = true;
   		callerFragment.updateLayout();
		}
	
	// Tájékoztatás futás közben
    @Override
    protected void onProgressUpdate(Integer... progress) 
    	{
		super.onProgressUpdate( progress );
		
		if ( progress.length > 1 )
			{
	    	callerFragment.setProgressMax( progress[1] );
	   		callerFragment.updateLayout();
			}
		
   		callerFragment.setProgress( progress[0] );
    	}
	}   
