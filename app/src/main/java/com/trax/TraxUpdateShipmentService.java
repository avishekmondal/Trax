package com.trax;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.asynctask.RunBackgroundAsync;
import com.bean.ShipmentItem;
import com.interfaces.BackgroundTaskInterface;
import com.utility.ConnectionCheck;
import com.utility.Constant;
import com.utility.DBAdapter;
import com.utility.Pref;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Avishek on 8/14/2015.
 */
public class TraxUpdateShipmentService extends Service implements BackgroundTaskInterface {
    private static final String TAG = "TraxService1";

    private static Timer timer = new Timer();

    private ArrayList<ShipmentItem> pendingList;
    private ShipmentItem pendingItem;

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
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        _pref = new Pref(TraxUpdateShipmentService.this);
        _connectionCheck = new ConnectionCheck(TraxUpdateShipmentService.this);
        db = new DBAdapter(TraxUpdateShipmentService.this);

        pendingList = new ArrayList<ShipmentItem>();

        startService();

    }


    @Override
    public void onStart(Intent intent, int startid) {

        Log.d(TAG, "onStart");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

    }

    private void startService()
    {
        timer.scheduleAtFixedRate(new mainTask(), 0, 1000 * 60 * 15);
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            toastHandler.sendEmptyMessage(0);
        }
    }

    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            if(_pref.getLoginFlag().equals("2") || _pref.getLoginFlag().equals("3")){

                getShipmentListFromWS();
            }

        }
    };

    private void getShipmentListFromWS(){

        db.open();
        db.deleteAllRecord();
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
                RunBackgroundAsync async = new RunBackgroundAsync(
                        TraxUpdateShipmentService.this);
                async.taskInterface = TraxUpdateShipmentService.this;
                async.execute(url, jsonInput);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        } else {

            _connectionCheck.getNetworkActiveAlert().show();
        }

    }

    private void getShipmentListFromDB(){

        db.open();

        pendingList = db.getRecords("pending");
        Constant.pendingList = pendingList;

        intransitList = db.getRecords("intransit");
        Constant.intransitList = intransitList;

        completedList = db.getRecords("complete");
        Constant.completedList = completedList;

        rejectedList = db.getRecords("reject");
        Constant.rejectedList = rejectedList;

        db.close();

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onCompleted(String jsonStr) {

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


        }

    }

}
