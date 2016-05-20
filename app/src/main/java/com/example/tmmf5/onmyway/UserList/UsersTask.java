package com.example.tmmf5.onmyway.UserList;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.tmmf5.onmyway.User;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class UsersTask extends AsyncTask<Void, Void, InputStream> {

    Activity parent;
    ArrayList<User> users;
    RecyclerView.Adapter adapter;

    public UsersTask(Activity p, ArrayList<User> users, RecyclerView.Adapter mAdapter) {
        this.parent = p;
        this.adapter = mAdapter;
        this.users = users;
    }

    protected void onPreExecute() {
        //progressBar.setVisibility(View.VISIBLE);
        //responseTextView.setText("");
    }

    @Override
    protected InputStream doInBackground(Void... params) {

        try {
            URL url = new URL("https://lapd-on-my-way.herokuapp.com/users");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            /*
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            */

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
        if(response != null) {
            UsersXMLParser usersParser = new UsersXMLParser();
            try {
                usersParser.parse(response, users);
                Log.d("Users", String.valueOf(users));
                this.adapter.notifyDataSetChanged();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
