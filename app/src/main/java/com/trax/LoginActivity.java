package com.trax;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.interfaces.BackgroundTaskInterface;
import com.utility.Constant;
import com.utility.ConnectionCheck;
import com.utility.Pref;
import com.utility.ProgressGenerator;
import com.asynctask.RunBackgroundAsync;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Avishek on 6/16/2015.
 */
public class LoginActivity extends Activity implements BackgroundTaskInterface {

    private EditText etMobile, etPassword;
    private TextView tvForgotPassword;
    private ActionProcessButton btnSubmit;
    ProgressGenerator progressGenerator;

    String mobile = "", password = "";

    private Pref _pref;
    private ConnectionCheck _connectionCheck;

    GoogleCloudMessaging gcm;
    String regId = "";
    int reqWebService = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();
        onClick();
    }

    private void initialize(){

        _pref = new Pref(LoginActivity.this);
        _connectionCheck = new ConnectionCheck(LoginActivity.this);

        etMobile = (EditText) findViewById(R.id.etMobile);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        btnSubmit = (ActionProcessButton) findViewById(R.id.btnSubmit);

        progressGenerator = new ProgressGenerator(LoginActivity.this);
        btnSubmit.setMode(ActionProcessButton.Mode.ENDLESS);

    }

    private void onClick(){

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mobile = etMobile.getText().toString();
                password = etPassword.getText().toString();

                if(!mobile.equals("") && !password.equals("")){

                    checkLogin();

                }

                else{

                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Required fields should not be left blank!")
                            .show();

                }

            }
        });
    }

    private void checkLogin(){

        if (_connectionCheck.isNetworkAvailable()) {

            String url = Constant.baseUrl  + "login";

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mobileNo", mobile);
                jsonObject.put("password", password);

                JSONObject data = new JSONObject();
                data.put("data", jsonObject);

                String jsonInput = data.toString();
                Log.v("jsonInput", jsonInput);

                RunBackgroundAsync async = new RunBackgroundAsync(
                        LoginActivity.this);
                async.taskInterface = LoginActivity.this;
                async.execute(url, jsonInput);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        } else {

            _connectionCheck.getNetworkActiveAlert().show();
        }

    }

    private void sendGCMRegId(){

        if (_connectionCheck.isNetworkAvailable()) {

            try {

                String url = Constant.baseUrl  + "register-device";

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("deviceId", _pref.getDeviceId());
                jsonObject.put("mobileNo", _pref.getMobileNo());
                jsonObject.put("regId", _pref.getGCMRegId());
                jsonObject.put("deviceType", "android");

                JSONObject data = new JSONObject();
                data.put("data", jsonObject);

                String jsonInput = data.toString();
                Log.v("jsonInput", jsonInput);

                reqWebService = 1;
                RunBackgroundAsync async = new RunBackgroundAsync(
                        LoginActivity.this);
                async.taskInterface = LoginActivity.this;
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

        if(reqWebService == 0) {

            progressGenerator.progress(btnSubmit, 0);
            btnSubmit.setEnabled(false);

        }

    }

    @Override
    public void onCompleted(String jsonStr) {

        if(reqWebService == 0){

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

                            _pref.saveLoginFlag("1");
                            _pref.saveMobileNo(mobile);

                            if(!_pref.getGCMRegId().equals("")){

                                Log.v("GCM RegId: ", regId);
                                sendGCMRegId();
                            }
                            else{

                                Log.v("GCM RegId: ", regId);
                                regId = registerGCM();

                            }

                        }
                        else{

                            new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText(errMsg)
                                    .show();

                            progressGenerator.progress(btnSubmit, 1000);
                            btnSubmit.setCompleteText("SUBMIT");
                            btnSubmit.setEnabled(true);

                            etMobile.setText("");
                            etPassword.setText("");
                            mobile = "";
                            password = "";

                        }
                    }

                    else{

                        new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText(errMsg)
                                .show();

                        progressGenerator.progress(btnSubmit, 1000);
                        btnSubmit.setCompleteText("SUBMIT");
                        btnSubmit.setEnabled(true);

                        etMobile.setText("");
                        etPassword.setText("");
                        mobile = "";
                        password = "";

                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            else{

                new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something going wrong!! Please Try Again")
                        .show();

                progressGenerator.progress(btnSubmit, 1000);
                btnSubmit.setCompleteText("SUBMIT");
                btnSubmit.setEnabled(true);

                etMobile.setText("");
                etPassword.setText("");
                mobile = "";
                password = "";

            }

        }

        else{

            progressGenerator.progress(btnSubmit, 1000);
            btnSubmit.setCompleteText("VERIFIED");

            startActivity(new Intent(LoginActivity.this, OTPVerificationActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();

        }

    }

    public String registerGCM() {

       registerInBackground();
       return regId;

    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(LoginActivity.this);
                    }
                    regId = gcm.register(Constant.GOOGLE_PROJECT_ID);
                    msg = "Device registered, registration ID=" + regId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                Log.v("RegisterActivity", "AsyncTask completed: " + msg);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

                _pref.saveGCMRegId(regId);
                sendGCMRegId();

            }
        }.execute(null, null, null);
    }
}
