package com.example.tmmf5.onmyway;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class APIPostTask extends AsyncTask<Void, Void, InputStream> {

    private String mApiUrl;
    private APIListener mListener;
    private JSONObject mBody;


    public APIPostTask(String url, JSONObject body, APIListener listener) {
        this.mApiUrl = url;
        this.mListener = listener;
        this.mBody = body;
    }

    protected void onPreExecute() {
        mListener.preRequest();
    }

    @Override
    protected InputStream doInBackground(Void... params) {

        if(mBody == null) {
            return null;
        }

        try {
            URL url = new URL(mApiUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("POST");

            OutputStream writer = urlConnection.getOutputStream();
            writer.write(mBody.toString().getBytes("UTF-8"));
            writer.close();
            writer.flush();


            return urlConnection.getInputStream();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    protected void onPostExecute(InputStream response) {
        mListener.requestCompleted(response);
    }

}

