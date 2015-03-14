package com.strobelb69.vplan.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.test.AndroidTestCase;

/**
 * Created by Bernd on 14.03.2015.
 */
public class TestProvider extends AndroidTestCase {

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                VplanContract.Plan.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                VplanContract.Kurse.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                VplanContract.Klassen.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                VplanContract.FreieTage.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                VplanContract.Kopf.CONTENT_URI,
                null,
                null
        );

        Cursor c = mContext.getContentResolver().query(
                VplanContract.Kopf.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, c.getCount());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }


    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                VplanProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: VplanProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + VplanContract.CONTENT_AUTHORITY,
                    providerInfo.authority, VplanContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: VplanProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }
}
