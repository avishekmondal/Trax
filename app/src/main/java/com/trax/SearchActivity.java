package com.trax;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.adapter.SearchAdapter;
import com.asynctask.RunBackgroundAsync;
import com.bean.ShipmentItem;
import com.interfaces.BackgroundTaskInterface;
import com.pnikosis.materialishprogress.ProgressWheel;
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
 * Created by Avishek on 7/21/2015.
 */
public class SearchActivity extends ActionBarActivity implements BackgroundTaskInterface{

    private ActionBar actionBar;

    private EditText etSearch;
    private LinearLayout llSearch;
    private ListView lvSearch;
    private ProgressWheel progressWheel;

    private ArrayList<ShipmentItem> searchList;
    private ShipmentItem searchItem;
    private SearchAdapter searchAdapter;

    private Pref _pref;
    private ConnectionCheck _connectionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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

        onClick();

    }

    @Override
    protected void onResume() {
        super.onResume();

        lvSearch.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();

    }

    private void initialize() {

        _pref = new Pref(SearchActivity.this);
        _connectionCheck = new ConnectionCheck(SearchActivity.this);

        etSearch = (EditText) findViewById(R.id.etSearch);
        llSearch = (LinearLayout) findViewById(R.id.llSearch);
        lvSearch = (ListView) findViewById(R.id.lvSearch);
        progressWheel = (ProgressWheel)findViewById(R.id.progressWheel);

        searchList = new ArrayList<ShipmentItem>();
        Constant.searchList  = new ArrayList<ShipmentItem>();

    }

    private void onClick(){

        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!etSearch.getText().toString().equals("")){

                    getSearchListFromWS();

                }
                else{

                    new SweetAlertDialog(SearchActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Search string is blank!")
                            .show();

                }
            }
        });

        lvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = null;

                if(searchList.get(position).getShipmentStatus().equals("702")){
                    intent = new Intent(SearchActivity.this, PendingDetailsActivity.class);
                    intent.putExtra("startFrom", "SearchActivity");
                    intent.putExtra("position", position);
                }
                else if(searchList.get(position).getShipmentStatus().equals("704") || searchList.get(position).getShipmentStatus().equals("700")){
                    intent = new Intent(SearchActivity.this, CompletedDetailsActivity.class);
                    intent.putExtra("startFrom", "SearchActivity");
                    intent.putExtra("position", position);
                }
                else {
                    intent = new Intent(SearchActivity.this, TraxActivity.class);
                    intent.putExtra("startFrom", "SearchActivity");
                }

                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

    }

    private void getSearchListFromWS(){

        searchList.clear();

        if (_connectionCheck.isNetworkAvailable()) {

            String url = Constant.baseUrl  + "shipment-search-result";

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("agentId", _pref.getAgentId());
                jsonObject.put("accessToken", _pref.getAccessToken());
                jsonObject.put("searchKeyword", etSearch.getText().toString());

                JSONObject data = new JSONObject();
                data.put("data", jsonObject);

                String jsonInput = data.toString();
                Log.v("jsonInput", jsonInput);

                RunBackgroundAsync async = new RunBackgroundAsync(
                        SearchActivity.this);
                async.taskInterface = SearchActivity.this;
                async.execute(url, jsonInput);

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

        progressWheel.setVisibility(View.VISIBLE);
        lvSearch.setVisibility(View.GONE);

    }

    @Override
    public void onCompleted(String jsonStr) {

        progressWheel.setVisibility(View.GONE);
        lvSearch.setVisibility(View.VISIBLE);

        if (jsonStr != null) {

            try {

                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONObject errNodeObj = jsonObj.getJSONObject("errNode");
                String errCode = errNodeObj.getString("errCode");
                String errMsg = errNodeObj.getString("errMsg");

                if(errCode.equalsIgnoreCase("0")){

                    JSONObject dataObj = jsonObj.getJSONObject("data");

                    if (dataObj.has("pending")) {

                        JSONArray pendingArray = dataObj.getJSONArray("pending");
                        for (int i = 0; i < pendingArray.length(); i++) {

                            searchItem = new ShipmentItem();

                            JSONObject shipmentObj = pendingArray.getJSONObject(i);
                            searchItem.setShipmentId(shipmentObj.getString("shipment_id"));
                            searchItem.setShipmentType(shipmentObj.getString("shipment_type"));
                            searchItem.setShipmentTrakingNo(shipmentObj.getString("traking_no"));
                            searchItem.setShipmentVendorRefNo(shipmentObj.getString("vendor_shipment_ref_no"));
                            searchItem.setShipmentDescription(shipmentObj.getString("shipment_description"));
                            searchItem.setShipmentClientCode(shipmentObj.getString("client_code"));
                            searchItem.setShipmentNoOfCheques(shipmentObj.getString("no_of_cheques"));
                            searchItem.setShipmentNoOfPackages(shipmentObj.getString("no_of_packages"));
                            searchItem.setShipmentTotalAmount(shipmentObj.getString("total_amount"));
                            searchItem.setShipmentCashReceived(shipmentObj.getString("cash_received"));
                            searchItem.setShipmentStatus(shipmentObj.getString("status"));
                            searchItem.setShipmentPickupDate(shipmentObj.getString("shipment_pickup_date"));
                            searchItem.setShipmentPickupTime(shipmentObj.getString("shipment_pickup_time"));

                            JSONObject pickupObj = shipmentObj.getJSONObject("pickup_address");
                            searchItem.setPickupId(pickupObj.getString("id"));
                            searchItem.setPickupName(pickupObj.getString("contact_name"));
                            searchItem.setPickupContactNo(pickupObj.getString("contact_no"));
                            searchItem.setPickupEmailId(pickupObj.getString("email_id"));
                            searchItem.setPickupHomeName(pickupObj.getString("address1"));
                            searchItem.setPickupStreetName(pickupObj.getString("address2"));
                            searchItem.setPickupLocationName(pickupObj.getString("location_name"));
                            searchItem.setPickupCityName(pickupObj.getString("city_name"));
                            searchItem.setPickupStateName(pickupObj.getString("state_name"));
                            searchItem.setPickupCountryName(pickupObj.getString("country_name"));
                            searchItem.setPickupPincode(pickupObj.getString("zipcode"));
                            searchItem.setPickupLandmark(pickupObj.getString("landmark"));
                            searchItem.setPickupRemarks(pickupObj.getString("remarks"));
                            searchItem.setPickupLongValue(pickupObj.getString("long_value"));
                            searchItem.setPickupLatValue(pickupObj.getString("lat_value"));

                            JSONObject deliveryObj = shipmentObj.getJSONObject("delivery_address");
                            searchItem.setDeliveryId(deliveryObj.getString("id"));
                            searchItem.setDeliveryName(deliveryObj.getString("contact_name"));
                            searchItem.setDeliveryContactNo(deliveryObj.getString("contact_no"));
                            searchItem.setDeliveryEmailId(deliveryObj.getString("email_id"));
                            searchItem.setDeliveryHomeName(deliveryObj.getString("address1"));
                            searchItem.setDeliveryStreetName(deliveryObj.getString("address2"));
                            searchItem.setDeliveryLocationName(deliveryObj.getString("location_name"));
                            searchItem.setDeliveryCityName(deliveryObj.getString("city_name"));
                            searchItem.setDeliveryStateName(deliveryObj.getString("state_name"));
                            searchItem.setDeliveryCountryName(deliveryObj.getString("country_name"));
                            searchItem.setDeliveryPincode(deliveryObj.getString("zipcode"));
                            searchItem.setDeliveryLandmark(deliveryObj.getString("landmark"));
                            searchItem.setDeliveryRemarks(deliveryObj.getString("remarks"));
                            searchItem.setDeliveryLongValue(deliveryObj.getString("long_value"));
                            searchItem.setDeliveryLatValue(deliveryObj.getString("lat_value"));

                            searchList.add(searchItem);

                        }

                    }

                    if (dataObj.has("intransit")) {

                        JSONArray intransitArray = dataObj.getJSONArray("intransit");
                        for (int i = 0; i < intransitArray.length(); i++) {

                            searchItem = new ShipmentItem();

                            JSONObject shipmentObj = intransitArray.getJSONObject(i);
                            searchItem.setShipmentId(shipmentObj.getString("shipment_id"));
                            searchItem.setShipmentType(shipmentObj.getString("shipment_type"));
                            searchItem.setShipmentTrakingNo(shipmentObj.getString("traking_no"));
                            searchItem.setShipmentVendorRefNo(shipmentObj.getString("vendor_shipment_ref_no"));
                            searchItem.setShipmentDescription(shipmentObj.getString("shipment_description"));
                            searchItem.setShipmentClientCode(shipmentObj.getString("client_code"));
                            searchItem.setShipmentNoOfCheques(shipmentObj.getString("no_of_cheques"));
                            searchItem.setShipmentNoOfPackages(shipmentObj.getString("no_of_packages"));
                            searchItem.setShipmentTotalAmount(shipmentObj.getString("total_amount"));
                            searchItem.setShipmentCashReceived(shipmentObj.getString("cash_received"));
                            searchItem.setShipmentStatus(shipmentObj.getString("status"));
                            searchItem.setShipmentPickupDate(shipmentObj.getString("shipment_pickup_date"));
                            searchItem.setShipmentPickupTime(shipmentObj.getString("shipment_pickup_time"));

                            JSONObject pickupObj = shipmentObj.getJSONObject("pickup_address");
                            searchItem.setPickupId(pickupObj.getString("id"));
                            searchItem.setPickupName(pickupObj.getString("contact_name"));
                            searchItem.setPickupContactNo(pickupObj.getString("contact_no"));
                            searchItem.setPickupEmailId(pickupObj.getString("email_id"));
                            searchItem.setPickupHomeName(pickupObj.getString("address1"));
                            searchItem.setPickupStreetName(pickupObj.getString("address2"));
                            searchItem.setPickupLocationName(pickupObj.getString("location_name"));
                            searchItem.setPickupCityName(pickupObj.getString("city_name"));
                            searchItem.setPickupStateName(pickupObj.getString("state_name"));
                            searchItem.setPickupCountryName(pickupObj.getString("country_name"));
                            searchItem.setPickupPincode(pickupObj.getString("zipcode"));
                            searchItem.setPickupLandmark(pickupObj.getString("landmark"));
                            searchItem.setPickupRemarks(pickupObj.getString("remarks"));
                            searchItem.setPickupLongValue(pickupObj.getString("long_value"));
                            searchItem.setPickupLatValue(pickupObj.getString("lat_value"));

                            JSONObject deliveryObj = shipmentObj.getJSONObject("delivery_address");
                            searchItem.setDeliveryId(deliveryObj.getString("id"));
                            searchItem.setDeliveryName(deliveryObj.getString("contact_name"));
                            searchItem.setDeliveryContactNo(deliveryObj.getString("contact_no"));
                            searchItem.setDeliveryEmailId(deliveryObj.getString("email_id"));
                            searchItem.setDeliveryHomeName(deliveryObj.getString("address1"));
                            searchItem.setDeliveryStreetName(deliveryObj.getString("address2"));
                            searchItem.setDeliveryLocationName(deliveryObj.getString("location_name"));
                            searchItem.setDeliveryCityName(deliveryObj.getString("city_name"));
                            searchItem.setDeliveryStateName(deliveryObj.getString("state_name"));
                            searchItem.setDeliveryCountryName(deliveryObj.getString("country_name"));
                            searchItem.setDeliveryPincode(deliveryObj.getString("zipcode"));
                            searchItem.setDeliveryLandmark(deliveryObj.getString("landmark"));
                            searchItem.setDeliveryRemarks(deliveryObj.getString("remarks"));
                            searchItem.setDeliveryLongValue(deliveryObj.getString("long_value"));
                            searchItem.setDeliveryLatValue(deliveryObj.getString("lat_value"));

                            searchList.add(searchItem);

                        }

                    }

                    if(dataObj.has("complete")){

                        JSONArray completedArray = dataObj.getJSONArray("complete");
                        for (int i = 0; i < completedArray.length(); i++) {

                            searchItem = new ShipmentItem();

                            JSONObject shipmentObj = completedArray.getJSONObject(i);
                            searchItem.setShipmentId(shipmentObj.getString("shipment_id"));
                            searchItem.setShipmentType(shipmentObj.getString("shipment_type"));
                            searchItem.setShipmentTrakingNo(shipmentObj.getString("traking_no"));
                            searchItem.setShipmentVendorRefNo(shipmentObj.getString("vendor_shipment_ref_no"));
                            searchItem.setShipmentDescription(shipmentObj.getString("shipment_description"));
                            searchItem.setShipmentClientCode(shipmentObj.getString("client_code"));
                            searchItem.setShipmentNoOfCheques(shipmentObj.getString("no_of_cheques"));
                            searchItem.setShipmentNoOfPackages(shipmentObj.getString("no_of_packages"));
                            searchItem.setShipmentTotalAmount(shipmentObj.getString("total_amount"));
                            searchItem.setShipmentCashReceived(shipmentObj.getString("cash_received"));
                            searchItem.setShipmentStatus(shipmentObj.getString("status"));
                            searchItem.setShipmentPickupDate(shipmentObj.getString("shipment_pickup_date"));
                            searchItem.setShipmentPickupTime(shipmentObj.getString("shipment_pickup_time"));

                            JSONObject pickupObj = shipmentObj.getJSONObject("pickup_address");
                            searchItem.setPickupId(pickupObj.getString("id"));
                            searchItem.setPickupName(pickupObj.getString("contact_name"));
                            searchItem.setPickupContactNo(pickupObj.getString("contact_no"));
                            searchItem.setPickupEmailId(pickupObj.getString("email_id"));
                            searchItem.setPickupHomeName(pickupObj.getString("address1"));
                            searchItem.setPickupStreetName(pickupObj.getString("address2"));
                            searchItem.setPickupLocationName(pickupObj.getString("location_name"));
                            searchItem.setPickupCityName(pickupObj.getString("city_name"));
                            searchItem.setPickupStateName(pickupObj.getString("state_name"));
                            searchItem.setPickupCountryName(pickupObj.getString("country_name"));
                            searchItem.setPickupPincode(pickupObj.getString("zipcode"));
                            searchItem.setPickupLandmark(pickupObj.getString("landmark"));
                            searchItem.setPickupRemarks(pickupObj.getString("remarks"));
                            searchItem.setPickupLongValue(pickupObj.getString("long_value"));
                            searchItem.setPickupLatValue(pickupObj.getString("lat_value"));

                            JSONObject deliveryObj = shipmentObj.getJSONObject("delivery_address");
                            searchItem.setDeliveryId(deliveryObj.getString("id"));
                            searchItem.setDeliveryName(deliveryObj.getString("contact_name"));
                            searchItem.setDeliveryContactNo(deliveryObj.getString("contact_no"));
                            searchItem.setDeliveryEmailId(deliveryObj.getString("email_id"));
                            searchItem.setDeliveryHomeName(deliveryObj.getString("address1"));
                            searchItem.setDeliveryStreetName(deliveryObj.getString("address2"));
                            searchItem.setDeliveryLocationName(deliveryObj.getString("location_name"));
                            searchItem.setDeliveryCityName(deliveryObj.getString("city_name"));
                            searchItem.setDeliveryStateName(deliveryObj.getString("state_name"));
                            searchItem.setDeliveryCountryName(deliveryObj.getString("country_name"));
                            searchItem.setDeliveryPincode(deliveryObj.getString("zipcode"));
                            searchItem.setDeliveryLandmark(deliveryObj.getString("landmark"));
                            searchItem.setDeliveryRemarks(deliveryObj.getString("remarks"));
                            searchItem.setDeliveryLongValue(deliveryObj.getString("long_value"));
                            searchItem.setDeliveryLatValue(deliveryObj.getString("lat_value"));

                            searchList.add(searchItem);

                        }

                    }

                    if(dataObj.has("reject")){

                        JSONArray rejectedArray = dataObj.getJSONArray("reject");
                        for (int i = 0; i < rejectedArray.length(); i++) {

                            searchItem = new ShipmentItem();

                            JSONObject shipmentObj = rejectedArray.getJSONObject(i);
                            searchItem.setShipmentId(shipmentObj.getString("shipment_id"));
                            searchItem.setShipmentType(shipmentObj.getString("shipment_type"));
                            searchItem.setShipmentTrakingNo(shipmentObj.getString("traking_no"));
                            searchItem.setShipmentVendorRefNo(shipmentObj.getString("vendor_shipment_ref_no"));
                            searchItem.setShipmentDescription(shipmentObj.getString("shipment_description"));
                            searchItem.setShipmentClientCode(shipmentObj.getString("client_code"));
                            searchItem.setShipmentNoOfCheques(shipmentObj.getString("no_of_cheques"));
                            searchItem.setShipmentNoOfPackages(shipmentObj.getString("no_of_packages"));
                            searchItem.setShipmentTotalAmount(shipmentObj.getString("total_amount"));
                            searchItem.setShipmentCashReceived(shipmentObj.getString("cash_received"));
                            searchItem.setShipmentStatus(shipmentObj.getString("status"));
                            searchItem.setShipmentPickupDate(shipmentObj.getString("shipment_pickup_date"));
                            searchItem.setShipmentPickupTime(shipmentObj.getString("shipment_pickup_time"));

                            JSONObject pickupObj = shipmentObj.getJSONObject("pickup_address");
                            searchItem.setPickupId(pickupObj.getString("id"));
                            searchItem.setPickupName(pickupObj.getString("contact_name"));
                            searchItem.setPickupContactNo(pickupObj.getString("contact_no"));
                            searchItem.setPickupEmailId(pickupObj.getString("email_id"));
                            searchItem.setPickupHomeName(pickupObj.getString("address1"));
                            searchItem.setPickupStreetName(pickupObj.getString("address2"));
                            searchItem.setPickupLocationName(pickupObj.getString("location_name"));
                            searchItem.setPickupCityName(pickupObj.getString("city_name"));
                            searchItem.setPickupStateName(pickupObj.getString("state_name"));
                            searchItem.setPickupCountryName(pickupObj.getString("country_name"));
                            searchItem.setPickupPincode(pickupObj.getString("zipcode"));
                            searchItem.setPickupLandmark(pickupObj.getString("landmark"));
                            searchItem.setPickupRemarks(pickupObj.getString("remarks"));
                            searchItem.setPickupLongValue(pickupObj.getString("long_value"));
                            searchItem.setPickupLatValue(pickupObj.getString("lat_value"));

                            JSONObject deliveryObj = shipmentObj.getJSONObject("delivery_address");
                            searchItem.setDeliveryId(deliveryObj.getString("id"));
                            searchItem.setDeliveryName(deliveryObj.getString("contact_name"));
                            searchItem.setDeliveryContactNo(deliveryObj.getString("contact_no"));
                            searchItem.setDeliveryEmailId(deliveryObj.getString("email_id"));
                            searchItem.setDeliveryHomeName(deliveryObj.getString("address1"));
                            searchItem.setDeliveryStreetName(deliveryObj.getString("address2"));
                            searchItem.setDeliveryLocationName(deliveryObj.getString("location_name"));
                            searchItem.setDeliveryCityName(deliveryObj.getString("city_name"));
                            searchItem.setDeliveryStateName(deliveryObj.getString("state_name"));
                            searchItem.setDeliveryCountryName(deliveryObj.getString("country_name"));
                            searchItem.setDeliveryPincode(deliveryObj.getString("zipcode"));
                            searchItem.setDeliveryLandmark(deliveryObj.getString("landmark"));
                            searchItem.setDeliveryRemarks(deliveryObj.getString("remarks"));
                            searchItem.setDeliveryLongValue(deliveryObj.getString("long_value"));
                            searchItem.setDeliveryLatValue(deliveryObj.getString("lat_value"));

                            searchList.add(searchItem);

                        }

                    }

                    Constant.searchList = searchList;
                    Log.v("searchList size", String.valueOf(Constant.searchList.size()));

                    if(Constant.searchList.size() > 0){

                        searchAdapter = new SearchAdapter(SearchActivity.this, searchList);
                        searchAdapter.notifyDataSetChanged();
                        lvSearch.setAdapter(searchAdapter);

                        etSearch.setText("");

                    }

                }
                else{

                    new SweetAlertDialog(SearchActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("No records found!! Please Try Again")
                            .show();

                    etSearch.setText("");

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else{

            new SweetAlertDialog(SearchActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Something going wrong!! Please Try Again")
                    .show();

        }

    }
}
