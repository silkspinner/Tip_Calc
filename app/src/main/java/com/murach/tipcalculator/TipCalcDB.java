package com.murach.tipcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TipCalcDB {

    //DB constants
    public static final String DB_NAME = "tipcalc.db";
    public static final int DB_VERSION = 1;

    // tip table constants

    public static final String TIP_TABLE = "tip";

    public static final String TIP_ID = "_id";
    public static final int TIP_ID_COL = 0;

    public static final String TIP_DATE = "tip_date";
    public static final int TIP_DATE_COL = 1;

    public static final String TIP_BILL_AMOUNT = "bill_amount";
    public static final int TIP_BILL_AMOUNT_COL = 2;

    public static final String TIP_PERCENT = "tip_percent";
    public static final int TIP_PERCENT_COL = 3;

    public static final String CREATE_TIP_TABLE =
            "CREATE TABLE " + TIP_TABLE + " (" +
                    TIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TIP_DATE + " INTEGER NOT NULL, " +
                    TIP_BILL_AMOUNT + " FLOAT NOT NULL, " +
                    TIP_PERCENT + " FLOAT NOT NULL);";


    public static final String DROP_TIP_TABLE =
            "DROP TABLE IF EXISTS " + TIP_TABLE;



    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name,
                        SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TIP_TABLE);

            // insert default lists
            db.execSQL("INSERT INTO tip VALUES (1, 1526083200000, 12.76, 0.15)");
            db.execSQL("INSERT INTO tip VALUES (2, 1526256000000, 33.57, 0.22)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion, int newVersion) {
            Log.d("Tip Calculator", "Upgrading db from version "
                    + oldVersion + "to " + newVersion);

            db.execSQL(TipCalcDB.DROP_TIP_TABLE);
            onCreate(db);
        }
    }

    // database object and database helper object
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    // constructor
    public TipCalcDB(Context context) {
        this.dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    //private methods
    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    private void closeDB() {
        if (db != null) db.close();
    }

    //public methods

    public ArrayList<Tip> getTips() {
        ArrayList<Tip> tips = new ArrayList<Tip>();
        openReadableDB();
        Cursor cursor = db.query(TIP_TABLE,
                null, null, null, null, null, null);
        while(cursor.moveToNext()) {
            Tip tip = new Tip();
            tip.setId(cursor.getInt(TIP_ID_COL));
            tip.setDateMillis(cursor.getInt(TIP_DATE_COL));
            tip.setBillAmount(cursor.getFloat(TIP_BILL_AMOUNT_COL));
            tip.setTipPercent(cursor.getFloat(TIP_PERCENT_COL));

            tips.add(tip);
        }


        //cleanup
        if(cursor != null) cursor.close();
        closeDB();

        return tips;
    }

    public Tip getTip(int id) {
        String where = TIP_ID + "= ?";
        String[] whereArgs = { Integer.toString(id) };

        this.openReadableDB();
        Cursor cursor= db.query(TIP_TABLE, null, where, whereArgs,
                null, null, null);
        cursor.moveToFirst();
        Tip tip = getTipFromCursor(cursor);
        if (cursor != null) cursor.close();
        this.closeDB();

        return tip;
    }

    private static Tip getTipFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            try {
                Tip tip = new Tip(
                        cursor.getInt(TIP_ID_COL),
                        cursor.getInt(TIP_DATE_COL),
                        cursor.getFloat(TIP_BILL_AMOUNT_COL),
                        cursor.getFloat(TIP_PERCENT_COL));
                return tip;
            } catch(Exception e) {
                return null;
            }
        }
    }

    public long insertTip(Tip tip) {
        ContentValues cv = new ContentValues();
        cv.put(TIP_ID, tip.getId());
        cv.put(TIP_DATE, tip.getDateMillis());
        cv.put(TIP_BILL_AMOUNT, tip.getBillAmount());
        cv.put(TIP_PERCENT, tip.getTipPercent());

        this.openWriteableDB();
        long rowID = db.insert(TIP_TABLE, null, cv);
        this.closeDB();

        return rowID;
    }

    public int updateTip(Tip tip) {
        ContentValues cv = new ContentValues();
        cv.put(TIP_ID, tip.getId());
        cv.put(TIP_DATE, tip.getDateMillis());
        cv.put(TIP_BILL_AMOUNT, tip.getBillAmount());
        cv.put(TIP_PERCENT, tip.getTipPercent());

        String where = TIP_ID + "= ?";
        String[] whereArgs = {String.valueOf(tip.getId())};

        this.openWriteableDB();
        int rowCount = db.update(TIP_TABLE, cv, where, whereArgs);
        this.closeDB();

        return rowCount;
    }

    public int deleteTip(long id) {
        String where = TIP_ID + "= ?";
        String[] whereArgs = { String.valueOf(id)};

        this.openWriteableDB();
        int rowCount = db.delete(TIP_TABLE, where, whereArgs);
        this.closeDB();

        return rowCount;
    }

}
