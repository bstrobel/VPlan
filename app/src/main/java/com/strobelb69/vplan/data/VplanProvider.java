package com.strobelb69.vplan.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Bernd on 14.03.2015.
 */
public class VplanProvider extends ContentProvider {

    public static final int KOPF = 100;
    public static final int FREIETAGE = 200;
    public static final int KLASSEN = 300;
    public static final int KURSE = 400;
    public static final int KURSE_FUER_KLASSE = 500;
    public static final int PLAN = 600;
    public static final int PLAN_FUER_KLASSE = 700;

    private VplanDbHelper dbHelper;
    private static final SQLiteQueryBuilder qbKurseFuerKlasse;
    private static final SQLiteQueryBuilder qbPlanFuerKlasse;
    static {
        qbKurseFuerKlasse = new SQLiteQueryBuilder();
        qbKurseFuerKlasse.setTables(
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
    }

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        final UriMatcher m = new UriMatcher(UriMatcher.NO_MATCH);
        final String auth = VplanContract.CONTENT_AUTHORITY;
        m.addURI(auth,VplanContract.PATH_KOPF,KOPF);
        m.addURI(auth,VplanContract.PATH_FREIETAGE,FREIETAGE);
        m.addURI(auth,VplanContract.PATH_KLASSEN,KLASSEN);
        m.addURI(auth,VplanContract.PATH_KURSE,KURSE);
        m.addURI(auth,VplanContract.PATH_KURSE + "/*",KURSE_FUER_KLASSE);
        m.addURI(auth,VplanContract.PATH_PLAN,PLAN);
        m.addURI(auth,VplanContract.PATH_PLAN + "/*",PLAN_FUER_KLASSE);
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
            case KURSE:
                c = dbHelper.getReadableDatabase().query(
                        VplanContract.Kurse.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case KURSE_FUER_KLASSE:
                c = qbKurseFuerKlasse.query(
                        dbHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PLAN:
                c = dbHelper.getReadableDatabase().query(
                        VplanContract.Plan.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PLAN_FUER_KLASSE:
                c = qbPlanFuerKlasse.query(
                        dbHelper.getReadableDatabase(),
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
            case KURSE_FUER_KLASSE: return VplanContract.Kurse.CONTENT_ITEM_TYPE;
            case PLAN: return VplanContract.Plan.CONTENT_TYPE;
            case PLAN_FUER_KLASSE: return VplanContract.Plan.CONTENT_ITEM_TYPE;
            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri u;
        switch(uriMatcher.match(uri)) {
            case KOPF: {
                long id = db.insert(VplanContract.Kopf.TABLE_NAME,null,values);
                if (id > 0) {
                    u = ContentUris.withAppendedId(VplanContract.Kopf.CONTENT_URI,id);
                } else {
                    throw new SQLException("Failed to insert uri: " + uri);
                }
                break;
            }
            case FREIETAGE: {
                long id = db.insert(VplanContract.FreieTage.TABLE_NAME,null,values);
                if (id > 0) {
                    u = ContentUris.withAppendedId(VplanContract.FreieTage.CONTENT_URI,id);
                } else {
                    throw new SQLException("Failed to insert uri: " + uri);
                }
                break;
            }
            case KLASSEN: {
                long id = db.insert(VplanContract.Klassen.TABLE_NAME,null,values);
                if (id > 0) {
                    u = ContentUris.withAppendedId(VplanContract.Klassen.CONTENT_URI,id);
                } else {
                    throw new SQLException("Failed to insert uri: " + uri);
                }
                break;
            }
            case KURSE: {
                long id = db.insert(VplanContract.Kurse.TABLE_NAME,null,values);
                if (id > 0) {
                    u = ContentUris.withAppendedId(VplanContract.Kurse.CONTENT_URI,id);
                } else {
                    throw new SQLException("Failed to insert uri: " + uri);
                }
                break;
            }
            case PLAN: {
                long id = db.insert(VplanContract.Plan.TABLE_NAME,null,values);
                if (id > 0) {
                    u = ContentUris.withAppendedId(VplanContract.Plan.CONTENT_URI,id);
                } else {
                    throw new SQLException("Failed to insert uri: " + uri);
                }
                break;
            }
            default: throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
        return u;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch(uriMatcher.match(uri)) {
            case KOPF: return db.delete(VplanContract.Kopf.TABLE_NAME,selection,selectionArgs);
            case FREIETAGE: return db.delete(VplanContract.FreieTage.TABLE_NAME,selection,selectionArgs);
            case KLASSEN: return db.delete(VplanContract.Klassen.TABLE_NAME,selection,selectionArgs);
            case KURSE: return db.delete(VplanContract.Kurse.TABLE_NAME,selection,selectionArgs);
            case PLAN: return db.delete(VplanContract.Plan.TABLE_NAME,selection,selectionArgs);
            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Updates not implemented!");
    }
}
