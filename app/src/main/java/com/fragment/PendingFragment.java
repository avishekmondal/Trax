package com.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.adapter.PendingAdapter;
import com.bean.ShipmentItem;
import com.interfaces.BackgroundTaskInterface;
import com.trax.PendingDetailsActivity;
import com.trax.R;
import com.trax.TraxRejectReasonService;
import com.utility.Constant;
import com.utility.ConnectionCheck;
import com.utility.DBAdapter;
import com.utility.Pref;
import com.asynctask.RunBackgroundAsync;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class PendingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BackgroundTaskInterface{

    private View rootView;
    private ListView lvPending;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<ShipmentItem> pendingList;
    private ShipmentItem pendingItem;
    private PendingAdapter pendingAdapter;

    private ArrayList<ShipmentItem> intransitList;
    private ShipmentItem intransitItem;

    private ArrayList<ShipmentItem> completedList;
    private ShipmentItem completedItem;

    private ArrayList<ShipmentItem> rejectedList;
    private ShipmentItem rejectedItem;

    private Pref _pref;
    private ConnectionCheck _connectionCheck;
    private DBAdapter db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_pending, container, false);

        initialize();
        onClick();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                getShipmentListFromDB();

            }
        });
    }

    @Override
    public void onRefresh() {

        getShipmentListFromWS();

    }

    private void initialize(){

        _pref = new Pref(getActivity());
        _connectionCheck = new ConnectionCheck(getActivity());
        db = new DBAdapter(getActivity());

        lvPending = (ListView) rootView.findViewById(R.id.lvPending);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(PendingFragment.this);



    }

    private void onClick(){

        lvPending.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), PendingDetailsActivity.class);
                intent.putExtra("startFrom", "PendingFragment");
                intent.putExtra("position", position);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

    }

    private void getShipmentListFromDB(){

        pendingList = new ArrayList<ShipmentItem>();
        intransitList = new ArrayList<ShipmentItem>();
        completedList = new ArrayList<ShipmentItem>();
        rejectedList = new ArrayList<ShipmentItem>();

        Constant.pendingList = new ArrayList<ShipmentItem>();
        Constant.intransitList = new ArrayList<ShipmentItem>();
        Constant.completedList = new ArrayList<ShipmentItem>();
        Constant.rejectedList = new ArrayList<ShipmentItem>();

        db.open();

        pendingList = db.getRecords("pending");
        Constant.pendingList = pendingList;
        Log.v("pendinglistsize", String.valueOf(Constant.pendingList.size()));
        pendingAdapter = new PendingAdapter(getActivity(), pendingList);
        pendingAdapter.notifyDataSetChanged();
        lvPending.setAdapter(pendingAdapter);

        intransitList = db.getRecords("intransit");
        Constant.intransitList = intransitList;
        Log.v("intransitlistsize", String.valueOf(Constant.intransitList.size()));
        if(Constant.intransitList.size() >= 1) {
            _pref.saveIntransitShipmentId(Constant.intransitList.get(0).getShipmentId());
        }
        else {
            _pref.saveIntransitShipmentId("");
        }

        completedList = db.getRecords("complete");
        Constant.completedList = completedList;
        Log.v("completedlistsize", String.valueOf(Constant.completedList.size()));

        rejectedList = db.getRecords("reject");
        Constant.rejectedList = rejectedList;
        Log.v("rejectedlistsize", String.valueOf(Constant.rejectedList.size()));

        db.close();

    }

    private void getShipmentListFromWS(){

        db.open();
        db.deleteAllRecord();
        db.close();

        getActivity().startService(new Intent(getActivity(),
                TraxRejectReasonService.class));

        if (_connectionCheck.isNetworkAvailable()) {

            String url = Constant.baseUrl  + "shipment";

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("agentId", _pref.getAgentId());
                jsonObject.put("accessToken", _pref.getAccessToken());

                JSONObject data = new JSONObject();
                data.put("data", jsonObject);

                String jsonInput = data.toString();

                RunBackgroundAsync async = new RunBackgroundAsync(
                        getActivity());
                async.taskInterface = PendingFragment.this;
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

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);

    }

    @Override
    public void onCompleted(String jsonStr) {

        // stopping swipe refresh
        swipeRefreshLayout.setRefreshing(false);

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

                    if(dataObj.has("complete")){

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

                    if(dataObj.has("reject")){

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
        }
        else{

            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Something going wrong!! Please Try Again")
                    .show();

        }

    }

}
