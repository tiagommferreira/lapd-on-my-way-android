package com.example.tmmf5.onmyway.UserList;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tmmf5.onmyway.R;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    private ArrayList<User> mDataset;
    private RecyclerView parentRV;
    private Activity parentActivity;
    private int tab;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.user_name);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserListAdapter(ArrayList<User> myDataset, RecyclerView mRecyclerView, Activity parentActivity, Object o) {
        mDataset = myDataset;
        parentRV = mRecyclerView;
        this.parentActivity = parentActivity;
        this.tab = (int) o;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);

        if(tab == 2)
            v.setOnClickListener(new UserListClickListener(this.parentRV,mDataset,parentActivity));

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position).getFirst_name() + " " + mDataset.get(position).getLast_name());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
