package com.trax;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import com.asynctask.RunBackgroundAsync;
import com.interfaces.BackgroundTaskInterface;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.utility.Constant;
import com.utility.ConnectionCheck;
import com.utility.DBAdapter;
import com.utility.Pref;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SplashActivity extends Activity implements BackgroundTaskInterface {

    private ProgressWheel progressWheel;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    String currentDate = "", savedDate = "";

    private Pref _pref;
    private ConnectionCheck _connectionCheck;
    private DBAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        initialize();

        try{
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = telephonyManager.getDeviceId();
            _pref.saveDeviecId(deviceId);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        currentDate = dateFormat.format(calendar.getTime());
        Log.v("currentDate", currentDate);
        savedDate = _pref.getDate();
        Log.v("savedDate", savedDate);

        if(!currentDate.equals(savedDate)){
            db.open();
            db.deleteAllRecord();
            db.close();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(currentDate.equals(savedDate)){

            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    goToNext();

                }
            }, 3000);

        }
        else{

            checkVersion();
        }

    }

    private void initialize(){

        _pref = new Pref(SplashActivity.this);
        _connectionCheck = new ConnectionCheck(SplashActivity.this);
        db = new DBAdapter(SplashActivity.this);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        progressWheel = (ProgressWheel)findViewById(R.id.progressWheel);

    }

    private void goToNext(){

        if(_pref.getLoginFlag().equals("1")){

            _pref.saveDate(currentDate);

            startActivity(new Intent(SplashActivity.this,
                    OTPVerificationActivity.class));
            finish();

        }
        else if(_pref.getLoginFlag().equals("2")){

            _pref.saveDate(currentDate);

            startActivity(new Intent(SplashActivity.this,
                    HomeActivity.class));
            finish();

        }
        else if(_pref.getLoginFlag().equals("3")){

            if(currentDate.equals(savedDate)){

                startActivity(new Intent(SplashActivity.this,
                        HomeActivity.class));
                finish();

            }
            else{

                _pref.saveLoginFlag("2");
                _pref.saveDate(currentDate);

                startActivity(new Intent(SplashActivity.this,
                        HomeActivity.class));
                finish();

            }

        }
        else{

            _pref.saveDate(currentDate);

            startActivity(new Intent(SplashActivity.this,
                    LoginActivity.class));
            finish();

        }

    }

    private void checkVersion(){

        if (_connectionCheck.isNetworkAvailable()) {

            PackageManager manager = this.getPackageManager();
            try {
                PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                String packageName = info.packageName;
                int versionCode = info.versionCode;
                String versionName = info.versionName;

                String url = Constant.baseUrl  + "version-check";

                try {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("version", versionName);

                    JSONObject data = new JSONObject();
                    data.put("data", jsonObject);

                    String jsonInput = data.toString();
                    Log.v("jsonInput", jsonInput);

                    RunBackgroundAsync async = new RunBackgroundAsync(
                            SplashActivity.this);
                    async.taskInterface = SplashActivity.this;
                    async.execute(url, jsonInput);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
            }
        } else {

            _connectionCheck.getNetworkActiveAlert().show();
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

                if(errCode.equalsIgnoreCase("0")){

                    JSONObject dataObj = jsonObj.getJSONObject("data");
                    String verified = dataObj.getString("verified");

                    if(verified.equalsIgnoreCase("true")){

                        goToNext();

                    }
                    else{

                        new SweetAlertDialog(SplashActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("Please install latest APK and Try again")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                                        finish();
                                    }
                                })
                                .show();

                    }
                }

                else{

                    new SweetAlertDialog(SplashActivity.this, SweetAlertDialog.ERROR_TYPE)
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

            new SweetAlertDialog(SplashActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Something going wrong!! Please Try Again")
                    .show();

        }

    }
}
