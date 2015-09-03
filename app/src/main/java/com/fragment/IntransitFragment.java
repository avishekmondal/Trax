package com.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.interfaces.BackgroundTaskInterface;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.trax.TraxActivity;
import com.utility.DBAdapter;
import com.utility.GPSTrackerSecond;
import com.trax.R;
import com.utility.Constant;
import com.utility.HttpConnection;
import com.utility.ConnectionCheck;
import com.utility.PathJSONParser;
import com.utility.Pref;
import com.asynctask.RunBackgroundAsync;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Avishek on 6/15/2015.
 */
public class IntransitFragment extends Fragment implements BackgroundTaskInterface{

    private View rootView;
    private LinearLayout llIntransit;
    private MapView mapView;
    private GoogleMap googleMap;

    private ParallaxScrollView scrollViewParent;
    private View customView;

    private TextView tvDropType, tvShipmentType, tvFromTo, tvShipmentTime, tvShipmentTitle, tvShipmentAddress, tvContactPerson, tvContactNo;
    private LinearLayout llCall, llRejected, llReached, llCompleted, llPending;
    private RelativeLayout rlReached, rlCompleted;

    int reqWebService = 0;
    String updateStatusAction = "", capturedData = "";

    private ArrayList<ShipmentItem> intransitList;
    private ShipmentItem intransitItem;

    private ProgressWheel progressWheel;
    private ProgressDialog pDialog;

    private Pref _pref;
    private ConnectionCheck _connectionCheck;
    private GPSTrackerSecond _GpsTrackerSecond;
    private DBAdapter db;

    Spinner dialog_spinRejectReason;
    String filePath;
    String jsonstr = null;
    String selectedRejectReason = "";
    ArrayList<String> _reasonItems;
    ArrayAdapter reasonAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    rootView = inflater.inflate(R.layout.fragment_intransit, container, false);

        initialize();
        onClick();

        filePath = Environment.getExternalStorageDirectory() + File.separator
                + getActivity().getPackageName() + File.separator
                + Constant.JSON_REJECTED_REASON_FILE_NAME;

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        googleMap = mapView.getMap();
        //googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(getActivity());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        getintransitListFromDB();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void initialize(){

        _pref = new Pref(getActivity());
        _connectionCheck = new ConnectionCheck(getActivity());
        _GpsTrackerSecond = new GPSTrackerSecond(getActivity());
        db = new DBAdapter(getActivity());

        llIntransit = (LinearLayout) rootView.findViewById(R.id.llIntransit);
        tvDropType = (TextView) rootView.findViewById(R.id.tvDropType);
        tvShipmentType = (TextView) rootView.findViewById(R.id.tvShipmentType);
        tvFromTo = (TextView) rootView.findViewById(R.id.tvFromTo);
        tvShipmentTime = (TextView) rootView.findViewById(R.id.tvShipmentTime);
        tvShipmentTitle = (TextView) rootView.findViewById(R.id.tvShipmentTitle);
        tvShipmentAddress = (TextView) rootView.findViewById(R.id.tvShipmentAddress);
        tvContactPerson = (TextView) rootView.findViewById(R.id.tvContactPerson);
        tvContactNo = (TextView) rootView.findViewById(R.id.tvContactNo);
        llCall = (LinearLayout) rootView.findViewById(R.id.llCall);
        llRejected = (LinearLayout) rootView.findViewById(R.id.llRejected);
        llReached =  (LinearLayout) rootView.findViewById(R.id.llReached);
        llCompleted =  (LinearLayout) rootView.findViewById(R.id.llCompleted);
        llPending = (LinearLayout) rootView.findViewById(R.id.llPending);
        rlReached =  (RelativeLayout) rootView.findViewById(R.id.rlReached);
        rlCompleted =  (RelativeLayout) rootView.findViewById(R.id.rlCompleted);

