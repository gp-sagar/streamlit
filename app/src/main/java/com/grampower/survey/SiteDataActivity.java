package com.grampower.survey;

import android.app.Activity;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SiteDataActivity extends Activity {

    SQLiteDatabase mDB;

    private String mSiteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_data);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSiteName = extras.getString("siteName");
        }
        Log.i(getLocalClassName(), "Viewing site: " + mSiteName);

        // set the title of the activity to the current site's name
        setTitle(mSiteName.toUpperCase());

        mDB = new SurveyDBHelper(this).getWritableDatabase();

        showData("homeTable");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.site_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.house_data:
                showData("homeTable");
//                showData("feederTable");
                return false;
            case R.id.pole_data:
                showData("poleTable");
                return false;
            case R.id.transformer_data:
                showData("transformerTable");
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showData(String tableName) {

        Cursor dataCursor = mDB.query(
                tableName
                , null
                , "siteID = '" + Utils.IDFromSiteName(getBaseContext(), mSiteName) + "'"
                , null
                , null
                , null
                , null
                , null
        );

        dataCursor.moveToFirst();
        System.out.println(dataCursor.getCount());

        if (0 >= dataCursor.getCount()) {
            ((ListView) findViewById(R.id.list)).setAdapter(
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[0])
            );
            return;
        }

        String[] listItems = new String[dataCursor.getCount()];

        do {
            listItems[dataCursor.getPosition()] = DatabaseUtils.dumpCurrentRowToString(dataCursor);
        } while (dataCursor.moveToNext());

        ListView listView = (ListView) findViewById(R.id.list);

        // Assign adapter to ListView
        listView.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems)
        );

//        listView.setOnItemClickListener(new OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (i == 1) {
//                    cursor.moveToFirst();
//                    cursor.move(position);
//                    double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
//                    double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
//                    Cursor houseDataCursor = mDB.query("surveytable", null, "SiteName " +
//                                    "='" + mSiteName + "' and latitude = '" +
//                                    latitude + "' and longitude ='" +
//                                    longitude + "'",
//                            null, null, null, null);
//                    System.out.println("No of Rows are:" + houseDataCursor.getCount());
//                    householdData(houseDataCursor);
//                }
//            }
//        });

        dataCursor.close();
    }

//    private void householdData(Cursor houseDataCursor) {
//        String[] columns = new String[]{"surveyorName", "consumerName", "fatherName", "mobile",
//                "latitude", "longitude", "time", "familyMembers", "adults",
//                "children", "occupation", "affordability", "light", "fan",
//                "tv", "otherDevices", "totalPower", "mobiles", "rechargeAmount",
//                "rechargeFrequency", "kerosene", "subsidyRate", "blackRate", "rent",
//                "connection"};
//        int[] to = new int[]{R.id.surveyor_name, R.id.consumer_name, R.id.consumer_fathers_name,
//                R.id.mobileNumber, R.id.textLatitude, R.id.textLongitude, R.id.textTime, R.id.family,
//                R.id.adults, R.id.children, R.id.occupation, R.id.affordable, R.id.light,
//                R.id.fan, R.id.television, R.id.other_equipment, R.id.total_power, R.id.mobiles, R.id.recharge_quantity
//                , R.id.recharge_times, R.id.kerosene_quantity,
//                R.id.subsidy_cost, R.id.black_cost, R.id.survey_question_6, R.id.connection_type
//        };
//
//        // create the adapter using the mCursor pointing to the desired data
//        //as well as the layout information
//        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(
//                this, R.layout.household_data,
//                houseDataCursor,
//                columns,
//                to,
//                0);
//        ListView listView = (ListView) findViewById(R.id.list);
//        // Assign adapter to ListView
//        listView.setAdapter(dataAdapter);
//    }
}
