package com.grampower.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.util.constants.MapViewConstants;

import java.util.ArrayList;
import java.util.List;

public class SiteViewActivity extends Activity {

    private static final String TAG = "SiteViewActivity";
    Cursor mCursor;
    private MapView mMapView;
    private MapController mMapController;
    private String mSiteName;

    private SurveyDBHelper mDBHelper;
    private SQLiteDatabase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSiteName = extras.getString("siteName");
        }
        Log.i(TAG, "Viewing site: " + mSiteName);

        // set the title of the activity to the current site's name
        setTitle(mSiteName.toUpperCase());

        // the MapView
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        mMapView.setEnabled(true);

        // map controller
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(MapViewConstants.MAXIMUM_ZOOMLEVEL);

        /* PlaceOverlayIconsTask place = new PlaceOverlayIconsTask(mContext);
        place.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/
        //MyDatabaseHelper mDBHelper= new MyDatabaseHelper(mContext);
        //SQLiteDatabase mDB = mDBHelper.getWritableDatabase();

        mDBHelper = new SurveyDBHelper(this);
        mDB = mDBHelper.getWritableDatabase();

        //Initializing mCursor to get data from geopointTable
        mCursor = mDB.query(
                "geopointTable"
                , null
                , "siteName = '" + mSiteName + "'"
                , null
                , null
                , null
                , null
                , null
        );

        Log.i(getLocalClassName(), "Number of objects is : " + mCursor.getCount());

        mCursor.moveToFirst();

        SiteViewIconOverlay siteViewItemizedIconOverlay;

        ArrayList<OverlayItem> siteViewOverlayItemArray = new ArrayList<>();

        siteViewItemizedIconOverlay = new SiteViewIconOverlay(
                siteViewOverlayItemArray
                , getResources().getDrawable(R.drawable.site_marker)
                , new DefaultResourceProxyImpl(this)
        );

        mMapView.getOverlays().add(siteViewItemizedIconOverlay);

        if (0 != mCursor.getCount()) {
            do {
                GeoPoint locGeoPoint = new GeoPoint(
                        mCursor.getDouble(mCursor.getColumnIndex("latitude"))
                        , mCursor.getDouble(mCursor.getColumnIndex("longitude"))
                );

                String objectType = mCursor.getString(mCursor.getColumnIndex("objectType"));
                Log.i(getLocalClassName(), "Object is " + objectType);

                OverlayItem newMyLocationItem = new OverlayItem(
                        mCursor.getString(mCursor.getColumnIndex("siteName"))
                        , objectType
                        , locGeoPoint
                );

                if (objectType.equalsIgnoreCase("centralroom")) {
//                    newMyLocationItem.setMarker(getResources().getDrawable(R.drawable.control_room));
                    mMapController.setCenter(locGeoPoint);
                } else if (objectType.equalsIgnoreCase("pole")) {
//                    newMyLocationItem.setMarker(getResources().getDrawable(R.drawable.pole));
                }
                siteViewItemizedIconOverlay.addItem(newMyLocationItem);

            } while (mCursor.moveToNext());
        } else {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set title
            alertDialogBuilder.setTitle("Error");

            // set dialog message
            alertDialogBuilder
                    .setMessage("No objects at selected site.")
                    .setCancelable(true);

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }

        Log.i(getLocalClassName(), "No. of items added is: " + siteViewItemizedIconOverlay.size());
        Log.i(getLocalClassName(), "All markers placed.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.site_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ViewData:
                startActivity(
                        new Intent(this, SiteDataActivity.class)
                                .putExtra("siteName", mSiteName)
                );
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class SiteViewIconOverlay extends ItemizedIconOverlay<OverlayItem> {
        public SiteViewIconOverlay(final List<OverlayItem> pList, Drawable pDefaultMarker, ResourceProxy pResourceProxy) {
            super(pList, pDefaultMarker, new OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    return false;
                }

                @Override
                public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
                }
            }, pResourceProxy);
        }
    }
}
