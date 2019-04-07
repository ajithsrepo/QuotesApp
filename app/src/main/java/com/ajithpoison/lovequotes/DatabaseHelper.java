package com.ajithpoison.lovequotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "quotesDB.db";
    private static final String DB_SUB_PATH = "/databases/" + DB_NAME;
    private static String APP_DATA_PATH = "";
    private static String checkValue = "";
    private SQLiteDatabase dataBase;
    private static final String TABLE_NAME = "quotes";
    private static final String COL1 = "id";
    private static final String COL2 = "quote";
    private static final String COL3 = "favorite";

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        APP_DATA_PATH = context.getApplicationInfo().dataDir;
    }

    void openDataBase() throws SQLException {
        String mPath = APP_DATA_PATH + DB_SUB_PATH;
        //Note that this method assumes that the db file is already copied in place
        dataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (dataBase != null) {
            dataBase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //Check if a quote is favorite or not

    boolean checkFavQuote(Integer quoteID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String checkQuery = "SELECT " + COL3 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = " + quoteID;

        Cursor check = db.rawQuery(checkQuery, null);
        if (check.moveToFirst()) {
            do {
                checkValue = check.getString(0);
            } while (check.moveToNext());
        }
        check.close();
        if (checkValue.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    //Mark a quote as favorite in the database

    int markAsFavID(Integer quoteID) {
        SQLiteDatabase db = this.getWritableDatabase();

        String checkQuery = "SELECT " + COL3 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = " + quoteID;
        Cursor check = db.rawQuery(checkQuery, null);
        if (check.moveToFirst()) {
            do {
                checkValue = check.getString(0);
            } while (check.moveToNext());
        }
        check.close();
        if (checkValue.equals("false")) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("favorite", "true");

            long result = db.update(TABLE_NAME, contentValues, "id=" + quoteID, null);

            //if data is inserted incorrectly it will return -1
            if (result != -1) {
                return 1;
            }
        } else if (checkValue.equals("true")) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("favorite", "false");

            long result = db.update(TABLE_NAME, contentValues, "id=" + quoteID, null);

            //if data is inserted incorrectly it will return -1
            if (result != -1) {
                return 2;
            }
        }

        return 3;

    }

    Cursor QueryData(String query) {
        return dataBase.rawQuery(query, null);
    }
}