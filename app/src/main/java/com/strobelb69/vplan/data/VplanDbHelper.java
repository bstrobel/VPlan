package com.strobelb69.vplan.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Bernd on 14.03.2015.
 */
public class VplanDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "vplan.db";

    public VplanDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE ";
        final String INTEGER_PRIMARY_KEY = " INTEGER PRIMARY KEY";
        final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT";
        final String INTEGER = " INTEGER";
        final String INTEGER_NOT_NULL = " INTEGER NOT NULL";
        final String INTEGER_UNIQUE_NOT_NULL = " INTEGER UNIQUE NOT NULL";
        final String TEXT = " TEXT";
        final String TEXT_NOT_NULL = " TEXT NOT NULL";
        final String BOOLEAN_NOT_NULL = " BOOLEAN NOT NULL";

        final String SQL_CREATE_VPLANSTATE = CREATE_TABLE + VplanContract.Kopf.TABLE_NAME + "(" +
                VplanContract.Kopf._ID + INTEGER_PRIMARY_KEY + "," +
                VplanContract.Kopf.COL_TIMESTAMP + INTEGER_UNIQUE_NOT_NULL + "," +
                VplanContract.Kopf.COL_FOR_DATE + INTEGER_UNIQUE_NOT_NULL +
                ")";

        final String SQL_CREATE_FREIETAGE = CREATE_TABLE + VplanContract.FreieTage.TABLE_NAME + "(" +
                VplanContract.FreieTage._ID + INTEGER_PRIMARY_KEY + "," +
                VplanContract.FreieTage.COL_FREIERTAG + INTEGER_UNIQUE_NOT_NULL +
                ")";

        final String SQL_CREATE_KLASSEN = CREATE_TABLE + VplanContract.Klassen.TABLE_NAME + "(" +
                VplanContract.Klassen._ID + INTEGER_PRIMARY_KEY + "," +
                VplanContract.Klassen.COL_KLASSE + TEXT_NOT_NULL +
                ")";

        final String SQL_CREATE_KURSE = CREATE_TABLE + VplanContract.Kurse.TABLE_NAME + "(" +
                VplanContract.Kurse._ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + "," +
                VplanContract.Kurse.COL_KLASSEN_KEY + INTEGER_NOT_NULL + "," +
                VplanContract.Kurse.COL_LEHRER + TEXT_NOT_NULL + "," +
                VplanContract.Kurse.COL_KURS + TEXT_NOT_NULL +"," +
                " FOREIGN KEY (" + VplanContract.Plan.COL_KLASSEN_KEY + ") REFERENCES " +
                VplanContract.Klassen.TABLE_NAME + " (" + VplanContract.Klassen._ID + ") " +
                ")";

        final String SQL_CREATE_PLAN = CREATE_TABLE + VplanContract.Plan.TABLE_NAME + "(" +
                VplanContract.Plan._ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + "," +
                VplanContract.Plan.COL_KLASSEN_KEY + INTEGER_NOT_NULL + "," +
                VplanContract.Plan.COL_STUNDE + TEXT_NOT_NULL + "," +
                VplanContract.Plan.COL_FACH + TEXT + "," +
                VplanContract.Plan.COL_FACH_NEU + BOOLEAN_NOT_NULL + "," +
                VplanContract.Plan.COL_LEHRER + TEXT + "," +
                VplanContract.Plan.COL_LEHRER_NEU + BOOLEAN_NOT_NULL + "," +
                VplanContract.Plan.COL_RAUM + TEXT + "," +
                VplanContract.Plan.COL_RAUM_NEU + BOOLEAN_NOT_NULL + "," +
                VplanContract.Plan.COL_INF + TEXT + "," +
                " FOREIGN KEY (" + VplanContract.Plan.COL_KLASSEN_KEY + ") REFERENCES " +
                VplanContract.Klassen.TABLE_NAME + " (" + VplanContract.Klassen._ID + ") " +
                ")";

        db.execSQL(SQL_CREATE_VPLANSTATE);
        db.execSQL(SQL_CREATE_FREIETAGE);
        db.execSQL(SQL_CREATE_KLASSEN);
        db.execSQL(SQL_CREATE_KURSE);
        db.execSQL(SQL_CREATE_PLAN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + VplanContract.Kopf.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VplanContract.FreieTage.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VplanContract.Plan.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VplanContract.Kurse.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VplanContract.Klassen.TABLE_NAME);
        onCreate(db);

    }
}
