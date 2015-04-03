package com.strobelb69.vplan.sync;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * Created by bstrobel on 18.03.2015.
 */
public class VplanSyncService extends Service {
    public static final String EXTRAS_KEY_FROM_APP = "keyFromApp";
    public static final int NOTIFICATIONS_OFF = 0;
    public static final int NOTIFICATIONS_ON = 1;
    private static VplanSyncAdapter vplanSyncAdapter = null;
    private static final Object vplanSyncAdapterLock = new Object();
    public final String LT = getClass().getSimpleName();
    private Messenger vplanSyncMessenger = new Messenger(new VplanSyncServiceHandler());

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
        Bundle extras = intent.getExtras();
        if (extras != null && extras.getBoolean(EXTRAS_KEY_FROM_APP,false)) {
            Log.d(LT,"Returning the MessengerBinder!");
            return vplanSyncMessenger.getBinder();
        } else {
            Log.d(LT,"Returning the SyncAdapterBinder!");
            return vplanSyncAdapter.getSyncAdapterBinder();
        }
    }


    public class VplanSyncServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(LT,"Message received: " + msg);
            switch (msg.what) {
                case NOTIFICATIONS_OFF:
                    vplanSyncAdapter.setShowNotification(false);
                    break;
                case NOTIFICATIONS_ON:
                default:
                    vplanSyncAdapter.setShowNotification(true);
            }
        }
    }

}