        scrollViewParent = (ParallaxScrollView)rootView.findViewById(R.id.scrollViewParent);
        customView = (View)rootView.findViewById(R.id.customView);

    }

    private void onClick(){

        tvDropType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSecondaryContactDetails();

            }
        });

        llReached.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirmation...")
                        .setContentText("Are you sure to checkin?")
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

                                updateStatusAction = "reach";
                                updateShipmentStatus(updateStatusAction, "");
                            }
                        })
                        .show();

            }

        });

        llCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirmation...")
                        .setContentText("Are you sure to complete?")
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

                                if (intransitList.get(0).getShipmentType().equals("pickup")) {

                                    showCompletedFormPickup();

                                } else if (intransitList.get(0).getShipmentType().equals("cms")) {

                                    showCompletedFormCMS();

                                } else if (intransitList.get(0).getShipmentType().equals("delivery")) {

                                    showCompletedFormDelivery();

                                } else if (intransitList.get(0).getShipmentType().equals("cod")) {

                                    showCompletedFormCOD();

                                } else {

                                }
                            }
                        })
                        .show();

            }

        });

        llRejected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirmation...")
                        .setContentText("Are you sure to reject?")
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

                                showRejectedForm();

                            }
                        })
                        .show();

            }

        });

        llPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirmation...")
                        .setContentText("Are you sure to pending?")
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

                                updateStatusAction = "pending";
                                updateShipmentStatus(updateStatusAction, "");
                            }
                        })
                        .show();

            }
        });

        llCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number = tvContactNo.getText().toString();
                Log.v("number", number);

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

    private void getintransitListFromDB(){

        intransitList = new ArrayList<ShipmentItem>();
        Constant.intransitList = new ArrayList<ShipmentItem>();

        db.open();

        intransitList = db.getRecords("intransit");
        Constant.intransitList = intransitList;
        Log.v("intransitlistsize", String.valueOf(Constant.intransitList.size()));

        db.close();

        if(intransitList.size() >= 1){

            _pref.saveIntransitShipmentId(intransitList.get(0).getShipmentId());
            setValue();
        }
        else{

            _pref.saveIntransitShipmentId("");
            llIntransit.setVisibility(View.GONE);

        }

    }

    private void setValue(){

        llIntransit.setVisibility(View.VISIBLE);

        tvShipmentType.setText(intransitList.get(0).getShipmentType().toUpperCase());
        tvShipmentTime.setText(intransitList.get(0).getShipmentPickupTime());

        if(intransitList.get(0).getShipmentType().equals("pickup") || intransitList.get(0).getShipmentType().equals("cms")){

            tvDropType.setText("Deliver to " + intransitList.get(0).getDeliveryName());
            tvFromTo.setText("From");
            tvShipmentTitle.setText(intransitList.get(0).getPickupName());
            tvShipmentAddress.setText(intransitList.get(0).getPickupHomeName()+ ", " + intransitList.get(0).getPickupStreetName()+ ", " + intransitList.get(0).getPickupLocationName() + ", " + "near " + intransitList.get(0).getPickupLandmark() + ", " +  intransitList.get(0).getPickupCityName() + " - " + intransitList.get(0).getPickupPincode());
            tvContactPerson.setText("Contact Person : " + intransitList.get(0).getPickupName());
            tvContactNo.setText("Mobile Number : " + intransitList.get(0).getPickupContactNo());

            if(! intransitList.get(0).getPickupLatValue().equals("") && !intransitList.get(0).getPickupLongValue().equals("")){

                double lat = Double.parseDouble(intransitList.get(0).getPickupLatValue());
                double lng = Double.parseDouble(intransitList.get(0).getPickupLongValue());

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_GpsTrackerSecond.getLatitude(), _GpsTrackerSecond.getLongitude()) , 8));
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(new LatLng(lat, lng)));

                if(_connectionCheck.isNetworkAvailable() && !String.valueOf(_GpsTrackerSecond.getLatitude()).equals("0.0") && !String.valueOf(_GpsTrackerSecond.getLongitude()).equals("0.0")) {

                    String source_pos = String.valueOf(_GpsTrackerSecond.getLatitude()) + "," + _GpsTrackerSecond.getLongitude();
                    String destination_pos = String.valueOf(lat) + "," + String.valueOf(lng);
                    String url = getMapsApiDirectionsUrl(source_pos, destination_pos);
                    ReadTask downloadTask = new ReadTask();
                    downloadTask.execute(url);

                }

            }

            else{

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_GpsTrackerSecond.getLatitude(), _GpsTrackerSecond.getLongitude()), 8));
            }

        }

        else{

            tvDropType.setText("Pickup from " + intransitList.get(0).getPickupHomeName());
            tvFromTo.setText("To");
            tvShipmentTitle.setText(intransitList.get(0).getDeliveryName());
            tvShipmentAddress.setText(intransitList.get(0).getDeliveryHomeName()+ ", " + intransitList.get(0).getDeliveryStreetName()+ ", " + intransitList.get(0).getDeliveryLocationName() + ", " + "near " + intransitList.get(0).getPickupLandmark() + ", " +  intransitList.get(0).getDeliveryCityName() + " - " + intransitList.get(0).getDeliveryPincode());
            tvContactPerson.setText("Contact Person : " + intransitList.get(0).getDeliveryName());
            tvContactNo.setText("Mobile Number : " + intransitList.get(0).getDeliveryContactNo());

            if(!intransitList.get(0).getDeliveryLatValue().equals("") && !intransitList.get(0).getDeliveryLongValue().equals("")){

                double lat = Double.parseDouble(intransitList.get(0).getDeliveryLatValue());
                double lng = Double.parseDouble(intransitList.get(0).getDeliveryLongValue());

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_GpsTrackerSecond.getLatitude(), _GpsTrackerSecond.getLongitude()) , 8));
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(new LatLng(lat, lng)));

                if(_connectionCheck.isNetworkAvailable() && !String.valueOf(_GpsTrackerSecond.getLatitude()).equals("0.0") && !String.valueOf(_GpsTrackerSecond.getLongitude()).equals("0.0")) {

                    String source_pos = String.valueOf(_GpsTrackerSecond.getLatitude())+","+_GpsTrackerSecond.getLongitude();
                    String destination_pos = String.valueOf(lat)+","+String.valueOf(lng);
                    String url = getMapsApiDirectionsUrl(source_pos, destination_pos);
                    ReadTask downloadTask = new ReadTask();
                    downloadTask.execute(url);

                }

            }
            else{

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_GpsTrackerSecond.getLatitude(), _GpsTrackerSecond.getLongitude()), 8));
            }

        }

        if(Constant.intransitList.get(0).getShipmentStatus().equals("705")){

            rlReached.setVisibility(View.GONE);
            rlCompleted.setVisibility(View.VISIBLE);

        }
        else{

            rlReached.setVisibility(View.VISIBLE);
            rlCompleted.setVisibility(View.GONE);

        }


        /*String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);*/

    }

    private void showSecondaryContactDetails(){

        final Dialog dialog = new Dialog(getActivity());
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

        if(intransitList.get(0).getShipmentType().equals("pickup") || intransitList.get(0).getShipmentType().equals("cms")){

            dialog_tvFromTo.setText("To");
            dialog_tvShipmentTitle.setText(intransitList.get(0).getDeliveryName());
            dialog_tvShipmentAddress.setText(intransitList.get(0).getDeliveryHomeName()+ ", " + intransitList.get(0).getDeliveryStreetName()+ ", " + intransitList.get(0).getDeliveryLocationName() + ", " + "near " + intransitList.get(0).getPickupLandmark() + ", " +  intransitList.get(0).getDeliveryCityName() + " - " + intransitList.get(0).getDeliveryPincode());
            dialog_tvContactPerson.setText("Contact Person : " + intransitList.get(0).getDeliveryName());
            dialog_tvContactNo.setText("Mobile Number : " + intransitList.get(0).getDeliveryContactNo());

        }
        else{

            dialog_tvFromTo.setText("From");
            dialog_tvShipmentTitle.setText(intransitList.get(0).getPickupName());
            dialog_tvShipmentAddress.setText(intransitList.get(0).getPickupHomeName()+ ", " + intransitList.get(0).getPickupStreetName()+ ", " + intransitList.get(0).getPickupLocationName() + ", " + "near " + intransitList.get(0).getPickupLandmark() + ", " +  intransitList.get(0).getPickupCityName() + " - " + intransitList.get(0).getPickupPincode());
            dialog_tvContactPerson.setText("Contact Person : " + intransitList.get(0).getPickupName());
            dialog_tvContactNo.setText("Mobile Number : " + intransitList.get(0).getPickupContactNo());

        }

        dialog_btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }

    private void showCompletedFormPickup(){

        capturedData = "";

        final Dialog dialog = new Dialog(getActivity(), R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_completed_form_pickup);
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        dialog.show();

        final EditText dialog_etReceivedFrom = (EditText) dialog.findViewById(R.id.etReceivedFrom);
        final EditText dialog_etContactNo = (EditText) dialog.findViewById(R.id.etContactNo);
        final EditText dialog_etNoOfPackages = (EditText) dialog.findViewById(R.id.etNoOfPackages);
        final LinearLayout dialog_llCancel = (LinearLayout)dialog.findViewById(R.id.llCancel);
        final LinearLayout dialog_llSubmit = (LinearLayout)dialog.findViewById(R.id.llSubmit);

        dialog_llSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!dialog_etReceivedFrom.getText().toString().equals("") && !dialog_etContactNo.getText().toString().equals("")
                        && !dialog_etNoOfPackages.getText().toString().equals("")){

                    capturedData = "ReceivedFrom:" + dialog_etReceivedFrom.getText().toString() +
                            "; ContactNo:" + dialog_etContactNo.getText().toString() +
                            "; NoOfPackages:" + dialog_etNoOfPackages.getText().toString();

                    Log.v("capturedData", capturedData);
                    dialog.dismiss();

                    updateStatusAction = "complete";
                    updateShipmentStatus(updateStatusAction, capturedData);

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Required fields should not be left blank!")
                            .show();

                }

            }
        });

        dialog_llCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }

    private void showCompletedFormCMS(){

        capturedData = "";

        final Dialog dialog = new Dialog(getActivity(), R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_completed_form_cms);
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        dialog.show();

        final EditText dialog_etReceivedFrom = (EditText) dialog.findViewById(R.id.etReceivedFrom);
        final EditText dialog_etContactNo = (EditText) dialog.findViewById(R.id.etContactNo);
        final EditText dialog_etNoOfLocalCheques = (EditText) dialog.findViewById(R.id.etNoOfLocalCheques);
        final EditText dialog_etNoOfOutstationCheques = (EditText) dialog.findViewById(R.id.etNoOfOutstationCheques);
        //final EditText dialog_etTotalAmount = (EditText) dialog.findViewById(R.id.etTotalAmount);
        final LinearLayout dialog_llCancel = (LinearLayout)dialog.findViewById(R.id.llCancel);
        final LinearLayout dialog_llSubmit = (LinearLayout)dialog.findViewById(R.id.llSubmit);

        dialog_llSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!dialog_etReceivedFrom.getText().toString().equals("") && !dialog_etContactNo.getText().toString().equals("")
                        && !dialog_etNoOfLocalCheques.getText().toString().equals("") && !dialog_etNoOfOutstationCheques.getText().toString().equals("")){

                    capturedData = "ReceivedFrom:" + dialog_etReceivedFrom.getText().toString() +
                            "; ContactNo:" + dialog_etContactNo.getText().toString() +
                            "; NoOfLocalCheques:" + dialog_etNoOfLocalCheques.getText().toString() +
                            "; NoOfOutstationCheques:" + dialog_etNoOfOutstationCheques.getText().toString();

                    Log.v("capturedData", capturedData);
                    dialog.dismiss();

                    updateStatusAction = "complete";
                    updateShipmentStatus(updateStatusAction, capturedData);

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Required fields should not be left blank!")
                            .show();

                }

            }
        });

        dialog_llCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }

    private void showCompletedFormDelivery(){

        capturedData = "";

        final Dialog dialog = new Dialog(getActivity(), R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_completed_form_delivery);
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        dialog.show();

        final EditText dialog_etDeliveredTo = (EditText) dialog.findViewById(R.id.etDeliveredTo);
        final EditText dialog_etContactNo = (EditText) dialog.findViewById(R.id.etContactNo);
        final LinearLayout dialog_llCancel = (LinearLayout)dialog.findViewById(R.id.llCancel);
        final LinearLayout dialog_llSubmit = (LinearLayout)dialog.findViewById(R.id.llSubmit);

        dialog_llSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!dialog_etDeliveredTo.getText().toString().equals("") && !dialog_etContactNo.getText().toString().equals("")){

                    capturedData = "DeliveredTo:" + dialog_etDeliveredTo.getText().toString() +
                            "; ContactNo:" + dialog_etContactNo.getText().toString();

                    Log.v("capturedData", capturedData);
                    dialog.dismiss();

                    updateStatusAction = "complete";
                    updateShipmentStatus(updateStatusAction, capturedData);

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Required fields should not be left blank!")
                            .show();

                }

            }
        });

        dialog_llCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }

    private void showCompletedFormCOD(){

        capturedData = "";

        final Dialog dialog = new Dialog(getActivity(), R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_completed_form_cod);
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        dialog.show();

        final EditText dialog_etDeliveredTo = (EditText) dialog.findViewById(R.id.etDeliveredTo);
        final EditText dialog_etContactNo = (EditText) dialog.findViewById(R.id.etContactNo);
        final CheckBox dialog_chkCashReceived = (CheckBox) dialog.findViewById(R.id.chkCashReceived);
        final LinearLayout dialog_llCancel = (LinearLayout)dialog.findViewById(R.id.llCancel);
        final LinearLayout dialog_llSubmit = (LinearLayout)dialog.findViewById(R.id.llSubmit);

        dialog_chkCashReceived.setText("Amount Received Rs. " +  intransitList.get(0).getShipmentTotalAmount());

        dialog_llSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!dialog_etDeliveredTo.getText().toString().equals("") && !dialog_etContactNo.getText().toString().equals("")){

                    if(dialog_chkCashReceived.isChecked()){

                        capturedData = "DeliveredTo:" + dialog_etDeliveredTo.getText().toString() +
                                "; ContactNo:" + dialog_etContactNo.getText().toString() +
                                "; CashReceived:Yes";
                    }
                    else{

                        capturedData = "DeliveredTo:" + dialog_etDeliveredTo.getText().toString() +
                                "; ContactNo:" + dialog_etContactNo.getText().toString() +
                                "; CashReceived:No";
                    }

                    Log.v("capturedData", capturedData);
                    dialog.dismiss();

                    updateStatusAction = "complete";
                    updateShipmentStatus(updateStatusAction, capturedData);

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Required fields should not be left blank!")
                            .show();

                }

            }
        });

        dialog_llCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }

    private void showRejectedForm(){

        capturedData = "";

        final Dialog dialog = new Dialog(getActivity(), R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_rejected_form);
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        dialog.show();

        final EditText dialog_etRejectedBy = (EditText) dialog.findViewById(R.id.etRejectedBy);
        final EditText dialog_etContactNo = (EditText) dialog.findViewById(R.id.etContactNo);
        dialog_spinRejectReason = (Spinner) dialog.findViewById(R.id.spinRejectReason);
        final EditText dialog_etRejectReason = (EditText) dialog.findViewById(R.id.etRejectReason);
        final LinearLayout dialog_llCancel = (LinearLayout)dialog.findViewById(R.id.llCancel);
        final LinearLayout dialog_llSubmit = (LinearLayout)dialog.findViewById(R.id.llSubmit);

        _reasonItems = new ArrayList<String>();
        _reasonItems.add("Select Reject Reason");

        reasonAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                _reasonItems);
        dialog_spinRejectReason.setAdapter(reasonAdapter);

        new RejectedReasonAsync().execute("");

        dialog_spinRejectReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(reasonAdapter.getItem(position).toString().equals("Select Reject Reason")){

                    selectedRejectReason = "";

                    dialog_etRejectReason.setVisibility(View.GONE);
                    dialog_etRejectReason.setText("");

                }
                else if(reasonAdapter.getItem(position).toString().equals("Other")){

                    selectedRejectReason = "Other";

                    dialog_etRejectReason.setVisibility(View.VISIBLE);

                }
                else{

                    selectedRejectReason = reasonAdapter.getItem(position).toString();

                    dialog_etRejectReason.setVisibility(View.GONE);
                    dialog_etRejectReason.setText("");

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialog_llSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!dialog_etRejectedBy.getText().toString().equals("") && !dialog_etContactNo.getText().toString().equals("")){

                    if(selectedRejectReason.equals("")){

                        new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("Please Select Particular Reason for Rejection")
                                .show();
                    }
                    else if(selectedRejectReason.equals("Other")){

                        if(dialog_etRejectReason.getText().toString().equals("")){

                            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText("Please Write Down Particular Reason for Rejection")
                                    .show();
                        }
                        else{

                            capturedData = "RejectedBy:" + dialog_etRejectedBy.getText().toString() +
                                    "; ContactNo:" + dialog_etContactNo.getText().toString() +
                                    "; RejectReason:Other," + dialog_etRejectReason.getText().toString();

                            Log.v("capturedData", capturedData);
                            dialog.dismiss();

                            updateStatusAction = "reject";
                            updateShipmentStatus(updateStatusAction, capturedData);

                        }
                    }
                    else{

                        capturedData = "RejectedBy:" + dialog_etRejectedBy.getText().toString() +
                                "; ContactNo:" + dialog_etContactNo.getText().toString() +
                                "; RejectReason:" + selectedRejectReason;

                        Log.v("capturedData", capturedData);
                        dialog.dismiss();

                        updateStatusAction = "reject";
                        updateShipmentStatus(updateStatusAction, capturedData);

                    }

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Required fields should not be left blank!")
                            .show();

                }

            }
        });

        dialog_llCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }


    public class RejectedReasonAsync extends AsyncTask<String, Long, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                jsonstr = getStringFromFile(filePath);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return jsonstr;
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            // TODO Auto-generated method stub
            super.onPostExecute(jsonStr);

            if (jsonStr != null) {
                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject errNodeObj = jsonObj.getJSONObject("errNode");
                    String errCode = errNodeObj.getString("errCode");

                    if(errCode.equals("0")){

                        JSONObject dataObj = jsonObj.getJSONObject("data");
                        JSONArray reasonArray = new JSONArray();

                        if (intransitList.get(0).getShipmentType().equals("pickup")) {
                            reasonArray = dataObj.getJSONArray("pickup");
                        } else if (intransitList.get(0).getShipmentType().equals("cms")) {
                            reasonArray = dataObj.getJSONArray("cms");
                        } else if (intransitList.get(0).getShipmentType().equals("delivery")) {
                            reasonArray = dataObj.getJSONArray("delivery");
                        } else if (intransitList.get(0).getShipmentType().equals("cod")) {
                            reasonArray = dataObj.getJSONArray("cod");
                        } else {

                        }

                        //_reasonItems.add("Select Reject Reason");
                        for (int i = 0; i < reasonArray.length(); i++) {

                            JSONObject reasonObj = reasonArray.getJSONObject(i);
                            _reasonItems.add(reasonObj.getString("reason"));

                        }
                        _reasonItems.add("Other");

                        reasonAdapter.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }

    }

    public String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        // Make sure you close all streams.
        fin.close();
        return ret;
    }

    private void getintransitList(){

        db.open();
        db.deleteRecord(Constant.intransitList.get(0).getShipmentId());
        db.close();

        if (_connectionCheck.isNetworkAvailable()) {

            String url = Constant.baseUrl  + "shipment";

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("agentId", _pref.getAgentId());
                jsonObject.put("accessToken", _pref.getAccessToken());

                JSONObject data = new JSONObject();
                data.put("data", jsonObject);

                String jsonInput = data.toString();

                reqWebService = 0;
                RunBackgroundAsync async = new RunBackgroundAsync(
                        getActivity());
                async.taskInterface = IntransitFragment.this;
                async.execute(url, jsonInput);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }
        else {

            _connectionCheck.getNetworkActiveAlert().show();
        }

    }

    private void updateShipmentStatus(String updateStatusAction, String capturedData){

        _GpsTrackerSecond = new GPSTrackerSecond(getActivity());

        if (_connectionCheck.isNetworkAvailable()) {

            String url = Constant.baseUrl  + "updateshipmentstatus";

            try {

                if(!String.valueOf(_GpsTrackerSecond.getLatitude()).equals("0.0") && !String.valueOf(_GpsTrackerSecond.getLongitude()).equals("0.0")){

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("agentId", _pref.getAgentId());
                    jsonObject.put("accessToken", _pref.getAccessToken());
                    jsonObject.put("shipmentId", _pref.getIntransitShipmentId());
                    jsonObject.put("latValue", String.valueOf(_GpsTrackerSecond.getLatitude()));
                    jsonObject.put("longValue", String.valueOf(_GpsTrackerSecond.getLongitude()));
                    jsonObject.put("action", updateStatusAction);
                    jsonObject.put("capturedData", capturedData);

                    JSONObject data = new JSONObject();
                    data.put("data", jsonObject);

                    String jsonInput = data.toString();
                    Log.v("jsonInput", jsonInput);

                    reqWebService = 1;
                    RunBackgroundAsync async = new RunBackgroundAsync(
                            getActivity());
                    async.taskInterface = IntransitFragment.this;
                    async.execute(url, jsonInput);

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Can't fetch your current location!! Please Check GPS and Try Again")
                            .show();

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else {

            _connectionCheck.getNetworkActiveAlert().show();
        }

    }

    @Override
    public void onStarted() {

        pDialog = new ProgressDialog(getActivity());

        if(reqWebService == 1){

            pDialog.setMessage("Please Wait...");
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();

        }

    }

    @Override
    public void onCompleted(String jsonStr) {

        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }

        if(reqWebService == 0){

            if (jsonStr != null) {

                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject errNodeObj = jsonObj.getJSONObject("errNode");
                    String errCode = errNodeObj.getString("errCode");
                    String errMsg = errNodeObj.getString("errMsg");

                    if(errCode.equalsIgnoreCase("0")){

                        JSONObject dataObj = jsonObj.getJSONObject("data");

                        if (dataObj.has("intransit")) {

                            JSONArray intransitArray = dataObj.getJSONArray("intransit");
                            for (int i = 0; i < intransitArray.length(); i++) {

                                intransitItem = new ShipmentItem();

                                JSONObject shipmentObj = intransitArray.getJSONObject(i);
                                JSONObject pickupObj = shipmentObj.getJSONObject("pickup_address");
                                JSONObject deliveryObj = shipmentObj.getJSONObject("delivery_address");

                                db.open();
                                db.insertValue("intransit", shipmentObj.getString("shipment_id"), shipmentObj.getString("shipment_type"), shipmentObj.getString("traking_no"),
                                        shipmentObj.getString("vendor_shipment_ref_no"), shipmentObj.getString("shipment_description"), shipmentObj.getString("client_code"),
                                        shipmentObj.getString("no_of_cheques"), shipmentObj.getString("no_of_packages"), shipmentObj.getString("total_amount"), shipmentObj.getString("cash_received"),
                                        shipmentObj.getString("status"), shipmentObj.getString("shipment_pickup_date"), shipmentObj.getString("shipment_pickup_time"),
                                        pickupObj.getString("id"), pickupObj.getString("contact_name"), pickupObj.getString("contact_no"), pickupObj.getString("email_id"), pickupObj.getString("address1"),
                                        pickupObj.getString("address2"), pickupObj.getString("location_name"), pickupObj.getString("city_name"), pickupObj.getString("state_name"),
                                        pickupObj.getString("country_name"), pickupObj.getString("zipcode"), pickupObj.getString("landmark"), pickupObj.getString("remarks"),
                                        pickupObj.getString("long_value"), pickupObj.getString("lat_value"),
                                        deliveryObj.getString("id"), deliveryObj.getString("contact_name"), deliveryObj.getString("contact_no"), deliveryObj.getString("email_id"), deliveryObj.getString("address1"),
                                        deliveryObj.getString("address2"), deliveryObj.getString("location_name"), deliveryObj.getString("city_name"), deliveryObj.getString("state_name"),
                                        deliveryObj.getString("country_name"), deliveryObj.getString("zipcode"), deliveryObj.getString("landmark"), deliveryObj.getString("remarks"),
                                        deliveryObj.getString("long_value"), deliveryObj.getString("lat_value"));
                                db.close();

                            }

                        }

                        getintransitListFromDB();

                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else{

                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something going wrong!! Please Try Again")
                        .show();

            }

        }
        else{

            if (jsonStr != null) {

                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject errNodeObj = jsonObj.getJSONObject("errNode");
                    String errCode = errNodeObj.getString("errCode");
                    String errMsg = errNodeObj.getString("errMsg");

                    if(errCode.equalsIgnoreCase("0")){

                        if(updateStatusAction.equals("complete")){

                            db.open();
                            db.updateRecord(intransitList.get(0).getShipmentId(), "complete");
                            db.close();
                            getintransitListFromDB();

                            TraxActivity.setViewPager(0);
                            updateStatusAction = "";
                        }

                        else if(updateStatusAction.equals("reject")){

                            db.open();
                            db.updateRecord(intransitList.get(0).getShipmentId(), "reject");
                            db.close();
                            getintransitListFromDB();

                            TraxActivity.setViewPager(0);
                            updateStatusAction = "";
                        }

                        else if(updateStatusAction.equals("pending")){

                            db.open();
                            db.updateRecord(intransitList.get(0).getShipmentId(), "pending");
                            db.updateStatus(intransitList.get(0).getShipmentId());
                            db.close();
                            getintransitListFromDB();

                            updateStatusAction = "";

                            Intent intent = new Intent(getActivity(), TraxActivity.class);
                            intent.putExtra("startFrom", "DashBoardActivity");
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            getActivity().finish();
                        }

                        else{

                            getintransitList();

                        }

                    }
                    else{

                        new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
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

                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something going wrong!! Please Try Again")
                        .show();

            }

        }

    }

    private String getMapsApiDirectionsUrl(String source_pos, String destination_pos) {

        String waypoints = "waypoints=optimize:true|";
        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params + "&origin=" + source_pos +"&destination=" + destination_pos;
        return url;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("result", result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(5);
                polyLineOptions.color(Color.parseColor("#99FF0000"));
            }

            googleMap.addPolyline(polyLineOptions);

        }
    }
}