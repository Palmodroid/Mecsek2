package digitalgarden.mecsek;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import digitalgarden.mecsek.permission.PermissionRequestDialog;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.Longtime;

import static digitalgarden.mecsek.Debug.initScribe;

public class MecsekActivity extends AppCompatActivity implements PermissionRequestDialog.OnPermissionRequestFinished
    {
    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        PermissionRequestDialog permissionRequestDialog =
                (PermissionRequestDialog) getFragmentManager().findFragmentByTag("dialog");
        if (permissionRequestDialog == null)
            {
            //Scribe.debug(Debug.PERMISSION, "Permission dialog is not found, it should be recreated!");
            permissionRequestDialog = PermissionRequestDialog.newInstance();
            // permissionRequestDialog.setRetainInstance(true); - do not need to retain
            // testDialog.setCancelable(false);
            permissionRequestDialog.show(getFragmentManager(), "dialog");
            }
        }


    @Override
    public void onPermissionRequestFinish(boolean permissionsGranted)
        {
        initScribe(this);

        if (permissionsGranted)
            {
            setContentView(R.layout.activity_mecsek);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener()
                {
                @Override
                public void onClick(View view)
                    {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    }
                });
            }
        else
            finish();
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
    }
