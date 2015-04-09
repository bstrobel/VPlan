package com.strobelb69.vplan.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.strobelb69.vplan.MainActivity;
import com.strobelb69.vplan.R;
import com.strobelb69.vplan.VplanNotificationService;
import com.strobelb69.vplan.data.VplanParser;
import com.strobelb69.vplan.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bstrobel on 18.03.2015.
 */
public class VplanSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int DEF_SYNCINTERVAL = 30 * 60;
    public static final int DEF_FLEXTIME = DEF_SYNCINTERVAL /3;

    private final String LT = getClass().getSimpleName();

    private final VplanParser vplanParser;
    private final String urlStr;

    public VplanSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        vplanParser = new VplanParser(context);
        urlStr = context.getString(R.string.vplanUrl);
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
        } catch (IOException e) {
            Log.e(LT,e.getMessage()
                    + " while getting URL " + urlStr
                    + "\nStackTrace:\n" + Utils.getStackTraceString(e));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(LT,e.getMessage()
                            + " while getting URL " + urlStr
                            + "\nStackTrace:\n" + Utils.getStackTraceString(e));
                }
            }
        }
    }

    public static void syncImmediately(Context ctx) {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(ctx), ctx.getString(R.string.vplan_provider_authority), b);
    }

    public static Account getSyncAccount(Context ctx) {
        AccountManager mgr = (AccountManager) ctx.getSystemService(Context.ACCOUNT_SERVICE);
        Account acc = new Account(ctx.getString(R.string.app_name),ctx.getString(R.string.vplan_authenticator_account_type));
        if (mgr.getPassword(acc) == null) {
            if (!mgr.addAccountExplicitly(acc, "", null)) {
                return null;
            }
            onAccountCreated(acc, ctx);
        }
        return acc;
    }

    private static void onAccountCreated(Account acc, Context ctx) {
        configurePeriodicSync(ctx, DEF_SYNCINTERVAL, DEF_FLEXTIME);
        ContentResolver.setSyncAutomatically(acc,ctx.getString(R.string.vplan_provider_authority),true);
        syncImmediately(ctx);
    }

    public static void configurePeriodicSync(Context ctx, int syncInterval, int flexTime) {
        Account account = getSyncAccount(ctx);
        String authority = ctx.getString(R.string.vplan_provider_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void initializeSyncAdapter(Context ctx) {
        getSyncAccount(ctx);
    }
}
