package digitalgarden.mecsek.generic;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.formtypes.EditField;
import digitalgarden.mecsek.formtypes.ExternKey;
import digitalgarden.mecsek.formtypes.ForeignKey;
import digitalgarden.mecsek.formtypes.SourceFieldButton;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.DatabaseMirror.table;

/**
 * [Main] Table -  a "fő" tábla, mely idegen (Foreign) táblákra hivatkozhat
 * Foreign Key -   a "fő" táblában lévő column, mely az idegen tábla egy adatsorára mutat.
 * Foreign Table - az idegen tábla. Ennek egy sorát jelöli ki a Key.
 * FONTOS!! A ForeignKey egy adatsort mutat meg, de nem tartalmazza a ForeignTable-t!
 * (Ezt csak az adatbázis struktúrájából ismerjük.)
 *
 * Az EDIT Fragment egy konkrét adatsor szerkesztését végzi. A View mezőit feltölti a kiválasztott
 * adatsor adataival, ill. a szerkesztés befejezésekor (UPDATE, ADD) a megváltozott mezőket vissza-
 * tölti az adatsorba.
 * Adatok előemelése és a mezők kitöltése: PULL DATA
 * Adatok visszahelyezése a mezők alapján: PUSH DATA
 *
 * EditField - EditFieldText vagy EditFieldDate
 *      összeköt egy EditText mezőt egy Text típusú oszloppal
 * ForeignTextField -
 *      összeköt egy TextView mezőt egy Text típusú oszloppal, ForeignKey alapján
 *      A mező értéke a ForeignKey által kiválasztott adatsorból származik; közvetlenül nem
 *      változtatható meg. Érintésre a ForeignKey Selector-a indul el.
 * ForeignKey -
 *      Ténylegesen nem jelenik meg a mezőkben. A ForeignKey oszlop adatsorának megfelelő érték.
 *      Ennek alapján kerülnek kiválasztásra a ForeignTextField-ek
 *      Beállít egy Selector-t (LIST), ami segít kiválasztani a megfelelő sort.
 *
 * ??? Hol definiáljuk a Foreign Table-t, ill. egyáltalán van-e rá szükség ???
 * Igen, kell; mert nem a kapcsolt, hanem csak az idegen táblából keresi ki a megfelelő értéket.
 * Talán logikusabb a ForeignTextField-ben megadni, mint tábla-oszlop párt.
 * ^^^^
 * Ez lesz a megoldás!
 *
 * A Selector is ismeri a Foreign Table-t. (Mivel több selector is lehet, ezért a Table nem ismeri
 * a selectort) Ennek alapján viszont felesleges a ForeignTable definiálása, mert a Selector tartalmazza.
 * ??? Vajon lehet olyan helyzet, hogy nem kell Selector ???
 *
 * A msik probléma, hogy a Selector egy Activity, amiből a tábla csak akkor érhető el, ha van példánya.
 * Vagy static-nak kell definiálni, de akkor meg nem tudjuk kikényszeríteni a létrehozását, hacsak nem
 * egy throw utasítással.
 *
 */
