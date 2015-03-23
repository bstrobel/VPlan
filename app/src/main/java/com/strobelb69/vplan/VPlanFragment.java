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
import android.widget.TextView;

import com.strobelb69.vplan.data.VplanContract;
import com.strobelb69.vplan.sync.VplanSyncAdapter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Bernd on 15.03.2015.
 */
public class VPlanFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

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
    private static String lblTimeStamp;
    private Uri uriKlasse;
    VPlanAdapter vplanAdapter;
    private DateFormat df;
    private PlanLoader planLoader;
    private TimeStampLoader timeStampLoader;
    private ZusatzinfoLoader zusinfoLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        keyKlasse = getString(R.string.prefKeyKlasse);
        keyKomprDoppelStd = getString(R.string.prefKeyDoppelstunde);
        lblTimeStamp = getString(R.string.lblTimestamp);
        setUriKlasse(PreferenceManager.getDefaultSharedPreferences(getActivity()));
        planLoader = new PlanLoader();
        timeStampLoader = new TimeStampLoader();
        zusinfoLoader = new ZusatzinfoLoader();
        df = DateFormat.getDateTimeInstance();
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
        getLoaderManager().restartLoader(MainActivity.PLAN_LIST_LOADER, null, planLoader);
        getLoaderManager().restartLoader(MainActivity.TIMESTAMP_LOADER, null, timeStampLoader);
        getLoaderManager().restartLoader(MainActivity.ZUSATZINFO_LOADER, null, zusinfoLoader);
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
                        key.contains(SettingsMainFragment.KLASSE_KURS_SEP))) {
            setUriKlasse(prefs);
            getLoaderManager().restartLoader(MainActivity.PLAN_LIST_LOADER, null, planLoader);
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
            if (key.startsWith(selectedKlasse+ SettingsMainFragment.KLASSE_KURS_SEP)) {
                String kurs = key.split(SettingsMainFragment.KLASSE_KURS_SEP)[1];
                if (!prefs.getBoolean(key,true)) {
                    urib.appendQueryParameter(VplanContract.PARAM_KEY_KURS,kurs);
                }
            }
        }
        uriKlasse=urib.build();
    }

    private class TimeStampLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        String LT = getClass().getSimpleName();
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uriKopf = VplanContract.BASE_CONTENT_URI.buildUpon().appendPath(VplanContract.PATH_KOPF).build();
            Log.d(LT,"onCreateLoader: Loader created with URI="+uriKopf);
            return new CursorLoader(getActivity(),uriKopf,new String[]{VplanContract.Kopf.COL_TIMESTAMP},null,null,null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
            Log.d(LT,"onLoadFinished:");
            if (c != null && c.moveToFirst()) {
                TextView tvTimeStamp = (TextView) getActivity().findViewById(R.id.textview_timestamp_of_last_update);
                tvTimeStamp.setText(lblTimeStamp + " " + df.format(new Date(c.getLong(0))));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Log.d(LT,"onLoadReset:");
        }
    }

    private class ZusatzinfoLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        String LT = getClass().getSimpleName();

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uriKopf = VplanContract.BASE_CONTENT_URI.buildUpon().appendPath(VplanContract.PATH_ZUSATZINFO).build();
            Log.d(LT,"onCreateLoader: Loader created with URI="+uriKopf);
            return new CursorLoader(getActivity(),uriKopf,new String[]{VplanContract.Zusatzinfo.COL_ZIZEILE},null,null,null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
            Log.d(LT,"onLoadFinished:");
            TextView tvZusInfo = (TextView) getActivity().findViewById(R.id.textview_zusatzinfo);
            StringBuilder sb = new StringBuilder();
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    sb.append(c.getString(0));
                    if (!c.isLast()) {
                        sb.append("\n");
                    }
                }
            } else {
                sb.append(getString(R.string.strNoZusinfo));
            }
            tvZusInfo.setText(sb.toString());
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Log.d(LT,"onLoadReset:");
        }
    }

    private class PlanLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        String LT = getClass().getSimpleName();

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
}
