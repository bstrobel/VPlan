package com.strobelb69.vplan;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Bernd on 15.03.2015.
 */
public class VPlanAdapter extends CursorAdapter implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int COL_ID = 0;
    public static final int COL_STUNDE = 1;
    public static final int COL_FACH = 2;
    public static final int COL_FACH_NEU = 3;
    public static final int COL_LEHRER = 4;
    public static final int COL_LEHRER_NEU = 5;
    public static final int COL_RAUM = 6;
    public static final int COL_RAUM_NEU = 7;
    public static final int COL_INF = 8;
    private final String keyKonprDoppelStd;
    private boolean isKeyKomprDoppelStd;
    private final String LT = getClass().getSimpleName();

    public VPlanAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        keyKonprDoppelStd = context.getString(R.string.prefKeyDoppelstunde);
        onSharedPreferenceChanged(prefs, keyKonprDoppelStd);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View newView(Context ctx, Cursor cursor, ViewGroup vg) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.list_item_vplan,vg,false);
        return v;
    }

    @Override
    public void bindView(View v, Context ctx, Cursor c) {
        String stunde = c.getString(COL_STUNDE);
        try {
            int stundeInt = Integer.parseInt(stunde);
            if (isKeyKomprDoppelStd) {
                stunde = stundeInt + "+" + (stundeInt + 1);
            }
        } catch (NumberFormatException e) {
            Log.w(LT,"String for stunde cannot be parsed to int: "+stunde);
        }
        String line = String.format("%s: Fach %s, Lehrer %s, Raum %s - %s",
                stunde,
                c.getString(COL_FACH),
                c.getString(COL_LEHRER),
                c.getString(COL_RAUM),
                c.getString(COL_INF));

        TextView tv = (TextView) v;
        tv.setText(line);
        if (c.getInt(COL_FACH_NEU) == 1 ||
                c.getInt(COL_LEHRER_NEU) == 1 ||
                c.getInt(COL_RAUM_NEU) == 1) {
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key != null && key.equals(keyKonprDoppelStd)) {
            isKeyKomprDoppelStd = prefs.getBoolean(keyKonprDoppelStd,isKeyKomprDoppelStd);
        }
    }
}
