package com.strobelb69.vplan;

//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by bstrobel on 16.03.2015.
 */
public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // brauchen wir nicht, weil das Fragment schon in activity_settings.xml deklariert ist.
//        if (savedInstanceState == null) {
//            // add the Fragment
//            FragmentManager fm = getFragmentManager();
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.replace(R.id.container, new SettingsFragment());
//            ft.commit();
//        }

    }
}