public abstract class GenericEditFragment extends Fragment
    {
    // Ha EDIT befejezte a ténykedését, akkor mindenképp itt tér vissza
    // Most csak az ADD adja vissza a létrehozott item id-jét, hogy
    // SELECT mode-ban kiléphessünk. A többi -1L lesz
    public interface OnFinishedListener
        {
        public void onFinished( long rowId );
        }


    public final static String EDITED_ITEM = "edited item";
    public final static long NEW_ITEM = -1L;

    // ListFragment át tudja adni a limited értékeit, és akkor azt kapásból kitölthetjük
    public final static String LIMITED_COLUMN = "limited col";
    public final static String LIMITED_ITEM = "limited item";

    private ForeignKey limitedForeignKey = null;


    private Connection connection;

    private ArrayList<ForeignKey> foreignKeys = new ArrayList<>();

    private View view;

    // A szerkesztés végén ide térünk vissza
    OnFinishedListener onFinishedListener;

    // Az egyes UI elemeket tartalmazó változók
    private Button buttonAdd;
    private Button buttonUpdate;
    private Button buttonList;
    private Button buttonCancel;

    private AlertDialog confirmationDialog;

    // Az osztály példányosítása után a getCode() minden egyes meghívásra új értéket ad vissza
    // Ez teszi lehetővé, hogy a ForeignKey miatti Activity hívások request kódja mindig megfeleljen
    private int codeGenerator = 0;

    // http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
    // alapján egy custom view is elmentheti az állapotát. (Még nem dolgoztam ki)
    // DE! Itt nem a customView-t, hanem a ForeignKey-t kell elmenteni, erre szolgál ez a két metódus
    //protected abstract void saveForeignKeyData(Bundle data);
    //protected abstract void retrieveForeignKeyData(Bundle data);
    // Ld. fent

    // ForeignKey-eket kell végigellenőrizni, melyikhez tartozó ForeignTextField adta ki az Activity hívást
//    protected abstract void checkReturningSelector( int requestCode, long selectedId );

    // edited: értéke true-ra vált, ha valamelyik column-et módosítottuk
    // setEdited beállítja, isEdited lekérdezi. Törlés szükségtelen, hiszen nem vonjuk vissza a módosításokat
    // a beállítást az egyes Field-ek végzik el.
    // Arra vigyázni kell, hogy a felhasználó csak onResumed állapotban állíthat be értéket
    // viszont a meghívott Activity más állapotban is visszatérhet!
    // private boolean edited = false;


    // Az űrlapot két adat azonosítja:
    // - a tábla neve: getTableContentUri()
    // - a sor azonosítója: getItemId() ((NEW_ITEM, ha üres űrlapról van szó))
    // !! Ez mindig konstans egy adott űrlapnál/EditFragment-nél!!
    // A konkrét sorra a getItemContentUri()-val is hivatkozhatunk

    /**
     * @return index of the table
     */
    public abstract int defineTableIndex();

    // Leszármazottak által biztosított metódusok
    // A beépítésre kerülő űrlap azonosítóját adja vissza
    protected abstract int defineFormLayout();

    // Az űrlap mezőinek megfelelő objektumok itt kerülnek létrehozásra, ill. összekapcsolásra az adatbázissal
    protected abstract void setupFormLayout();


    public long getRowIndex()
        {
        // Ez korábban egy külső változó is azonosította, így munkásabb, de nincs külön hivatkozás
        Bundle args = getArguments();
        return (args != null) ? args.getLong(EDITED_ITEM, NEW_ITEM) : NEW_ITEM;
        }

    // Ehelyett jobb lenne vmi összehasonlítás egy függvényben, ami a hint értéket adja vissza
    public long getLimitedItem()
        {
        Bundle args = getArguments();
        return (args != null) ? args.getLong(LIMITED_ITEM, NEW_ITEM) : NEW_ITEM;
        }

    public int getLimitedColumn()
        {
        Bundle args = getArguments();
        return (args != null) ? args.getInt(LIMITED_COLUMN, -1) : -1;
        }


    public EditField addEditField(int editFieldId, int columnIndex )
        {
        EditField editField = (EditField) view.findViewById( editFieldId );
        editField.connect( this, columnIndex );
        connection.add( editField );

        return editField;
        }

    public EditField addEditField( int editFieldId, int columnIndex, String hintKey )
        {
        EditField editField = addEditField( editFieldId, columnIndex );
        editField.setHint( getArguments(), hintKey );

        return editField;
        }


    public ForeignKey addForeignKey( int foreignKeyIndex, int foreignTableIndex,
                                     Class<?> selectorActivity, String selectorTitle, TextView selectorTitleOwner )
        {
        ForeignKey foreignKey = new ForeignKey( this, foreignKeyIndex, foreignTableIndex );
        foreignKey.setupSelector( selectorActivity, selectorTitle, selectorTitleOwner );

        foreignKeys.add( foreignKey );
        connection.add( foreignKey );

        if (foreignKeyIndex == getLimitedColumn())
            {
            limitedForeignKey = foreignKey;
            }

        return foreignKey;
        }


    public ExternKey addExternKey(int externKeyIndex, int externTableIndex )
        {
        ExternKey externKey = new ExternKey( this, externKeyIndex, externTableIndex );
        connection.add( externKey );

        return externKey;
        }

    public SourceFieldButton addSourceField( int sourceFieldId )
        {
        SourceFieldButton sourceField = (SourceFieldButton) view.findViewById( sourceFieldId );
        sourceField.connect( this );
        connection.add( sourceField );

        return sourceField;
        }

    /**
     * onAttach()  onCreate()  onCreateView()  onActivityCreated()  onStart() onResume()
     * onDetach()  onDestroy() onDestroyView()                      onStop()  onPause()
     **/


    @Override
    public void onAttach(Context context)
        {
        super.onAttach(context);

        try
            {
            onFinishedListener = (OnFinishedListener) context;
            }
        catch (ClassCastException e)
            {
            throw new ClassCastException(context.toString() + " must implement OnFinishedListener");
            }
        }


    // Az űrlapot defineFormLayout() alapján illeszti be
    // Az űrlap mezőkkel történő összekapcsolását setFormLayout() végzi el. (Ezt megelőzően codeGenerator-t nullázuk!)
    // Az egyes mezők viszont NEM itt kapnak alapértéket!!
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
        Scribe.note("General EDIT Fragment onCreateView");

        // Az alapvető layout egy "stub"-ot tart fenn a form számára. Ezt itt töltjük fel tartalommal
        view = inflater.inflate(R.layout.general_edit_fragment, container, false);
        ViewStub form = (ViewStub) view.findViewById(R.id.stub);
        form.setLayoutResource( defineFormLayout() );
        form.inflate();

        buttonAdd = (Button) view.findViewById(R.id.button_add);
        buttonUpdate = (Button) view.findViewById(R.id.button_update);

        if ( getRowIndex() < 0)
            {
            Scribe.note(" Id < 0 -> ADD Button activated");
            buttonAdd.setOnClickListener(new View.OnClickListener()
                {
                public void onClick(View view)
                    {
                    addItem();
                    }
                });
            buttonUpdate.setVisibility(View.GONE);
            }
        else
            {
            Scribe.note(" Id valid -> UPDATE Button activated");
            buttonUpdate.setOnClickListener(new View.OnClickListener()
                {
                public void onClick(View view)
                    {
                    updateItem();
                    }
                });
            buttonAdd.setVisibility(View.GONE);
            }

        buttonList = (Button) view.findViewById(R.id.button_list);

        buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener()
            {
            public void onClick(View view)
                {
                Scribe.note("General EDIT Fragment: CANCEL button");
                cancelEdit();
                }
            });

        codeGenerator = 0;
        //setupFormLayout(); - már kell bele a getView! Áttettem onActivityCreatedbe

        return view;
        }


    // Ez az adatfel- és visszatöltés leglényegesebb része:
    // Ha edited==TRUE, akkor edited-del együtt az összes változónk érvényes, nincs teendő
    // 		(pl. amikor visszatérünk a ForeignKey miatt meghívott Activity-ből)
    // Különben: Ha savedInstanceState != null, akkor vannak elmentett értékeink, töltsük vissza őket
    // retrieveForeignKeyData()
    //		(pl. elfordítás miatti újraindítás)
    // Különben: első indítás (vagy legalábbis nem volt edit), töltsük fel az alapértelmezett értékeket
    // pullData
    @Override
    public void onActivityCreated (Bundle savedInstanceState)
        {
        super. onActivityCreated(savedInstanceState);
        Scribe.note("Genaral EDIT Fragment onActivity Created");

        // CSAK ELLENŐRZÉS
        if ( connection != null )
            {
            Scribe.debug( "EditFragment's variables are reserved" );
            }


        connection = new Connection( getContext(), defineTableIndex() );
        setupFormLayout(); // Ezt vajon csak egyszer hívja meg?

        // Itt kell jelezni, ha a Fragment rendelkezik menüvel
        setHasOptionsMenu(true);

        // onActivityCreate onCreateView UTÁN kerül meghívásra !!
        // Itt adunk értéket az UI elemeknek (EditText-ről gondoskodik a program)

        // Újraindítás történt, értékekeket kivesszük a rendelkezésre álló csomagból
        if (savedInstanceState != null)
            {
            // Korábbi megjegyzés:
            // Az add miatt ez újra meghívásra kerül, de nincs párja, vagyis bemenete, ezért lesz értéke mindig null.
            // Az alapértéket meg kell adni eredendőan, aztán itt csak átállítjuk.
            connection.retrieveData( savedInstanceState );
            }

        else // alapértéket - elvileg csak itt kell beállítani
            {
            if (limitedForeignKey != null )
                limitedForeignKey.setValue( getLimitedItem() );

            // -1L-nél ez nem csinál semmit, hint meg csak ott van
            connection.pullData( getRowIndex());
            }

        }


    // A hagyományos módon megnyitott confirmationDialog-ot elfordításkor le kell választani!
    @Override
    public void onDestroyView()
        {
        super.onDestroyView();

        // Ha véletlenül meg van nyitva a megerősítő kérdés, akkor tüntessük el!
        if (confirmationDialog != null)
            {
            Scribe.note("General EDIT Fragment: confirmationDialog was removed in onDestroyView!");
            confirmationDialog.dismiss();
            confirmationDialog = null;
            }
        }


    @Override
    public void onDetach()
        {
        super.onDetach();

        onFinishedListener = null;
        }


    @Override
    public void onSaveInstanceState(Bundle outState)
        {
        super.onSaveInstanceState(outState);

        // Ez elvileg eredeti módon is jó, de így jobban összhangban van a párjával
        connection.saveData(outState);
        }

    // Két lehetőséget: Add as new ill. Delete csak a menüben ajánlunk fel
    // Add as new egyébként ugyanaz, mint az Add billentyű (csak ez Update esetén nem látszik.
    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
        {
        inflater.inflate(R.menu.general_edit_menu, menu);
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
        switch (item.getItemId())
            {
            case R.id.menu_add_as_new:
                addItem();
                return true;

            case R.id.menu_delete:
                deleteItem();
                return true;

            default:
                return super.onOptionsItemSelected(item);
            }
        }

    // Ez a rész lehet, h. minden más ELŐTT kerül végrehajtásra! Ezért fontos edited-et TRUE-ra állítani
    // A ForeignTextField érintésére meghívásra kerül egy új Activity, ahol kiválaszthatjuk az új elemet
    // Ez azonban itt tér vissza, és a (TextField alapján megadott) requestCode alapján kell végigellenőrizni a ForeignKey mezőket
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
        super.onActivityResult(requestCode, resultCode, data);
        Scribe.note("General EDIT Fragment onActivityResult");

        // ??????? getActivity().getContentResolver().unregisterContentObserver( this );

        if ( resultCode == Activity.RESULT_OK )
            {
            long selectedId = data.getLongExtra(GenericCombinedListFragment.SELECTED_ITEM, GenericCombinedListFragment.SELECTED_NONE);
            checkReturningSelector( requestCode, selectedId );
            }
        }


    protected void checkReturningSelector(int requestCode, long selectedId)
        {
        for (ForeignKey key : foreignKeys)
            {
            key.checkReturningSelector( requestCode, selectedId );
            }
        }


	public int getCode()
		{
		codeGenerator++;
		Scribe.note("General EDIT Fragment: Code generated: " + codeGenerator);
		return codeGenerator;
		}


	// Kilistázhatjuk egy tábla azon elemeit, melyek a mi elemünkre hivatkoznak
	// listingActivity: GenericControllActivity megfelelő táblához tartozó leszármazottja
	// buttonTitle: mi kerüljön a gombra? (Eredetileg List)
	// listTitle: a címsor eleje
	// listOwner: a mi elemünket azonosító (legjobban jellemző) TextView
	// ?? Ez még nem a legtökéletesebb, mert mi van, ha több listát akarunk ??
	// LIMITED_ITEM_hez tartozik a COLUMN is, csak abból most csak egyetlen van
	protected void setupListButton( final Class<?> listingActivity, final String buttonTitle, final String listTitle, final TextView listOwner )
		{
    	Scribe.note("Genaral EDIT Fragment: ListButton was set: " + buttonTitle );

		buttonList.setVisibility( View.VISIBLE );
		buttonList.setText( buttonTitle );
		buttonList.setOnClickListener(new View.OnClickListener()
    		{
    		public void onClick(View view) 
    			{

    			// getActivity().getContentResolver().registerContentObserver();

    			Intent intent = new Intent(getActivity(), listingActivity);
    			intent.putExtra( GenericControllActivity.TITLE, listTitle + listOwner.getText() );
    			intent.putExtra( GenericCombinedListFragment.LIMITED_ITEM, getRowIndex() );
    			startActivity( intent );
    			} 
    		});
		}
	
	
	// Az egyes gombokért felelős akciók
	// Ha kellenek az adatok (add/update), azokat a pushData() adja meg
	private void addItem()
		{
		Scribe.note("Genaral EDIT Fragment: ADD button");

        long rowId = connection.pushData( -1L);
        if ( rowId != -1L )
            {
            onFinishedListener.onFinished(rowId);
            }
        }


	private void updateItem()
		{
		Scribe.note("Genaral EDIT Fragment: UPDATE button");
        if ( connection.pushData( getRowIndex()) != -1L )
            {
            onFinishedListener.onFinished(-1L);
            }
		}


    protected Uri getItemContentUri()
        {
        return table(defineTableIndex()).itemContentUri( getRowIndex() );
        // Uri.parse( table(defineTableIndex()).contentUri() + "/" + getRowIndex());
        }

    private void deleteItem()
		{
		Scribe.note("Genaral EDIT Fragment: DELETE menu");
        Activity activity = getActivity();
		if (getRowIndex() >= 0 && activity != null)
			{
			try
				{
		    	getActivity().getContentResolver().delete(getItemContentUri(), null, null);
				}
			catch (Exception e)
				{
				Toast.makeText(getActivity(), "ERROR: Delete item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
				}
			onFinishedListener.onFinished(-1L);
			}
		else
			Scribe.note("ID < 0 or ACTIVITY MISSING!!!");
		}


    // Itt jelezzük, ha megszakítani kívánjuk a szerkesztést
    // isEdited() esetén egy dialogusablakban meg kell erősíteni a szándékunkat
    public void cancelEdit()
    	{
    	Scribe.note("General EDIT Fragment: cancelEdit was started");
    	
        Activity activity = getActivity();
        if (activity == null)
        	{
        	Scribe.note("cancelEdit: Fragment's ACTIVITY MISSING!!!");
        	return;
        	}

		if ( connection.isEdited() )
			{
			if (confirmationDialog != null)
				confirmationDialog.dismiss();
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( getActivity() );
			alertDialogBuilder.setTitle( R.string.confirmation_title );
			// alertDialogBuilder.setMessage( "Click yes to exit!" );
			alertDialogBuilder.setCancelable(false);
			alertDialogBuilder.setPositiveButton( R.string.confirmation_yes, new DialogInterface.OnClickListener()
				{
				public void onClick(DialogInterface dialog,int id) 
					{
					Scribe.note("Data was edited, user was asked, till exiting from EDIT Fragment...");
					onFinishedListener.onFinished( -1L );
					}
				});
			alertDialogBuilder.setNegativeButton( R.string.confirmation_no, new DialogInterface.OnClickListener() 
				{
				public void onClick(DialogInterface dialog,int id) 
					{
					Scribe.note("Data was edited, exit canceled, returning to EDIT Fragment...");

					dialog.dismiss(); // ez ide nem is kell talán...
					// kevésbé lényeges, de különben onDestroyView()-ban ismét dismiss-eli
					confirmationDialog = null;
					}
				});
			// create alert dialog
			confirmationDialog = alertDialogBuilder.create();
			// show it
			confirmationDialog.show();
			}
		else
			{
			Scribe.note("Data was not edited, exiting from EDIT Fragment...");
			onFinishedListener.onFinished( -1L );
			}
    	}
   	}
