package com.strobelb69.vplan.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.strobelb69.vplan.R;

import java.io.InputStream;

/**
 * Tests whole Parser/Provider/Db subsystem
 *
 * Created by Bernd on 15.03.2015.
 */
public class TestParser extends AndroidTestCase {
    private final String LT = getClass().getSimpleName();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testQueryKlasseKurse() throws Exception {
        deleteOldTables();
        InputStream is = mContext.getResources().openRawResource(R.raw.klassen_org);
        assertTrue("retrievePlan() hat nicht true returned!", new VplanParser(mContext).retrievePlan(is));
        is.close();
        String klasse = "8c";
        String kurs = "GEWI_1";
//        String klasse = "5b";
//        String kurs = "Ree5";
        Uri uriKlasse;
        uriKlasse = VplanContract.Plan.CONTENT_URI.buildUpon().appendQueryParameter(VplanContract.PARAM_KEY_KLASSE,klasse).build();
        Uri uriKurse = VplanContract.Kurse.CONTENT_URI.buildUpon().appendQueryParameter(VplanContract.PARAM_KEY_KLASSE, klasse).build();

        Uri.Builder urib = uriKlasse.buildUpon();
        urib.appendQueryParameter(VplanContract.PARAM_KEY_KURS,kurs);
        StringBuilder sb = new StringBuilder();
        VplanParser.logContentsDbTable(mContext, sb, "Plan für "+klasse+" ohne Kurs "+kurs, urib.build());
        VplanParser.logContentsDbTable(mContext, sb, "Plan für "+klasse , uriKlasse);
        VplanParser.logContentsDbTable(mContext, sb, "Kurse für Klasse " + klasse, uriKurse);
        System.out.println(sb.toString());
        deleteOldTables();
    }

    public void testUpdate() throws Exception{
        deleteOldTables();
        final InputStream is = mContext.getResources().openRawResource(R.raw.klassen_zus_inf);
        assertTrue("retrievePlan() hat nicht true returned!", new VplanParser(mContext).retrievePlan(is));
        is.close();
        final InputStream is2 = mContext.getResources().openRawResource(R.raw.klassen_zus_inf_upd_same_day);
        assertTrue("retrievePlan() hat nicht true returned!", new VplanParser(mContext).retrievePlan(is2));
        is2.close();

        Cursor klAktCrs = mContext
                .getContentResolver()
                .query(
                        VplanContract.KlassenAktualisiert.CONTENT_URI,
                        new String[]{VplanContract.KlassenAktualisiert.COL_KLASSE},
                        null,
                        null,
                        VplanContract.KlassenAktualisiert.COL_KLASSE + " ASC");
        StringBuilder sb = new StringBuilder();
        VplanParser.logContentsDbTable(mContext, sb, VplanContract.KlassenAktualisiert.TABLE_NAME, VplanContract.KlassenAktualisiert.CONTENT_URI);
        assertTrue("Erste Zeile nicht vorhanden!", klAktCrs.moveToNext());
        assertEquals("Klasse 5a erwartet", "5a", klAktCrs.getString(0));
        assertTrue("Zweite Zeile nicht vorhanden!", klAktCrs.moveToNext());
        assertEquals("Klasse 5b erwartet", "5b", klAktCrs.getString(0));
        assertEquals("Zuviele Daten in " + VplanContract.KlassenAktualisiert.TABLE_NAME + "\n" + sb.toString(), 2, klAktCrs.getCount());
        klAktCrs.close();
        System.out.println(sb.toString());
        deleteOldTables();
    }

    public void testNoUpdate() throws Exception {
        deleteOldTables();
        final InputStream is = mContext.getResources().openRawResource(R.raw.klassen_zus_inf);
        assertTrue("retrievePlan() hat nicht true returned!", new VplanParser(mContext).retrievePlan(is));
        is.close();
        final InputStream is2 = mContext.getResources().openRawResource(R.raw.klassen_zus_inf);
        assertFalse("retrievePlan() hat nicht falsereturned beim 2. Mal Einlesen!", new VplanParser(mContext).retrievePlan(is2));
        is.close();
        deleteOldTables();
    }

