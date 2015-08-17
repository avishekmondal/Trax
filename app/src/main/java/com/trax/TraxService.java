package com.trax;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.interfaces.BackgroundTaskInterface;
import com.utility.ConnectionCheck;
import com.utility.Constant;
import com.utility.Pref;
import com.asynctask.RunBackgroundAsync;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TraxService extends Service implements BackgroundTaskInterface{
	private static final String TAG = "TraxService";

    boolean service = false;

    private Pref _pref;
    private ConnectionCheck _connectionCheck;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	@Override
	public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        //Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();

        _pref = new Pref(TraxService.this);
        _connectionCheck = new ConnectionCheck(TraxService.this);

	}

	@Override
	public void onDestroy() {
        super.onDestroy();

		Log.d(TAG, "onDestroy");
        service = false;
        //Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();

	}
	
	@Override
	public void onStart(Intent intent, int startid) {

		Log.d(TAG, "onStart");
        service = true;
        update_location(return_location());
	}

    private void update_location(Location location) {

        SimpleDateFormat format = new SimpleDateFormat("HHmm",Locale.getDefault());
        String currentTime = format.format(new Date());
        String endTime = "23:00".replace(":", "");
        boolean isEnd = Integer.valueOf(currentTime) > Integer.valueOf(endTime);
        Log.v("isEnd", String.valueOf(isEnd));
        Log.v("service", String.valueOf(service));

        if (location != null  && service == true  && isEnd == false) {

            if (_connectionCheck.isNetworkAvailable() && !String.valueOf(location.getLatitude()).equals("0.0") && !String.valueOf(location.getLongitude()).equals("0.0")) {

                String url = Constant.baseUrl  + "updateagentlocation";

                try {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("agentId", _pref.getAgentId());
                    jsonObject.put("accessToken", _pref.getAccessToken());
                    jsonObject.put("shipmentId", _pref.getIntransitShipmentId());
                    jsonObject.put("latValue", String.valueOf(location.getLatitude()));
                    jsonObject.put("longValue", String.valueOf(location.getLongitude()));

                    JSONObject data = new JSONObject();
                    data.put("data", jsonObject);

                    String jsonInput = data.toString();
                    Log.v("jsonInput", jsonInput);

                    RunBackgroundAsync async = new RunBackgroundAsync(TraxService.this);
                    async.taskInterface = TraxService.this;
                    async.execute(url, jsonInput);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

    }

    private Location return_location() {

        //Access to the location-based services is handled by the Location Manager system Service. To access the Location Manager, request an instance of the LOCATION_SERVICE using the getSystemService method.
        String context = Context.LOCATION_SERVICE;
        LocationManager locationManager = (LocationManager)getSystemService(context);

        //The LocationManager class includes static string constants that return the provider name for the two most common Location Providers:
        //String provider = LocationManager.GPS_PROVIDER;
        //String provider = LocationManager.NETWORK_PROVIDER;

        //Instead of using GPS_PROVIDER or NETWORK_PROVIDER directly, Criteria class to dictate the requirements of a provider in terms of accuracy (fine or coarse), power use (low, medium, high), financial cost, and the ability to return values for altitude, speed, and bearing.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        List<String> matchingProviders = locationManager.getProviders(criteria, false);  //getProviders() returns all the possible matches. Boolean lets you restrict the result to a currently enabled provider.If no provider is found, null is returned.

        String provider = locationManager.getBestProvider(criteria, true); //getBestProvider() returns the best matching Location Provider. Boolean lets you restrict the result to a currently enabled provider. If no provider is found, null is returned.

        //We can find the last location fix determined by a particular Location Provider using the getLastKnownLocation method, passing in the name of the Location Provider.
        Location location = locationManager.getLastKnownLocation(provider);


        //--------------------------------------------------------------------
        //THIS IS NOT REQUIRED, IF DON'T WANT AUTOMATIC UPDATE AFTER INTERVAL.
        //--------------------------------------------------------------------



        if(service == true) {


            locationManager.requestLocationUpdates(provider, 60 * 1000, 0, locationListener); //PARAMETER: providername, interval time in milisecond, distance, location listener.

        }
        else{

            locationManager.removeUpdates(locationListener); //Stops location updates.

        }


        //
        //--------------------------------------------------------------------

        return location;
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            update_location(location); //----------------------[Function Calling]
        }
        public void onProviderDisabled(String provider){
            update_location(null); //----------------------[Function Calling]
        }
        public void onProviderEnabled(String provider){

        }
        public void onStatusChanged(String provider, int status, Bundle extras){

        }
    };

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
