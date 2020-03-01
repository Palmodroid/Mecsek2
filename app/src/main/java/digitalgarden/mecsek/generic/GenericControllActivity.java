package digitalgarden.mecsek.generic;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.HidingActionButton;
import digitalgarden.mecsek.utils.Keyboard;

import static digitalgarden.mecsek.utils.HidingActionButton.BY_LIST;


// res:
// http://stackoverflow.com/a/5796606

/*
 * GenericControllActivity
 * 
 *  Minden egyes táblához tartozik egy-egy programrész:
 *  A tábla egészét a ListFragment mutatja be List formában
 *  Az egyes kiválasztott elemeket az EditFragment kezeli
 *  Az Activity e két fragmentumot kontrollálja.
 *  
 *  Csak a fregmenteket elkészítő részt kell a leszármazottaknak elkészíteni:
 *  Fragment createEditFragment()
 *  Fragment cretaeListFragment()
 *
 *  Beállítható Extrák:
 *  	NAME - a teljes Activity címe
 *
 *  SELECTED_ITEM - kijelöl egy már kiválasztott sort a listában. ListFragment nyílik meg
 *
 *  EDITED_ITEM - a ListFragment-ben kijelölt elemmel megnyílik az EditFragment
 *
 *  Ha ezzel hívjuk meg az Activity-t, akkor rögtön erre az elemre ugrik.
 *
 *  Visszatéréskor:
 *  	ITEM_SELECTED - a kiválasztott elem id-je
 *
 *
 */


/*

Több elvi lehetőségünk van:

-   MEGNYILIK   ALAPÉRTELMEZETT     KIVÁLASZTÁSKOR      VISSZA      HOSSZÚ      VISSZA
"sima" lista
    LIST                            EDIT                LIST
lista szelektáláshoz
    LIST       SELECTED_ITEM        ITEM_SELECTED                   EDIT        ITEM_SELECTED
"sima" edit
    EDIT       EDITED_ITEM                              LIST
edit máshonnan, lista nélkül (pl. diary ilyen)
    EDIT       EDITED_ITEM
 */


