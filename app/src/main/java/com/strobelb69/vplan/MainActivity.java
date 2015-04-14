package com.strobelb69.vplan;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    public final String LT=getClass().getSimpleName();
    public static final String VPFMT_TAG = "VPFMT_TAG";
    public static final int TITLE_LOADER = 0;
    public static final int PLAN_LIST_LOADER = 1;
    public static final int TIMESTAMP_LOADER = 2;
    public static final int ZUSATZINFO_LOADER = 3;
    public static boolean prefDefDoppelstunde=true;

    public static final int DEF_SYNCINTERVAL = 30 * 60;
    public static final int DEF_FLEXTIME = DEF_SYNCINTERVAL /3;

    private String authority;
    private String currKlasse;
    private String klasseKey;
    private String notifKey;
    private String syncAutoKey;
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // We need to keep a reference to SharedPrefListener object. It will garbage collected
        // if we don't do this. See Javadoc of registerOnSharedPreferenceChangeListener()!
        prefListener = new SharedPrefListener();

        setContentView(R.layout.activity_main);
        Log.d(LT, "onCreate() called");
        if (savedInstanceState == null) {
            Log.d(LT, "onCreate(null) called");
            prefs.registerOnSharedPreferenceChangeListener(prefListener);
            authority = getString(R.string.vplan_provider_authority);
            klasseKey = getString(R.string.prefKeyKlasse);
            currKlasse = prefs.getString(klasseKey,getString(R.string.prefDefKlasse));
            notifKey = getString(R.string.prefKeySendNotification);
            syncAutoKey = getString(R.string.prefKeyDoSyncAutomatically);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_main_container, new VPlanFragment(), VPFMT_TAG)
                    .commit();
            // check if someone switched off syncing in the settings app of android
            boolean isSyncEnabled = ContentResolver.getSyncAutomatically(
                    getSyncAccount(),
                    authority);
            Log.d(LT, "isSyncEnabled=" + isSyncEnabled);
            prefs.edit()
                    .putBoolean(syncAutoKey, isSyncEnabled)
                    .apply();
            getSupportLoaderManager().initLoader(TITLE_LOADER, null, new ActionBarLoader());
        }

        if (prefs.getBoolean(syncAutoKey, true)) {
            syncImmediately();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        VplanNotificationService.clearNotification(this);
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

    private class SharedPrefListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Log.d(LT, "SharedPrefsKey key=" + key + " changed!");
            if (klasseKey.equals(key)) {
                currKlasse = prefs.getString(key, getString(R.string.prefDefKlasse));
                getSupportLoaderManager().restartLoader(TITLE_LOADER, null, new ActionBarLoader());
            } else if (notifKey.equals(key)) {
                if (!prefs.getBoolean(notifKey, true)) {
                    VplanNotificationService.clearNotification(MainActivity.this);
                }
            } else if (syncAutoKey.equals(key)) {
                boolean isSyncEnabled = prefs.getBoolean(syncAutoKey,true);
                Log.d(LT, "onSharedPreferenceChanged: isSyncEnabled="+isSyncEnabled);
                ContentResolver.setSyncAutomatically(
                        getSyncAccount(),
                        authority,
                        isSyncEnabled);
            }
        }
    }

    private class ActionBarLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(LT, "onCreateLoader called!");
            return new CursorLoader(
                    MainActivity.this,
                    VplanContract.Kopf.CONTENT_URI,
                    new String[]{VplanContract.Kopf.COL_FOR_DATE}, null, null, null);
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        public void onLoadFinished(Loader<Cursor> l, Cursor c) {
            Log.d(LT, "onLoadFinished called!");
            ActionBar ab = getSupportActionBar();
            String d = "";
            if (c != null && c.getCount() > 0) {
                c.moveToFirst(); // we always have only one row in the underlying table
                Date date = new Date(c.getLong(0));
                d = d + new SimpleDateFormat("EEE ").format(date) +
                        DateFormat.getDateFormat(MainActivity.this).format(date);
            }
            ab.setTitle(d + " - " + currKlasse);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            Log.d(LT, "onLoaderReset called!");
        }
    }

    public void syncImmediately() {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(
                getSyncAccount(),
                authority,
                b);
    }

    public Account getSyncAccount() {
        Account acc = new Account(
                getString(R.string.app_name),
                getString(R.string.vplan_authenticator_account_type));

        AccountManager mgr = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        if (mgr.getPassword(acc) == null) {
            if (!mgr.addAccountExplicitly(acc, "", null)) {
                return null;
            }
            ContentResolver.setSyncAutomatically(
                    acc, authority,true);
            configurePeriodicSync(DEF_SYNCINTERVAL, DEF_FLEXTIME);
        }

        return acc;
    }

    public void configurePeriodicSync(int syncInterval, int flexTime) {
        Account account = getSyncAccount();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }
}
