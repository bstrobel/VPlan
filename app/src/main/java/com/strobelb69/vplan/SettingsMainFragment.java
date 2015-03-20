package com.strobelb69.vplan;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
public class SettingsMainFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KLASSE_KURS_SEP = "~";
    private SharedPreferences sPref;
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

        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sPref.registerOnSharedPreferenceChangeListener(this);

        onSharedPreferenceChanged(sPref, prefKlasse.getKey());
    }

    /*
        Set the Summaries of the preferences according to the new Settings.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (isAdded()) {
            String keyKlasse = getString(R.string.prefKeyKlasse);
            ListPreference klassePref = (ListPreference) findPreference(keyKlasse);
            String klasse = klassePref.getValue();
            if (key.equals(keyKlasse)) {
                klassePref.setSummary(klassePref.getValue());
                setKursListSummary(klasse);
            } else if (key.contains(klasse + KLASSE_KURS_SEP)) {
                setKursListSummary(klasse);
            }
        }

    }

    private void setKursListSummary(String klasse) {
        Preference kursScreen = findPreference(getString(R.string.prefKeyKurs));
        Uri uriKurseFuerKlasse = VplanContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(VplanContract.PATH_KURSE)
                .appendQueryParameter(VplanContract.PARAM_KEY_KLASSE,klasse)
                .build();
        Cursor c = getActivity().getContentResolver().query(uriKurseFuerKlasse,new String[]{VplanContract.Kurse.COL_KURS, VplanContract.Kurse.COL_LEHRER},null,null,null);
        if (c!=null) {
            StringBuilder sb = new StringBuilder();
            while (c.moveToNext()) {
                String kurs = c.getString(0);
                String key = klasse+KLASSE_KURS_SEP+kurs;
                boolean isSelected = sPref.getBoolean(key, true);
                if (isSelected) {
                    if (sb.length()>0) {
                        sb.append(", ");
                    }
                    sb.append(kurs);
                }
            }
            kursScreen.setSummary(sb.toString());
        }
    }
}