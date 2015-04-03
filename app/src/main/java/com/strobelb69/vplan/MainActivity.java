package com.strobelb69.vplan;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.strobelb69.vplan.data.VplanContract;
import com.strobelb69.vplan.sync.VplanSyncAdapter;
import com.strobelb69.vplan.sync.VplanSyncService;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
    public final String LT=getClass().getSimpleName();
    public static final String VPFMT_TAG = "VPFMT_TAG";
    public static final int TITLE_LOADER = 0;
    public static final int PLAN_LIST_LOADER = TITLE_LOADER+1;
    public static boolean prefDefDoppelstunde=true;
    private String currKlasse;
    private String klasseKey;
    private String notifKey;
    private Messenger vplanSyncMessenger;
    private VplanSyncServiceConnection svcConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new VPlanFragment(),VPFMT_TAG)
                    .commit();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        klasseKey = getString(R.string.prefKeyKlasse);
        currKlasse = prefs.getString(klasseKey,getString(R.string.prefDefKlasse));

        notifKey = getString(R.string.prefKeySendNotification);
        getSupportLoaderManager().initLoader(TITLE_LOADER,null,this);
        VplanSyncAdapter.initializeSyncAdapter(this);

        Bundle extras = getIntent().getExtras();
        if (extras!=null && extras.getBoolean(VplanSyncAdapter.CLEAR_NOTIFICATION_KEY)) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(VplanSyncAdapter.NOTIFICATION_ID);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        svcConn = new VplanSyncServiceConnection(PreferenceManager.getDefaultSharedPreferences(this));
        Intent intent = new Intent(this, VplanSyncService.class);
        intent.putExtra(VplanSyncService.EXTRAS_KEY_FROM_APP, true);
        bindService(intent, svcConn, BIND_WAIVE_PRIORITY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (svcConn != null) {
            unbindService(svcConn);
        }
    }

    private class VplanSyncServiceConnection implements ServiceConnection {
        SharedPreferences prefs;

        private VplanSyncServiceConnection(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(LT, "onServiceConnected(" + name + ", " + service + ")");
                vplanSyncMessenger = new Messenger(service);
                if (prefs.getBoolean(notifKey, true)) ;
            try {
                vplanSyncMessenger.send(getMsgFromNotifFlag(prefs));
            } catch (RemoteException ex) {
                Log.e(LT, ex.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            vplanSyncMessenger = null;
        }

    }

    private Message getMsgFromNotifFlag(SharedPreferences prefs) {
        Message msg;
        if (prefs.getBoolean(notifKey, true)) {
            msg = Message.obtain(null, VplanSyncService.NOTIFICATIONS_ON);
        } else {
            msg = Message.obtain(null, VplanSyncService.NOTIFICATIONS_OFF);
        }
        return msg;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intPrefs = new Intent(this, SettingsActivity.class);
            intPrefs.setData(SettingsActivity.BASE_PREF_URI.buildUpon().appendPath(SettingsActivity.PATH_MAIN).build());
            startActivity(intPrefs);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (klasseKey.equals(key)) {
            currKlasse = prefs.getString(key,getString(R.string.prefDefKlasse));
            getSupportLoaderManager().restartLoader(TITLE_LOADER, null, this);
        } else if (notifKey.equals(key) && vplanSyncMessenger != null) {
            if (!prefs.getBoolean(notifKey,true)) {
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(VplanSyncAdapter.NOTIFICATION_ID);
            }
            try {
                vplanSyncMessenger.send(getMsgFromNotifFlag(prefs));
            } catch (RemoteException ex) {
                Log.e(LT,ex.toString());
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LT,"onCreateLoader called!");
        return new CursorLoader(this, VplanContract.Kopf.CONTENT_URI,new String[]{VplanContract.Kopf.COL_FOR_DATE},null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor c) {
        Log.d(LT,"onLoadFinished called!");
        ActionBar ab = getSupportActionBar();
        String d = "";
        if (c != null && c.getCount() >0 ) {
            c.moveToFirst(); // we always have only one row in the underlying table
            Date date = new Date(c.getLong(0));
            d = d + new SimpleDateFormat("EEE ").format(date) + DateFormat.getDateFormat(this).format(date);
        }
        ab.setTitle(d + " - " + currKlasse);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(LT,"onLoaderReset called!");
    }
}
