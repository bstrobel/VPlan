package com.strobelb69.vplan;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.strobelb69.vplan.data.VplanContract;

/**
 * Creates dynamically a PreferenceFragment with a list of available Kurse for the Klasse
 *
 * Created by bstrobel on 20.03.2015.
 */
public class SettingsKurseFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen kursScreen = getPreferenceManager().createPreferenceScreen(getActivity());
        SharedPreferences sPref = getPreferenceManager().getSharedPreferences();
        String klasse = sPref.getString(getString(R.string.prefKeyKlasse), getString(R.string.prefDefKlasse));

        Uri uriKurseFuerKlasse = VplanContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(VplanContract.PATH_KURSE)
                .appendQueryParameter(VplanContract.PARAM_KEY_KLASSE, klasse)
                .build();

        Cursor c = getActivity()
                .getContentResolver()
                .query(
                        uriKurseFuerKlasse,
                        new String[]{VplanContract.Kurse.COL_KURS, VplanContract.Kurse.COL_LEHRER},
                        null, null, null);
        if (c!=null) {
            while (c.moveToNext()) {
                String kurs = c.getString(0);
                String key = klasse+ SettingsMainFragment.KLASSE_KURS_SEP+kurs;
                boolean isSelected = sPref.getBoolean(key, true);
                CheckBoxPreference cbp = new CheckBoxPreference(getActivity());
                cbp.setTitle(kurs + " - " + c.getString(1));
                cbp.setKey(key);
                cbp.setChecked(isSelected);
                kursScreen.addPreference(cbp);
            }
            c.close();
        }
        setPreferenceScreen(kursScreen);
   }
}
