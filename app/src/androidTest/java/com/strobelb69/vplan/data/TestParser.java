package com.strobelb69.vplan.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.strobelb69.vplan.net.VplanRetriever;

import java.io.File;
import java.io.InputStream;

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
//        Das funktioniert nicht. Wahrscheinlich geht getResourceAsStream() nicht in Android
//        oder es geht bei Tests nicht.
//        InputStream is = getClass().getResourceAsStream("com/strobelb69/vplan/data/Klassen.xml");
//        assertNotNull("Test XML Klassen.xml nicht gefunden!",is);
//        new VplanParser(mContext).retrievePlan(is);
        VplanRetriever rt = new VplanRetriever(mContext);
        rt.retrieveFromNet();
        ContentResolver cntrslv = mContext.getContentResolver();

        Cursor crsKopf = cntrslv.query(VplanContract.Kopf.CONTENT_URI,null,null,null,null);
        assertTrue(crsKopf.isBeforeFirst());
        assertTrue("Tabelle kopf nicht gefüllt!", crsKopf.getCount() == 1);
        crsKopf.close();

        Cursor crsFreieTage = cntrslv.query(VplanContract.FreieTage.CONTENT_URI,null,null,null,null);
        assertTrue(crsFreieTage.isBeforeFirst());
        assertTrue("Tabelle freietage nicht gefüllt!", crsFreieTage.getCount() > 0);
        crsFreieTage.close();

        Cursor crsKlassen = cntrslv.query(VplanContract.Klassen.CONTENT_URI,null,null,null,null);
        assertTrue(crsKlassen.isBeforeFirst());
        assertTrue("Tabelle klassen nicht gefüllt!", crsKlassen.getCount() > 0);
        crsKlassen.close();

        Cursor crsKurse = cntrslv.query(VplanContract.Kurse.CONTENT_URI,null,null,null,null);
        assertTrue(crsKurse.isBeforeFirst());
        assertTrue("Tabelle kurse nicht gefüllt!", crsKurse.getCount() > 0);
        crsKurse.close();

        Cursor crsPlan = cntrslv.query(VplanContract.Plan.CONTENT_URI,null,null,null,null);
        assertTrue(crsPlan.isBeforeFirst());
        assertTrue("Tabelle plan nicht gefüllt!", crsPlan.getCount() > 0);
        crsPlan.close();

        String testKlasse = "8c";
        Uri uriKurse8c = VplanContract.Kurse.CONTENT_URI.buildUpon().appendPath(testKlasse).build();
        String type = cntrslv.getType(uriKurse8c);
        assertEquals("Wrong type returned: " + type, type, VplanContract.Kurse.CONTENT_ITEM_TYPE);
        String[] projKurse = new String[] {
                VplanContract.Kurse.COL_KURS,
                VplanContract.Kurse.COL_LEHRER
        };
        crsKurse = cntrslv.query(uriKurse8c,projKurse,null,null,null);
        assertTrue(crsKurse.isBeforeFirst());
        assertTrue("Tabelle kurse enthält nichts für klasse="+testKlasse, crsKurse.getCount() > 0);

        while (crsKurse.moveToNext()) {
            Log.d(LT, testKlasse + ": Kurs" + crsKurse.getString(0) + "bei Lehrer " + crsKurse.getString(1));
        }
        crsKurse.close();

        Uri uriPlan8C = VplanContract.Plan.CONTENT_URI.buildUpon().appendPath(testKlasse).build();
        String typePlan = cntrslv.getType(uriPlan8C);
        assertEquals("Wrong type returned: " + typePlan, typePlan, VplanContract.Plan.CONTENT_ITEM_TYPE);
        String[] projPlan = new String[] {
                VplanContract.Plan.COL_STUNDE,
                VplanContract.Plan.COL_FACH,
                VplanContract.Plan.COL_FACH_NEU,
                VplanContract.Plan.COL_LEHRER,
                VplanContract.Plan.COL_LEHRER_NEU,
                VplanContract.Plan.COL_RAUM,
                VplanContract.Plan.COL_RAUM_NEU,
                VplanContract.Plan.COL_INF
        };
        crsPlan = cntrslv.query(uriPlan8C,projPlan,null,null,null);
        assertTrue(crsPlan.isBeforeFirst());
        assertTrue("Tabelle plan enthält nichts für klasse="+testKlasse, crsPlan.getCount() > 0);
        while (crsPlan.moveToNext()) {
            Log.d(LT,
                    "Plan für Klasse "+testKlasse+":\n" +
                    VplanContract.Plan.COL_STUNDE + "=" + crsPlan.getString(0) + "\n" +
                    VplanContract.Plan.COL_FACH + "=" + crsPlan.getString(1) + "\n" +
                    VplanContract.Plan.COL_FACH_NEU + "=" + crsPlan.getString(2) + "\n" +
                    VplanContract.Plan.COL_LEHRER + "=" + crsPlan.getString(3) + "\n" +
                    VplanContract.Plan.COL_LEHRER_NEU + "=" + crsPlan.getString(4) + "\n" +
                    VplanContract.Plan.COL_RAUM + "=" + crsPlan.getString(5) + "\n" +
                    VplanContract.Plan.COL_RAUM_NEU + "=" + crsPlan.getString(6) + "\n" +
                    VplanContract.Plan.COL_INF + "=" + crsPlan.getString(7)
            );
        }
        crsPlan.close();
    }
}