    public void testUpdateZusInfo1() throws Exception{
        deleteOldTables();
        final InputStream is = mContext.getResources().openRawResource(R.raw.klassen_zus_inf);
        assertTrue("retrievePlan() hat nicht true returned!", new VplanParser(mContext).retrievePlan(is));
        is.close();
        final InputStream is2 = mContext.getResources().openRawResource(R.raw.klassen_zus_inf_neu_zus_inf1);
        assertTrue("retrievePlan() hat nicht true returned!", new VplanParser(mContext).retrievePlan(is2));
        is2.close();

        Cursor klAktCrs = mContext
                .getContentResolver()
                .query(
                        VplanContract.KlassenAktualisiert.CONTENT_URI,
                        new String[]{VplanContract.KlassenAktualisiert.COL_KLASSE},
                        null,
                        null,
                        VplanContract.KlassenAktualisiert.COL_KLASSE + " ASC");
        assertEquals("Zuviele Daten in " + VplanContract.KlassenAktualisiert.TABLE_NAME, 1, klAktCrs.getCount());
        klAktCrs.close();

        Cursor klAktCrs2 = mContext
                .getContentResolver()
                .query(
                        VplanContract.KlassenAktualisiert.CONTENT_URI,
                        new String[]{VplanContract.KlassenAktualisiert.COL_KLASSE},
                        VplanContract.KlassenAktualisiert.COL_KLASSE + "= ?",
                        new String[]{VplanParser.ZUSINFO_TAG},
                        null);
        assertEquals(
                VplanParser.ZUSINFO_TAG +
                        " nicht eingefuegt in " +
                        VplanContract.KlassenAktualisiert.TABLE_NAME, 1, klAktCrs2.getCount());
        klAktCrs2.close();

    }

    public void testToParse() throws Exception {
        deleteOldTables();
        InputStream is = mContext.getResources().openRawResource(R.raw.klassen_org);
        assertTrue("retrievePlan() hat nicht true returned!", new VplanParser(mContext).retrievePlan(is));
        is.close();
        ContentResolver cntrslv = mContext.getContentResolver();
        String keyKlasse = VplanContract.PARAM_KEY_KLASSE;

        Cursor crsKopf = cntrslv.query(VplanContract.Kopf.CONTENT_URI,null,null,null,null);
        assertTrue(crsKopf.isBeforeFirst());
        assertTrue("Tabelle kopf nicht gefüllt: " + getTableAsString("kopf",VplanContract.Kopf.CONTENT_URI), crsKopf.getCount() == 1);
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
        while(crsKurse.moveToNext()) {
            Log.d("TESTPARSER",String.format("_ID=%d KLASSEN_ID=%d KURS=%s",crsKurse.getInt(0),crsKurse.getInt(1),crsKurse.getString(2)));
        }
        crsKurse.close();

        Cursor crsPlan = cntrslv.query(VplanContract.Plan.CONTENT_URI,null,null,null,null);
        assertTrue(crsPlan.isBeforeFirst());
        assertTrue("Tabelle plan nicht gefüllt!", crsPlan.getCount() > 0);
        crsPlan.close();

        String testKlasse = "8c";
        Uri uriKurse8c = VplanContract.Kurse.CONTENT_URI.buildUpon().appendQueryParameter(keyKlasse,testKlasse).build();
        String type = cntrslv.getType(uriKurse8c);
        assertEquals("Wrong type returned: " + type, type, VplanContract.Kurse.CONTENT_TYPE);
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

        Uri uriPlan8C = VplanContract.Plan.CONTENT_URI.buildUpon().appendQueryParameter(keyKlasse,testKlasse).build();
        String typePlan = cntrslv.getType(uriPlan8C);
        assertEquals("Wrong type returned: " + typePlan, typePlan, VplanContract.Plan.CONTENT_TYPE);
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
        deleteOldTables();
    }

    private String getTableAsString(String desc, Uri uri) {
        StringBuilder sb = new StringBuilder();
        VplanParser.logContentsDbTable(mContext, sb, desc, uri);
        return sb.toString();
    }

    private void deleteOldTables() {
        ContentResolver cr = mContext.getContentResolver();
        cr.delete(VplanContract.KlassenAktualisiert.CONTENT_URI,null,null);
        cr.delete(VplanContract.Zusatzinfo.CONTENT_URI,null,null);
        cr.delete(VplanContract.Plan.CONTENT_URI,null,null);
        cr.delete(VplanContract.Kurse.CONTENT_URI,null,null);
        cr.delete(VplanContract.Klassen.CONTENT_URI,null,null);
        cr.delete(VplanContract.FreieTage.CONTENT_URI,null,null);
        cr.delete(VplanContract.Kopf.CONTENT_URI,null,null);

    }
}
