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

import com.strobelb69.vplan.data.VplanContract;
import com.strobelb69.vplan.data.VplanParser;

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
        nm.cancel(NOTIFICATION_ID);
    }


    private void showNotification() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String notifKey = getString(R.string.prefKeySendNotification);

        if (prefs.getBoolean(notifKey,true) && isNewDataForKlasseOrZusInfo(ctx, prefs)) {
            Intent mainIntent = new Intent(ctx, MainActivity.class);

            TaskStackBuilder sb = TaskStackBuilder.create(ctx);
            sb.addParentStack(MainActivity.class);
            sb.addNextIntent(mainIntent);

            PendingIntent pi = sb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx)
                    .setSmallIcon(R.drawable.bs_icon_notification)
                    .setContentTitle(getString(R.string.notificationTitle))
                    .setContentText(getString(R.string.notificationText))
                    .setContentIntent(pi);

            NotificationManager nmgr =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nmgr.notify(NOTIFICATION_ID, nb.build());
        }
    }

    private boolean isNewDataForKlasseOrZusInfo(Context ctx, SharedPreferences prefs) {
        String klasse = prefs.getString(getString(R.string.prefKeyKlasse),"XXX");
        Cursor klAktCrs = ctx.getContentResolver().query(
                VplanContract.KlassenAktualisiert.CONTENT_URI,
                null,
                VplanContract.KlassenAktualisiert.COL_KLASSE + " IN (?,?)",
                new String[]{klasse, VplanParser.ZUSINFO_TAG},
                null);
        boolean returnVal = false;
        if (klAktCrs !=null) {
            returnVal = klAktCrs.getCount() > 0;
            klAktCrs.close();
        }
        return returnVal;
    }
}
