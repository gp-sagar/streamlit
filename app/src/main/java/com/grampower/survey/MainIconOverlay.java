package com.grampower.survey;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.List;

public class MainIconOverlay extends ItemizedIconOverlay<OverlayItem> {

    public MainIconOverlay(
            final Context parentContext
            , final List<OverlayItem> pList
            , Drawable pDefaultMarker
            , ResourceProxy pResourceProxy
    ) {
        super(pList, pDefaultMarker, new OnItemGestureListener<OverlayItem>() {

            @Override
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                Log.i(getClass().getName(), item.getTitle() + " clicked");
                AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
                builder.setTitle(
                        "Please Select an Action for the site " + item.getTitle().toUpperCase());

                final View promptsView = LayoutInflater.from(parentContext)
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
                                parentContext.startActivity(
                                        new Intent(parentContext, SurveyActivity.class)
                                                .putExtra("siteName", item.getTitle())
                                );
                                break;
                            case R.id.radioButton2:
                                parentContext.startActivity(
                                        new Intent(parentContext, SiteViewActivity.class)
                                                .putExtra("siteName", item.getTitle())
                                );
                                break;
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }

            @Override
            public boolean onItemLongPress(int i, OverlayItem overlayItem) {
                return false;
            }

        }, pResourceProxy);
    }
}