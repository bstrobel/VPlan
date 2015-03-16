package com.strobelb69.vplan;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.strobelb69.vplan.data.VplanContract;
import com.strobelb69.vplan.net.VplanRetriever;

/**
 * Created by Bernd on 15.03.2015.
 */
public class VPlanFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String[] PROJECTION_PLAN = new String[] {
            VplanContract.Plan.TABLE_NAME+"."+VplanContract.Plan._ID, // this needs the the Loader
            VplanContract.Plan.COL_STUNDE,
            VplanContract.Plan.COL_FACH,
            VplanContract.Plan.COL_FACH_NEU,
            VplanContract.Plan.COL_LEHRER,
            VplanContract.Plan.COL_LEHRER_NEU,
            VplanContract.Plan.COL_RAUM,
            VplanContract.Plan.COL_RAUM_NEU,
            VplanContract.Plan.COL_INF
    };
    private final int THIS_LOADER = 0;
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
            new UpdatePlan().execute(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private class UpdatePlan extends AsyncTask<LoaderManager.LoaderCallbacks<Cursor>, Void, Void> {
        LoaderManager.LoaderCallbacks<Cursor> lcb;
        @Override
        protected Void doInBackground(LoaderManager.LoaderCallbacks<Cursor>... lcb) {
            this.lcb = lcb[0];
            new VplanRetriever(getActivity()).retrieveFromNet();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getLoaderManager().restartLoader(THIS_LOADER, null, lcb);
        }
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
        getLoaderManager().restartLoader(THIS_LOADER, null, this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // Check for isAdded() is done to avoid "java.lang.IllegalStateException: Fragment VPlanFragment{19a95b71} not attached to Activity"
        if (isAdded() && (key.equals(keyKlasse) || key.equals(keyKomprDoppelStd))) {
            setUriKlasse(prefs);
            getLoaderManager().restartLoader(THIS_LOADER, null, this);
        }
    }

    private void setUriKlasse(SharedPreferences prefs) {
        String selectedKlasse = prefs.getString(keyKlasse,"");
        Boolean isKomprDoppelStd = prefs.getBoolean(keyKomprDoppelStd,false);
        if (isKomprDoppelStd) {
            uriKlasse = VplanContract.Plan.CONTENT_URI.buildUpon().appendPath(selectedKlasse).appendPath(VplanContract.PATH_PART_KOMP_DOPPELSTD).build();
        } else {
            uriKlasse = VplanContract.Plan.CONTENT_URI.buildUpon().appendPath(selectedKlasse).build();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
