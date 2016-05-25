package com.example.tmmf5.onmyway.UserList;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.tmmf5.onmyway.MapsActivity;

import java.util.ArrayList;

public class UserListClickListener implements View.OnClickListener {
    private RecyclerView rv;
    private ArrayList<User> dataset;
    private Activity activity;

    public UserListClickListener(RecyclerView rv, ArrayList<User> dataset, Activity parentActivity) {
        this.rv = rv;
        this.dataset = dataset;
        this.activity = parentActivity;
    }

    @Override
    public void onClick(View v) {
        int pos = rv.getChildAdapterPosition(v);
        User user = dataset.get(pos);

        Intent intent = new Intent(activity, MapsActivity.class);
        intent.putExtra("user", user);
        activity.startActivity(intent);
    }

}
