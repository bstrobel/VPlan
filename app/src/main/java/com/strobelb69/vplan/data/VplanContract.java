package com.strobelb69.vplan.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by Bernd on 14.03.2015.
 */
public class VplanContract {
    // Fuer content provider
    public static final String CONTENT_AUTHORITY = "com.strobelb69.vplan";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_KOPF = "kopf";
    public static final String PATH_FREIETAGE = "freietage";
    public static final String PATH_KURSE = "kurse";
    public static final String PATH_KLASSEN = "klassen";
    public static final String PATH_PLAN = "plan";
    public static final String PATH_ZUSATZINFO = "zusatzinfo";
    public static final String PATH_KLASSEN_AKTUALISIERT = "klassenAktualisiert";
    public static final String PARAM_KEY_KOMP_DOPPELSTD = "komprDplStd";
    public static final String PARAM_KEY_KLASSE = "klasse";
    public static final String PARAM_KEY_KURS = "kurs";

    public static final class Kopf implements BaseColumns {
        // content provider stuff
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_KOPF).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_KOPF;
        // actual contract fields and names
        public static final String TABLE_NAME = PATH_KOPF;
        public static final String COL_TIMESTAMP = "zeitstempel";
        public static final String COL_FOR_DATE = "datumplan";
        public static final String COL_LAST_SYNC = "lastsync";
        public static final String COL_NEUER_TAG = "neuer_tag";
    }

    public static final class FreieTage implements BaseColumns {
        // content provider stuff
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FREIETAGE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FREIETAGE;
        // actual contract fields and names
        public static final String TABLE_NAME = PATH_FREIETAGE;
        public static final String COL_FREIERTAG = "freiertag";
    }

    public static final class Klassen implements BaseColumns {
        // content provider stuff
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_KLASSEN).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_KLASSEN;
        // actual contract fields and names
        public static final String TABLE_NAME = PATH_KLASSEN;
        public static final String COL_KLASSE = "klasse";
    }

    public static final class Kurse implements BaseColumns {
        // content provider stuff
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_KURSE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_KURSE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_KURSE;
        // actual contract fields and names
        public static final String TABLE_NAME = PATH_KURSE;
        public static final String COL_KLASSEN_KEY = Klassen.TABLE_NAME+"_id";
        public static final String COL_LEHRER = "lehrer";
        public static final String COL_KURS = "kurs";
    }

    public static final class Plan implements BaseColumns {
        // content provider stuff
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAN).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAN;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAN;
        // actual contract fields and names
        public static final String TABLE_NAME = PATH_PLAN;
        public static final String COL_KLASSEN_KEY = Klassen.TABLE_NAME + "_id";
        public static final String COL_STUNDE = "stunde";
        public static final String COL_FACH = "fach";
        public static final String COL_FACH_NEU = "fach_neu";
        public static final String COL_KURSE_KEY = Kurse.TABLE_NAME + "_id";
        public static final String COL_LEHRER = "lehrer";
        public static final String COL_LEHRER_NEU = "lehrer_neu";
        public static final String COL_RAUM = "raum";
        public static final String COL_RAUM_NEU = "raum_neu";
        public static final String COL_INF = "inf";
    }

    public static final class Zusatzinfo implements BaseColumns {
        // content provider stuff
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ZUSATZINFO).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ZUSATZINFO;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ZUSATZINFO;
        // actual contract fields and names
        public static final String TABLE_NAME = PATH_ZUSATZINFO;
        public static final String COL_ZIZEILE= "zizeile";
    }

    public static final class KlassenAktualisiert implements BaseColumns {
        // content provider stuff
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_KLASSEN_AKTUALISIERT).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_KLASSEN_AKTUALISIERT;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_KLASSEN_AKTUALISIERT;
        // actual contract fields and names
        public static final String TABLE_NAME = PATH_KLASSEN_AKTUALISIERT;
        public static final String COL_KLASSE= "klasse";

    }
}
