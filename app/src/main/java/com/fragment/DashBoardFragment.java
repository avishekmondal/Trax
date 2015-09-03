package com.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asynctask.RunBackgroundAsync;
import com.bean.ShipmentItem;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.LargeValueFormatter;
import com.interfaces.BackgroundTaskInterface;
import com.trax.R;
import com.trax.SearchActivity;
import com.trax.TraxActivity;
import com.trax.TraxRejectReasonService;
import com.trax.TraxService;
import com.trax.TraxUpdateShipmentService;
import com.utility.ConnectionCheck;
import com.utility.Constant;
import com.utility.DBAdapter;
import com.utility.GPSTrackerSecond;
import com.utility.Pref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Avishek on 8/12/2015.
 */
public class DashBoardFragment extends Fragment implements BackgroundTaskInterface {

    private View rootView;

    private LinearLayout llDashBoard, llGoOnline, llGoOfflineMain, llGoOffline, llGps, llRefresh, llSearch;
    private TextView tvOnline, tvGPS;
    private PieChart mChart;

    private ArrayList<ShipmentItem> pendingList;
    private ShipmentItem pendingItem;

    private ArrayList<ShipmentItem> intransitList;
    private ShipmentItem intransitItem;

    private ArrayList<ShipmentItem> completedList;
    private ShipmentItem completedItem;

    private ArrayList<ShipmentItem> rejectedList;
    private ShipmentItem rejectedItem;

    int reqWebService = 0;

    private ProgressDialog pDialog;

