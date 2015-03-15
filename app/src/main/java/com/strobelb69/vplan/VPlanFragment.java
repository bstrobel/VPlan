package com.strobelb69.vplan;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
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
public class VPlanFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
    private int THIS_LOADER = 0;
    VPlanAdapter vplanAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
            new UpdatePlan().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private class UpdatePlan extends AsyncTask <Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            new VplanRetriever(getActivity()).retrieveFromNet();
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vplanAdapter = new VPlanAdapter(getActivity(), null, 0);

        View rv = inflater.inflate(R.layout.vplan_fragment,container,false);
        ListView lv = (ListView) rv.findViewById(R.id.listview_vplan);
        lv.setAdapter(vplanAdapter);
        return rv;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().restartLoader(THIS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uriPlan8C = VplanContract.Plan.CONTENT_URI.buildUpon().appendPath("8c").build();
        return new CursorLoader(getActivity(),uriPlan8C,PROJECTION_PLAN,null,null,null);
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
