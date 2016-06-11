package com.example.tmmf5.onmyway;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tmmf5.onmyway.UserList.UsersActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

public class LoginActivity extends AppCompatActivity {

    LoginButton loginButton;
    CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);

        if (loginButton != null) {
            loginButton.setReadPermissions("email,user_friends");

            // Callback registration
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {

                                    Log.d("Facebook response", String.valueOf(response.getJSONObject()));

                                    JSONObject responseObject = response.getJSONObject();

                                    JSONObject body = new JSONObject();

                                    JSONObject position = new JSONObject();

                                    try {
                                        position.put("latitude", 41.182466);
                                        position.put("longitude", -8.598667);

                                        AppStatics.FB_ID = responseObject.getLong("id");
                                        body.put("id",responseObject.getLong("id"));
                                        body.put("first_name",responseObject.getString("first_name"));
                                        body.put("last_name",responseObject.getString("last_name"));
                                        body.put("gender",responseObject.getString("gender"));
                                        body.put("position", position);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    new APIPostTask("https://lapd-on-my-way.herokuapp.com/login", body, new APIListener() {
                                        @Override
                                        public void preRequest() {

                                        }

                                        @Override
                                        public void requestCompleted(InputStream response) {
                                            if(response != null) {
                                                startUsersActivity();
                                                finish();
                                            }
                                        }
                                    }).execute();

                                }

                            });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,link,gender,first_name,last_name");
                    request.setParameters(parameters);
                    request.executeAsync();

                }

                @Override
                public void onCancel() {
                    Log.d("Cancel", "canceled");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e("Error", error.toString());
                }

            });

        }

    }

    public void startUsersActivity() {
        Intent intent = new Intent(this, UsersActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
