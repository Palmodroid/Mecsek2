package digitalgarden.mecsek.generic;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.tables.authors.AuthorsEditFragment;
import digitalgarden.mecsek.utils.Keyboard;
import digitalgarden.mecsek.utils.StringUtils;
import digitalgarden.mecsek.utils.Utils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.database;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


/*
 * A ListFragment-ek egy-egy teljes táblát mutatnak be. Működésük nagyrészt azonos, 
 * csak néhány helyen térnek el:
 * LOADER_ID
 * R.layout.row_view
 * from
 * to
 * content_uri
 * projection
 * limit
 * filter
 * order
 * delete
 * 
 * A Fragment-ek miatt ezeket CSAK üres konstruktorral lehet létrehozni. A működést
 * a GenericListFragment végzi, ebből származtatjuk az egyes tényleges ListFragment-eket.
 * Minden ListFragment tartalmaz egy newInstance() metódust.
 * Maga a GenericListFragment egy üres konstruktorral kerül létrehozásra. A szükséges részeket
 * abstract függvények és argumentum-ként átadott értékek adják meg.
 ??? http://www.heimetli.ch/java/create-new-instance.html *
 * Korábbi bejegyzés:
 * Template_ListFragment
 * 
 *  Listaként mutatja be a táblát. 
 *  Az activity-vel két úton kommunikál:
 *	- onItemEditing(long id); - ha egy elemet szerkesztésre választunk ki
 *  - onItemSelected(long id); - ha egy elemet visszaadásra választottunk ki
 *  
 *  Megjegyzés: a ListFragmentnek ismernie kell a "SELECT" értéket, mert ettől függően
 *  lép valamelyik irányban. Másrészt "SELECT" esetén a visszaadás történhetne közvetlenül a 
 *  fragment-ből is, de logikusabb visszatérni az activity-hez, és aztán az adja vissza az értéket.
 *  
 */


