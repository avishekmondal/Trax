package com.trax;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import com.asynctask.RunBackgroundAsync;
import com.interfaces.BackgroundTaskInterface;
import com.utility.ConnectionCheck;
import com.utility.Constant;
import com.utility.LocationProvider;
import com.utility.Pref;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class TraxService1 extends Service implements BackgroundTaskInterface, LocationProvider.LocationCallback{
    private static final String TAG = "TraxService";

    boolean service = false;

    private SimpleDateFormat dateFormat;
    String currentDate = "", savedDate = "";

    private Pref _pref;
    private ConnectionCheck _connectionCheck;

    private LocationProvider mLocationProvider;

    private static Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _pref = new Pref(TraxService1.this);
        _connectionCheck = new ConnectionCheck(TraxService1.this);

        service = true;
        startService();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        service = false;
        mLocationProvider.disconnect();

    }

    private void startService() {

        timer.scheduleAtFixedRate(new mainTask(), 0, 1000 * 5 );

    }

    private class mainTask extends TimerTask
    {
        public void run() {

            toastHandler.sendEmptyMessage(0);

        }
    }

    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            mLocationProvider = new LocationProvider(TraxService1.this, TraxService1.this);
            mLocationProvider.connect();

        }
    };

    @Override
    public void handleNewLocation(Location location) {

        _pref.saveLatitude(String.valueOf(location.getLatitude()));
        _pref.saveLongitude(String.valueOf(location.getLongitude()));

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String currentDate = dateFormat.format(calendar.getTime());
        String savedDate = _pref.getDate();

        if (_pref.getLoginFlag().equals("3") && currentDate.equals(savedDate)) {

            if (_connectionCheck.isNetworkAvailable() && !String.valueOf(location.getLatitude()).equals("0.0") && !String.valueOf(location.getLongitude()).equals("0.0")) {

                String url = Constant.baseUrl  + "updateagentlocation";

                try {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("agentId", _pref.getAgentId());
                    jsonObject.put("accessToken", _pref.getAccessToken());
                    jsonObject.put("shipmentId", _pref.getIntransitShipmentId());
                    jsonObject.put("latValue", _pref.getLatitude());
                    jsonObject.put("longValue", _pref.getLongitude());

                    JSONObject data = new JSONObject();
                    data.put("data", jsonObject);

                    String jsonInput = data.toString();
                    Log.v("jsonInput", jsonInput);

                    RunBackgroundAsync async = new RunBackgroundAsync(TraxService1.this);
                    async.taskInterface = TraxService1.this;
                    async.execute(url, jsonInput);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

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


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        else{

            /*new SweetAlertDialog(TraxService.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Something going wrong!! Please Try Again")
                    .show();*/

        }

    }
}
