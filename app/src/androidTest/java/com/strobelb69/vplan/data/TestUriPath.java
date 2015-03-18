package com.strobelb69.vplan.data;

import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by bstrobel on 18.03.2015.
 */
public class TestUriPath extends AndroidTestCase {

    public static final String LT="######## Test #########";
    public void testUri() {
        String uriStr="content://com.strobelb69.vplan";
        Uri baseUri=Uri.parse(uriStr);
        Log.d(LT,baseUri.toString());
        Uri queryKlasse=baseUri.buildUpon().appendQueryParameter("klasse","8c").build();
        Log.d(LT,queryKlasse.toString());
        Uri queryKlasseKurs=queryKlasse.buildUpon().appendQueryParameter("Kurs","Eng1").build();
        Log.d(LT,queryKlasseKurs.toString());
        Uri queryKlasseKursParam = queryKlasseKurs.buildUpon().encodedFragment("komprDoppels").build();
        Log.d(LT,queryKlasseKursParam.toString());
        Log.d(LT,".getFragment():"+queryKlasseKursParam.getFragment());
        Log.d(LT,".getQuery():"+queryKlasseKursParam.getQuery());
        Log.d(LT,".getQueryParameter(\"Kurs\"):"+queryKlasseKursParam.getQueryParameter("Kurs"));

        Uri queryKlasseKurse=queryKlasseKurs.buildUpon().appendQueryParameter("Kurs","Rus1").build();
        Log.d(LT,queryKlasseKurse.toString());
        Log.d(LT,".getQuery():"+queryKlasseKurse.getQuery());
        Log.d(LT,".getQueryParameter(\"Kurs\"):"+queryKlasseKurse.getQueryParameters("Kurs"));
    }
}
