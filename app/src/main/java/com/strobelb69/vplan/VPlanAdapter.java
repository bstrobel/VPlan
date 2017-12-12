package com.strobelb69.vplan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
    private final String lblBlock;
    private final String lblStunde;
    private boolean isKeyKomprDoppelStd;
    private final String LT = getClass().getSimpleName();

    public VPlanAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        keyKonprDoppelStd = context.getString(R.string.prefKeyDoppelstunde);
        lblBlock = context.getString(R.string.lblBlock);
        lblStunde = context.getString(R.string.lblStunde);
        onSharedPreferenceChanged(prefs, keyKonprDoppelStd);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View newView(Context ctx, Cursor cursor, ViewGroup vg) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.list_item_vplan,vg,false);
        v.setTag(new ViewHolder(v));
        return v;
    }

    @Override
    public void bindView(View v, Context ctx, Cursor c) {
        String stunde = c.getString(COL_STUNDE);
        try {
            if (isKeyKomprDoppelStd) {
                int stundeInt = Integer.parseInt(stunde);
//                stunde = stundeInt + "+" + (stundeInt + 1);
                stunde = String.valueOf((stundeInt + 1)/2) + lblBlock;
            } else {
                stunde = stunde + lblStunde;
            }
        } catch (NumberFormatException e) {
            Log.w(LT,"String for stunde cannot be parsed to int: "+stunde);
        }

        ViewHolder vh = (ViewHolder) v.getTag();
        vh.tvStunde.setText(stunde);
        vh.tvFach.setText(c.getString(COL_FACH));
        vh.tvRaum.setText(c.getString(COL_RAUM));
        vh.tvLehrer.setText(c.getString(COL_LEHRER));
        vh.tvInfo.setText(c.getString(COL_INF));

        if (c.getInt(COL_FACH_NEU) == 1) {
            vh.tvFach.setTextColor(Color.RED);
        } else {
            vh.tvFach.setTextColor(vh.tvStunde.getTextColors());
        }
        if (c.getInt(COL_LEHRER_NEU) == 1) {
            vh.tvLehrer.setTextColor(Color.RED);
        } else {
            vh.tvLehrer.setTextColor(vh.tvStunde.getTextColors());
        }
        if (c.getInt(COL_RAUM_NEU) == 1) {
            vh.tvRaum.setTextColor(Color.RED);
        } else {
            vh.tvRaum.setTextColor(vh.tvStunde.getTextColors());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key != null && key.equals(keyKonprDoppelStd)) {
            isKeyKomprDoppelStd = prefs.getBoolean(keyKonprDoppelStd,MainActivity.prefDefDoppelstunde);
        }
    }

    public static class ViewHolder {
        TextView tvStunde;
        TextView tvFach;
        TextView tvRaum;
        TextView tvLehrer;
        TextView tvInfo;

        public ViewHolder(View v) {
            tvStunde = (TextView) v.findViewById(R.id.list_item_vplan_stunde);
            tvFach = (TextView) v.findViewById(R.id.list_item_vplan_fach);
            tvRaum = (TextView) v.findViewById(R.id.list_item_vplan_raum);
            tvLehrer = (TextView) v.findViewById(R.id.list_item_vplan_lehrer);
            tvInfo = (TextView) v.findViewById(R.id.list_item_vplan_info);
        }
    }
}
