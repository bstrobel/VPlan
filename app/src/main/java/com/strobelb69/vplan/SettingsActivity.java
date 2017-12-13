package com.strobelb69.vplan;

import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Activity for generic settings of the app
 *
 * Created by bstrobel on 16.03.2015.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String CONTENT_AUTHORITY = "com.strobelb69.vplan";
    public static final Uri BASE_PREF_URI = Uri.parse("preferences://" + CONTENT_AUTHORITY);
    public static final String PATH_MAIN = "main";
    public static final String PATH_KURSE = "kurse";
    public static final int MAIN = 0;
    public static final int KURSE = 1;

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        final UriMatcher m = new UriMatcher(UriMatcher.NO_MATCH);
        final String auth = CONTENT_AUTHORITY;
        m.addURI(auth,PATH_MAIN,MAIN);
        m.addURI(auth,PATH_KURSE,KURSE);
        return m;
    }

    public final String LT = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.vplan_settings_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Uri target = intent.getData();
        if (target != null) {
            Log.d(LT, "Intent for Uri " + target.toString());
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (uriMatcher.match(target)) {
            case KURSE:
                ft.replace(R.id.vplan_settings_avtivity, new SettingsKurseFragment());
                getSupportActionBar().setTitle(R.string.settings_kurse_actionbar_title);
                break;
            default:
            case MAIN:
                // check if someone switched off syncing in the settings app of android
                boolean isSyncEnabled = ContentResolver.getSyncAutomatically(
                        MainActivity.getSyncAccountObj(this),
                        getString(R.string.vplan_provider_authority));
                Log.d(LT, "isSyncEnabled=" + isSyncEnabled);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit()
                        .putBoolean(getString(R.string.prefKeyDoSyncAutomatically), isSyncEnabled)
                        .apply();
                ft.replace(R.id.vplan_settings_avtivity, new SettingsMainFragment());
                break;
        }
        ft.commit();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        // This returns to main settings screen instead of to main screen
        // when the back button in the ActionBar is pressed.
        if (uriMatcher.match(getIntent().getData()) == KURSE) {
            Intent newIntent = new Intent(this,SettingsActivity.class);
            newIntent.setData(BASE_PREF_URI.buildUpon().appendPath(PATH_MAIN).build());
            return newIntent;
        }
        return super.getSupportParentActivityIntent();
    }
}
