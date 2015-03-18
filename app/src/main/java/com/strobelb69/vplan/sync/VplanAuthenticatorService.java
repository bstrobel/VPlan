package com.strobelb69.vplan.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by bstrobel on 18.03.2015.
 */
public class VplanAuthenticatorService extends Service {
    private AbstractAccountAuthenticator authenticator;
    public VplanAuthenticatorService() {
    }

    @Override
    public void onCreate() {
        authenticator = new VplanDummyAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
