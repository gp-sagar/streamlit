package com.grampower.survey;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private String mSiteName;

    private MapView mMapView;
    private MapController mMapController;

    private SurveyDBHelper mDBHelper;
    private SQLiteDatabase mDB;

    private ProgressDialog mDialog;

    /**
     *  The authority for the sync adapter's content provider
     */
    public static final String AUTHORITY = "com.grampower.survey.Syncing";

    /**
     *  An account type, in the form of a domain name
     */
    public static final String ACCOUNT_TYPE = "grampower.com";

    /**
     *  The account name
     */
    public static final String ACCOUNT = "defaultaccount";

    /**
     * Instance of Account
     */
    Account mAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /*
         * Create the dummy account. The code for CreateSyncAccount
         * is listed in the lesson Creating a Sync Adapter
         */
        mAccount = CreateSyncAccount(this);

        // if user is not signed in, finish current activity and launch login screen
        if (!isUserSignedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        setContentView(R.layout.activity_main);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Exporting...");
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);

        mDBHelper = new SurveyDBHelper(this);
        mDB = mDBHelper.getWritableDatabase();

        // get the mapview up and running
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        mMapView.setEnabled(true);

        // get a controller for the view
        mMapController = (MapController) mMapView.getController();

        Cursor cursor;
        MainIconOverlay mItemizedIconOverlay;

        //Initializing mCursor to get data from geopoint table
        cursor = mDB.query(
                true
                , "geopointTable"
                , null
                , null
                , null
                , "siteID"
                , null
                , "geoID"
                , null
        );

        Log.i(this.getLocalClassName(), "Number of sites in geopointTable is: " + cursor.getCount());

        // get the first site
        cursor.moveToFirst();

        ArrayList<OverlayItem> overlayItemArray = new ArrayList<>();

        DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
        Drawable mCRoomMarker = getResources().getDrawable(R.mipmap.ic_launcher);

        mItemizedIconOverlay = new MainIconOverlay(
                this
                , overlayItemArray
                , mCRoomMarker
                , defaultResourceProxyImpl);

        mMapView.getOverlays().add(mItemizedIconOverlay);

        if (0 != cursor.getCount()) {
            do {
                GeoPoint locGeoPoint = new GeoPoint(
                        cursor.getDouble(cursor.getColumnIndex("latitude"))
                        , cursor.getDouble(cursor.getColumnIndex("longitude")));

                OverlayItem newMyLocationItem = new OverlayItem(
                        cursor.getString(cursor.getColumnIndex("siteName"))
                        , cursor.getString(cursor.getColumnIndex("objectType"))
                        , locGeoPoint
                );

                mItemizedIconOverlay.addItem(newMyLocationItem);
            } while (cursor.moveToNext());
        }

        //Initializing mCursor to get data from geopoint table
        cursor = mDB.query(
                true
                , "siteTable"
                , null
                , null
                , null
                , null
                , null
                , "siteID"
                , null
        );

        Spinner spinner = (Spinner) findViewById(R.id.spinnerSite);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> siteNames = new ArrayList<>();

        Log.i(this.getLocalClassName(), "Number of sites in siteTable is: " + cursor.getCount());

        // get the first site
        cursor.moveToFirst();

        if (0 != cursor.getCount()) {
            do {
                siteNames.add(cursor.getString(cursor.getColumnIndex("siteName")));
            } while (cursor.moveToNext());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                this
                , android.R.layout.simple_spinner_item
                , siteNames);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        //        double lat,lon,centerLat = 0,centerLon = 0;
        //        mCursor.moveToFirst();
        //        int i=0;
        //        do {
        //            lat = mCursor.getDouble(mCursor.getColumnIndex("latitude"));
        //            System.out.println("Latitude is :"+lat);
        //            lon = mCursor.getDouble(mCursor.getColumnIndex("longitude"));
        //            System.out.println("Longitude is :"+lon);
        //            centerLat= (centerLat+lat);
        //            centerLon= (centerLon+lon);
        //
        //            i++;
        //        } while (mCursor.moveToNext());
        //        centerLat = centerLat/(i);
        //        centerLon = centerLon/(i);
        //        System.out.println("Center : latitude is : "+centerLat+" Longitude is : "+centerLon);
        //        System.out.println("i is :"+i);

        // the whole of India needs to be visible
        mMapController.setZoom(6);

        // India is at 21.0000° N, 78.0000° E
        // TODO strange offset required
        mMapController.setCenter(new GeoPoint(31.0, 71.0));
        cursor.close();
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */

    public static Account CreateSyncAccount(Context context) {

        /**
         *  Create the account type and default account
         */
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);

        /**
         *  Get an instance of the Android account manager
         */
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);

        /**
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        try {
            if (accountManager.addAccountExplicitly(newAccount, null, null)) {

                /**
                 * If you don't set android:syncable="true" in
                 * in your <provider> element in the manifest,
                 * then call context.setIsSyncable(account, AUTHORITY, 1)
                 * here.
                 */
                System.out.println("new Account created explicitly");
            } else {

                /**
                 * The account exists or some other error occurred. Log this, report it,
                 * or handle it internally
                 */
                System.out.println("Account exists");
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return newAccount;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_site_option:
                setContentView(R.layout.activity_new_site);
                Spinner s = (Spinner) findViewById(R.id.spinnerFeed);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this
                        , android.R.layout.simple_spinner_item
                        , Utils.getAllFeeders(getBaseContext())
                );
                s.setAdapter(adapter);
                Button saveButton = (Button) findViewById(R.id.buttonSave);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doSiteSurvey();
                    }
                });
                return true;
            case R.id.new_feeder_option:
                setContentView(R.layout.activity_new_feeder);
                Button feederButton = (Button) findViewById(R.id.feederSave);
                final TextView feedermsg = (TextView) findViewById(R.id.feederMsg);
                feederButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.addFeederToDatabase(
                                getApplicationContext()
                                , ((EditText) findViewById(R.id.editFeederName))
                                        .getEditableText()
                                        .toString()
                                , Integer.parseInt(((EditText) findViewById(R.id.editRatedCapacity))
                                        .getEditableText()
                                        .toString())
                        );
                    feedermsg.setText("feeder created");
                    }
                });
                return true;
            case R.id.upload_map_option:
                new ExportDatabaseCSVAsyncTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // method to save community survey data
    public void doSiteSurvey() {
        ContentValues siteValues = new ContentValues();
        final EditText editSiteName = (EditText) findViewById(R.id.editSiteName);
        final Spinner spinFeed = (Spinner) findViewById(R.id.spinnerFeed);
        mSiteName = editSiteName.getEditableText().toString();
        if (mSiteName.equals("")) {
            editSiteName.setError("Please enter a site name.");
        } else {
            siteValues.put("siteName", mSiteName);
            siteValues.put("feederId", spinFeed.getSelectedItemPosition());
            long newRowId = mDB.insert("siteTable", null, siteValues);
            Log.i(this.getLocalClassName(), "The id of row inserted is: " + newRowId);
            startActivity(
                    new Intent(
                            getBaseContext()
                            , SurveyActivity.class)
                    .putExtra("siteName", mSiteName)
            );
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        final String item = (String) adapterView.getItemAtPosition(i);
        Log.i(getLocalClassName(), item + " selected.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please Select an Action for the site " + item.toUpperCase());

        final View promptsView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_site_view, null);

        // set prompts.xml to alertdialog builder
        builder.setView(promptsView);
        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                RadioGroup types = (RadioGroup) promptsView.findViewById(R.id.radioGroup);
                switch (types.getCheckedRadioButtonId()) {
                    case R.id.radioButton1:
                        startActivity(
                                new Intent(MainActivity.this, SurveyActivity.class)
                                        .putExtra("siteName", item)
                        );
                        break;
                    case R.id.radioButton2:
                        startActivity(
                                new Intent(MainActivity.this, SiteViewActivity.class)
                                        .putExtra("siteName", item)
                        );
                        break;
                }
            }
        });

        // create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    // retrieve access token from preferences
    public boolean isUserSignedIn() {
        return getSharedPreferences(
                        GramPowerSurvey.PREFERENCES_STRING
                        , Context.MODE_MULTI_PROCESS
                ).getBoolean("isUserSignedIn", false);
    }

    class ExportDatabaseCSVAsyncTask extends AsyncTask<String, Void, Boolean> {

        private boolean doExportOfTable(String tableName, File exportDir) {

            CSVWriter csvWrite;
            try {
                csvWrite = new CSVWriter(new FileWriter(new File(exportDir, tableName + ".csv")));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            Cursor cursorCSV = mDB.rawQuery("select * from " + tableName, null);

            csvWrite.writeNext(cursorCSV.getColumnNames());
            cursorCSV.moveToFirst();

            while (cursorCSV.moveToNext()) {
                String arrStr[] = new String[cursorCSV.getColumnCount()];

                for (int i = 0; i < cursorCSV.getColumnCount(); i++) {
                    if (cursorCSV.getColumnName(i).equals("latitude")
                            || cursorCSV.getColumnName(i).equals("longitude")) {
                        arrStr[i] = Double.toString(cursorCSV.getDouble(i));
                    } else {
                        arrStr[i] = cursorCSV.getString(i);
                    }
                }

                csvWrite.writeNext(arrStr);
            }
            try {
                csvWrite.close();
                Log.d(this.getClass().getName(), "Exported the table " + tableName + ".");
                cursorCSV.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            mDialog.setMessage("Exporting database...");
            mDialog.show();
        }

        protected Boolean doInBackground(final String... args) {

            File exportDir = new File(Environment.getExternalStorageDirectory() + "/GramPowerSurvey");

            if (!exportDir.exists()) {
                Log.i(this.getClass().getName(), "Created export dir." + exportDir.mkdirs());
            }

            boolean res;
            res = doExportOfTable("geopointTable", exportDir);
            res &= doExportOfTable("feederTable", exportDir);
            res &= doExportOfTable("siteTable", exportDir);
            res &= doExportOfTable("transformerTable", exportDir);
            res &= doExportOfTable("poleTable", exportDir);
            res &= doExportOfTable("homeTable", exportDir);
            return res;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            if (success) {
                Toast.makeText(getBaseContext(), "Export successful!", Toast.LENGTH_SHORT).show();

                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                /*
                 * Request the sync for the default account, authority, and
                 * manual sync settings
                 */
                ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);

            } else {
                Toast.makeText(getBaseContext(), "Export failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}