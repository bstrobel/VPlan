package com.strobelb69.vplan.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.strobelb69.vplan.R;
import com.strobelb69.vplan.util.Utils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
            Document planXml = new SAXBuilder().build(is);
            Element re = planXml.getRootElement();
            getKopf(re.getChild("Kopf"));
            getFreieTage(re.getChild("FreieTage"));
            getKlassen(re.getChild("Klassen"));
        } catch (ParseException | JDOMException | IOException e) {
            Log.e(LT,e.getMessage() + " while parsing XML \nStackTrace:\n" + Utils.getStackTraceString(e));
        }
    }

    private void getKopf(Element kopf) throws ParseException {
        crslv.delete(VplanContract.Kopf.CONTENT_URI, null, null);
        ContentValues cv = new ContentValues();
        cv.put(VplanContract.Kopf.COL_TIMESTAMP,zeitstempelParser(kopf.getChildText("zeitstempel")));
        cv.put(VplanContract.Kopf.COL_FOR_DATE,datumPlanParser(kopf.getChildText("DatumPlan")));
        crslv.insert(VplanContract.Kopf.CONTENT_URI,cv);
    }

    private long zeitstempelParser(String z) throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.GERMANY).parse(z).getTime();
    }

    private long datumPlanParser(String z) throws ParseException {
        return new SimpleDateFormat("EEE, dd. MMM yyyy", Locale.GERMANY).parse(z).getTime();
    }

    private void getFreieTage(Element t) throws ParseException {
        crslv.delete(VplanContract.FreieTage.CONTENT_URI, null, null);
        for (Element d: t.getChildren("ft")) {
            ContentValues cv = new ContentValues();
            cv.put(VplanContract.FreieTage.COL_FREIERTAG,freieTageParser(d.getValue()));
            crslv.insert(VplanContract.FreieTage.CONTENT_URI, cv);
        }
    }

    private long freieTageParser(String z) throws ParseException {
        return new SimpleDateFormat("yyMMdd",Locale.US).parse(z).getTime();
    }

    private void getKlassen(Element k) {
        crslv.delete(VplanContract.Plan.CONTENT_URI, null, null);
        crslv.delete(VplanContract.Kurse.CONTENT_URI, null, null);
        crslv.delete(VplanContract.Klassen.CONTENT_URI, null, null);

        for (Element kl: k.getChildren("Kl")) {
            ContentValues cvKlassen = new ContentValues();
            cvKlassen.put(VplanContract.Klassen.COL_KLASSE,kl.getChildText("Kurz"));
            long klasseId = getIdFromUri(crslv.insert(VplanContract.Klassen.CONTENT_URI,cvKlassen));
            if (klasseId > -1) {
                for (Element ku: kl.getChild("Kurse").getChildren("Ku")) {
                    Element kkz = ku.getChild("KKz");
                    ContentValues cvKurse = new ContentValues();
                    cvKurse.put(VplanContract.Kurse.COL_KLASSEN_KEY,klasseId);
                    cvKurse.put(VplanContract.Kurse.COL_LEHRER, remNbsp(kkz.getAttributeValue("KLe")));
                    cvKurse.put(VplanContract.Kurse.COL_KURS, remNbsp(kkz.getValue()));
                    crslv.insert(VplanContract.Kurse.CONTENT_URI,cvKurse);
                }
                for (Element std: kl.getChild("Pl").getChildren("Std")) {
                    ContentValues cvPlan = new ContentValues();
                    cvPlan.put(VplanContract.Plan.COL_KLASSEN_KEY,klasseId);
                    cvPlan.put(VplanContract.Plan.COL_STUNDE,remNbsp(std.getChildText("St")));
                    cvPlan.put(VplanContract.Plan.COL_FACH,remNbsp(std.getChildText("Fa")));
                    cvPlan.put(VplanContract.Plan.COL_FACH_NEU,isNeu(std.getChild("Fa"), "FaAe"));
                    cvPlan.put(VplanContract.Plan.COL_LEHRER,remNbsp(std.getChildText("Le")));
                    cvPlan.put(VplanContract.Plan.COL_LEHRER_NEU,isNeu(std.getChild("Le"), "LeAe"));
                    cvPlan.put(VplanContract.Plan.COL_RAUM,remNbsp(std.getChildText("Ra")));
                    cvPlan.put(VplanContract.Plan.COL_RAUM_NEU,isNeu(std.getChild("Ra"), "RaAe"));
                    cvPlan.put(VplanContract.Plan.COL_INF,remNbsp(std.getChildText("If")));
                    crslv.insert(VplanContract.Plan.CONTENT_URI, cvPlan);
                }
            }

        }
    }

    private boolean isNeu(Element el, String attrName) {
        return el != null && el.hasAttributes() && el.getAttribute(attrName) != null;
    }

    private long getIdFromUri(Uri u) {
        try {
            List<String> segs = u.getPathSegments();
            String idStr = segs.get(segs.size()-1);
            return new Long(idStr);
        } catch (Exception e) {
            return -1L;
        }
    }

    private String remNbsp(String str) {
        if (str != null && !str.equals("&nbsp;")) {
            return str;
        } else {
            return "";
        }
    }
}
