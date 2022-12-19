package com.grampower.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SurveyActivity extends Activity {

    private static final double MIN_ACCURACY_METER = 10.0;
    private static final int GPS_RUNTIME_TIMEOUT_MS = 40000;

    private SQLiteDatabase mDB;

    private MapController mMapController;
    private MapView mMapView;

    private LocationManager mLocationManager;
    private String mSiteName;
    private int mSiteID;

    private SiteViewIconOverlay siteItemizedIconOvelay;
    private SiteViewIconOverlay markerItemizedIconOverlay;

    private Cursor mCursor;

    private Location mLastLocation;
    private cGPSListener mGPSListner;

    private ProgressDialog mDialog;

    private long retu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        // setup the blocking dialog
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("GPS event in progress...");
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSiteName = extras.getString("siteName");
            mSiteID = Utils.IDFromSiteName(this, mSiteName);
        }
        Log.i(getLocalClassName(), "Surveying site: " + mSiteName);

        // set the title of the activity to the current site's name
        setTitle(mSiteName.toUpperCase());

        // obtain a reference to the map view
        mMapView = (MapView) findViewById(R.id.mapview);

        // going out of the way to get scalable map tiles

//        OnlineTileSourceBase MapSource = TileSourceFactory.MAPQUESTAERIAL;
        final float scale = getBaseContext().getResources().getDisplayMetrics().density;
        final int newScale = (int) (256 * scale);
        System.out.println(newScale);
        XYTileSource MapSource = new XYTileSource(
                "OSM"
                , null
                , 1
                , 19
                , newScale
                , ".png"
                , new String[] {
                    "http://a.tile.openstreetmap.org/",
                    "http://b.tile.openstreetmap.org/",
                    "http://c.tile.openstreetmap.org/"
                }
        );