public abstract class GenericControllActivity extends AppCompatActivity
        implements GenericCombinedListFragment.OnListReturnedListener, GenericEditFragment.OnFinishedListener
    {
    public final static String TITLE = "title";

    // Ezeket az értékeket GenericListFragment definiálja
    // public static final String SELECTED_ITEM = "selected item";
    // public static final long SELECTED_NONE = -2L;
    // public static final long SELECT_DISABLED = -1L;

    // Ezeket az értékeket GenericEditFragment definiálja
    // public final static String EDITED_ITEM = "edited item";

    // Edit Fragment létrehozásáért felelős rész
    protected abstract GenericEditFragment createEditFragment();

    // List Fragment létrehozásáért felelős rész
    protected abstract GenericCombinedListFragment createListFragment();

    //boolean flag to know if main FAB is in open or closed state.
    private boolean fabExpanded = false;
    private FloatingActionButton fabSettings;

    //Linear layout holding the Save submenu
    private LinearLayout layoutFabSave;

    //Linear layout holding the Edit submenu
    private LinearLayout layoutFabEdit;
    private LinearLayout layoutFabPhoto;

    private HidingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        Scribe.note("GenericContro1llActivity.onCreate started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_controll_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().hide();

        fab = (HidingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                onItemEditing(-1L); // Mintha a ListFragment térne vissza
                }
            });

        fabSettings = (FloatingActionButton) this.findViewById(R.id.fabSetting);

        layoutFabSave = (LinearLayout) this.findViewById(R.id.layoutFabSave);
        layoutFabSave.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                fabSettings.show();
                }
            });
        layoutFabEdit = (LinearLayout) this.findViewById(R.id.layoutFabEdit);
        layoutFabEdit.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                fabSettings.hide();
                }
            });

        layoutFabPhoto = (LinearLayout) this.findViewById(R.id.layoutFabPhoto);
        layoutFabPhoto.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                Snackbar.make(view, "Photo was taken!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                }
            });


        //When main Fab (Settings) is clicked, it expands if not expanded already.
        //Collapses if main FAB was open already.
        //This gives FAB (Settings) open/close behavior
        fabSettings.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                if (fabExpanded == true)
                    {
                    closeSubMenusFab();
                    }
                else
                    {
                    openSubMenusFab();
                    }
                }
            });

        //Only main FAB is visible in the beginning
        closeSubMenusFab();

        String title = getIntent().getStringExtra(TITLE);
        if (title != null)
            {
            Scribe.note("GenericControllActivity NAME set: " + title);
            setTitle(title);
            }
        }

    // https://ptyagicodecamp.github.io/creating-sub-menuitems-for-fab-floating-action-button.html
    //closes FAB submenus
    private void closeSubMenusFab()
        {
        layoutFabSave.setVisibility(View.INVISIBLE);
        layoutFabEdit.setVisibility(View.INVISIBLE);
        layoutFabPhoto.setVisibility(View.INVISIBLE);
        // fabSettings.setImageResource(R.drawable.ic_settings_black_24dp);
        fabExpanded = false;
        }

    //Opens FAB submenus
    private void openSubMenusFab()
        {
        layoutFabSave.setVisibility(View.VISIBLE);
        layoutFabEdit.setVisibility(View.VISIBLE);
        layoutFabPhoto.setVisibility(View.VISIBLE);
        //Change settings icon to 'X' icon
        // fabSettings.setImageResource(R.drawable.ic_close_black_24dp);
        fabExpanded = true;
        }


    public void showActionButtonByList(boolean show)
        {
        if ( show )
            fab.show( BY_LIST );
        else
            fab.hide( BY_LIST );
        }


    // FragmentTransaction csak ettől a ponttól történhet
    @Override
    protected void onResumeFragments()
        {
        super.onResumeFragments();
        Scribe.note("GenericControllActivity.onResumeFragments started");

        FragmentManager fragmentManager = getSupportFragmentManager();

        // LIST és EDIT Fragment is magától megjelenik a látható két Frame-ben
        // Ha nincs LIST, akkor létrehozzuk (ez első indításkor fordulhat elő)
        // Ha nincs EDIT, akkor a hozzá tartozó Frame-t is kikapcsoljuk.
        // Ha van EDIT ÉS PORTRAIT-ban vagyunk, akkor az átfedő LIST Frame-jét kapcsoljuk ki

        // Újítás!
        // Ha EditFrag nem létezik

        Fragment listFrag = fragmentManager.findFragmentByTag("LIST");
        if (listFrag == null)
            {
            listFrag = createListFragment();

            // !!!!!!!!!!!!!!! EZ VAJON IDE KELL ???????????????
            ((GenericCombinedListFragment)listFrag).rollToSelectedItem();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.list_frame, listFrag, "LIST");
            fragmentTransaction.commit();

            Keyboard.hide(this);

            Scribe.note("new LIST Fragment was created, added");
            }

        Fragment editFrag = fragmentManager.findFragmentById(R.id.edit_frame);
        if (editFrag == null)
            {
            long editedItem = getIntent().getLongExtra(GenericEditFragment.EDITED_ITEM,-1L);

            // Van EDITÁLANDÓ értékünk - EZ ONITEMEDITINGBŐL JÖTT NEM LEHET KÉT ILYEN !!!!!!!!!!!!!!!!!!!!!!!!
            if ( editedItem >= -0L )
                {
                editFrag = createEditFragment();
                Bundle args = new Bundle();
                args.putLong(GenericEditFragment.EDITED_ITEM, editedItem);
                editFrag.setArguments(args);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                // http://stackoverflow.com/questions/4817900/android-fragments-and-animation és
                // http://daniel-codes.blogspot.hu/2012/06/fragment-transactions-reference.html
                // fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left , android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.add(R.id.edit_frame, editFrag, "EDIT");
                fragmentTransaction.addToBackStack("LIBDB");
                fragmentTransaction.commit();

                Scribe.note("List item selected: New EDIT was created, added");

                findViewById(R.id.edit_frame).setVisibility(View.VISIBLE);
                Scribe.note("EDIT Frame VISIBLE");
// ListFragment van, de elrejtjük
//                if (findViewById(R.id.landscape) == null)
//                    {
                    findViewById(R.id.list_frame).setVisibility(View.GONE);
//                    Scribe.note("PORTRAIT MODE: LIST Frame GONE");
//                    }
                }
            // Nincs editálandó elem
            else
                {
                findViewById(R.id.edit_frame).setVisibility(View.GONE);
                Scribe.note("EDIT Fragment not found, EDIT Frame GONE");
                }
            }
        else if (findViewById(R.id.landscape) == null) // editFrag létezik!
            {
            findViewById(R.id.list_frame).setVisibility(View.GONE);
            Scribe.note("EDIT Fragment found in PORTRAIT mode, LIST Frame GONE");
            fab.hide( HidingActionButton.BY_FRAGMENT );
            }
        else // editFrag létezik, és landscape-ben vagyunk.
            {
            Scribe.note("EDIT Fragment found in LANDSCAPE mode, LIST and EDIT visible");
            fab.hide( HidingActionButton.BY_FRAGMENT );
            }
        }


    // Listfragment jelzett vissza - elemet választottunk ki
    public void onItemSelected(long id)
        {
        Scribe.note("LIST ITEM was selected for returning: " + id);

        // Ezt lehet, h. a fragment is meg tudná tenni
        Intent i = new Intent();
        i.putExtra(GenericCombinedListFragment.SELECTED_ITEM, id);
        setResult(RESULT_OK, i);
        finish();
        }

    // ListFragment kiválasztotta editálásra az id elemet
    public void onItemEditing(long id)
        {
        Scribe.note("LIST ITEM was selected for editing: " + id);
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Ameddig van aktív EDIT, addig nem választhatunk újat!
        Fragment editFrag = fragmentManager.findFragmentById(R.id.edit_frame);
        if (editFrag != null)
            {
            Scribe.note("New ITEM ignored, previous EDIT is active");
            return;
            }

        Bundle args = new Bundle();
        // Ha új elemet hozunk létre akkor, listFragment (ha van) header (ha van) adhat "hint"-et
        // Lehet, hogy list-fragmentet lehetne class-variable-ben tárolni.

        if ( id == -1L ) // Uj elemet hozunk létre
            {
            GenericCombinedListFragment listFrag =
                    (GenericCombinedListFragment)fragmentManager.findFragmentById(R.id.list_frame);
            if (listFrag != null && listFrag.isHeaderDefinied())
                {
                args.putInt(GenericEditFragment.LIMITED_COLUMN, listFrag.getHintColumn());
                args.putLong(GenericEditFragment.LIMITED_ITEM, listFrag.getHintItem());
                }
            }

        editFrag = createEditFragment();
        args.putLong(GenericEditFragment.EDITED_ITEM, id);
        editFrag.setArguments(args);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // http://stackoverflow.com/questions/4817900/android-fragments-and-animation és
        // http://daniel-codes.blogspot.hu/2012/06/fragment-transactions-reference.html
        // fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left , android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.add(R.id.edit_frame, editFrag, "EDIT");
        fragmentTransaction.addToBackStack("LIBDB");
        fragmentTransaction.commit();

        Scribe.note("List item selected: New EDIT was created, added");

        findViewById(R.id.edit_frame).setVisibility(View.VISIBLE);
        Scribe.note("EDIT Frame VISIBLE");
        if (findViewById(R.id.landscape) == null)
            {
            findViewById(R.id.list_frame).setVisibility(View.GONE);
            Scribe.note("PORTRAIT MODE: LIST Frame GONE");
            }
        fab.hide( HidingActionButton.BY_FRAGMENT );
        }

    public void onItemEditing( long id, Class editClass )
        {
        Scribe.note("LIST ITEM was selected for editing: " + id);
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Ameddig van aktív EDIT, addig nem választhatunk újat!
        if (fragmentManager.findFragmentById(R.id.edit_frame) != null)
            {
            Scribe.note("New ITEM ignored, previous EDIT is active");
            return;
            }

        Fragment editFrag;
        try
            {
            editFrag = (Fragment)editClass.getConstructor().newInstance();
            }
        catch (Exception e)
            {
            Scribe.error("No EDIT Fragment for header");
            return;
            }

        Bundle args = new Bundle();
        args.putLong(GenericEditFragment.EDITED_ITEM, id);
        editFrag.setArguments(args);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // http://stackoverflow.com/questions/4817900/android-fragments-and-animation és
        // http://daniel-codes.blogspot.hu/2012/06/fragment-transactions-reference.html
        // fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left , android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.add(R.id.edit_frame, editFrag, "EDIT");
        fragmentTransaction.addToBackStack("LIBDB");
        fragmentTransaction.commit();

        Scribe.note("List item selected: New EDIT was created, added");

        findViewById(R.id.edit_frame).setVisibility(View.VISIBLE);
        Scribe.note("EDIT Frame VISIBLE");
        if (findViewById(R.id.landscape) == null)
            {
            findViewById(R.id.list_frame).setVisibility(View.GONE);
            Scribe.note("PORTRAIT MODE: LIST Frame GONE");
            }
        fab.hide( HidingActionButton.BY_FRAGMENT );
        }

    @Override
    public void onBackPressed()
        {
        // A BACK Billentyűt visszaadjuk editFragment-nek, ha létezik
        // (uis. nem lehet csak a BackStack-et visszaszedni)
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment editFrag = fragmentManager.findFragmentById(R.id.edit_frame);
        if (editFrag != null)
            {
            Scribe.note("BACK PRESS was forwarded to EDIT Fragment");
            ((GenericEditFragment) editFrag).cancelEdit();
            }
        else
            {
            // Mi lesz itt a setResult értéke??
            Scribe.note("BACK PRESS (normal way)");
            super.onBackPressed();
            }
        }

    // EditFragment jelzett vissza - befejeztük a szerkesztést.
    // rowID csak akkor pozitív, ha sikerrel hozzáadtunk egy új elemet
    public void onFinished( long rowId )
        {
        if ( rowId != -1L )
            {
            // Ez eddig az editFinished részben volt, de akkor felvillan a List
            // A másik nagy kérdés, hogy a backStacket nem kell-e kiüríteni.
            if (getIntent().getLongExtra(GenericCombinedListFragment.SELECTED_ITEM,
                    GenericCombinedListFragment.SELECT_DISABLED) != GenericCombinedListFragment.SELECT_DISABLED)
                {
                // Ugyanaz, mint OnItemEditing
                // Ezt lehet, h. a fragment is meg tudná tenni
                Intent i = new Intent();
                i.putExtra(GenericCombinedListFragment.SELECTED_ITEM, rowId);
                setResult(RESULT_OK, i);
                finish();
                return;
                }
            }

        // Csak EDIT-ért jöttünk, vszínű source-ból
        else if (getIntent().getLongExtra(GenericEditFragment.EDITED_ITEM, -1L) >= 0L)
            {
            // Nem kell semmit tenni, mert a source-t nem változtatjuk, legfeljebb itt UPDATE-eltünk egyetlen elemet
            finish();
            return;
            }

        FragmentManager fragmentManager = getSupportFragmentManager();
        findViewById(R.id.list_frame).setVisibility(View.VISIBLE);
        Fragment listFrag = fragmentManager.findFragmentById(R.id.list_frame);
        if (listFrag != null)
            {
            ((GenericCombinedListFragment) listFrag).editFinished( rowId );
            }

        fragmentManager.popBackStack();
        findViewById(R.id.edit_frame).setVisibility(View.GONE);

        Keyboard.hide(this);
        fab.show( HidingActionButton.BY_FRAGMENT );
        Scribe.note("EDIT Fragment was finished, BackStack was popped");
        }

    // Az Options Menu (Export/Import) letiltva, csak főoldalról elérhető functiók lettek!
    /*
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
		{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.library_controll_menu, menu);
		return true;
		}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 	
		{
		switch (item.getItemId())
			{ 	
			case R.id.menu_export:
				{
				Scribe.note("MainActivity Menu: EXPORT started");
				Intent i = new Intent();
				
				i.setClass( this, SelectCsvActivity.class );
				
				i.putExtra( SelectCsvActivity.MODE, SelectCsvActivity.Mode.EXPORT );
				i.putExtra( SelectCsvActivity.FILE_ENDING, getString( R.string.extension ) );
				i.putExtra( SelectCsvActivity.DIRECTORY_SUB_PATH, getString( R.string.directory ) );
				
				startActivity( i );
					
				return true; 	
				}
			case R.id.menu_import:
				{
				Scribe.note("MainActivity Menu: IMPORT started");
				Intent i = new Intent();

				i.setClass( this, SelectCsvActivity.class );

				i.putExtra( SelectCsvActivity.MODE, SelectCsvActivity.Mode.IMPORT );
				i.putExtra( SelectCsvActivity.FILE_ENDING, getString( R.string.extension ) );
				i.putExtra( SelectCsvActivity.DIRECTORY_SUB_PATH, getString( R.string.directory ) );

				startActivity( i );
						
				return true; 	
				}
			default: 	
				return super.onOptionsItemSelected(item); 	 
			}
		}
	*/
    }
