package com.example.tmmf5.onmyway.UserList;

import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.tmmf5.onmyway.APIListener;
import com.example.tmmf5.onmyway.APITask;
import com.example.tmmf5.onmyway.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class UsersActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_users, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private RecyclerView mRecyclerView;
        private RecyclerView.Adapter mAdapter;
        private RecyclerView.LayoutManager mLayoutManager;
        private SwipeRefreshLayout mSwipeRefreshLayout;

        private ArrayList<User> myDataset;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_users, container, false);

            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeLayout);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshItems();
                }
            });

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.users_list);

            mLayoutManager = new LinearLayoutManager(this.getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            myDataset = new ArrayList<>();

            mAdapter = new UserListAdapter(myDataset, mRecyclerView, this.getActivity(), this.getArguments().get(ARG_SECTION_NUMBER));
            mRecyclerView.setAdapter(mAdapter);

            getUsers();

            return rootView;
        }

        void refreshItems() {
            getUsers();
        }

        void getUsers() {
            new APITask("https://lapd-on-my-way.herokuapp.com/users", new APIListener() {
                @Override
                public void preRequest() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    Log.d("Task","Getting users");
                }

                @Override
                public void requestCompleted(InputStream response) {
                    if(response != null) {
                        UsersXMLParser usersParser = new UsersXMLParser();
                        try {
                            myDataset.clear();
                            usersParser.parse(response, myDataset);

                            if(getArguments().get(ARG_SECTION_NUMBER).equals(2)) {
                                //get facebook friends
                                new GraphRequest(
                                        AccessToken.getCurrentAccessToken(),
                                        "/me/friends",
                                        null,
                                        HttpMethod.GET,
                                        new GraphRequest.Callback() {
                                            public void onCompleted(GraphResponse response) {
                                                Log.d("Friends response", String.valueOf(response.getJSONObject()));

                                                ArrayList<Long> friendIds = new ArrayList<Long>();

                                                try {
                                                    JSONArray friends =  response.getJSONObject().getJSONArray("data");
                                                    for(int i = 0; i < friends.length(); i++) {
                                                        friendIds.add(friends.getJSONObject(i).getLong("id"));
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                for (Iterator<User> iterator = myDataset.iterator(); iterator.hasNext();) {
                                                    User u = iterator.next();

                                                    if(!friendIds.contains(u.getId())) {
                                                        iterator.remove();
                                                    }
                                                }

                                                mAdapter.notifyDataSetChanged();
                                                mSwipeRefreshLayout.setRefreshing(false);
                                            }
                                        }
                                ).executeAsync();
                            }
                            else {
                                mAdapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }

                        } catch (XmlPullParserException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).execute();
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ALL USERS";
                case 1:
                    return "FRIENDS";
                case 2:
                    return "CHAT";
            }
            return null;
        }
    }



}
