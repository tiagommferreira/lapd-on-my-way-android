package com.example.tmmf5.onmyway;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.tmmf5.onmyway.UserList.User;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap mMap;
    private boolean mIsMapReady = false;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private User mUser;
    private Polyline mPath;
    private LocationRequest mLocationRequest;
    private static final int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }

        mUser = (User) getIntent().getExtras().getSerializable("user");
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.tmmf5.onmyway/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.tmmf5.onmyway/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("Location", mLastLocation.toString());
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        //get location changes and update map
        mLocationRequest = LocationRequest.create();

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("MapsActivity", "Location changed");
        mLastLocation = location;

        sendPosition();
        getUserLocation();

    }

    private void sendPosition() {

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        Log.d("Facebook response", String.valueOf(response.getJSONObject()));

                        JSONObject responseObject = response.getJSONObject();

                        JSONObject position = new JSONObject();

                        try {
                            AppStatics.FB_ID = responseObject.getLong("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String url = "https://lapd-on-my-way.herokuapp.com/users/" + AppStatics.FB_ID + "/location";
                        JSONObject body = new JSONObject();
                        try {
                            body.put("latitude",mLastLocation.getLatitude());
                            body.put("longitude",mLastLocation.getLongitude());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        new APIPostTask(url, body, new APIListener() {
                            @Override
                            public void preRequest() {

                            }

                            @Override
                            public void requestCompleted(InputStream response) {

                            }
                        }).execute();

                    }

                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void getUserLocation() {
        String url = "https://lapd-on-my-way.herokuapp.com/users/" + mUser.getId() + "/location";

        new APITask(url, new APIListener() {
            @Override
            public void preRequest() {

            }

            @Override
            public void requestCompleted(InputStream response) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);

                    StringBuilder theStringBuilder = new StringBuilder();

                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        theStringBuilder.append(line + "\n");
                    }

                    JSONObject userPosition = new JSONObject(theStringBuilder.toString());


                    setUserPosition((float) userPosition.getDouble("latitude"), (float) userPosition.getDouble("longitude") );

                    getPath();


                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }
        }).execute();

    }

    private void setUserPosition(float latitude, float longitude) {
        mUser.setLatitude(latitude);
        mUser.setLongitude(longitude);

        Location userLocation = new Location(LocationManager.GPS_PROVIDER);
        userLocation.setLatitude(mUser.getLatitude());
        userLocation.setLongitude(mUser.getLongitude());

        Log.d("Location Changed", "antes do if");
        if(mLastLocation.distanceTo(userLocation) < 100) {
            Log.d("Location Changed", "dentro da distancia");
            //get nearby places
            getNearbyPlaces();

            this.finish();
        }

    }

    private void getNearbyPlaces() {
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            //intentBuilder.setLatLngBounds(new LatLngBounds(
            //        new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new LatLng(mUser.getLatitude(), mUser.getLongitude())));
            Intent intent = intentBuilder.build(MapsActivity.this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mIsMapReady = true;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                .zoom(17)                   // Sets the zoom
                //.bearing(90)                // Sets the orientation of the camera to east
                .tilt(20)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mMap.setMyLocationEnabled(true);

        getPath();
    }

    private void getPath() {
        LatLng toLocation = new LatLng(mUser.getLatitude(),mUser.getLongitude());

        String mapsUrl = "https://maps.googleapis.com/maps/api/directions/json?origin="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&destination="+toLocation.latitude+","+toLocation.longitude+"&key=" + getResources().getString(R.string.directions_key);

        Log.d("toLocation latitude", String.valueOf(toLocation.latitude));
        Log.d("toLocation longitude", String.valueOf(toLocation.longitude));
        Log.d("my latitude", String.valueOf(mLastLocation.getLatitude()));
        Log.d("my longitude", String.valueOf(mLastLocation.getLongitude()));

        new APITask(mapsUrl, new APIListener() {
            @Override
            public void preRequest() {
                if(mPath != null) {
                    mPath.remove();
                }
            }

            @Override
            public void requestCompleted(InputStream response) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);

                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();

                    JSONObject json = null;

                    json = new JSONObject(stringBuilder.toString());
                    JSONObject route = (JSONObject) json.getJSONArray("routes").get(0);
                    String polyline = route.getJSONObject("overview_polyline").getString("points");

                    PolylineOptions options = new PolylineOptions();
                    options.width(10);
                    options.color(Color.BLUE);

                    for(LatLng l : PolyUtil.decode(polyline)) {
                        options.add(l);
                    }

                    mPath = mMap.addPolyline(options);


                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }
        }).execute();
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            String attributions = (String) place.getAttributions();
            if (attributions == null) {
                attributions = "";
            }
            Log.d("Picked place", (String) place.getName());
            //mName.setText(name);
            //mAddress.setText(address);
            //mAttributions.setText(Html.fromHtml(attributions));

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
