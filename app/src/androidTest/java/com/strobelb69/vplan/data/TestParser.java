package com.strobelb69.vplan.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

import com.strobelb69.vplan.net.VplanRetriever;

/**
 * Created by Bernd on 15.03.2015.
 */
public class TestParser extends AndroidTestCase {
    private final String LT = getClass().getSimpleName();
    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    public void testToParse() {
        new VplanParser(mContext).retrievePlan(getClass().getResourceAsStream("Klassen.xml"));
//        VplanRetriever rt = new VplanRetriever(mContext);
//        rt.retrieve();
        ContentResolver cntrslv = mContext.getContentResolver();

        Cursor crsKopf = cntrslv.query(VplanContract.Kopf.CONTENT_URI,null,null,null,null);
        assertTrue(crsKopf.isBeforeFirst());
        assertTrue("Tabelle kopf nicht gefüllt!", crsKopf.getCount() == 1);
        Cursor crsFreieTage = cntrslv.query(VplanContract.FreieTage.CONTENT_URI,null,null,null,null);
        assertTrue(crsFreieTage.isBeforeFirst());
        assertTrue("Tabelle freietage nicht gefüllt!", crsFreieTage.getCount() > 0);
        Cursor crsKlassen = cntrslv.query(VplanContract.Klassen.CONTENT_URI,null,null,null,null);
        assertTrue(crsKlassen.isBeforeFirst());
        assertTrue("Tabelle klassen nicht gefüllt!", crsKlassen.getCount() > 0);
        Cursor crsKurse = cntrslv.query(VplanContract.Kurse.CONTENT_URI,null,null,null,null);
        assertTrue(crsKurse.isBeforeFirst());
        assertTrue("Tabelle kurse nicht gefüllt!", crsKurse.getCount() > 0);
        Cursor crsPlan = cntrslv.query(VplanContract.Plan.CONTENT_URI,null,null,null,null);
        assertTrue(crsPlan.isBeforeFirst());
        assertTrue("Tabelle plan nicht gefüllt!", crsPlan.getCount() > 0);


        while (crsFreieTage.moveToNext()) {
            Log.d(LT,crsFreieTage.getString(1));
        }
        while (crsKlassen.moveToNext()) {
            Log.d(LT,"Id=" + crsKlassen.getLong(0) + ", " + crsKlassen.getString(1));
        }
    }
}
