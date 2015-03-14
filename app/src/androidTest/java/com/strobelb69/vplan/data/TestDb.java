package com.strobelb69.vplan.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

/**
 * Created by Bernd on 14.03.2015.
 */
public class TestDb extends AndroidTestCase {

    void deleteTheDatabase() {
        mContext.deleteDatabase(VplanDbHelper.DATABASE_NAME);
    }

    @Override
    protected void setUp() throws Exception {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        deleteTheDatabase();

        SQLiteDatabase db = new VplanDbHelper(this.mContext).getWritableDatabase();
        assertTrue(db.isOpen());

        ContentValues klasse8c = new ContentValues();
        klasse8c.put(VplanContract.Klassen.COL_KLASSE,"8c");
        long id8c = db.insert(VplanContract.Klassen.TABLE_NAME,null,klasse8c);
        assertTrue(id8c != -1);

        ContentValues kurse8c = new ContentValues();
        kurse8c.put(VplanContract.Kurse.COL_KLASSEN_KEY,id8c);
        kurse8c.put(VplanContract.Kurse.COL_KURS,"Fr");
        kurse8c.put(VplanContract.Kurse.COL_LEHRER,"Twa");
        long idkurse8c = db.insert(VplanContract.Kurse.TABLE_NAME,null,kurse8c);
        assertTrue(idkurse8c != -1);

        ContentValues plan8c = new ContentValues();
        plan8c.put(VplanContract.Plan.COL_KLASSEN_KEY,id8c);
        plan8c.put(VplanContract.Plan.COL_FACH,"De");
        plan8c.put(VplanContract.Plan.COL_FACH_NEU,true);
        plan8c.put(VplanContract.Plan.COL_LEHRER,"Ktz");
        plan8c.put(VplanContract.Plan.COL_LEHRER_NEU,false);
        plan8c.put(VplanContract.Plan.COL_RAUM,"407");
        plan8c.put(VplanContract.Plan.COL_RAUM_NEU, true);
        plan8c.put(VplanContract.Plan.COL_STUNDE,"1");
        plan8c.put(VplanContract.Plan.COL_STUNDE_NEU,false);
        plan8c.put(VplanContract.Plan.COL_INF,"Test");
        long idplan8c = db.insert(VplanContract.Plan.TABLE_NAME,null,plan8c);
        assertTrue(idplan8c != -1);
    }
}
