package com.trax;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.asynctask.RunBackgroundAsync;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.interfaces.BackgroundTaskInterface;
import com.utility.Constant;
import com.utility.Pref;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends Activity implements BackgroundTaskInterface{

    Button btnGCMRegister;

    GoogleCloudMessaging gcm;

    Context context;
    String regId = "", deviceId = "";

    public static final String REG_ID = "regId";

    static final String TAG = "MainActivity";

    private Pref _pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _pref = new Pref(MainActivity.this);

        try{
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
            _pref.saveDeviecId(deviceId);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        btnGCMRegister = (Button) findViewById(R.id.btnGCMRegister);
        btnGCMRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(regId)) {
                    regId = registerGCM();
                    Log.d("RegisterActivity", "GCM RegId: " + regId);

                    try {

                        String url = Constant.baseUrl  + "register-device";

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("deviceId", deviceId);
                        jsonObject.put("mobileNo", "8759569353");
                        jsonObject.put("regId", regId);
                        jsonObject.put("deviceType", "android");

                        JSONObject data = new JSONObject();
                        data.put("data", jsonObject);

                        String jsonInput = data.toString();


                        RunBackgroundAsync async = new RunBackgroundAsync(
                                MainActivity.this);
                        async.taskInterface = MainActivity.this;
                        async.execute(url, jsonInput);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(),
                            "RegId already available. RegId: " + regId,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public String registerGCM() {

        gcm = GoogleCloudMessaging.getInstance(this);
        regId = getRegistrationId(context);

        if (TextUtils.isEmpty(regId)) {

            registerInBackground();

            Log.d("RegisterActivity",
                    "registerGCM - successfully registered with GCM server - regId: "
                            + regId);
        }
        return regId;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(Constant.GOOGLE_PROJECT_ID);
                    Log.d("RegisterActivity", "registerInBackground - regId: "
                            + regId);
                    msg = "Device registered, registration ID=" + regId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.d("RegisterActivity", "Error: " + msg);
                }
                Log.d("RegisterActivity", "AsyncTask completed: " + msg);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getApplicationContext(),
                        "Registered with GCM Server." + msg, Toast.LENGTH_LONG)
                        .show();
                saveRegisterId(context, regId);
            }
        }.execute(null, null, null);
    }



    private void saveRegisterId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        Log.i(TAG, "Saving regId on app version ");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.commit();
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onCompleted(String jsonStr) {

        Toast.makeText(getApplicationContext(),
                "Completed", Toast.LENGTH_LONG)
                .show();

    }
}



