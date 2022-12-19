package com.grampower.survey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SurveyDBHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE_GEO = "CREATE TABLE geopointTable ("+
            "  geoID INTEGER PRIMARY KEY AUTOINCREMENT"+
            "  , UID INTEGER"+
            "  , siteID INTEGER"+
            "  , siteName TEXT"+
            "  , objectType TEXT"+
            "  , latitude REAL"+
            "  , longitude REAL"+
            "  , altitude REAL"+
            "  , accuracy REAL"+
            "  , time TEXT"+
            ");";

    private static final String CREATE_TABLE_FEED = "CREATE TABLE feederTable ("+
            "  feederID INTEGER PRIMARY KEY AUTOINCREMENT"+
            "  , feederName TEXT"+
            "  , ratedCapacity INTEGER"+
            ");";

    private static final String CREATE_TABLE_SITE = "CREATE TABLE siteTable ("+
            "  siteID INTEGER PRIMARY KEY AUTOINCREMENT"+
            "  , feederID INTEGER"+
            "  , siteName TEXT"+
            ");";

    private static final String CREATE_TABLE_TRAN = "CREATE TABLE transformerTable ("+
            "  transformerID INTEGER PRIMARY KEY AUTOINCREMENT"+
            "  , siteID INTEGER"+
            "  , ratedCapacity INTEGER"+
            "  , powerFactor REAL"+
            "  , voltage TEXT"+
            ");";

    private static final String CREATE_TABLE_POLE = "CREATE TABLE poleTable ("+
            "  poleID INTEGER PRIMARY KEY AUTOINCREMENT"+
            "  , siteID INTEGER"+
            "  , transformerID INTEGER"+
            "  , prevPoleID INTEGER"+
            ");";

    private static final String CREATE_TABLE_HOME = "CREATE TABLE homeTable ("+
            "  homeID INTEGER PRIMARY KEY AUTOINCREMENT"+
            "  , siteID INTEGER"+
            "  , poleID INTEGER"+
            "  , consumerID TEXT" +
            "  , surveyorName TEXT"+
            "  , consumerName TEXT"+
            "  , fathersName TEXT" +
            "  , mobile INTEGER"+
            "  , lastPayment REAL"+
            "  , preWill TEXT"+
            "  , hoursAvail INTEGER"+
            "  , monthlyBill REAL"+
            "  , category TEXT"+
            "  , voltage TEXT"+
            "  , load INTEGER"+
            "  , theft TEXT"+
            "  , probFace TEXT" +
            "  , notes TEXT"+
            ");";

    private static final String DATABASE_NAME = "grampower.db";

    private static final int DATABASE_VERSION = 1;

    public SurveyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_GEO);
        db.execSQL(CREATE_TABLE_FEED);
        db.execSQL(CREATE_TABLE_SITE);
        db.execSQL(CREATE_TABLE_TRAN);
        db.execSQL(CREATE_TABLE_POLE);
        db.execSQL(CREATE_TABLE_HOME);
        Log.i(this.getClass().getName(), "All tables created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(this.getClass().getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS *");
        onCreate(db);
    }
}
