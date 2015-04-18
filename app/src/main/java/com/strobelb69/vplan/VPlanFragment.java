package com.strobelb69.vplan;

import android.content.ContentResolver;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.strobelb69.vplan.data.VplanContract;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Main Fragment of MainActivity
 *
 * Created by Bernd on 15.03.2015.
 */
public class VPlanFragment extends Fragment {

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
    private static String lblLastSync;
    private Uri uriKlasse;
    VPlanAdapter vplanAdapter;
    private SharedPreferences sPref;
    private DateFormat df;
    private PlanLoader planLoader;
    private TimeStampLoader timeStampLoader;
    private ZusatzinfoLoader zusinfoLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        keyKlasse = getString(R.string.prefKeyKlasse);
        keyKomprDoppelStd = getString(R.string.prefKeyDoppelstunde);
        lblTimeStamp = getString(R.string.lblTimestamp);
        lblLastSync = getString(R.string.lblLastSync);
        setUriKlasse(sPref);
        planLoader = new PlanLoader();
        timeStampLoader = new TimeStampLoader();
        zusinfoLoader = new ZusatzinfoLoader();
        df = DateFormat.getDateTimeInstance();
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
    public void onResume() {
        super.onResume();
        setSyncDisableWarning(getView());
        setUriKlasse(sPref);
        getLoaderManager().restartLoader(MainActivity.TIMESTAMP_LOADER, null, timeStampLoader);
        getLoaderManager().restartLoader(MainActivity.ZUSATZINFO_LOADER, null, zusinfoLoader);
        getLoaderManager().restartLoader(MainActivity.PLAN_LIST_LOADER, null, planLoader);
    }

    private void setSyncDisableWarning(View rv) {
        TextView tv = (TextView) rv.findViewById(R.id.textview_sync_disabled_warning);
        if (ContentResolver.getSyncAutomatically(
                MainActivity.getSyncAccountObj(getActivity()),
                getString(R.string.vplan_provider_authority))) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
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
        String LT = VPlanFragment.this.LT + "." + getClass().getSimpleName();
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uriKopf = VplanContract.BASE_CONTENT_URI.buildUpon().appendPath(VplanContract.PATH_KOPF).build();
            Log.d(LT,"onCreateLoader: Loader created with URI="+uriKopf);
            return new CursorLoader(getActivity(),uriKopf,new String[]{VplanContract.Kopf.COL_TIMESTAMP, VplanContract.Kopf.COL_LAST_SYNC},null,null,null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
            if (c != null && c.moveToFirst()) {
                TextView tvTimeStamp = (TextView) getActivity().findViewById(R.id.textview_timestamp_of_last_update);
                tvTimeStamp.setText(lblTimeStamp + " " + df.format(new Date(c.getLong(0))) + lblLastSync + " " + df.format(new Date(c.getLong(1))));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    private class ZusatzinfoLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        String LT = VPlanFragment.this.LT + "." + getClass().getSimpleName();

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uriKopf = VplanContract.BASE_CONTENT_URI.buildUpon().appendPath(VplanContract.PATH_ZUSATZINFO).build();
            Log.d(LT,"onCreateLoader: Loader created with URI="+uriKopf);
            return new CursorLoader(getActivity(),uriKopf,new String[]{VplanContract.Zusatzinfo.COL_ZIZEILE},null,null,null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
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
        }
    }

    private class PlanLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        String LT = VPlanFragment.this.LT + "." + getClass().getSimpleName();

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(LT,"onCreateLoader: Loader created with URI="+uriKlasse);
            return new CursorLoader(getActivity(),uriKlasse,PROJECTION_PLAN,null,null,null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            vplanAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            vplanAdapter.swapCursor(null);
        }
    }
}
