package com.trax;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
public class LoginActivity extends Activity implements BackgroundTaskInterface {

    private EditText etMobile, etPassword;
    private TextView tvForgotPassword;
    private ActionProcessButton btnSubmit;
    ProgressGenerator progressGenerator;

    String mobile = "", password = "";

    private Pref _pref;
    private ConnectionCheck _connectionCheck;


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

    @Override
    public void onStarted() {

        progressGenerator.progress(btnSubmit, 0);
        btnSubmit.setEnabled(false);

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

                        _pref.saveLoginFlag("1");
                        _pref.saveMobileNo(mobile);

                        startActivity(new Intent(LoginActivity.this, OTPVerificationActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();

                        progressGenerator.progress(btnSubmit, 1000);
                        btnSubmit.setCompleteText("VERIFIED");

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
}
