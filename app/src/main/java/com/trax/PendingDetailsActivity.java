package com.trax;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bean.ShipmentItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.interfaces.BackgroundTaskInterface;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.utility.Constant;
import com.utility.DBAdapter;
import com.utility.GPSTrackerSecond;
import com.utility.ConnectionCheck;
import com.utility.Pref;
import com.asynctask.RunBackgroundAsync;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Avishek on 6/17/2015.
 */
public class PendingDetailsActivity extends ActionBarActivity implements BackgroundTaskInterface{

    // Google Map
    private MapView mapView;
    private GoogleMap googleMap;

    private ParallaxScrollView scrollViewParent;
    private View customView;

    private TextView tvDropType, tvShipmentType, tvFromTo, tvShipmentTime, tvShipmentTitle, tvShipmentAddress, tvContactPerson, tvContactNo;
    private LinearLayout llCall, llStart;

    private ArrayList<ShipmentItem> pendingList;
    int position = 0;

    private ActionBar actionBar;
    private ProgressDialog pDialog;

    private Pref _pref;
    private ConnectionCheck _connectionCheck;
    private GPSTrackerSecond _GpsTrackerSecond;
    private DBAdapter db;

    String startFrom = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_details);

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
        MapsInitializer.initialize(PendingDetailsActivity.this);

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

        _pref = new Pref(PendingDetailsActivity.this);
        _connectionCheck = new ConnectionCheck(PendingDetailsActivity.this);
        _GpsTrackerSecond = new GPSTrackerSecond(PendingDetailsActivity.this);
        db = new DBAdapter(PendingDetailsActivity.this);

        tvDropType = (TextView) findViewById(R.id.tvDropType);
        tvShipmentType = (TextView) findViewById(R.id.tvShipmentType);
        tvFromTo = (TextView) findViewById(R.id.tvFromTo);
        tvShipmentTime = (TextView) findViewById(R.id.tvShipmentTime);
        tvShipmentTitle = (TextView) findViewById(R.id.tvShipmentTitle);
        tvShipmentAddress = (TextView) findViewById(R.id.tvShipmentAddress);
        tvContactPerson = (TextView) findViewById(R.id.tvContactPerson);
        tvContactNo = (TextView) findViewById(R.id.tvContactNo);
        llCall = (LinearLayout) findViewById(R.id.llCall);
        llStart = (LinearLayout) findViewById(R.id.llStart);

        scrollViewParent = (ParallaxScrollView)findViewById(R.id.scrollViewParent);
        customView = (View)findViewById(R.id.customView);

        pendingList = new ArrayList<ShipmentItem>();

    }

    private void setValue(){

        startFrom = getIntent().getStringExtra("startFrom");

        if(startFrom.equalsIgnoreCase("SearchActivity")){
            pendingList = Constant.searchList;
            position = getIntent().getIntExtra("position", 0);
            Log.v("size", String.valueOf(Constant.pendingList.size()));
            Log.v("position", String.valueOf(position));
        }
        else{
            pendingList = Constant.pendingList;
            position = getIntent().getIntExtra("position", 0);
            Log.v("size", String.valueOf(Constant.pendingList.size()));
            Log.v("position", String.valueOf(position));
        }

        tvShipmentType.setText(pendingList.get(position).getShipmentType().toUpperCase());
        tvShipmentTime.setText(pendingList.get(position).getShipmentPickupTime());

        if(pendingList.get(position).getShipmentType().equals("pickup") || pendingList.get(position).getShipmentType().equals("cms")){

            tvDropType.setText("Deliver to " + pendingList.get(position).getDeliveryName());
            tvFromTo.setText("From");
            tvShipmentTitle.setText(pendingList.get(position).getPickupName());
            tvShipmentAddress.setText(pendingList.get(position).getPickupHomeName() + ", " + pendingList.get(position).getPickupStreetName() + ", " + pendingList.get(position).getPickupLocationName() + ", " + "Near " + pendingList.get(position).getPickupLandmark() + ", " + pendingList.get(position).getPickupCityName() + " - " + pendingList.get(position).getPickupPincode());
            tvContactPerson.setText("Contact Person : " + pendingList.get(position).getPickupName());
            tvContactNo.setText("Mobile Number : " + pendingList.get(position).getPickupContactNo());

            if(!pendingList.get(position).getPickupLatValue().equals("") && !pendingList.get(position).getPickupLongValue().equals("")){

                double lat = Double.parseDouble(pendingList.get(position).getPickupLatValue());
                double lng = Double.parseDouble(pendingList.get(position).getPickupLongValue());

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng) , 8));
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(new LatLng(lat, lng)));


            }
            else{

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_GpsTrackerSecond.getLatitude(), _GpsTrackerSecond.getLongitude()) , 8));
            }

        }

        else{

            tvDropType.setText("Pickup from " + pendingList.get(position).getPickupName());
            tvFromTo.setText("To");
            tvShipmentTitle.setText(pendingList.get(position).getDeliveryName());
            tvShipmentAddress.setText(pendingList.get(position).getDeliveryHomeName() + ", " + pendingList.get(position).getDeliveryStreetName()+ ", " + pendingList.get(position).getDeliveryLocationName() + ", " + "Near " + pendingList.get(position).getDeliveryLandmark() + ", " + pendingList.get(position).getDeliveryCityName() + " - " + pendingList.get(position).getDeliveryPincode());
            tvContactPerson.setText("Contact Person : " + pendingList.get(position).getDeliveryName());
            tvContactNo.setText("Mobile Number : " + pendingList.get(position).getDeliveryContactNo());


            if(!pendingList.get(position).getDeliveryLatValue().equals("") && !pendingList.get(position).getDeliveryLongValue().equals("")){

                double lat = Double.parseDouble(pendingList.get(position).getDeliveryLatValue());
                double lng = Double.parseDouble(pendingList.get(position).getDeliveryLongValue());

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng) , 8));
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(new LatLng(lat, lng)));


            }
            else{

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_GpsTrackerSecond.getLatitude(), _GpsTrackerSecond.getLongitude()) , 8));
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

        llStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Constant.intransitList.size() > 0){

                    new SweetAlertDialog(PendingDetailsActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("New shipment cant be start until finish the previous one")
                            .show();

                }

                else{

                    new SweetAlertDialog(PendingDetailsActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Confirmation...")
                            .setContentText("Are you sure to start?")
                            .setCancelText("No, Cancel")
                            .setConfirmText("Yes, Sure")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
                                    updateShipmentStatus();
                                }
                            })
                            .show();

                }

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

        final Dialog dialog = new Dialog(PendingDetailsActivity.this);
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

        if(pendingList.get(position).getShipmentType().equals("pickup") || pendingList.get(position).getShipmentType().equals("cms")){

            dialog_tvFromTo.setText("To");
            dialog_tvShipmentTitle.setText(pendingList.get(position).getDeliveryName());
            dialog_tvShipmentAddress.setText(pendingList.get(position).getDeliveryHomeName()+ ", " + pendingList.get(position).getDeliveryStreetName()+ ", " + pendingList.get(position).getDeliveryLocationName() + ", " + "Near " + pendingList.get(position).getDeliveryLandmark() + ", " +  pendingList.get(position).getDeliveryCityName() + " - " + pendingList.get(position).getDeliveryPincode());
            dialog_tvContactPerson.setText("Contact Person : " + pendingList.get(position).getDeliveryName());
            dialog_tvContactNo.setText("Mobile Number : " + pendingList.get(position).getDeliveryContactNo());

        }

        else{

            dialog_tvFromTo.setText("From");
            dialog_tvShipmentTitle.setText(pendingList.get(position).getPickupName());
            dialog_tvShipmentAddress.setText(pendingList.get(position).getPickupHomeName()+ ", " + pendingList.get(position).getPickupStreetName()+ ", " + pendingList.get(position).getPickupLocationName() + ", " + "Near " + pendingList.get(position).getPickupLandmark() + ", " +  pendingList.get(position).getPickupCityName() + " - " + pendingList.get(position).getPickupPincode());
            dialog_tvContactPerson.setText("Contact Person : " + pendingList.get(position).getPickupName());
            dialog_tvContactNo.setText("Mobile Number : " + pendingList.get(position).getPickupContactNo());

        }

        dialog_btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }

    private void updateShipmentStatus(){

        if (_connectionCheck.isNetworkAvailable()) {

            String url = Constant.baseUrl  + "updateshipmentstatus";

            try {

                if(!String.valueOf(_GpsTrackerSecond.getLatitude()).equals("0.0") && !String.valueOf(_GpsTrackerSecond.getLongitude()).equals("0.0")){

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("agentId", _pref.getAgentId());
                    jsonObject.put("accessToken", _pref.getAccessToken());
                    jsonObject.put("shipmentId", pendingList.get(position).getShipmentId());
                    jsonObject.put("latValue", String.valueOf(_GpsTrackerSecond.getLatitude()));
                    jsonObject.put("longValue", String.valueOf(_GpsTrackerSecond.getLongitude()));
                    jsonObject.put("action", "start");
                    jsonObject.put("capturedData", "");

                    JSONObject data = new JSONObject();
                    data.put("data", jsonObject);

                    String jsonInput = data.toString();
                    Log.v("jsonInput", jsonInput);

                    RunBackgroundAsync async = new RunBackgroundAsync(
                            PendingDetailsActivity.this);
                    async.taskInterface = PendingDetailsActivity.this;
                    async.execute(url, jsonInput);

                }
                else{

                    new SweetAlertDialog(PendingDetailsActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Can't fetch your current location!! Please Check GPS and Try Again")
                            .show();

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {

            _connectionCheck.getNetworkActiveAlert().show();
        }

    }

    @Override
    public void onStarted() {

        pDialog = new ProgressDialog(PendingDetailsActivity.this);
        pDialog.setMessage("Please Wait...");
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

    }

    @Override
    public void onCompleted(String jsonStr) {

        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }

        if (jsonStr != null) {

            try {

                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONObject errNodeObj = jsonObj.getJSONObject("errNode");
                String errCode = errNodeObj.getString("errCode");
                String errMsg = errNodeObj.getString("errMsg");

                if(errCode.equalsIgnoreCase("0")){

                    db.open();
                    db.updateRecord(pendingList.get(position).getShipmentId(), "intransit");
                    db.close();

                    _pref.saveIntransitShipmentId(pendingList.get(position).getShipmentId());

                    if(startFrom.equalsIgnoreCase("SearchActivity")){
                        Intent intent = new Intent(PendingDetailsActivity.this, TraxActivity.class);
                        intent.putExtra("startFrom", "PendingDetailsActivity");
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    }
                    else{
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        TraxActivity.setViewPager(1);
                        finish();
                    }



                }
                else{

                    new SweetAlertDialog(PendingDetailsActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText(errMsg)
                            .show();

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        else{

            new SweetAlertDialog(PendingDetailsActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Something going wrong!! Please Try Again")
                    .show();

        }

    }

}