//        XYTileSource MapSource = new XYTileSource(
//                "CustomAerial",
//                null
//                , 0
//                , 11
//                , newScale
//                , ".jpg"
//                , new String[] {
//                    "http://otile1.mqcdn.com/tiles/1.0.0/sat/",
//                    "http://otile2.mqcdn.com/tiles/1.0.0/sat/",
//                    "http://otile3.mqcdn.com/tiles/1.0.0/sat/",
//                    "http://otile4.mqcdn.com/tiles/1.0.0/sat/"
//                }
//        );
        mMapView.setTileSource(MapSource);
        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        mMapView.setEnabled(true);

        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(MapSource.getMaximumZoomLevel());

        mDB = new SurveyDBHelper(this).getWritableDatabase();

        mCursor = mDB.query(
                "geopointTable"
                , null
                , "siteID = '" + mSiteID + "'"
                , null
                , null
                , null
                , "geoID"
                , null
        );
        mCursor.moveToFirst();

        Log.i(getLocalClassName(), "No of rows are : " + mCursor.getCount());

        markerItemizedIconOverlay = new SiteViewIconOverlay(
                new ArrayList<OverlayItem>()
                , getResources().getDrawable(R.drawable.home_marker)
                , new DefaultResourceProxyImpl(this)
        );
        mMapView.getOverlays().add(markerItemizedIconOverlay);

        if (mCursor.getCount() != 0) {
            do {
                GeoPoint locGeoPoint = new GeoPoint(
                        mCursor.getDouble(mCursor.getColumnIndex("latitude"))
                        , mCursor.getDouble(mCursor.getColumnIndex("longitude"))
                );

                String objectType = mCursor.getString(mCursor.getColumnIndex("objectType"));

                OverlayItem newMyLocationItem = new OverlayItem(
                        Integer.toString(mCursor.getInt(mCursor.getColumnIndex("geoID")))
                        , objectType
                        , Integer.toString(mCursor.getInt(mCursor.getColumnIndex("UID")))
                        , locGeoPoint
                );

//                if (objectType.equals("centralroom")) {
//                    mMapController.setCenter(locGeoPoint);
//                } else if (objectType.equals("pole")) {
//                    mMapController.setCenter(locGeoPoint);
//                }

                switch (objectType) {
                    case "house":
                        newMyLocationItem.setMarker(getResources().getDrawable(R.drawable.home_marker));
                        break;
                    case "pole":
                        newMyLocationItem.setMarker(getResources().getDrawable(R.drawable.pole_marker));
                        break;
                    case "transformer":
                        newMyLocationItem.setMarker(getResources().getDrawable(R.drawable.transformer_marker));
                    default:
                        break;
                }

                markerItemizedIconOverlay.addItem(newMyLocationItem);
                Log.i(getLocalClassName(), "Object of type " + objectType + " added.");
            } while (mCursor.moveToNext());

            Log.i(getLocalClassName(), "No of items added: " + markerItemizedIconOverlay.size());
            mCursor.close();
        }

        siteItemizedIconOvelay = new SiteViewIconOverlay(
                new ArrayList<OverlayItem>()
                , getResources().getDrawable(R.drawable.site_marker)
                , new DefaultResourceProxyImpl(this)
        );
        mMapView.getOverlays().add(siteItemizedIconOvelay);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastLocation != null) {
            updateLoc(lastLocation);
            Log.i(getLocalClassName(), "Location object obtained: " + lastLocation.toString());
        }
        mGPSListner = new cGPSListener();
        mLocationManager.addGpsStatusListener(mGPSListner);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.addGpsStatusListener(mGPSListner);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeGpsStatusListener(mGPSListner);
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeGpsStatusListener(mGPSListner);
        mLocationManager.removeUpdates(mLocationListener);
    }

    private void updateLoc(Location loc) {
        mMapController.setCenter(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
        siteItemizedIconOvelay.removeAllItems();
        siteItemizedIconOvelay.addItem(
                new OverlayItem(
                        "My Location"
                        , "My Location"
                        , new GeoPoint(loc)
                )
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.survey, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.place_transformer:
                new AddItemAsync(-1, "transformer").execute();
                return true;
            case R.id.view_data:
                startActivity(new Intent(this, SiteDataActivity.class).putExtra("siteName", mSiteName));
                return true;
            case R.id.logout:
                GramPowerSurvey.getInstance().logout();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private long householdSurvey(final double mLatitude, final double mLongitude, final float
            accuracy, final double altitude, final int poleID) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.house_details);
        dialog.setTitle("Household Survey");
        dialog.setCancelable(true);

        // UI elements
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        final String mTime = DateFormat.getDateTimeInstance().format(date);
        TextView time = (TextView) dialog.findViewById(R.id.textTime);
        time.setText("Time: \t\t\t" + mTime);

        TextView textLongitude = (TextView) dialog.findViewById(R.id.textLongitude);
        textLongitude.setText("Longitude: \t" + Double.toString(mLongitude));

        TextView latitude = (TextView) dialog.findViewById(R.id.textLatitude);
        latitude.setText("Latitude: \t" + Double.toString(mLatitude));

        TextView textAccuracy = (TextView) dialog.findViewById(R.id.textAccuracy);
        textAccuracy.setText("Accuracy: \t" + Double.toString(accuracy) + "m");

        TextView altitudeView = (TextView) dialog.findViewById(R.id.textAltitude);
        altitudeView.setText("Altitude: \t\t" + Double.toString(altitude) + "m");

        Button surveyButton = (Button) dialog.findViewById(R.id.survey_button);

        final EditText editSurveyorName = (EditText) dialog.findViewById(R.id.surveyor_name);
        editSurveyorName.setText("default");

        LinearLayout layoutTheft = (LinearLayout) dialog.findViewById(R.id.linTheft);
        ArrayList<String> theftListArray = new ArrayList<>();
        theftListArray.add("Meter hooking");
        theftListArray.add("Meter bypass");
        theftListArray.add("Cover open");
        theftListArray.add("Neutral missing");
        theftListArray.add("Neutral disturbance");
        theftListArray.add("Magnetic tamper");
        theftListArray.add("Reverse");
//        theftListArray.add("");
        final SurveyListAdapter theftListAdapter = new SurveyListAdapter(this, theftListArray);
        int countTh = theftListAdapter.getCount();
        for (int i = 0; i < countTh; i++) {
            View item = theftListAdapter.getView(i, null, null);
            layoutTheft.addView(item);
        }

        LinearLayout layoutProb = (LinearLayout) dialog.findViewById(R.id.linProb);
        ArrayList<String> probListArray = new ArrayList<>();
        probListArray.add("Low voltage");
        probListArray.add("Poor supply");
        probListArray.add("Wrong billing");
        probListArray.add("Average billing");
        probListArray.add("Bill not generated");
        probListArray.add("Transformer fault");
        probListArray.add("Line fault");
        probListArray.add("Meter not functioning");
        probListArray.add("Pole required");
//        probListArray.add("");
        final SurveyListAdapter probListAdapter = new SurveyListAdapter(this, probListArray);
        int countPr = probListAdapter.getCount();
        for (int i = 0; i < countPr; i++) {
            View item = probListAdapter.getView(i, null, null);
            layoutProb.addView(item);
        }

        surveyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                final = (EditText)dialog.findViewById(R.id.);
                final EditText editConsumerID = (EditText) dialog.findViewById(R.id.editConsumerID);
                final EditText editConsumerName = (EditText) dialog.findViewById(
                        R.id.editConsumerName
                );
                final EditText editFatherName = (EditText) dialog.findViewById(R.id.editFatherName);

                final EditText editMobile = (EditText) dialog.findViewById(R.id.editMobile);
                final EditText editLastPayment = (EditText) dialog.findViewById(R.id.editLastPayment);
                final EditText editMonthlyBill = (EditText) dialog.findViewById(R.id.editMonthlyBill);
                final EditText editHoursAvail = (EditText) dialog.findViewById(R.id.editHoursAvail);
                final EditText editVoltage = (EditText) dialog.findViewById(R.id.editVoltage);
                final EditText editLoad = (EditText)dialog.findViewById(R.id.editLoad);
                final EditText editNotes = (EditText) dialog.findViewById(R.id.editNotes);
                final Spinner cateSpin = (Spinner) dialog.findViewById(R.id.category);
                final Spinner preWill = (Spinner) dialog.findViewById(R.id.prepaidWillingness);

                String surveyorName = editSurveyorName.getEditableText().toString().trim();
                String consumerName = editConsumerName.getEditableText().toString().trim();
                String mobile = editMobile.getEditableText().toString();

                if (surveyorName.equals("") || consumerName.equals("") || mobile.equals("")) {
                    if (surveyorName.equals(""))
                        editSurveyorName.setError("Please Enter the Surveyor's Name.");
                    if (consumerName.equals(""))
                        editConsumerName.setError("Please Enter the Consumer's Name.");
                    if (mobile.equals(""))
                        editMobile.setError("Please Enter a Mobile Number.");
                } else {
                    ContentValues surveyValues = new ContentValues();
                    surveyValues.put("siteID", Utils.IDFromSiteName(getBaseContext(), mSiteName));
                    surveyValues.put("poleID", poleID);
                    surveyValues.put("consumerID", editConsumerID.getEditableText().toString().trim());
                    surveyValues.put("surveyorName", surveyorName);
                    surveyValues.put("consumerName", consumerName);
                    surveyValues.put("fathersName", editFatherName.getEditableText().toString().trim());
                    surveyValues.put("mobile", mobile);
                    surveyValues.put("lastPayment", editLastPayment.getEditableText().toString().trim());
                    surveyValues.put("preWill", preWill.getSelectedItem().toString().trim());
                    surveyValues.put("hoursAvail", editHoursAvail.getEditableText().toString().trim());
                    surveyValues.put("monthlyBill", editMonthlyBill.getEditableText().toString().trim());
                    surveyValues.put("category", cateSpin.getSelectedItem().toString().trim());
                    surveyValues.put("voltage", editVoltage.getEditableText().toString().trim());
                    surveyValues.put("load", editLoad.getEditableText().toString().trim());
                    surveyValues.put("theft", theftListAdapter.asString());
                    surveyValues.put("probFace", probListAdapter.asString());
                    surveyValues.put("notes", editNotes.getEditableText().toString().trim());

                    long newRowId = mDB.insert("homeTable", "", surveyValues);
//                    System.out.println(surveyValues.toString());
//                    System.out.println("The id of row inserted is:" + newRowId);
                    retu = newRowId;
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
        return retu;
    }

    private long transformerSurvey() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.transformer_details);
        dialog.setTitle("Transformer Detail");
        dialog.setCancelable(true);
         final EditText editcapacity = (EditText) dialog.findViewById(R.id.rated_capacity);
         Button transformerSurvey = (Button) dialog.findViewById(R.id.survey_button);
        transformerSurvey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                final = (EditText)dialog.findViewById(R.id.);

               ContentValues update = new ContentValues();
                update.put("siteID", Utils.IDFromSiteName(getApplicationContext(), mSiteName));
                update.put("ratedCapacity", Integer.parseInt(editcapacity.getEditableText().toString()));
                update.put("powerFactor", -1);
                update.put("voltage", "");

                long newRowId = mDB.insert("transformerTable", "", update);
                //                    System.out.println(surveyValues.toString());
//                    System.out.println("The id of row inserted is:" + newRowId);
                retu = newRowId;
                dialog.dismiss();
            }
        });
        dialog.show();
        return retu;
    }


    private void placeMarker(String objectType) {

        Cursor cursor = mDB.query(
                "geopointTable"
                , null
                , "siteName = '" + mSiteName + "' and  objectType = '" + objectType + "'"
                , null
                , null
                , null
                , null
        );

        cursor.moveToLast();

        OverlayItem overlayitem = new OverlayItem(
                Integer.toString(cursor.getInt(cursor.getColumnIndex("geoID")))
                , objectType
                , Integer.toString(cursor.getInt(cursor.getColumnIndex("UID")))
                , new GeoPoint(
                        cursor.getDouble(cursor.getColumnIndex("latitude"))
                        , cursor.getDouble(cursor.getColumnIndex("longitude"))
                )
        );

        switch (objectType) {
            case "house":
                overlayitem.setMarker(this.getResources().getDrawable(R.drawable.home_marker));
                break;
            case "pole":
                overlayitem.setMarker(this.getResources().getDrawable(R.drawable.transformer_marker));
                break;
            case "transformer":
                overlayitem.setMarker(this.getResources().getDrawable(R.drawable.transformer_marker));
            default:
                break;
        }

        markerItemizedIconOverlay.addItem(overlayitem);

        cursor.close();
    }

    private void presentDialogOptions(final String[] options, final int UID) {
        final int[] picked = {0};
        new AlertDialog.Builder(this)
                .setTitle("Pick")
                .setSingleChoiceItems(
                        options
                        , picked[0]
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                picked[0] = which;
                            }
                        }
                )
                .setCancelable(false)
                .setPositiveButton(
                        "Ok"
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.out.println(options[picked[0]]);
                                switch (options[picked[0]]) {
                                    case "New House":
                                        new AddItemAsync(UID, "house").execute();
                                        break;
                                    case "New Pole":
                                        new AddItemAsync(UID, "pole").execute();
                                        break;
                                    default:
                                }
                            }
                        }
                )
                .setNegativeButton(
                        "Cancel"
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(getLocalClassName(), "Pole/House survey aborted.");
                            }
                        }
                )
                .create()
                .show();
    }

    private class SiteViewIconOverlay extends ItemizedIconOverlay<OverlayItem> {

        public SiteViewIconOverlay(
                final List<OverlayItem> pList
                , Drawable pDefaultMarker
                , ResourceProxy pResourceProxy
        ) {
            super(pList, pDefaultMarker, new OnItemGestureListener<OverlayItem>() {

                @Override
                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {

                    Log.i(
                            getLocalClassName()
                            , "Clicked: geopoint " + item.getUid() + " of type "
                                    + item.getTitle() + "and uid" + item.getSnippet()
                    );

                    switch (item.getTitle()) {
                        case "pole":
                            presentDialogOptions(
                                    new String[]{"New Pole", "New House"}
                                    , Integer.parseInt(item.getSnippet())
                            );
                            return false;
                        case "transformer":
                            new AddItemAsync(
                                    -Integer.parseInt(item.getSnippet())
                                    , "pole"
                            ).execute();
                            return false;
                        default:
                            return false;
                    }
                }

                @Override
                public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
                }

            }, pResourceProxy);
        }
    }

    private class AddItemAsync extends AsyncTask<Void, Integer, Location> {

        private final int mLinkUID;
        private final String objectType;

        public AddItemAsync(int i, String objectType) {
            this.mLinkUID = i;
            this.objectType = objectType;
        }

        @Override
        protected void onPreExecute() {
            mDialog.show();
        }

        @Override
        protected Location doInBackground(Void... params) {
            Location mLocation;
            long startTime = System.currentTimeMillis();
            do {
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mLocation.getAccuracy() < MIN_ACCURACY_METER) {
                    break;
                }
                publishProgress((int) (System.currentTimeMillis() / GPS_RUNTIME_TIMEOUT_MS));
            } while ((startTime + GPS_RUNTIME_TIMEOUT_MS) > System.currentTimeMillis());
            mLastLocation = mLocation;
            return mLocation;
        }

        @Override
        protected void onPostExecute(Location location) {
            if (mDialog.isShowing()) {
                mDialog.cancel();
            }

            Log.i(
                    getLocalClassName()
                    , "Got current location " + location + ". May be within accuracy."
            );

            long ret = 0;

            switch (objectType) {
                case "house":
                    ret = householdSurvey(
                            location.getLatitude()
                            , location.getLongitude()
                            , location.getAccuracy()
                            , location.getAltitude()
                            , mLinkUID
                    );
                    break;
                case "pole":
                    ContentValues update = new ContentValues();
                    update.put("siteID", Utils.IDFromSiteName(getApplicationContext(), mSiteName));
                    update.put("transformerID", (mLinkUID<0)?-mLinkUID:-1);
                    update.put("prevPoleID", (mLinkUID>0)?mLinkUID:-1);

                    ret = mDB.insert("poleTable", "", update);
                    break;
                case "transformer":
                    ret = transformerSurvey();
                    break;
                default:
            }

            if (ret == -1) {
                return;
            }

            String time = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

            ContentValues update = new ContentValues();
            update.put("siteName", mSiteName);
            update.put("UID", ret); // TODO check
            update.put("siteID", Utils.IDFromSiteName(getApplicationContext(), mSiteName));
            update.put("latitude", location.getLatitude());
            update.put("longitude", location.getLongitude());
            update.put("altitude", location.getAltitude());
            update.put("accuracy", location.getAccuracy());
            update.put("objectType", objectType);
            update.put("time", time);

            mDB.insert("geopointTable", "", update);
            Log.i(getLocalClassName(), "Inserted object into geopointTable.");

            placeMarker(objectType);
            Log.i(getLocalClassName(), "Object placed.");
        }
    }

    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLoc(location);
            TextView display = (TextView) findViewById(R.id.locationView);
            display.setText(
                    "Latitude: " + location.getLatitude() + "\n" +
                    "Longitude: " + location.getLongitude() + "\n" +
                    "Accuracy: " + location.getAccuracy() + " meters\n" +
                    "Altitude: " + location.getAltitude() + " meters"
            );
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS is Disabled.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS is Enabled.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private class cGPSListener implements GpsStatus.Listener {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
//                    mDialog = new ProgressDialog(getBaseContext());
//                    mDialog.setMessage("GPS event in progress...");
//                    mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                    mDialog.setIndeterminate(true);
//                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    TextView display = (TextView) findViewById(R.id.locationView);
                    display.setText("Waiting for GPS");
//                    Log.i(getLocalClassName(), "GPS event started.");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    mDialog.dismiss();
//                    Log.i(getLocalClassName(), "GPS fix obtained.");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    break;
            }
        }
    }
}