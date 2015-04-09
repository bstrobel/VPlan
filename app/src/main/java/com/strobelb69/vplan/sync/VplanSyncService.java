package com.strobelb69.vplan.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by bstrobel on 18.03.2015.
 */
public class VplanSyncService extends Service {
    private static VplanSyncAdapter vplanSyncAdapter = null;
    private static final Object vplanSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (vplanSyncAdapterLock) {
            if (vplanSyncAdapter == null) {
                vplanSyncAdapter = new VplanSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return vplanSyncAdapter.getSyncAdapterBinder();
    }
}
