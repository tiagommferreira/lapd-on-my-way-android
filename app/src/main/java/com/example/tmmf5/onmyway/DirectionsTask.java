package com.example.tmmf5.onmyway;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class DirectionsTask extends AsyncTask<Object, Void, String> {

    Activity parent;
    GoogleMap mMap;
    Location fromLocation;
    LatLng toLocation;

    @Override
    protected String doInBackground(Object... params) {
        this.parent = (Activity) params[0];
        this.mMap = (GoogleMap) params[1];
        this.fromLocation = (Location) params[2];
        this.toLocation = (LatLng) params[3];


        String link = "https://maps.googleapis.com/maps/api/directions/json?origin="+fromLocation.getLatitude()+","+fromLocation.getLongitude()+"&destination="+toLocation.latitude+","+toLocation.longitude+"&key=" + parent.getResources().getString(R.string.directions_key);

        Log.d("link", link);

        try {
            URL url = new URL(link);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();

            return stringBuilder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(String s) {
        if(s == null) {
            s = "THERE WAS AN ERROR";
        }
        //progressBar.setVisibility(View.GONE);
        JSONObject json = null;
        try {
            json = new JSONObject(s);
            JSONObject route = (JSONObject) json.getJSONArray("routes").get(0);
            String polyline = route.getJSONObject("overview_polyline").getString("points");

            PolylineOptions options = new PolylineOptions();
            options.width(10);
            options.color(Color.BLUE);

            for(LatLng l : PolyUtil.decode(polyline)) {
                options.add(l);
            }

            mMap.addPolyline(options);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
