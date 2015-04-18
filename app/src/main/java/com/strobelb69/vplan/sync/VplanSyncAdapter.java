package com.strobelb69.vplan.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.util.Log;

import com.strobelb69.vplan.R;
import com.strobelb69.vplan.VplanNotificationService;
import com.strobelb69.vplan.data.VplanParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * SyncAdapter that synchronizes the ContentProvider with data from the Internet.
 * Created by bstrobel on 18.03.2015.
 */
public class VplanSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String LT = getClass().getSimpleName();

    private final VplanParser vplanParser;
    private final String urlStr;

    public VplanSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        vplanParser = new VplanParser(context);
        urlStr = context.getString(R.string.vplanUrl);
        // we can't use it because the webserver does not work correctly. Not our fault
        //enableHttpResponseCache();
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
        Log.d(LT, "extras: " + extras);
        boolean isForced = extras != null
                && extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);

        if (isForced || isSyncTime()) {
            InputStream is = null;
            try {
                Log.i(LT, "Vplan sync started");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                if (vplanParser.retrievePlan(is)) {
                    Log.d(LT, "Something happened, showing notification!");
                    VplanNotificationService.showNotification(getContext());
                }
                Log.i(LT, "Vplan sync finished");
            } catch (IOException ex) {
                Log.e(LT,"Error while getting URL " + urlStr
                        + "\n" + ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Log.e(LT, "Error while getting URL " + urlStr
                                + "\n" + ex);
                    }
                }
            }
        }
    }


    // http://android-developers.blogspot.de/2011/09/androids-http-clients.html
    // http://developer.android.com/reference/android/net/http/HttpResponseCache.html
    private void enableHttpResponseCache() {
        try {
            if (HttpResponseCache.getInstalled() != null) {
                long httpCacheSize = 1 * 1024 * 1024; // 1 MiB
                File httpCacheDir = new File(getContext().getCacheDir(), "http");
                HttpResponseCache.install(httpCacheDir, httpCacheSize);
            }
        } catch (IOException ex) {
            Log.i(LT, "Setting up HTTP cache failed!\n" + ex);
        }
    }

    private boolean isSyncTime() {
        Date now = new Date();
        TimeZone tzKantGym = TimeZone.getTimeZone("Europe/Berlin");
        Log.d(LT, "Current time in millis: " + now.getTime());
        boolean val = now.getTime() > getLocalizedTimeMillis(tzKantGym, now, 6, 0) &&
                now.getTime() < getLocalizedTimeMillis(tzKantGym, now, 21, 0);
        if (!val) {
            Log.d(LT, "Skipping sync because we're outside of sync hours!");
        }
        return val;
    }

    private long getLocalizedTimeMillis(TimeZone tz, Date t, int h, int m) {
        Calendar c = Calendar.getInstance(tz);
        c.setTime(t);
        if (h < 12) {
            c.set(Calendar.AM_PM, Calendar.AM);
        }
        c.set(Calendar.HOUR,h);
        c.set(Calendar.MINUTE,m);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        Log.d(LT, "Calendars time in millis: " + c.getTimeInMillis());
        return c.getTimeInMillis();
    }
}
