package com.strobelb69.vplan;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.strobelb69.vplan.data.VplanContract;
import com.strobelb69.vplan.sync.VplanSyncAdapter;

import java.util.Map;

/**
 * Created by Bernd on 15.03.2015.
 */
public class VPlanFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String[] PROJECTION_PLAN = new String[] {
            VplanContract.Plan.TABLE_NAME+"."+VplanContract.Plan._ID, // this needs the the Loader
            VplanContract.Plan.COL_STUNDE,
            VplanContract.Plan.COL_FACH,
            VplanContract.Plan.COL_FACH_NEU,
            VplanContract.Plan.TABLE_NAME+"."+VplanContract.Plan.COL_LEHRER,
            VplanContract.Plan.COL_LEHRER_NEU,
            VplanContract.Plan.COL_RAUM,
            VplanContract.Plan.COL_RAUM_NEU,
            VplanContract.Plan.COL_INF
    };
    private String LT = getClass().getSimpleName();
    private static String keyKlasse;
    private static String keyKomprDoppelStd;
    private Uri uriKlasse;
    VPlanAdapter vplanAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        keyKlasse = getString(R.string.prefKeyKlasse);
        keyKomprDoppelStd = getString(R.string.prefKeyDoppelstunde);
        setUriKlasse(PreferenceManager.getDefaultSharedPreferences(getActivity()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vplan_fragment_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            VplanSyncAdapter.syncImmediately(getActivity());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vplanAdapter = new VPlanAdapter(getActivity(), null, 0);

        View rv = inflater.inflate(R.layout.vplan_fragment, container, false);
        ListView lv = (ListView) rv.findViewById(R.id.listview_vplan);
        lv.setAdapter(vplanAdapter);
        return rv;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().restartLoader(MainActivity.PLAN_LIST_LOADER, null, this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // Check for isAdded() is done to avoid "java.lang.IllegalStateException: Fragment VPlanFragment{19a95b71} not attached to Activity"
        if (isAdded() &&
                (key.equals(keyKlasse) ||
                        key.equals(keyKomprDoppelStd) ||
                        key.contains(SettingsFragment.KLASSE_KURS_SEP))) {
            setUriKlasse(prefs);
            getLoaderManager().restartLoader(MainActivity.PLAN_LIST_LOADER, null, this);
        }
    }

    private void setUriKlasse(SharedPreferences prefs) {
        String selectedKlasse = prefs.getString(keyKlasse,getString(R.string.prefDefKlasse));
        Boolean isKomprDoppelStd = prefs.getBoolean(keyKomprDoppelStd, MainActivity.prefDefDoppelstunde);

        uriKlasse = VplanContract.Plan.CONTENT_URI.buildUpon().appendQueryParameter(VplanContract.PARAM_KEY_KLASSE,selectedKlasse).build();
        if (isKomprDoppelStd) {
            uriKlasse = uriKlasse.buildUpon().appendQueryParameter(VplanContract.PARAM_KEY_KOMP_DOPPELSTD,"true").build();
        }

        Uri.Builder urib = uriKlasse.buildUpon();
        Map<String,?> pm = prefs.getAll();
        for (String key: pm.keySet()) {
            if (key.startsWith(selectedKlasse+SettingsFragment.KLASSE_KURS_SEP)) {
                String kurs = key.split(SettingsFragment.KLASSE_KURS_SEP)[1];
                if (!prefs.getBoolean(key,true)) {
                    urib.appendQueryParameter(VplanContract.PARAM_KEY_KURS,kurs);
                }
            }
        }
        uriKlasse=urib.build();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LT,"onCreateLoader: Loader created with URI="+uriKlasse);
        return new CursorLoader(getActivity(),uriKlasse,PROJECTION_PLAN,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LT,"onLoadFinished, Swapping Cursor");
        vplanAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LT,"onLoaderReset: Resetting Cursor to null");
        vplanAdapter.swapCursor(null);
    }
}
