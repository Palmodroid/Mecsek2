package digitalgarden.mecsek.generic;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import digitalgarden.mecsek.utils.Keyboard;
import digitalgarden.mecsek.utils.StringUtils;
import digitalgarden.mecsek.utils.Utils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
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

    // Ha értéke nem SELECT_DISABLED, akkor:
    // - a listát megjelenésekor erre az elemre pörgeti (rollToSelectedItem())
    // - rövid érintésre a kiválasztott elemet nem editálja, hanem visszaadja
    public static final String SELECTED_ITEM = "selected item";
    public static final long SELECTED_NONE = -1L;
    public static final long SELECT_DISABLED = -2L;

    // CSAK LIMITED_COLUMN-nal együtt értelmezhető!
    // Csak azokat az elemeket listázza, ahol LIMITED_COLUMN-ban LIMITED_ITEM érték szerepel
    public static final String LIMITED_COLUMN = "limited col";
    public static final String LIMITED_ITEM = "limited item";

    // A filtert erre a kifejezésre (ált. oszlop-név) alkalmazza
    public static final String FILTERED_COLUMN = "filtered col";

    // A rendezést ezen oszlop alapján végzi el
    public static final String ORDERED_COLUMN = "ordered col";

    // A tábla index, a következő két adathoz.
    protected abstract int defineTableIndex();

    // LoaderId - egyedi érték, a tábla azonosítója is megfelel. (tabla.TABLEID)
    protected int getLoaderId()
        {
        return table( defineTableIndex() ).id();
        }

    // A megjelenítendő tábla URI-ja. (tabla.CONTENT_URI)
    protected Uri getContentUri()
        {
        return table( defineTableIndex() ).contentUri();
        }

    // Az elemek megjelenítéséhez szükséges Layout
    protected abstract int defineRowLayout();

    protected abstract void setupRowLayout();

    private ArrayList<String> projection = new ArrayList<>();
    private ArrayList<String> from = new ArrayList<>();
    private ArrayList<Integer> to = new ArrayList<>();

    protected void addField( int fieldId, int columnIndex)
        {
        projection.add( columnFull( columnIndex ));
        from.add( column( columnIndex ));
        to.add( fieldId );
        }

    protected void addIdField()
        {
        projection.add( columnFull_id( defineTableIndex() ));
        from.add( column_id());
        to.add( R.id.id );
        }

    // Példák beszúrása
    protected abstract void addExamples();

    // Az activity-vel történő kommunikáció miatt szükséges részek
    OnListReturnedListener onListReturnedListener;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnListReturnedListener
        {
        public void onItemEditing(long id);
        public void onItemSelected(long id);
        public void showActionButtonByList(boolean show );
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


    private GenericCursorAdapter globalAdapter;

    // Ez a teljes rész a filter miatt kell, egyébként a ListFragment is tartalmaz egy gyári ListView-t
    private EditText filter;

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
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                // TODO Auto-generated method stub
                }

            @Override
            public void afterTextChanged(Editable s)
                {
                // TODO Auto-generated method stub
                }
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

        setupRowLayout();

        globalAdapter = new GenericCursorAdapter(
                getActivity(),
                defineRowLayout(),
                null,
                from.toArray(new String[0]),
                Utils.convertToIntArray(to),
                0,
                getArguments().getLong( SELECTED_ITEM , SELECT_DISABLED )
        );

        setListAdapter(globalAdapter);

        LoaderManager.getInstance(getActivity()).initLoader( getLoaderId(), null, this);

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
        long limitedItem = getArguments().getLong( LIMITED_ITEM, -1L );
        String limitedColumn = getArguments().getString( LIMITED_COLUMN );
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
                projection.toArray(new String[0]),
                filterClause + and + limitClause,
                filterStrings, //new String[] { "%"+filterString+"%" }, // ha nincs filterClause, akkor nem használja fel
                orderClause );

        return cursorLoader;
        }

    // Ha rollToSelectedItem == TRUE, akkor a betöltött adatbázist végignézi, és a kiválasztott elemet megjeleníti
    // Ha a rollToSelectedItem() utasítást közvetlenül a létrehozás után adjuk ki, akkor csak az első indításkor mutatja
    // a kiválasztott elemet, később már nem
    private boolean rollToSelectedItem = false;

    public void rollToSelectedItem()
        {
        rollToSelectedItem = true;
        }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
        Scribe.note("onLoadFinished (Query finished)");

        globalAdapter.swapCursor(data);

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

        globalAdapter.swapCursor(null);
        }


    @Override
    public void onListItemClick (ListView listView, View view, int position, long id)
        {
        Scribe.note("List item " + id + " was SHORT clicked");

        if (getArguments().getLong( SELECTED_ITEM , SELECT_DISABLED ) != SELECT_DISABLED)
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

