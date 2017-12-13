package com.strobelb69.vplan;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateFormat;

import com.strobelb69.vplan.data.VplanContract;
import com.strobelb69.vplan.data.VplanParser;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VplanNotificationService extends IntentService {
    private static final int NOTIFICATION_ID = 0;
    private static final String CLEAR_NOTIFICATION_KEY = "clearNotification";
    private static final String SHOW_NOTIFICATION_KEY = "showNotification";

    public VplanNotificationService() {
        super("VplanNotificationService");
    }

    public static void showNotification(Context ctx) {
        Intent intent = new Intent(ctx, VplanNotificationService.class);
        intent.setAction(VplanNotificationService.SHOW_NOTIFICATION_KEY);
        ctx.startService(intent);
    }

    public static void clearNotification(Context ctx) {
        Intent intent = new Intent(ctx, VplanNotificationService.class);
        intent.setAction(VplanNotificationService.CLEAR_NOTIFICATION_KEY);
        ctx.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (CLEAR_NOTIFICATION_KEY.equals(action)) {
                clearNotification();
            } else if (SHOW_NOTIFICATION_KEY.equals(action)) {
                showNotification();
            }
        }
    }

    private void clearNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIFICATION_ID);
        }
    }


    private void showNotification() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String notifKey = getString(R.string.prefKeySendNotification);

        if (prefs.getBoolean(notifKey,true)) {

            boolean isNeuerTag = isNewDataForVal(ctx, VplanParser.NEUER_TAG_TAG);
            boolean isZusInfoAkt = false;
            boolean isKlassePlanAkt = false;
            String klasse = prefs.getString(getString(R.string.prefKeyKlasse),"XXX");

            if (!isNeuerTag) {
                isZusInfoAkt = isNewDataForVal(ctx, VplanParser.ZUSINFO_TAG);
                isKlassePlanAkt = isNewDataForVal(ctx, klasse);
            }
            if (isNeuerTag || isZusInfoAkt || isKlassePlanAkt) {
                String text;
                if (isNeuerTag) {
                    text = String.format(getString(R.string.notificationTextNeuerTag),
                            getDateAkt(ctx));
                } else {
                    if (isKlassePlanAkt) {
                        text = String.format(getString(R.string.notificationTextKlasse), klasse);
                    } else {
                        text = getString(R.string.notificationTextZusInfo);
                    }
                }

                Intent mainIntent = new Intent(ctx, MainActivity.class);

                TaskStackBuilder sb = TaskStackBuilder.create(ctx);
                sb.addParentStack(MainActivity.class);
                sb.addNextIntent(mainIntent);

                PendingIntent pi = sb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.bs_icon_notification)
                        .setContentTitle(getString(R.string.notificationTitle))
                        .setContentText(text)
                        .setContentIntent(pi);

                NotificationManager nmgr =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (nmgr != null) {
                    nmgr.notify(NOTIFICATION_ID, nb.build());
                }
            }
        }
    }

    private boolean isNewDataForVal(Context ctx, String val) {
        Cursor c = ctx.getContentResolver().query(
                VplanContract.KlassenAktualisiert.CONTENT_URI,
                null,
                VplanContract.KlassenAktualisiert.COL_KLASSE + " = ?",
                new String[]{val},
                null);
        boolean returnVal = false;
        if (c !=null) {
            returnVal = c.getCount() > 0;
            c.close();
        }
        return returnVal;
    }

    private String getDateAkt(Context ctx) {
        Cursor c = ctx.getContentResolver().query(
                VplanContract.Kopf.CONTENT_URI,
                new String[]{VplanContract.Kopf.COL_FOR_DATE},
                null,
                null,
                null);
        String returnVal = "";
        if (c !=null) {
            if (c.moveToFirst()) {
                Date date = new Date(c.getLong(0));
                returnVal = new SimpleDateFormat("EEE ").format(date) +
                        DateFormat.getDateFormat(ctx).format(date);
            }
            c.close();
        }
        return returnVal;
    }
}
