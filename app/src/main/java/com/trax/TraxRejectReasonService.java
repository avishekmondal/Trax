package com.trax;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import com.asynctask.RunBackgroundAsync;
import com.interfaces.BackgroundTaskInterface;
import com.utility.ConnectionCheck;
import com.utility.Constant;
import com.utility.Pref;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Avishek on 8/3/2015.
 */
public class TraxRejectReasonService extends Service implements BackgroundTaskInterface {

    private Pref _pref;
    private ConnectionCheck _connectionCheck;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _pref = new Pref(TraxRejectReasonService.this);
        _connectionCheck = new ConnectionCheck(TraxRejectReasonService.this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStart(Intent intent, int startid) {

        if (_connectionCheck.isNetworkAvailable()) {

            String url = Constant.baseUrl  + "getrejectreason";

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("agentId", _pref.getAgentId());
                jsonObject.put("accessToken", _pref.getAccessToken());

                JSONObject data = new JSONObject();
                data.put("data", jsonObject);

                String jsonInput = data.toString();
                Log.v("jsonInput", jsonInput);

                RunBackgroundAsync async = new RunBackgroundAsync(TraxRejectReasonService.this);
                async.taskInterface = TraxRejectReasonService.this;
                async.execute(url, jsonInput);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
                String fileDIr = Environment.getExternalStorageDirectory()
                        + File.separator + getPackageName()
                        + File.separator;
                File f = new File(fileDIr);
                if (!f.exists()) {
                    f.mkdirs();
                }

                String filename = f.getAbsolutePath() + File.separator
                        + Constant.JSON_REJECTED_REASON_FILE_NAME;
                Log.e("filename", filename);
                File jsonF = new File(filename);

                if (jsonF.exists()) {
                    jsonF.createNewFile();
                }

                String data = jsonStr;

                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(jsonF.getAbsolutePath());
                    fos.write(data.getBytes());
                    fos.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

}

