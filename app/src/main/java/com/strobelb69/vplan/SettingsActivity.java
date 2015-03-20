package com.strobelb69.vplan;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

/**
 * Created by bstrobel on 16.03.2015.
 */
public class SettingsActivity extends ActionBarActivity {

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

        Intent intent = getIntent();
        Uri target = intent.getData();
        Log.d(LT, "Intent for Uri " + target.toString());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (uriMatcher.match(target)) {
            case KURSE:
                ActionBar ab = getSupportActionBar();
                ab.setTitle(getString(R.string.prefTitleKurs));
                ab.setSubtitle(getString(R.string.prefSubTitleKurs));
                ft.replace(R.id.vplan_settings_avtivity, new SettingsKurseFragment());
                break;
            default:
            case MAIN:
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