    private Pref _pref;
    private ConnectionCheck _connectionCheck;
    private GPSTrackerSecond _GpsTrackerSecond;
    private DBAdapter db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_dashboard, container, false);

        initialize();
        onClick();

        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();

        if(_pref.getLoginFlag().equals("3")){

            tvOnline.setText("CONTINUE");
            llGoOfflineMain.setVisibility(View.VISIBLE);

        }
        else{

            tvOnline.setText("GO ONLINE");
            llGoOfflineMain.setVisibility(View.GONE);
        }

        if(_connectionCheck.isGPSEnabled()) {

            tvGPS.setText("GPS ENABLED");
        }
        else{
            tvGPS.setText("GPS DISABLED");
        }

        getShipmentListFromDB();

    }

    private void initialize(){

        _pref = new Pref(getActivity());
        _connectionCheck = new ConnectionCheck(getActivity());
        _GpsTrackerSecond = new GPSTrackerSecond(getActivity());
        db = new DBAdapter(getActivity());


        llDashBoard = (LinearLayout) rootView.findViewById(R.id.llDashBoard);
        llGoOnline = (LinearLayout) rootView.findViewById(R.id.llGoOnline);
        llGoOfflineMain = (LinearLayout) rootView.findViewById(R.id.llGoOfflineMain);
        llGoOffline = (LinearLayout) rootView.findViewById(R.id.llGoOffline);
        llGps = (LinearLayout) rootView.findViewById(R.id.llGps);
        llRefresh = (LinearLayout) rootView.findViewById(R.id.llRefresh);
        llSearch = (LinearLayout) rootView.findViewById(R.id.llSearch);
        tvOnline = (TextView) rootView.findViewById(R.id.tvOnline);
        tvGPS = (TextView) rootView.findViewById(R.id.tvGPS);
        mChart = (PieChart) rootView.findViewById(R.id.chart1);

        pendingList = new ArrayList<ShipmentItem>();
        intransitList = new ArrayList<ShipmentItem>();
        completedList = new ArrayList<ShipmentItem>();
        rejectedList = new ArrayList<ShipmentItem>();

        Constant.pendingList = new ArrayList<ShipmentItem>();
        Constant.intransitList = new ArrayList<ShipmentItem>();
        Constant.completedList = new ArrayList<ShipmentItem>();
        Constant.rejectedList = new ArrayList<ShipmentItem>();

    }

    private void onClick(){

        llGoOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(_pref.getLoginFlag().equals("3")){

                    if(_connectionCheck.isGPSEnabled()) {

                        Intent intent = new Intent(getActivity(), TraxActivity.class);
                        intent.putExtra("startFrom", "DashBoardActivity");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    else{
                        _connectionCheck.getSettingsAlert().show();
                    }

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Confirmation...")
                            .setContentText("Are you sure to go online?")
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

                                    if(_connectionCheck.isGPSEnabled()) {

                                        getStarted();
                                    }
                                    else{
                                        _connectionCheck.getSettingsAlert().show();
                                    }

                                }
                            })
                            .show();
                }

            }
        });

        llGoOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirmation...")
                        .setContentText("Are you sure to go offline?")
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

                                if(_connectionCheck.isGPSEnabled()) {

                                    getStopped();
                                }
                                else{
                                    _connectionCheck.getSettingsAlert().show();
                                }

                            }
                        })
                        .show();

            }
        });

        llGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        });

        llRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getShipmentListFromWS();

            }
        });

        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(_pref.getLoginFlag().equals("3")){

                    startActivity(new Intent(getActivity(), SearchActivity.class));
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("You have to Go Online first!!")
                            .show();
                }

            }
        });

    }

    private void getShipmentListFromDB(){

        db.open();

        pendingList = db.getRecords("pending");
        Constant.pendingList = pendingList;
        Log.v("pendinglistsize", String.valueOf(Constant.pendingList.size()));

        intransitList = db.getRecords("intransit");
        Constant.intransitList = intransitList;
        Log.v("intransitlistsize", String.valueOf(Constant.intransitList.size()));

        completedList = db.getRecords("complete");
        Constant.completedList = completedList;
        Log.v("completedlistsize", String.valueOf(Constant.completedList.size()));

        rejectedList = db.getRecords("reject");
        Constant.rejectedList = rejectedList;
        Log.v("rejectedlistsize", String.valueOf(Constant.rejectedList.size()));

        db.close();

        setPieChartData();

    }

    private void setPieChartData() {

        mChart.setUsePercentValues(false);
        mChart.setDescription("");
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);
        mChart.setHoleColor(Color.WHITE);
        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setHoleRadius(50f);
        mChart.setDrawCenterText(true);
        mChart.setCenterTextColor(Color.BLACK);
        mChart.setCenterTextSize(25f);
        mChart.setTransparentCircleRadius(50f);
        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(true);
        mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(0f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setXOffset(0f);

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        yVals1.add(new Entry(Constant.pendingList.size() + Constant.intransitList.size(), 0));
        yVals1.add(new Entry(Constant.completedList.size(), 1));
        yVals1.add(new Entry(Constant.rejectedList.size(), 2));

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("pending");
        xVals.add("completed");
        xVals.add("rejected");

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.BLACK);

        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        //data.setValueFormatter(new PercentFormatter());
        data.setValueFormatter(new LargeValueFormatter());
        data.setValueTextSize(0f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);
        mChart.setCenterText(String.valueOf(Constant.pendingList.size()+ Constant.intransitList.size()+ Constant.completedList.size()+ Constant.rejectedList.size()));
        mChart.highlightValues(null);

        mChart.invalidate();

    }

    private void getShipmentListFromWS(){

        db.open();
        db.deleteAllRecord();
        db.close();

        if (_connectionCheck.isNetworkAvailable()) {

            getActivity().startService(new Intent(getActivity(), TraxRejectReasonService.class));

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
                async.taskInterface = DashBoardFragment.this;
                async.execute(url, jsonInput);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        } else {

            _connectionCheck.getNetworkActiveAlert().show();
        }

    }

    private void getStarted(){

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
                    jsonObject.put("action", "checkin");
                    jsonObject.put("capturedData", "");

                    JSONObject data = new JSONObject();
                    data.put("data", jsonObject);

                    String jsonInput = data.toString();
                    Log.v("jsonInput", jsonInput);

                    reqWebService = 1;
                    RunBackgroundAsync async = new RunBackgroundAsync(
                            getActivity());
                    async.taskInterface = DashBoardFragment.this;
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


        } else {

            _connectionCheck.getNetworkActiveAlert().show();
        }

    }

    private void getStopped(){

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
                    jsonObject.put("action", "checkout");
                    jsonObject.put("capturedData", "");

                    JSONObject data = new JSONObject();
                    data.put("data", jsonObject);

                    String jsonInput = data.toString();
                    Log.v("jsonInput", jsonInput);

                    reqWebService = 2;
                    RunBackgroundAsync async = new RunBackgroundAsync(
                            getActivity());
                    async.taskInterface = DashBoardFragment.this;
                    async.execute(url, jsonInput);

                }
                else{

                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Can't fetch your current location!! Please check GPS and Try Again")
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

        pDialog = new ProgressDialog(getActivity());
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

        if (reqWebService == 0) {

            if (jsonStr != null) {

                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject errNodeObj = jsonObj.getJSONObject("errNode");
                    String errCode = errNodeObj.getString("errCode");
                    String errMsg = errNodeObj.getString("errMsg");

                    if (errCode.equalsIgnoreCase("0")) {

                        JSONObject dataObj = jsonObj.getJSONObject("data");

                        if (dataObj.has("pending")) {

                            JSONArray pendingArray = dataObj.getJSONArray("pending");
                            for (int i = 0; i < pendingArray.length(); i++) {

                                pendingItem = new ShipmentItem();

                                JSONObject shipmentObj = pendingArray.getJSONObject(i);
                                JSONObject pickupObj = shipmentObj.getJSONObject("pickup_address");
                                JSONObject deliveryObj = shipmentObj.getJSONObject("delivery_address");

                                db.open();
                                db.insertValue("pending", shipmentObj.getString("shipment_id"), shipmentObj.getString("shipment_type"), shipmentObj.getString("traking_no"),
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


                        if (dataObj.has("complete")) {

                            JSONArray completedArray = dataObj.getJSONArray("complete");
                            for (int i = 0; i < completedArray.length(); i++) {

                                completedItem = new ShipmentItem();

                                JSONObject shipmentObj = completedArray.getJSONObject(i);
                                JSONObject pickupObj = shipmentObj.getJSONObject("pickup_address");
                                JSONObject deliveryObj = shipmentObj.getJSONObject("delivery_address");

                                db.open();
                                db.insertValue("complete", shipmentObj.getString("shipment_id"), shipmentObj.getString("shipment_type"), shipmentObj.getString("traking_no"),
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

                        if (dataObj.has("reject")) {

                            JSONArray rejectedArray = dataObj.getJSONArray("reject");
                            for (int i = 0; i < rejectedArray.length(); i++) {

                                rejectedItem = new ShipmentItem();

                                JSONObject shipmentObj = rejectedArray.getJSONObject(i);
                                JSONObject pickupObj = shipmentObj.getJSONObject("pickup_address");
                                JSONObject deliveryObj = shipmentObj.getJSONObject("delivery_address");

                                db.open();
                                db.insertValue("reject", shipmentObj.getString("shipment_id"), shipmentObj.getString("shipment_type"), shipmentObj.getString("traking_no"),
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

                        getShipmentListFromDB();

                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something going wrong!! Please Try Again")
                        .show();

            }

        } else if (reqWebService == 1) {

            if (jsonStr != null) {

                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject errNodeObj = jsonObj.getJSONObject("errNode");
                    String errCode = errNodeObj.getString("errCode");
                    String errMsg = errNodeObj.getString("errMsg");

                    if (errCode.equalsIgnoreCase("0")) {

                        _pref.saveLoginFlag("3");
                        getActivity().startService(new Intent(getActivity(), TraxService.class));

                        if(_connectionCheck.isGPSEnabled()) {

                            Intent intent = new Intent(getActivity(), TraxActivity.class);
                            intent.putExtra("startFrom", "DashBoardActivity");
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                        else{
                            _connectionCheck.getSettingsAlert().show();
                        }

                    } else {

                        new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText(errMsg)
                                .show();

                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something going wrong!! Please Try Again")
                        .show();

            }

        } else if (reqWebService == 2) {

            if (jsonStr != null) {

                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject errNodeObj = jsonObj.getJSONObject("errNode");
                    String errCode = errNodeObj.getString("errCode");
                    String errMsg = errNodeObj.getString("errMsg");

                    if (errCode.equalsIgnoreCase("0")) {

                        _pref.saveLoginFlag("2");
                        getActivity().stopService(new Intent(getActivity(), TraxService.class));
                        //getActivity().stopService(new Intent(getActivity(), TraxUpdateShipmentService.class));
                        getActivity().stopService(new Intent(getActivity(), TraxRejectReasonService.class));

                        tvOnline.setText("GO ONLINE");
                        llGoOfflineMain.setVisibility(View.GONE);

                    } else {

                        new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText(errMsg)
                                .show();

                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something going wrong!! Please Try Again")
                        .show();

            }

        }

    }

}
