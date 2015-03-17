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

import com.strobelb69.vplan.R;
import com.strobelb69.vplan.util.Utils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
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

    public void retrievePlan(InputStream is) {
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            Document planXml = new SAXBuilder().build(is);
            Element re = planXml.getRootElement();
            if (checkNewAndGetKopf(ops, re.getChild("Kopf"))) {
                Log.i(LT,"New data received. Data will be parsed and database will be udapted!");
                getFreieTage(ops, re.getChild("FreieTage"));
                getKlassen(ops, re.getChild("Klassen"));
                getZusatzinfo(ops, re.getChild("ZusatzInfo"));
                crslv.applyBatch(ctx.getString(R.string.vplan_provider_authority),ops);
                Log.i(LT,"New data received. Database udapted!");
            }
        } catch (ParseException | JDOMException | IOException | RemoteException | OperationApplicationException e) {
            Log.e(LT,e.getMessage() + " while parsing XML \nStackTrace:\n" + Utils.getStackTraceString(e));
        }
    }

    private boolean checkNewAndGetKopf(ArrayList<ContentProviderOperation> ops, Element kopf) throws ParseException {
        long newTimeStamp = zeitstempelParser(kopf.getChildText("zeitstempel"));
        long oldTimeStamp = getOldTimeStamp();
        Log.d(LT,"newTimeStamp="+newTimeStamp+", oldTimeStamp="+oldTimeStamp);
        if (newTimeStamp != oldTimeStamp) {
            ops.add(ContentProviderOperation.newDelete(VplanContract.Kopf.CONTENT_URI).build());
            ContentProviderOperation.Builder cpob = ContentProviderOperation.newInsert(VplanContract.Kopf.CONTENT_URI);
            ContentValues cv = new ContentValues();
            cv.put(VplanContract.Kopf.COL_TIMESTAMP, newTimeStamp);
            cv.put(VplanContract.Kopf.COL_FOR_DATE, datumPlanParser(kopf.getChildText("DatumPlan")));
            ops.add(cpob.withValues(cv).build());
            return true;
        } else {
            Log.i(LT,"No new data. No update of the database needed! Timestamp=" + newTimeStamp);
            return false;
        }
    }

    private long zeitstempelParser(String z) throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.GERMANY).parse(z).getTime();
    }

    private long getOldTimeStamp() {
        Cursor c = crslv.query(VplanContract.Kopf.CONTENT_URI,new String[]{VplanContract.Kopf.COL_TIMESTAMP},null,null,null);
        if (c != null && c.moveToFirst()) {
            long ts = c.getLong(0);
            c.close();
            return ts;
        } else {
            return 0l;
        }
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
        return;
    }

    private long freieTageParser(String z) throws ParseException {
        return new SimpleDateFormat("yyMMdd",Locale.US).parse(z).getTime();
    }

    private void getKlassen(ArrayList<ContentProviderOperation> ops, Element k) {
        ops.add(ContentProviderOperation.newDelete(VplanContract.Plan.CONTENT_URI).build());
        ops.add(ContentProviderOperation.newDelete(VplanContract.Kurse.CONTENT_URI).build());
        ops.add(ContentProviderOperation.newDelete(VplanContract.Klassen.CONTENT_URI).build());
        // https://stackoverflow.com/a/8168096/2882196 for dependend inserts in one transaction
        for (Element kl: k.getChildren("Kl")) {
            ContentProviderOperation.Builder cpob = ContentProviderOperation.newInsert(VplanContract.Klassen.CONTENT_URI);
            ContentValues cvKlassen = new ContentValues();
            cvKlassen.put(VplanContract.Klassen.COL_KLASSE,kl.getChildText("Kurz"));
            cpob.withValues(cvKlassen);
            ops.add(cpob.build());
            int j=ops.size()-1;
                for (Element ku: kl.getChild("Kurse").getChildren("Ku")) {
                    ContentProviderOperation.Builder cpobK = ContentProviderOperation.newInsert(VplanContract.Kurse.CONTENT_URI);
                    Element kkz = ku.getChild("KKz");
                    ContentValues cvKurse = new ContentValues();
                    cvKurse.put(VplanContract.Kurse.COL_KLASSEN_KEY,0);
                    cvKurse.put(VplanContract.Kurse.COL_LEHRER, remNbsp(kkz.getAttributeValue("KLe")));
                    cvKurse.put(VplanContract.Kurse.COL_KURS, remNbsp(kkz.getValue()));
                    cpobK.withValues(cvKurse);
                    cpobK.withValueBackReference(VplanContract.Kurse.COL_KLASSEN_KEY, j);
                    ops.add(cpobK.build());
                }
                for (Element std: kl.getChild("Pl").getChildren("Std")) {
                    boolean hasKu2 = std.getChild("Ku2") != null;
                    ContentProviderOperation.Builder cpobP = ContentProviderOperation.newInsert(VplanContract.Plan.CONTENT_URI);
                    ContentValues cvPlan = new ContentValues();
                    cvPlan.put(VplanContract.Plan.COL_KLASSEN_KEY,0);
                    cvPlan.put(VplanContract.Plan.COL_STUNDE,remNbsp(std.getChildText("St")));
                    cvPlan.put(VplanContract.Plan.COL_FACH,remNbsp(std.getChildText("Fa")));
                    cvPlan.put(VplanContract.Plan.COL_FACH_NEU,isNeu(std.getChild("Fa"), "FaAe"));
                    cvPlan.put(VplanContract.Plan.COL_LEHRER,remNbsp(std.getChildText("Le")));
                    cvPlan.put(VplanContract.Plan.COL_LEHRER_NEU,isNeu(std.getChild("Le"), "LeAe"));
                    cvPlan.put(VplanContract.Plan.COL_RAUM,remNbsp(std.getChildText("Ra")));
                    cvPlan.put(VplanContract.Plan.COL_RAUM_NEU,isNeu(std.getChild("Ra"), "RaAe"));
                    cvPlan.put(VplanContract.Plan.COL_INF,remNbsp(std.getChildText("If")));
                    if (hasKu2) {
                            cvPlan.put(VplanContract.Plan.COL_KURSE_KEY, 0);
                    }
                    cpobP.withValues(cvPlan);
                    cpobP.withValueBackReference(VplanContract.Plan.COL_KLASSEN_KEY, j);
                    if (hasKu2) {
                        // TODO: needs to be replaced with SelectionBackReference oder wir muess eine HashMap mit j und dem Kurs aufbauen!
//                        cpobP.withValueBackReference(VplanContract.Plan.COL_KURSE_KEY, j);
                    }
                    ops.add(cpobP.build());
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
            ContentProviderOperation.Builder cpobZ = ContentProviderOperation.newInsert(VplanContract.Zusatzinfo.CONTENT_URI);
            cpobZ.withValues(cv);
            ops.add(cpobZ.build());
        }
        return ;
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
}