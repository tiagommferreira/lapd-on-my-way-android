package com.example.tmmf5.onmyway;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class APITask extends AsyncTask<Void, Void, InputStream> {

    private String mApiUrl;
    private APIListener mListener;


    public APITask(String url, APIListener listener) {
        this.mApiUrl = url;
        this.mListener = listener;
    }

    protected void onPreExecute() {
        mListener.preRequest();
    }

    @Override
    protected InputStream doInBackground(Void... params) {

        try {
            URL url = new URL(mApiUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");

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
