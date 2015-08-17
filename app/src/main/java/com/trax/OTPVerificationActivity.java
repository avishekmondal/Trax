package com.trax;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.dd.processbutton.iml.ActionProcessButton;
import com.interfaces.BackgroundTaskInterface;
import com.utility.Constant;
import com.utility.ConnectionCheck;
import com.utility.Pref;
import com.utility.ProgressGenerator;
import com.asynctask.RunBackgroundAsync;
import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Avishek on 6/16/2015.
 */
public class OTPVerificationActivity extends Activity implements BackgroundTaskInterface{

    private EditText etOTP;
    private ActionProcessButton btnSubmitOTP;
    ProgressGenerator progressGenerator;

    private Pref _pref;
    private ConnectionCheck _connectionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        initialize();
        onClick();
    }

    private void initialize(){

        _pref = new Pref(OTPVerificationActivity.this);
        _connectionCheck = new ConnectionCheck(OTPVerificationActivity.this);

        etOTP = (EditText) findViewById(R.id.etOTP);
        btnSubmitOTP = (ActionProcessButton)findViewById(R.id.btnSubmitOTP);

        progressGenerator = new ProgressGenerator(OTPVerificationActivity.this);
        btnSubmitOTP.setMode(ActionProcessButton.Mode.ENDLESS);

    }

    private void onClick(){

        btnSubmitOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(etOTP.getText().toString().equalsIgnoreCase("123")){

                    getUserDetails();

                }
                else{

                    etOTP.setText("");
                    new SweetAlertDialog(OTPVerificationActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Enter 123 and Try Again.")
                            .show();

                }

            }
        });
    }

    private void getUserDetails(){

        if (_connectionCheck.isNetworkAvailable()) {

            String url = Constant.baseUrl  + "user";

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mobileNo", _pref.getMobileNo());
                jsonObject.put("deviceId", _pref.getDeviceId());
                jsonObject.put("deviceType", "android");

                JSONObject data = new JSONObject();
                data.put("data", jsonObject);

                String jsonInput = data.toString();
                Log.v("jsonInput", jsonInput);

                RunBackgroundAsync async = new RunBackgroundAsync(
                        OTPVerificationActivity.this);
                async.taskInterface = OTPVerificationActivity.this;
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

        progressGenerator.progress(btnSubmitOTP, 0);
        btnSubmitOTP.setEnabled(false);

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
                    String id = dataObj.getString("id");
                    String access_token = dataObj.getString("access_token");
                    String name = dataObj.getString("name");
                    String email = dataObj.getString("email");
                    String address = dataObj.getString("address");
                    String image = dataObj.getString("image_url");

                    _pref.saveLoginFlag("2");
                    _pref.saveAgentId(id);
                    _pref.saveAccessToken(access_token);
                    _pref.saveName(name);
                    _pref.saveEmailId(email);
                    _pref.saveAddress(address);
                    _pref.saveProfileImage(image);

                    progressGenerator.progress(btnSubmitOTP, 1000);
                    btnSubmitOTP.setCompleteText("VERIFIED");

                    startActivity(new Intent(OTPVerificationActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();

                }

                else{

                    new SweetAlertDialog(OTPVerificationActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText(errMsg)
                            .show();

                    progressGenerator.progress(btnSubmitOTP, 1000);
                    btnSubmitOTP.setCompleteText("SUBMIT");
                    btnSubmitOTP.setEnabled(true);

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        else{

            new SweetAlertDialog(OTPVerificationActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Something going wrong!! Please Try Again")
                    .show();

            progressGenerator.progress(btnSubmitOTP, 1000);
            btnSubmitOTP.setCompleteText("SUBMIT");
            btnSubmitOTP.setEnabled(true);

        }

    }
}