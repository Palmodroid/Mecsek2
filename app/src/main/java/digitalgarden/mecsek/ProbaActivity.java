package digitalgarden.mecsek;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import digitalgarden.mecsek.database.books.BooksTable;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.BOOKS;


public class ProbaActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
    {
    AppCompatTextView tvId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proba);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvId = (AppCompatTextView) findViewById(R.id.valami);

        LoaderManager.getInstance(this).initLoader( 1, null, this);

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                }
            });*/
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
        {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mecsek, menu);
        return true;
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            {
            return true;
            }

        return super.onOptionsItemSelected(item);
        }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
        Scribe.note("onCreateLoader (Query) started");

        if ( id != 1 )
            return null;

        String[] projection = {column(BooksTable.TITLE)};

        // http://code.google.com/p/android/issues/detail?id=3153
        CursorLoader cursorLoader = new CursorLoader(
                this,
                table( BOOKS ).contentUri(),
                projection,
                null,
                null,
                //new String[] { "%"+filterString+"%" },
                // ha nincs filterClause, akkor nem használja fel
                null );

        return cursorLoader;
        }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor)
        {
        StringBuilder sb = new StringBuilder("Adatok: * ");

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
            sb.append(cursor.getString(cursor.getColumnIndexOrThrow( column( BooksTable.TITLE ))));
            sb.append(" * ");
            }

        tvId.setText(sb.toString());

        cursor.close();
        }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader)
        {

        }

    }
