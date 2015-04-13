package com.strobelb69.vplan.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.http.HttpResponseCache;
import android.os.Build;
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
        enableHttpResponseCache();
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
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
                Log.d(LT,"Something happened, showing notification!");
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


    // http://android-developers.blogspot.de/2011/09/androids-http-clients.html
    // http://developer.android.com/reference/android/net/http/HttpResponseCache.html
    private void enableHttpResponseCache() {
        try {
            long httpCacheSize = 1 * 1024 * 1024; // 1 MiB
            File httpCacheDir = new File(getContext().getCacheDir(), "http");
            HttpResponseCache.install(httpCacheDir,httpCacheSize);
        } catch (IOException ex) {
            Log.i(LT, "Setting up HTTP cache failed!\n" + ex);
        }
    }
}
