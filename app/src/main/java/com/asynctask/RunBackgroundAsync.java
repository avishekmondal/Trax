package com.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.interfaces.BackgroundTaskInterface;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class RunBackgroundAsync extends AsyncTask<String, String, String> {

    private String serviceUrl, jsonInput;
    private InputStream is = null;
    private JSONObject jsonObj = new JSONObject();
    private String strJson;
    private Context context;

    public BackgroundTaskInterface taskInterface;

    public RunBackgroundAsync(Context context){

        this.context = context;
    }

    @Override
    protected String doInBackground(String... args) {

        serviceUrl = args[0];
        jsonInput = args[1];

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(serviceUrl);
            httpPost.addHeader("content-type", "application/json");
            StringEntity entity = new StringEntity(jsonInput, HTTP.UTF_8);
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            is = httpEntity.getContent();
        } catch (Exception e){
            e.printStackTrace();
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"),8);
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null){
                builder.append(line + "\n");
            }

            is.close();
            strJson = builder.toString();
            Log.v("json result", strJson);
        } catch (Exception e){
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        try {
            jsonObj = new JSONObject(strJson);
        } catch (Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        return jsonObj.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        taskInterface.onStarted();
    }

    @Override
    protected void onPostExecute(String jsonStr) {
        super.onPostExecute(jsonStr);
        taskInterface.onCompleted(jsonStr);
    }
}
