package com.example.reehams.goodreads;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


/**
 * Created by reehams on 2/19/17.
 */

public class SideBar extends AppCompatActivity {
    protected ListView mDrawerList;
    protected DrawerLayout mDrawerLayout;
    protected ArrayAdapter<String> mAdapter;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected String mActivityTitle;
    protected String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.side_bar);
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        userId = getIntent().getStringExtra("user_id");

        addDrawerItems();
        setupDrawer();

       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       getSupportActionBar().setHomeButtonEnabled(true);
    }

    protected void onCreateDrawer() {
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        userId = getIntent().getStringExtra("user_id");

        addDrawerItems();
        setupDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    protected void addDrawerItems() {
        String[] osArray = { "Home", "My Account", "My Watchlist", "Top Charts", "Movie Search", "User Search", "About Us", "Log Out"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent i = new Intent(SideBar.this,  HomeActivity.class);
                    i.putExtra("user_id", userId);
                    startActivity(i);
                }
                if (position == 1) {
                    Intent i = new Intent(SideBar.this,  MyAccountActivity.class);
                    i.putExtra("user_id", userId);
                    startActivity(i);
                }
                if (position == 2) {
                    Intent i = new Intent(SideBar.this,  WatchlistActivity.class);
                    i.putExtra("user_id", userId);
                    startActivity(i);
                }
                if (position == 3) {
                    Intent i = new Intent(SideBar.this,  TopChartsActivity.class);
                    i.putExtra("user_id", userId);
                    startActivity(i);
                }
                if (position == 4) {
                    Intent i = new Intent(SideBar.this,  MovieActivity.class);
                    i.putExtra("user_id", userId);
                    startActivity(i);
                }
                if (position == 5) {
                    Intent i = new Intent(SideBar.this,  UserSearch.class);
                    i.putExtra("user_id", userId);
                    startActivity(i);
                }
                if (position == 6) {
                    Intent i = new Intent(SideBar.this, AboutUs.class);
                    i.putExtra("user_id", userId);
                    startActivity(i);
                }
                if (position == 7) {
                    Intent i = new Intent(SideBar.this, LogOutActivity.class);
                    i.putExtra("user_id", userId);
                    startActivity(i);
                }
            }
        });
    }

    protected void setupDrawer() {

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mDrawerLayout.openDrawer(Gravity.LEFT);

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return true;
        //return super.onOptionsItemSelected(item);
    }
}