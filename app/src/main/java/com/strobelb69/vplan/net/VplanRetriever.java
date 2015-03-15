package com.strobelb69.vplan.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.strobelb69.vplan.R;
import com.strobelb69.vplan.data.VplanParser;
import com.strobelb69.vplan.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Bernd on 14.03.2015.
 */
public class VplanRetriever {
    private final VplanParser parser;
    private final String urlStr;
    private final String LT = getClass().getSimpleName();
    private final ConnectivityManager cm;

    public VplanRetriever(Context ctx) {
        super();
        parser = new VplanParser(ctx);
        urlStr = ctx.getResources().getString(R.string.vplanUrl);
        cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void retrieveFromNet() {
        InputStream is = null;
        try {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                Log.i(LT, "Not connected to network. Vplan not updated!");
                return;
            }
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            parser.retrievePlan(is);
        } catch (IOException e) {
            Log.e(LT,e.getMessage() + " while getting URL " + urlStr + "\nStackTrace:\n" + Utils.getStackTraceString(e));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(LT,e.getMessage() + " while getting URL " + urlStr + "\nStackTrace:\n" + Utils.getStackTraceString(e));
                }
            }
        }
    }
}