public abstract class GenericCombinedListFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener
	{
	/* 
	 * A kötelező elemek abstract metódusban,
	 * az opcionális paraméterek argument-ként kerülnek átadásra.
	 * Nem használhatunk hagyományos paramétereket, mert újraindításkor az üres konstruktor kerül meghívásra.
	 * 
	 * ((Ugyanezt Builder-rel is megoldhatnánk, de akkor az átadott paramétereket
	 * el kellene menteni. http://logout.hu/tema/android/hsz_1450-1453.html
	 */


    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnListReturnedListener
        {
        public void onItemEditing(long id);
        public void onItemEditing( long id, Class editClass );
        public void onItemSelected(long id);
        public void showActionButtonByList(boolean show );
        }


    /**
     * Header alkalmazása:
     * Header esetén az egyik foreignKey-re limitáljuk a listát
     * Ilyenkor a paraméterek között kapjuk meg a LIMITED_ITEM értéket, vagyis mire limitáljuk a
     * listát
     */
    protected abstract class Header
        {
        protected abstract int defineTableIndex();
        protected abstract int defineRowLayout(); // A header sor megjelenítéséhez szükséges Layout
        protected abstract void setupRowLayout();
        }


    // Ha értéke nem SELECT_DISABLED, akkor:
    // - a listát megjelenésekor erre az elemre pörgeti (rollToSelectedItem())
    // - rövid érintésre a kiválasztott elemet nem editálja, hanem visszaadja
    public static final String SELECTED_ITEM = "selected item";
    public static final long   SELECTED_NONE = -1L;
    public static final long   SELECT_DISABLED = -2L;

    // CSAK LIMITED_COLUMN-nal együtt értelmezhető!
    // Csak azokat az elemeket listázza, ahol LIMITED_COLUMN-ban LIMITED_ITEM érték szerepel
    public static final String LIMITED_COLUMN = "limited col";
    public static final String LIMITED_ITEM = "limited item";

    // A filtert erre a kifejezésre (ált. oszlop-név) alkalmazza
    public static final String FILTERED_COLUMN = "filtered col";

    // A rendezést ezen oszlop alapján végzi el
    public static final String ORDERED_COLUMN = "ordered col";


    @SuppressWarnings("unchecked")
    protected void addField(int fieldId, int columnIndex)
        {
        projection[rowType].add( columnFull( columnIndex ));
        from[rowType].add( column( columnIndex ));
        to[rowType].add( fieldId );
        }


    @SuppressWarnings("unchecked")
    protected void addIdField()
        {
        projection[rowType].add( columnFull_id(
                rowType == LIST_ROW ? defineTableIndex() : header.defineTableIndex()));
        from[rowType].add( column_id());
        to[rowType].add( R.id.id );
        }


    private Header header = null;

    // Az activity-vel történő kommunikáció miatt szükséges részek
    private OnListReturnedListener onListReturnedListener;

    private final static int HEADER_ROW = 0;
    private final static int LIST_ROW = 1;

    private ArrayList[] projection = { new ArrayList<String>(), new ArrayList<String>() };
    private ArrayList[] from = { new ArrayList<String>(), new ArrayList<String>() };
    private ArrayList[] to = { new ArrayList<Integer>(), new ArrayList<Integer>() };

    private int rowType = LIST_ROW;

    // HeaderCursort a fragment kezeli, ezért új letöltés előtt, vagy a legvégén be kell zárni!!
    private Cursor headerCursor = null;

    private GenericCombinedCursorAdapter globalAdapter;

    // Ez a teljes rész a filter miatt kell, egyébként a ListFragment is tartalmaz egy gyári ListView-t
    private EditText filter;

    private long limitedItem = -1L;
    private String limitedColumn;

    // Ha rollToSelectedItem == TRUE, akkor a betöltött adatbázist végignézi, és a kiválasztott elemet megjeleníti
    // Ha a rollToSelectedItem() utasítást közvetlenül a létrehozás után adjuk ki, akkor csak az első indításkor mutatja
    // a kiválasztott elemet, később már nem
    private boolean rollToSelectedItem = false;


    // A tábla index, a következő két adathoz.
    protected abstract int defineTableIndex();

    // Az elemek megjelenítéséhez szükséges Layout
    protected abstract int defineRowLayout();

    protected abstract void setupRowLayout();

    // Példák beszúrása
    protected abstract void addExamples();

    // Header is not obligatory
    protected Header defineHeader()
        {
        return null;
        }


    /**
     * To use header and limit to header both header and limited item (header) is needed
     * @return true, if limit to header
     */
    protected boolean isHeaderDefinied()
        {
        return header != null && limitedItem >= 0L;
        }


    // LoaderId - egyedi érték, a tábla azonosítója is megfelel. (tabla.TABLEID)
	// Elvileg a LIMIT-hez egyedi érték kellene. De mégis megy nélküle Hm.
    protected int getLoaderId()
        {
        return table( defineTableIndex() ).id();
        }


	// A megjelenítendő tábla URI-ja. (tabla.CONTENT_URI)
	protected Uri getContentUri()
        {
        return table( defineTableIndex() ).contentUri();
        }


	@Override
    public void onAttach(Activity activity) 
    	{
        super.onAttach(activity);

        try
        	{
        	onListReturnedListener = (OnListReturnedListener) activity;
        	} 
        catch (ClassCastException e) 
        	{
            throw new ClassCastException(activity.toString() + " must implement OnListReturnedListener");
        	}
    	}


	@Override
    public void onDetach() 
    	{
        super.onDetach();

        onListReturnedListener = null;
    	}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
		{
		Scribe.note("General LIST Fragment: onCreateView");

		// A LAYOUT MINDBEN UGYANAZ!!!! 
        View view = inflater.inflate(R.layout.general_list_fragment, container, false);
        
        filter = (EditText) view.findViewById(R.id.filter);
        filter.addTextChangedListener(new TextWatcher() 
        	{
	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) 
	        	{
	        	if (getActivity() != null)
	        		{
	        		LoaderManager.getInstance(getActivity()).restartLoader(getLoaderId(), null,
							GenericCombinedListFragment.this);
	        		Scribe.note("Filter text was changed!");
	        		}
	        	else
	        		// Ide sohase jut el...
	        		Scribe.note("Filter text was changed, ACTIVITY IS MISSING!!!");
	        	}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {}
        	});

        return view;
		}


    @Override
	public void onActivityCreated(Bundle savedInstanceState) 
		{
    	super.onActivityCreated(savedInstanceState);

    	Scribe.note("General LIST Fragment: onActivityCreated");
    	
    	// Itt kell jelezni, ha a Fragment rendelkezik menüvel
    	setHasOptionsMenu(true);
    	// setEmptyText("Database empty"); // Custom View esetén nem használható !!

        rowType = LIST_ROW;
        setupRowLayout();

		globalAdapter = new GenericCombinedCursorAdapter(
				getActivity(), 
				defineRowLayout(),
                (String[]) from[LIST_ROW].toArray(new String[0]),
                Utils.convertToIntArray(to[LIST_ROW]),
				getArguments().getLong( SELECTED_ITEM , SELECT_DISABLED )
				);

        // This was not obvious
        // String[] stringarray = (String[]) from[1].toArray(new String[0]);

        limitedItem = getArguments().getLong( LIMITED_ITEM, -1L );
        limitedColumn = getArguments().getString( LIMITED_COLUMN );

        header = defineHeader();

        if ( isHeaderDefinied() )
            {
            rowType = HEADER_ROW;
            header.setupRowLayout();

            globalAdapter.setHeaderRow( header.defineRowLayout(),
                    (String[]) from[HEADER_ROW].toArray(new String[0]),
                    Utils.convertToIntArray(to[HEADER_ROW]));
            }

		setListAdapter(globalAdapter);
		
		// http://stackoverflow.com/questions/6732611/long-click-on-listfragment
		getListView().setOnItemLongClickListener( this );
		
		getListView().setOnTouchListener( new OnTouchListener()
			{
			@Override
			public boolean onTouch(View v, MotionEvent event)
				{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                    Keyboard.hide(getActivity());
                    }
                else if (event.getAction() == MotionEvent.ACTION_MOVE)
                    {
                    onListReturnedListener.showActionButtonByList( false );
                    }
				else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                    {
                    // Az a baj, hogy akkor is megmutatja, ha már editView-ben vagyunk
                    onListReturnedListener.showActionButtonByList( true );
                    }
				return false;
				}
			});
		}

    @Override
    public void onResume()
        {
        super.onResume();

        // It should set headerCursor as well in onLoadFinished
        LoaderManager.getInstance(getActivity()).initLoader( getLoaderId(), null, this);

        // authorsObserver = new AuthorsObserver(null );
        // getActivity().getContentResolver().registerContentObserver(Uri.parse( table(AUTHORS)
        // .contentUri() + "/" + limitedItem), true,
        //        authorsObserver);
        }

    @Override
    public void onPause()
        {
        super.onPause();

        // We should close headerCursor, if it was active; onResume should reopen it
        if ( headerCursor != null )
            headerCursor.close();
        }

    // Creates a new loader after the initLoader () call
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
		{
		Scribe.note("onCreateLoader (Query) started");
		
		String filterClause = "";
		String[] filterStrings = null;
		
		String filterString = StringUtils.normalize( filter.getText().toString() );
		if (filterString.length() > 0)
			{
			String[] filteredColumns = getArguments().getStringArray( FILTERED_COLUMN );
			if ( filteredColumns != null && filteredColumns.length > 0 )
				{
				filterStrings = new String[filteredColumns.length];
				StringBuilder filterClauseBuilder = new StringBuilder(" ( ");
				for (int n=0; n < filteredColumns.length; n++)
					{
					if (n != 0)
						filterClauseBuilder.append(" or ");
					filterClauseBuilder.append( filteredColumns[n] );
					filterClauseBuilder.append(" like ? ");
				
					filterStrings[n] = "%" + filterString + "%";
					}
				filterClauseBuilder.append(" ) ");
				filterClause = filterClauseBuilder.toString();
				}
			}
		Scribe.note("onCreateLoader (Query) filter clause: [" + filterClause + "], filter string: [" + filterString + "]");
		
		String limitClause = "";

		// limitedItem és limitedColumn should be class variables beacause of header

		if ( limitedItem >= 0L && limitedColumn != null )
			{
			limitClause = limitedColumn + " = " + limitedItem;
			}
		Scribe.note("onCreateLoader (Query) limit clause: [" + limitClause + "]");

		String and = ( limitClause.length() > 0 && filterClause.length() > 0 ) ? " and " : "";
		
		String orderClause = "";
		String orderedColumn = getArguments().getString( ORDERED_COLUMN );
		if ( orderedColumn != null )
			{
			orderClause = orderedColumn + " COLLATE LOCALIZED ";
			}
		Scribe.note("onCreateLoader (Query) order clause: [" + orderClause + "]");
		
		// http://code.google.com/p/android/issues/detail?id=3153
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
                getContentUri(),
                (String[]) projection[LIST_ROW].toArray(new String[0]),
				filterClause + and + limitClause, 
				filterStrings, //new String[] { "%"+filterString+"%" }, // ha nincs filterClause, akkor nem használja fel
				orderClause );

		return cursorLoader;
		}

	public void rollToSelectedItem()
		{
		rollToSelectedItem = true;
		}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
		{
		Scribe.note("onLoadFinished (Query finished)");

		// Kiszélesítjük az URI-t a teljes adatbázisra, hogy a kereszthivatkozásokat is figyelje
        // Érdekes:
        // Loader a Cursor-t figyeli, Cursor az URI-t
        if ( table(defineTableIndex()).containsForeignReference() )
            {
            cursor.setNotificationUri(getActivity().getContentResolver(), database().contentUri());
            }

		globalAdapter.setListCursor(cursor);
		if (isHeaderDefinied())
             {
             // Ebben az a lényeg, hogy a Loader minden változásnál lefut, akkor lekérjük ezt az
             // egy elemet is. Nem alkotunk hozzá még egy loader-t
             pullHeaderCursor( limitedItem );
             globalAdapter.setHeaderCursor( headerCursor );
             }
		//globalAdapter.swapCursor(data);

		// Ha van kiválasztott elem, akkor itt kikeressük. Ha getCount túl nagy, akkor ezt letilthatjuk
		// Meg kéne nézni, nincs-e erre lehetőség egy saját Loader-segítségével
		if ( rollToSelectedItem )
			{
	    	long selectedItem = getArguments().getLong( SELECTED_ITEM, SELECTED_NONE );

			if ( selectedItem >= 0L )
				{
				for (int n=0; n<globalAdapter.getCount(); n++)
					{
					if (globalAdapter.getItemId(n) == selectedItem)
						{
						Scribe.note("Roll to " + selectedItem + ", position: " + n);
						getListView().setSelectionFromTop(n, 0);
						break;
						}
					}
				}
			rollToSelectedItem = false; // Már kiválasztottuk, többször nem kell
			}
    	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
		{
		// data is not available anymore, delete reference
		Scribe.note("onLoaderReset");

		globalAdapter.setHeaderCursor( null );

		globalAdapter.setListCursor( null );
		//globalAdapter.swapCursor(null);
		}


    // EZT AZ EDITEDBOL VETTÜK
    private void pullHeaderCursor( long rowIndex )
        {
        if ( headerCursor != null )
            {
            headerCursor.close();
            }

        Uri itemContentUri =
                Uri.parse(table( header.defineTableIndex()).contentUri() + "/" + rowIndex);

        //https://stackoverflow.com/questions/4042434/converting-arrayliststring-to-string-in-java
        headerCursor = getActivity().getContentResolver().query(
                    itemContentUri,
                    (String[]) projection[HEADER_ROW].toArray(new String[0]),
                    null, null,null );

        // nem kell-e null-t ellenőrizni, ill. kell-e mozgatni?
        // nem kell, mert majd ő bemozgatja. A null-t lehet, hogy nézni kellene, de elvileg csak
        // egy létező item-ről juthatunk ide, delete meg nincs
        // headerCursor.moveToFirst();
        }


    @Override
	public void onListItemClick (ListView listView, View view, int position, long id)
		{
		Scribe.note("List item " + id + " was SHORT clicked");

		if ( id < 0L )  // !!! A header-t negatív előjellel adja visszaű
            {
            onListReturnedListener.onItemEditing(-id, AuthorsEditFragment.class );
            // globalAdapter.setEditedItem(id);
            }

		else if (getArguments().getLong( SELECTED_ITEM , SELECT_DISABLED ) != SELECT_DISABLED)
			onListReturnedListener.onItemSelected(id);
		
		//getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //getListView().setItemChecked(position, true)
		else
			{
			onListReturnedListener.onItemEditing(id);
			globalAdapter.setEditedItem(id);
			}
		}
 

    // http://stackoverflow.com/questions/6732611/long-click-on-listfragment
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
		{
		Scribe.note("List item " + id + " was LONG clicked");
		onListReturnedListener.onItemEditing(id); 
		globalAdapter.setEditedItem(id);

		return true;
		}
    
	public void editFinished( long rowId )
		{
		globalAdapter.clearEditedItem();

        // Ezt lehet, hogy a másik oldalra (activity) kellene tenni.
        // De ahhoz a SELECT értéket is az activitynek kellene megkapnia
        // PRÓBAKÉNT betettük az onFinished() részbe
        //if (rowId >= 0 && getArguments().getLong( SELECTED_ITEM , SELECT_DISABLED ) != SELECT_DISABLED)
        //    onListReturnedListener.onItemSelected(rowId);
        }

	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
		{
		super.onCreateOptionsMenu(menu, inflater);
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.general_list_menu, menu);
		}
		
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    	{
        switch (item.getItemId()) 
        	{
        	case R.id.menu_add:
        		Scribe.note("General LIST Fragment menu: ADD started");
				try
					{
					onListReturnedListener.onItemEditing(-1L); 
					}
				catch (Exception e)
					{
					Toast.makeText(getActivity(), "ERROR: Add item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
					}
	    		return true;

        	case R.id.menu_delete_all:
        		Scribe.note("General LIST Fragment menu: DELETE_ALL started");
				try
					{
    	        	getActivity().getContentResolver().delete( getContentUri(), null, null);
					}
				catch (Exception e)
					{
					Toast.makeText(getActivity(), "ERROR: Delete all (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
					}
	    		return true;
	    		
        	case R.id.menu_example:
        		Scribe.note("General LIST Fragment menu: EXAMPLE INSERTS started");
				try
					{
					addExamples();
					}
				catch (Exception e)
					{
					Toast.makeText(getActivity(), "ERROR: Example (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
					}
	    		return true;

        	default:
            	return super.onOptionsItemSelected(item);
	        }
	    }
    
	}

