package com.grampower.survey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private final String CREATE_SURVEY_TABLE = "CREATE TABLE surveytable(_id INTEGER PRIMARY KEY," +
            "siteName TEXT,latitude REAL,longitude REAL,time TEXT,surveyorName TEXT,consumerName TEXT," +
            "fatherName TEXT,mobile TEXT,familyMembers INTEGER,adults INTEGER,children INTEGER," +
            "occupation TEXT,affordability TEXT,light INTEGER,fan INTEGER,tv INTEGER," +
            "otherDevices INTEGER,totalPower REAL,mobiles INTEGER,rechargeAmount REAL," +
            "rechargeFrequency INTEGER,kerosene REAL,subsidyRate REAL,blackRate REAL," +
            "rent TEXT,connection TEXT)";
    private final String CREATE_COMMUNITY_TABLE = "CREATE TABLE communitytable(_id INTEGER PRIMARY KEY," +
            "siteName TEXT,houses INTEGER,population INTEGER,block TEXT,district TEXT," +
            "distance REAL,plantSite TEXT,fieldSize REAL,room TEXT,roomRent REAL,celltower TEXT," +
            "celltowerDistance REAL,plantDetails TEXT,stockDetails TEXT)";
    private final String CREATE_GEOPOINTS_TABLE = "CREATE TABLE geopoint(_id INTEGER PRIMARY KEY," +
            "siteName TEXT,objectName TEXT,latitude REAL,longitude REAL,distance REAL," +
            "accuracy REAL,time TEXT)";
    private String dbName;

    public MyDatabaseHelper(Context context) {
        super(context, "gramPower", null, 1);
        this.dbName = "gramPower";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SURVEY_TABLE);
        System.out.println("Survey table has been created in database:" + dbName);
        db.execSQL(CREATE_COMMUNITY_TABLE);
        System.out.println("Community table is created");
        db.execSQL(CREATE_GEOPOINTS_TABLE);
        System.out.println("GeoPoint table is created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void creatSurveyTable() {
        // TODO Auto-generated method stub
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(CREATE_SURVEY_TABLE);
        System.out.println("Survey table has been created in database:" + dbName);
    }
}
