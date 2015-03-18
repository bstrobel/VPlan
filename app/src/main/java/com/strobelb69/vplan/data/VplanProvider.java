package com.strobelb69.vplan.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.List;

/**
 * Created by Bernd on 14.03.2015.
 */
public class VplanProvider extends ContentProvider {

    public static final int KOPF = 100;
    public static final int FREIETAGE = 200;
    public static final int KLASSEN = 300;
    public static final int KURSE = 400;
    public static final int PLAN = 600;
    public static final int ZUSATZINFO = 800;

    private VplanDbHelper dbHelper;
    private static final SQLiteQueryBuilder qbPlanFuerKlasseUndKurse;
    private static final SQLiteQueryBuilder qbPlanFuerKlasse;
    private static final SQLiteQueryBuilder qbKurseFuerKlasse;
    static {
        qbPlanFuerKlasseUndKurse = new SQLiteQueryBuilder();
        qbPlanFuerKlasseUndKurse.setTables(
                VplanContract.Plan.TABLE_NAME + " INNER JOIN " +
                        VplanContract.Klassen.TABLE_NAME + " ON " +
                        VplanContract.Plan.TABLE_NAME + "." + VplanContract.Plan.COL_KLASSEN_KEY +
                        " = " +
                        VplanContract.Klassen.TABLE_NAME + "." + VplanContract.Klassen._ID + "AND" +
                        VplanContract.Kurse.TABLE_NAME + " INNER JOIN " +
                        VplanContract.Klassen.TABLE_NAME + " ON " +
                        VplanContract.Kurse.TABLE_NAME + "." + VplanContract.Kurse.COL_KLASSEN_KEY +
                        " = " +
                        VplanContract.Klassen.TABLE_NAME + "." + VplanContract.Klassen._ID
        );
        qbPlanFuerKlasse = new SQLiteQueryBuilder();
        qbPlanFuerKlasse.setTables(
                VplanContract.Plan.TABLE_NAME + " INNER JOIN " +
                        VplanContract.Klassen.TABLE_NAME + " ON " +
                        VplanContract.Plan.TABLE_NAME + "." + VplanContract.Plan.COL_KLASSEN_KEY +
                        " = " +
                        VplanContract.Klassen.TABLE_NAME + "." + VplanContract.Klassen._ID
        );
        qbKurseFuerKlasse = new SQLiteQueryBuilder();
        qbKurseFuerKlasse.setTables(
                VplanContract.Kurse.TABLE_NAME + " INNER JOIN " +
                        VplanContract.Klassen.TABLE_NAME + " ON " +
                        VplanContract.Kurse.TABLE_NAME + "." + VplanContract.Kurse.COL_KLASSEN_KEY +
                        " = " +
                        VplanContract.Klassen.TABLE_NAME + "." + VplanContract.Klassen._ID
        );
    }

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        final UriMatcher m = new UriMatcher(UriMatcher.NO_MATCH);
        final String auth = VplanContract.CONTENT_AUTHORITY;
        m.addURI(auth,VplanContract.PATH_KOPF,KOPF);
        m.addURI(auth,VplanContract.PATH_FREIETAGE,FREIETAGE);
        m.addURI(auth,VplanContract.PATH_KLASSEN,KLASSEN);
        m.addURI(auth,VplanContract.PATH_KURSE,KURSE);
        m.addURI(auth,VplanContract.PATH_PLAN,PLAN);
        m.addURI(auth,VplanContract.PATH_ZUSATZINFO,ZUSATZINFO);
        return m;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new VplanDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c;
        switch(uriMatcher.match(uri)) {
            case KOPF:
                c = dbHelper.getReadableDatabase().query(
                        VplanContract.Kopf.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case FREIETAGE:
                c = dbHelper.getReadableDatabase().query(
                        VplanContract.FreieTage.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case KLASSEN:
                c = dbHelper.getReadableDatabase().query(
                        VplanContract.Klassen.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case KURSE: {
                String klasseStr = uri.getQueryParameter(VplanContract.PARAM_KEY_KLASSE);
                StringBuilder sbSelection = new StringBuilder();
                sbSelection.append(VplanContract.Klassen.TABLE_NAME)
                        .append(".")
                        .append(VplanContract.Klassen.COL_KLASSE)
                        .append(" = ?");
                if (klasseStr == null) {
                    c = dbHelper.getReadableDatabase().query(
                            VplanContract.Kurse.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                } else {
                    c = qbKurseFuerKlasse.query(
                            dbHelper.getReadableDatabase(),
                            projection,
                            sbSelection.toString(),
                            new String[]{klasseStr},
                            null,
                            null,
                            sortOrder
                    );
                }
                break;
            }
            case PLAN: {
                String klasseStr = uri.getQueryParameter(VplanContract.PARAM_KEY_KLASSE);
                List<String> kurse = uri.getQueryParameters(VplanContract.PARAM_KEY_KURS);
                String komprDopplStd = uri.getQueryParameter(VplanContract.PARAM_KEY_KOMP_DOPPELSTD);
                StringBuilder sbSelection = new StringBuilder();
                sbSelection.append(VplanContract.Klassen.TABLE_NAME)
                        .append(".")
                        .append(VplanContract.Klassen.COL_KLASSE)
                        .append(" = ?");
                if (komprDopplStd != null && komprDopplStd.toLowerCase().equals("true")) {
                    sbSelection
                            .append(" AND ")
                            .append(VplanContract.Plan.COL_STUNDE)
                            .append(" NOT IN (\"2\",\"4\",\"6\",\"8\")");
                }
                if (klasseStr == null) {
                    c = dbHelper.getReadableDatabase().query(
                            VplanContract.Plan.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                } else if (kurse == null || kurse.size() == 0) {
                    c = qbPlanFuerKlasse.query(
                            dbHelper.getReadableDatabase(),
                            projection,
                            sbSelection.toString(),
                            new String[]{klasseStr},
                            null,
                            null,
                            sortOrder
                    );
                } else {
                    String[] selectionArgsArray = new String[kurse.size()+1];
                    selectionArgsArray[0] = klasseStr;
                    sbSelection
                            .append(" AND ")
                            .append(VplanContract.Kurse.TABLE_NAME)
                            .append(".")
                            .append(VplanContract.Kurse.COL_KURS)
                            .append(" NOT IN (");
                    for (int i = 0; i < kurse.size(); i++) {
                        selectionArgsArray[i+1] = kurse.get(i);
                        sbSelection.append("\"").append(kurse.get(i)).append("\"");
                        if (i==kurse.size()-1) {
                            sbSelection.append(")");
                        } else {
                            sbSelection.append(",");
                        }
                    }
                    
                    c = qbPlanFuerKlasseUndKurse.query(
                            dbHelper.getReadableDatabase(),
                            projection,
                            sbSelection.toString(),
                            selectionArgsArray,
                            null,
                            null,
                            sortOrder
                    );
                }
                break;
            }
            case ZUSATZINFO:
                c = dbHelper.getReadableDatabase().query(
                        VplanContract.Zusatzinfo.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:   throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        c.setNotificationUri(getContext().getContentResolver(),uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        final int m = uriMatcher.match(uri);
        switch (m) {
            case KOPF: return VplanContract.Kopf.CONTENT_TYPE;
            case FREIETAGE: return VplanContract.FreieTage.CONTENT_TYPE;
            case KLASSEN: return VplanContract.Klassen.CONTENT_TYPE;
            case KURSE: return VplanContract.Kurse.CONTENT_TYPE;
            case PLAN: return VplanContract.Plan.CONTENT_TYPE;
            case ZUSATZINFO: return VplanContract.Zusatzinfo.CONTENT_TYPE;
            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri u;
        switch(uriMatcher.match(uri)) {
            case KOPF: {
                long id = db.insertOrThrow(VplanContract.Kopf.TABLE_NAME,null,values);
                u = ContentUris.withAppendedId(VplanContract.Kopf.CONTENT_URI,id);
                break;
            }
            case FREIETAGE: {
                long id = db.insertOrThrow(VplanContract.FreieTage.TABLE_NAME,null,values);
                u = ContentUris.withAppendedId(VplanContract.FreieTage.CONTENT_URI,id);
                break;
            }
            case KLASSEN: {
                long id = db.insertOrThrow(VplanContract.Klassen.TABLE_NAME,null,values);
                u = ContentUris.withAppendedId(VplanContract.Klassen.CONTENT_URI,id);
                break;
            }
            case KURSE: {
                long id = db.insertOrThrow(VplanContract.Kurse.TABLE_NAME,null,values);
                u = ContentUris.withAppendedId(VplanContract.Kurse.CONTENT_URI,id);
                break;
            }
            case PLAN: {
                long id = db.insertOrThrow(VplanContract.Plan.TABLE_NAME,null,values);
                u = ContentUris.withAppendedId(VplanContract.Plan.CONTENT_URI,id);
                break;
            }
            case ZUSATZINFO: {
                long id = db.insertOrThrow(VplanContract.Zusatzinfo.TABLE_NAME,null,values);
                u = ContentUris.withAppendedId(VplanContract.Zusatzinfo.CONTENT_URI,id);
                break;
            }
            default: throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(u, null);
        return u;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = 0;
        switch(uriMatcher.match(uri)) {
            case KOPF: {
                ret = db.delete(VplanContract.Kopf.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case FREIETAGE: {
                ret = db.delete(VplanContract.FreieTage.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case KLASSEN: {
                ret =  db.delete(VplanContract.Klassen.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case KURSE: {
                ret = db.delete(VplanContract.Kurse.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case PLAN: {
                ret = db.delete(VplanContract.Plan.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case ZUSATZINFO: {
                ret = db.delete(VplanContract.Zusatzinfo.TABLE_NAME,selection,selectionArgs);
                break;
            }
            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Updates not implemented!");
    }
}
