package com.trax;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bean.ShipmentItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.utility.Constant;
import com.utility.Pref;

import java.util.ArrayList;

/**
 * Created by Avishek on 7/17/2015.
 */
public class CompletedDetailsActivity extends ActionBarActivity{

    // Google Map
    private MapView mapView;
    private GoogleMap googleMap;

    private ParallaxScrollView scrollViewParent;
    private View customView;

    private TextView tvDropType, tvShipmentType, tvFromTo, tvShipmentTime, tvShipmentTitle, tvShipmentAddress, tvContactPerson, tvContactNo;
    private LinearLayout llCall;

    private ArrayList<ShipmentItem> completedList;
    int position = 0;

    private ActionBar actionBar;

    private Pref _pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_details);

        initialize();

        actionBar = getSupportActionBar();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = inflater.inflate(R.layout.custom_actionbar_layout, null);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222E68")));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionbarView, layoutParams);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        googleMap = mapView.getMap();
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(CompletedDetailsActivity.this);

        setValue();
        onClick();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void initialize(){

        _pref = new Pref(CompletedDetailsActivity.this);

        tvDropType = (TextView) findViewById(R.id.tvDropType);
        tvShipmentType = (TextView) findViewById(R.id.tvShipmentType);
        tvFromTo = (TextView) findViewById(R.id.tvFromTo);
        tvShipmentTime = (TextView) findViewById(R.id.tvShipmentTime);
        tvShipmentTitle = (TextView) findViewById(R.id.tvShipmentTitle);
        tvShipmentAddress = (TextView) findViewById(R.id.tvShipmentAddress);
        tvContactPerson = (TextView) findViewById(R.id.tvContactPerson);
        tvContactNo = (TextView) findViewById(R.id.tvContactNo);
        llCall = (LinearLayout) findViewById(R.id.llCall);

        scrollViewParent = (ParallaxScrollView)findViewById(R.id.scrollViewParent);
        customView = (View)findViewById(R.id.customView);

        completedList = new ArrayList<ShipmentItem>();

    }

    private void setValue(){

        if(getIntent().getStringExtra("startFrom").equalsIgnoreCase("SearchActivity")){
            completedList = Constant.searchList;
            position = getIntent().getIntExtra("position", 0);
            Log.v("size", String.valueOf(Constant.searchList.size()));
            Log.v("position", String.valueOf(position));
        }
        else{
            completedList = Constant.completedList;
            position = getIntent().getIntExtra("position", 0);
            Log.v("size", String.valueOf(Constant.completedList.size()));
            Log.v("position", String.valueOf(position));
        }

        tvShipmentType.setText(completedList.get(position).getShipmentType().toUpperCase());
        tvShipmentTime.setText(completedList.get(position).getShipmentPickupTime());

        if(completedList.get(position).getShipmentType().equals("pickup") || completedList.get(position).getShipmentType().equals("cms")){

            tvDropType.setText("Deliver to " + completedList.get(position).getDeliveryName());
            tvFromTo.setText("From");
            tvShipmentTitle.setText(completedList.get(position).getPickupName());
            tvShipmentAddress.setText(completedList.get(position).getPickupHomeName() + ", " + completedList.get(position).getPickupStreetName()+ ", " + completedList.get(position).getPickupLocationName() + ", " + "Near " + completedList.get(position).getPickupLandmark() + ", " +  completedList.get(position).getPickupCityName() + " - " + completedList.get(position).getPickupPincode());
            tvContactPerson.setText("Contact Person : " + completedList.get(position).getPickupName());
            tvContactNo.setText("Mobile Number : " + completedList.get(position).getPickupContactNo());

            if(!completedList.get(position).getPickupLatValue().equals("") && !completedList.get(position).getPickupLongValue().equals("")){

                double lat = Double.parseDouble(completedList.get(position).getPickupLatValue());
                double lng = Double.parseDouble(completedList.get(position).getPickupLongValue());

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 11));
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(new LatLng(lat, lng)));

            }
            else{

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(_pref.getLatitude()), Double.parseDouble(_pref.getLongitude())) , 10));
            }



        }

        else{

            tvDropType.setText("Pickup from " + completedList.get(position).getPickupName());
            tvFromTo.setText("To");
            tvShipmentTitle.setText(completedList.get(position).getDeliveryName());
            tvShipmentAddress.setText(completedList.get(position).getDeliveryHomeName() + ", " + completedList.get(position).getDeliveryStreetName()+ ", " + completedList.get(position).getDeliveryLocationName() + ", " + "Near " + completedList.get(position).getPickupLandmark() + ", " +  completedList.get(position).getDeliveryCityName() + " - " + completedList.get(position).getDeliveryPincode());
            tvContactPerson.setText("Contact Person : " + completedList.get(position).getDeliveryName());
            tvContactNo.setText("Mobile Number : " + completedList.get(position).getDeliveryContactNo());


            if(!completedList.get(position).getDeliveryLatValue().equals("") && !completedList.get(position).getDeliveryLongValue().equals("")){

                double lat = Double.parseDouble(completedList.get(position).getDeliveryLatValue());
                double lng = Double.parseDouble(completedList.get(position).getDeliveryLongValue());

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng) , 11));
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(new LatLng(lat, lng)));

            }
            else{

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(_pref.getLatitude()), Double.parseDouble(_pref.getLongitude())) , 10));
            }

        }

    }

    private void onClick(){

        tvDropType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSecondaryContactDetails();

            }
        });

        llCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number = tvContactNo.getText().toString();

                if(number.length() > 0){

                    Intent dial = new Intent();
                    dial.setAction("android.intent.action.DIAL");
                    dial.setData(Uri.parse("tel:" + number));
                    startActivity(dial);

                }

            }
        });

        customView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        scrollViewParent.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        scrollViewParent.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scrollViewParent.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

    }

    private void showSecondaryContactDetails(){

        final Dialog dialog = new Dialog(CompletedDetailsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setContentView(R.layout.dialog_secondary_contact);
        dialog.show();

        TextView dialog_tvFromTo = (TextView)dialog.findViewById(R.id.tvFromTo);
        TextView dialog_tvShipmentTitle = (TextView)dialog.findViewById(R.id.tvShipmentTitle);
        TextView dialog_tvShipmentAddress = (TextView)dialog.findViewById(R.id.tvShipmentAddress);
        TextView dialog_tvContactPerson = (TextView)dialog.findViewById(R.id.tvContactPerson);
        TextView dialog_tvContactNo = (TextView)dialog.findViewById(R.id.tvContactNo);
        Button dialog_btnOk = (Button)dialog.findViewById(R.id.btnOk);

        if(completedList.get(position).getShipmentType().equals("pickup") || completedList.get(position).getShipmentType().equals("cms")){

            dialog_tvFromTo.setText("To");
            dialog_tvShipmentTitle.setText(completedList.get(position).getDeliveryName());
            dialog_tvShipmentAddress.setText(completedList.get(position).getDeliveryHomeName()+ ", " + completedList.get(position).getDeliveryStreetName()+ ", " + completedList.get(position).getDeliveryLocationName() + ", " + "Near " + completedList.get(position).getPickupLandmark() + ", " +  completedList.get(position).getDeliveryCityName() + " - " + completedList.get(position).getDeliveryPincode());
            dialog_tvContactPerson.setText("Contact Person : " + completedList.get(position).getDeliveryName());
            dialog_tvContactNo.setText("Mobile Number : " + completedList.get(position).getDeliveryContactNo());

        }

        else{

            dialog_tvFromTo.setText("From");
            dialog_tvShipmentTitle.setText(completedList.get(position).getPickupName());
            dialog_tvShipmentAddress.setText(completedList.get(position).getPickupHomeName()+ ", " + completedList.get(position).getPickupStreetName()+ ", " + completedList.get(position).getPickupLocationName() + ", " + "Near " + completedList.get(position).getPickupLandmark() + ", " +  completedList.get(position).getPickupCityName() + " - " + completedList.get(position).getPickupPincode());
            dialog_tvContactPerson.setText("Contact Person : " + completedList.get(position).getPickupName());
            dialog_tvContactNo.setText("Mobile Number : " + completedList.get(position).getPickupContactNo());

        }

        dialog_btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }

}