package com.strobelb69.vplan.net;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.strobelb69.vplan.R;
import com.strobelb69.vplan.data.VplanContract;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Bernd on 14.03.2015.
 */
public class RetrieverTask extends AsyncTask<Void, Void, Void> {
    private final Context ctx;
    private final ContentResolver crslv;

    public RetrieverTask(Context ctx) {
        super();
        this.ctx = ctx;
        crslv = ctx.getContentResolver();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String urlStr = ctx.getResources().getString(R.string.vplanUrl);
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwinf = cm.getActiveNetworkInfo();
        if (nwinf != null && nwinf.isConnected()) {
            InputStream is = null;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                is = conn.getInputStream();
                retrievePlan(is);
            } catch (MalformedURLException e) {
            } catch (ProtocolException e) {
            } catch (IOException e) {
            } catch (JDOMException e) {
            } catch (ParseException e) {
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }

    private void retrievePlan(InputStream is) throws IOException, JDOMException, ParseException {
        Document planXml = new SAXBuilder().build(is);
        Element re = planXml.getRootElement();
        getKopf(re.getChild("Kopf"));
        getFreieTage(re.getChild("FreieTage"));
        getKlassen(re.getChild("Klassen"));
    }

    private void getKopf(Element kopf) throws ParseException {
        crslv.delete(VplanContract.Kopf.CONTENT_URI, null, null);
        ContentValues cv = new ContentValues();
        cv.put(VplanContract.Kopf.COL_TIMESTAMP,zeitstempelParser(kopf.getChildText("zeitstempel")));
        cv.put(VplanContract.Kopf.COL_FOR_DATE,datumPlanParser(kopf.getChildText("DatumPlan")));
        crslv.insert(VplanContract.Kopf.CONTENT_URI,cv);
    }

    private long zeitstempelParser(String z) throws ParseException {
        return new SimpleDateFormat("dd.MM.YYYY, HH:mm").parse(z).getTime();
    }

    private long datumPlanParser(String z) throws ParseException {
        return new SimpleDateFormat("EEE, dd. MMM YYYY").parse(z).getTime();
    }

    private void getFreieTage(Element t) throws ParseException {
        crslv.delete(VplanContract.FreieTage.CONTENT_URI, null, null);
        ContentValues cv = new ContentValues();
        for (Element d: t.getChildren("ft")) {
            cv.put(VplanContract.FreieTage.COL_FREIERTAG,freieTageParser(d.getValue()));
        }
        crslv.insert(VplanContract.FreieTage.CONTENT_URI,cv);
    }

    private long freieTageParser(String z) throws ParseException {
        return new SimpleDateFormat("YYMMdd").parse(z).getTime();
    }

    private void getKlassen(Element k) {
        crslv.delete(VplanContract.Plan.CONTENT_URI, null, null);
        crslv.delete(VplanContract.Kurse.CONTENT_URI, null, null);
        crslv.delete(VplanContract.Klassen.CONTENT_URI, null, null);
        for (Element kl: k.getChildren("Kl")) {
            ContentValues cvKlassen = new ContentValues();
            cvKlassen.put(VplanContract.Klassen.COL_KLASSE,kl.getChildText("Kurz"));
            //TODO: Id von Klasse holen und für inserts der anderen beiden Tabellen nutzen.
            crslv.insert(VplanContract.Klassen.CONTENT_URI,cvKlassen);
        }
    }
}
