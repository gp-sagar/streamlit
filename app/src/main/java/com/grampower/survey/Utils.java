package com.grampower.survey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Utils {
    public static int IDFromSiteName(Context context, String siteName) {
        SQLiteDatabase mDB = new SurveyDBHelper(context).getWritableDatabase();
        Cursor cursor = mDB.query(
                true
                , "siteTable"
                , null
                , "siteName LIKE '" + siteName + "'"
                , null
                , null
                , null
                , null
                , null
        );
        cursor.moveToFirst();
        int siteID = cursor.getInt(cursor.getColumnIndex("siteID"));
        mDB.close();
        return siteID;
    }

    public static void addFeederToDatabase(Context context, String feederName, int ratedCapacity) {
        SQLiteDatabase mDB = new SurveyDBHelper(context).getWritableDatabase();

        ContentValues update = new ContentValues();
        update.put("feederName", feederName);
        update.put("ratedCapacity", ratedCapacity);

        mDB.insert("feederTable", "", update);
        mDB.close();
    }

    public static String[] getAllFeeders(Context context) {
        SQLiteDatabase mDB = new SurveyDBHelper(context).getWritableDatabase();
        Cursor cursor = mDB.query(
                true
                , "feederTable"
                , null
                , null
                , null
                , null
                , null
                , null
                , null
        );
        cursor.moveToFirst();
        String[] result = new String[cursor.getCount()];
        do {
            result[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("feederName"));
        } while (cursor.moveToNext());
        mDB.close();
        return result;
    }
}
