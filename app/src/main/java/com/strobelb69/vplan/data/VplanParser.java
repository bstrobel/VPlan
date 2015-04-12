package com.strobelb69.vplan.data;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.strobelb69.vplan.BuildConfig;
import com.strobelb69.vplan.R;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Parses the XML data received from an InputStream into the database using a ContenProvider.
 *
 * Created by Bernd on 14.03.2015.
 */
public class VplanParser {
    private final Context ctx;
    private final ContentResolver crslv;
    private final String LT = getClass().getSimpleName();

    public VplanParser(Context ctx) {
        super();
        this.ctx = ctx;
        crslv = ctx.getContentResolver();
    }

    public boolean retrievePlan(InputStream is) {
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            Document planXml = new SAXBuilder().build(is);
            Element re = planXml.getRootElement();
            Element kopf = re.getChild("Kopf");
            boolean isNeuerTag = getIsNeuerTag(kopf);
            boolean isNeueDaten = getIsNeuerZeitStempel(kopf);
            boolean isNewData = isNeuerTag || isNeueDaten;
            if (isNewData) {
                ContentProviderOperation.Builder op =
                        ContentProviderOperation.newDelete(
                                VplanContract.KlassenAktualisiert.CONTENT_URI);
                ops.add(op.build());
                Log.i(LT, "New data received. Data will be parsed and database will be udapted!");
                getKopf(ops, kopf, isNeuerTag);
                getFreieTage(ops, re.getChild("FreieTage"));
                getKlassen(ops, re.getChild("Klassen"), isNeuerTag);
                getZusatzinfo(ops, re.getChild("ZusatzInfo"));
                Log.i(LT, "New data received. Updating database!");
            }
            crslv.applyBatch(ctx.getString(R.string.vplan_provider_authority),ops);
            if (isNewData) {
                logContentsDbTable(ctx, LT, VplanContract.Kopf.TABLE_NAME, VplanContract.Kopf.CONTENT_URI);
                logContentsDbTable(ctx, LT, VplanContract.Klassen.TABLE_NAME, VplanContract.Klassen.CONTENT_URI);
                logContentsDbTable(ctx, LT, VplanContract.Kurse.TABLE_NAME, VplanContract.Kurse.CONTENT_URI);
                logContentsDbTable(ctx, LT, VplanContract.Plan.TABLE_NAME, VplanContract.Plan.CONTENT_URI);
                logContentsDbTable(ctx, LT, VplanContract.Zusatzinfo.TABLE_NAME, VplanContract.Zusatzinfo.CONTENT_URI);
                logContentsDbTable(ctx, LT, VplanContract.KlassenAktualisiert.TABLE_NAME, VplanContract.KlassenAktualisiert.CONTENT_URI);
            }
            return isNewData;
        } catch (Exception ex) {
            Log.e(LT,"Error while parsing XML!\n" + ex);
        }
        return false;
    }

    private void getKopf(ArrayList<ContentProviderOperation> ops, Element kopf, boolean isNeuerTag)
            throws ParseException, RemoteException, OperationApplicationException {
        ops.add(ContentProviderOperation.newDelete(VplanContract.Kopf.CONTENT_URI).build());
        ContentProviderOperation.Builder cpob = ContentProviderOperation.newInsert(VplanContract.Kopf.CONTENT_URI);
        ContentValues cv = new ContentValues();
        cv.put(VplanContract.Kopf.COL_TIMESTAMP, zeitstempelParser(kopf.getChildText("zeitstempel")));
        cv.put(VplanContract.Kopf.COL_FOR_DATE, datumPlanParser(kopf.getChildText("DatumPlan")));
        cv.put(VplanContract.Kopf.COL_LAST_SYNC, new Date().getTime());
        cv.put(VplanContract.Kopf.COL_NEUER_TAG, isNeuerTag);
        ops.add(cpob.withValues(cv).build());
    }

    private boolean getIsNeuerTag(Element kopf) throws ParseException {
        long dateInDb = getKopfColumnAsLong(VplanContract.Kopf.COL_FOR_DATE);
        long dateInXml = datumPlanParser(kopf.getChildText("DatumPlan"));
        Log.d(LT,"dateInXml="+dateInXml+", dateInDb="+dateInDb);
        return dateInDb != dateInXml;
    }

    private boolean getIsNeuerZeitStempel(Element kopf) throws ParseException {
        long newTimeStamp = zeitstempelParser(kopf.getChildText("zeitstempel"));
        long oldTimeStamp = getKopfColumnAsLong(VplanContract.Kopf.COL_TIMESTAMP);
        Log.d(LT,"newTimeStamp="+newTimeStamp+", oldTimeStamp="+oldTimeStamp);
        return newTimeStamp != oldTimeStamp;
    }

    private long zeitstempelParser(String z) throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.GERMANY).parse(z).getTime();
    }

    private long getKopfColumnAsLong(String colName) {
        Cursor c = crslv.query(VplanContract.Kopf.CONTENT_URI,new String[]{colName},null,null,null);
        long ts = 0l;
        if (c != null) {
            if (c.moveToFirst()) {
                ts = c.getLong(0);
            }
            c.close();
        }
        return ts;
    }

    private long datumPlanParser(String z) throws ParseException {
        return new SimpleDateFormat("EEE, dd. MMM yyyy", Locale.GERMANY).parse(z).getTime();
    }

    private void getFreieTage(ArrayList<ContentProviderOperation> ops, Element t) throws ParseException {
        ops.add(ContentProviderOperation.newDelete(VplanContract.FreieTage.CONTENT_URI).build());
        for (Element d: t.getChildren("ft")) {
            ContentProviderOperation.Builder cpob = ContentProviderOperation.newInsert(VplanContract.FreieTage.CONTENT_URI);
            ContentValues cv = new ContentValues();
            cv.put(VplanContract.FreieTage.COL_FREIERTAG, freieTageParser(d.getValue()));
            cpob.withValues(cv);
            ops.add(cpob.build());
        }
    }

    private long freieTageParser(String z) throws ParseException {
        return new SimpleDateFormat("yyMMdd",Locale.US).parse(z).getTime();
    }

    private void getKlassen(ArrayList<ContentProviderOperation> ops, Element k, boolean isNeuerTag) {
        ops.add(ContentProviderOperation.newDelete(VplanContract.Plan.CONTENT_URI).build());
        ops.add(ContentProviderOperation.newDelete(VplanContract.Kurse.CONTENT_URI).build());
        ops.add(ContentProviderOperation.newDelete(VplanContract.Klassen.CONTENT_URI).build());
        // https://stackoverflow.com/a/8168096/2882196 for dependend inserts in one transaction
        for (Element kl: k.getChildren("Kl")) {
            ContentProviderOperation.Builder cpob = ContentProviderOperation.newInsert(VplanContract.Klassen.CONTENT_URI);
            String klasse = kl.getChildText("Kurz");
            boolean isAktualisiert = false;
            Uri uriPlanFuerKlasse = VplanContract.Plan.CONTENT_URI
                    .buildUpon()
                    .appendQueryParameter(VplanContract.PARAM_KEY_KLASSE,klasse)
                    .build();

            ContentValues cvKlassen = new ContentValues();
            cvKlassen.put(VplanContract.Klassen.COL_KLASSE,klasse);
            cpob.withValues(cvKlassen);
            ops.add(cpob.build());
            int j=ops.size()-1;
            Map<String,Integer> kursMap = new HashMap<>();
            for (Element ku: kl.getChild("Kurse").getChildren("Ku")) {
                ContentProviderOperation.Builder cpobK = ContentProviderOperation.newInsert(VplanContract.Kurse.CONTENT_URI);
                Element kkz = ku.getChild("KKz");
                String kurs = remNbsp(kkz.getValue());
                ContentValues cvKurse = new ContentValues();
                cvKurse.put(VplanContract.Kurse.COL_KLASSEN_KEY,0);
                cvKurse.put(VplanContract.Kurse.COL_LEHRER, remNbsp(kkz.getAttributeValue("KLe")));
                cvKurse.put(VplanContract.Kurse.COL_KURS, kurs);
                cpobK.withValues(cvKurse);
                cpobK.withValueBackReference(VplanContract.Kurse.COL_KLASSEN_KEY, j);
                ops.add(cpobK.build());
                kursMap.put(kurs,ops.size()-1);
            }

            for (Element std: kl.getChild("Pl").getChildren("Std")) {

                // get the values from the xml
                String stXml = remNbsp(std.getChildText("St"));
                String faXml = remNbsp(std.getChildText("Fa"));
                boolean faNeuXml = isNeu(std.getChild("Fa"), "FaAe");
                String leXml = remNbsp(std.getChildText("Le"));
                boolean leNeuXml = isNeu(std.getChild("Le"), "LeAe");
                String raXml = remNbsp(std.getChildText("Ra"));
                boolean raNeuXml = isNeu(std.getChild("Ra"), "RaAe");
                String ifXml = remNbsp(std.getChildText("If"));

                // build the insert operation and values
                ContentProviderOperation.Builder cpobP = ContentProviderOperation.newInsert(VplanContract.Plan.CONTENT_URI);
                ContentValues cvPlan = new ContentValues();
                cvPlan.put(VplanContract.Plan.COL_KLASSEN_KEY, 0);
                cvPlan.put(VplanContract.Plan.COL_STUNDE, stXml);
                cvPlan.put(VplanContract.Plan.COL_FACH, faXml);
                cvPlan.put(VplanContract.Plan.COL_FACH_NEU, faNeuXml);
                cvPlan.put(VplanContract.Plan.COL_LEHRER, leXml);
                cvPlan.put(VplanContract.Plan.COL_LEHRER_NEU, leNeuXml);
                cvPlan.put(VplanContract.Plan.COL_RAUM,raXml);
                cvPlan.put(VplanContract.Plan.COL_RAUM_NEU, raNeuXml);
                cvPlan.put(VplanContract.Plan.COL_INF, ifXml);

                String ku2 = std.getChildText("Ku2");
                if (ku2!=null) {
                    cvPlan.put(VplanContract.Plan.COL_KURSE_KEY, 0);
                }
                cpobP.withValues(cvPlan);
                cpobP.withValueBackReference(VplanContract.Plan.COL_KLASSEN_KEY, j);
                if (ku2!=null) {
                    cpobP.withValueBackReference(VplanContract.Plan.COL_KURSE_KEY, kursMap.get(ku2));
                }
                ops.add(cpobP.build());

                // check if it has changed compared to the values in the db
                if (!isNeuerTag && !isAktualisiert) {
                    Cursor stdCursor = crslv.query(
                            uriPlanFuerKlasse,
                            new String[]{
                                    VplanContract.Plan.COL_FACH,
                                    VplanContract.Plan.COL_FACH_NEU,
                                    VplanContract.Plan.COL_LEHRER,
                                    VplanContract.Plan.COL_LEHRER_NEU,
                                    VplanContract.Plan.COL_RAUM,
                                    VplanContract.Plan.COL_RAUM_NEU,
                                    VplanContract.Plan.COL_INF,
                            },
                            VplanContract.Plan.COL_STUNDE + " = ?" +
                            " AND " +
                            VplanContract.Plan.COL_FACH + " = ?",
                            new String[]{stXml,faXml},
                            null);
                    if (stdCursor != null) {
                        if (BuildConfig.DEBUG) {
                            int stdCursorCnt = stdCursor.getCount();
                            if (stdCursorCnt > 1) {
                                Log.d(LT,"stdCursor="+stdCursor.getCount());
                            }
                        }
                        if (stdCursor.moveToFirst()) {
                            String faDb = stdCursor.getString(0);
                            int faNeuDb = stdCursor.getInt(1);
                            String leDb = stdCursor.getString(2);
                            int leNeuDb = stdCursor.getInt(3);
                            String raDb = stdCursor.getString(4);
                            int raNeuDb = stdCursor.getInt(5);
                            String ifDb = stdCursor.getString(6);
                            if (!(faDb.equals(faXml)
                                    && ((faNeuDb == 1) == faNeuXml)
                                    && leDb.equals(leXml)
                                    && ((leNeuDb == 1) == leNeuXml)
                                    && raDb.equals(raXml)
                                    && ((raNeuDb == 1) == raNeuXml)
                                    && ifDb.equals(ifXml))) {
                                isAktualisiert = true;
                            }
                        } else {
                            // Cursor contained no rows so the row has not been there before
                            // so it must have changed
                            isAktualisiert = true;
                        }
                        stdCursor.close();
                    }
                }
            }
            if (isAktualisiert) {
                ContentProviderOperation.Builder b =
                        ContentProviderOperation.newInsert(
                                VplanContract.KlassenAktualisiert.CONTENT_URI);
                ContentValues v = new ContentValues();
                v.put(VplanContract.KlassenAktualisiert.COL_KLASSE, klasse);
                b.withValues(v);
                ops.add(b.build());
            }

        }
    }

    private void getZusatzinfo(ArrayList<ContentProviderOperation> ops, Element zusatzinfo) throws ParseException {
        crslv.delete(VplanContract.Zusatzinfo.CONTENT_URI, null, null);
        if (zusatzinfo == null) {
            return;
        }
        for (Element zizeile: zusatzinfo.getChildren("ZiZeile")) {
            ContentValues cv = new ContentValues();
            cv.put(VplanContract.Zusatzinfo.COL_ZIZEILE, zizeile.getText());
            cv.put(VplanContract.Zusatzinfo.COL_ZIZEILE_NEU, true);
            ContentProviderOperation.Builder cpobZ = ContentProviderOperation.newInsert(VplanContract.Zusatzinfo.CONTENT_URI);
            cpobZ.withValues(cv);
            ops.add(cpobZ.build());
        }
    }

    private boolean isNeu(Element el, String attrName) {
        return el != null && el.hasAttributes() && el.getAttribute(attrName) != null;
    }

    private String remNbsp(String str) {
        if (str != null && !str.equals("&nbsp;")) {
            return str;
        } else {
            return "";
        }
    }

    /**
     * Logs the contents of a ContentProvider URI with level DEBUG
     * @param ctx Context to retrieve the ContentResolver from
     * @param LT Log tag
     * @param queryDesc description of the query (for logging output only)
     * @param uri URI to query
     */
    public static void logContentsDbTable(Context ctx, String LT, String queryDesc, Uri uri) {
        Log.d(LT, "Logging contents for " + queryDesc + ", URI=" + uri);
        Cursor c = ctx.getContentResolver().query(uri, null, null, null, null);
        if (c != null) {
            Log.d(LT, "Size of query " + queryDesc + " is " + c.getCount());
            String[] colNames = c.getColumnNames();
            int colCount = c.getColumnCount();
            // print column header
            StringBuilder sb = new StringBuilder();
            for (String cn: colNames) {
                sb.append(cn).append(" # ");
            }
            Log.d(LT,sb.toString());
            while(c.moveToNext()) {
                StringBuilder row =new StringBuilder();
                for (int i = 0; i < colCount; i++) {
                    switch (c.getType(i)) {
                        case Cursor.FIELD_TYPE_BLOB:
                            row.append("BLOB of byte[");
                            row.append(c.getBlob(i).length);
                            row.append("]");
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            row.append(c.getFloat(i));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            row.append(c.getInt(i));
                            break;
                        case Cursor.FIELD_TYPE_NULL:
                            row.append("NULL");
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            row.append(c.getString(i));
                            break;
                        default:
                            row.append("UNKNOWN_COL_TYPE");
                    }
                    row.append(" # ");
                }
                Log.d(LT,row.toString());
            }
            c.close();
        } else {
            Log.d(LT, "Cursor is NULL for query " + queryDesc + ", URI=" + uri);
        }

    }
}
