package com.strobelb69.vplan;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.strobelb69.vplan.data.VplanContract;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by bstrobel on 16.03.2015.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // build the values for the Klassen list.
        ListPreference prefKlasse = (ListPreference) findPreference(getString(R.string.prefKeyKlasse));
        ContentResolver crslv = getActivity().getContentResolver();
        Cursor klCrs = crslv.query(
                VplanContract.Klassen.CONTENT_URI,
                new String[]{VplanContract.Klassen.COL_KLASSE},
                null,
                null,
                VplanContract.Klassen._ID + " ASC");
        List<String> klassen = new LinkedList<>();
        while (klCrs.moveToNext()) {
            klassen.add(klCrs.getString(0));
        }
        klCrs.close();
        String[] klassenA = klassen.toArray(new String[klassen.size()]);
        prefKlasse.setEntries(klassenA); //Actual values for the setting (could be _ID for example)
        prefKlasse.setEntryValues(klassenA); //Display values

        // Register us as a listener for changes in the preference
        prefKlasse.setOnPreferenceChangeListener(this);

        // Call the listener to set the ListPreference to the last selection.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(prefKlasse.getContext());
        String currKlasse = prefs.getString(prefKlasse.getKey(),klassen.get(0));
        onPreferenceChange(prefKlasse, currKlasse);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newpfv) {
        String strV = newpfv.toString();
        if (pref instanceof ListPreference) {
            ListPreference lp = (ListPreference) pref;
            int i = lp.findIndexOfValue(strV);
            if (i >= 0) {
                CharSequence value = lp.getEntries()[i];
                lp.setSummary(value);
            }
        }
        return true;
    }
}
