package com.recoded.taqadam;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawer;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_tasks:
                    setTitle("FragmentTasks");
                    FragmentTasks fragmentTasks = new FragmentTasks();
                    FragmentTransaction fragmentTransactionTasks = getSupportFragmentManager().beginTransaction();
                    fragmentTransactionTasks.replace(R.id.frame_layout, fragmentTasks);
                    fragmentTransactionTasks.commit();

                    return true;
                case R.id.navigation_qa:
                    setTitle("FragmentQa");
                    FragmentQA fragmentQA = new FragmentQA();
                    FragmentTransaction fragmentTransactionQa = getSupportFragmentManager().beginTransaction();
                    fragmentTransactionQa.replace(R.id.frame_layout, fragmentQA);
                    fragmentTransactionQa.commit();

                    return true;
                case R.id.navigation_cash_out:
                    setTitle("FragmentCashout");
                    FragmentCashOut fragmentCashOut = new FragmentCashOut();
                    FragmentTransaction fragmentTransactionCashout = getSupportFragmentManager().beginTransaction();
                    fragmentTransactionCashout.replace(R.id.frame_layout, fragmentCashOut);
                    fragmentTransactionCashout.commit();

                    return true;
                case R.id.navigation_discuss:
                    setTitle("FragmentDiscuss");
                    FragmentDiscuss fragmentDiscuss = new FragmentDiscuss();
                    FragmentTransaction fragmentTransactionDiscuss = getSupportFragmentManager().beginTransaction();
                    fragmentTransactionDiscuss.replace(R.id.frame_layout, fragmentDiscuss);
                    fragmentTransactionDiscuss.commit();

                    return true;
                case R.id.navigation_profile:
                    setTitle("FragmentProfile");
                    FragmentProfile fragmentProfile = new FragmentProfile();
                    FragmentTransaction fragmentTransactionProfile = getSupportFragmentManager().beginTransaction();
                    fragmentTransactionProfile.replace(R.id.frame_layout, fragmentProfile);
                    fragmentTransactionProfile.commit();

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_notification) {
//            // Handle the camera action
//        } else if (id == R.id.nav_setting) {
//
//        } else if (id == R.id.nav_help) {
//
//        } else if (id == R.id.nav_help) {
//
//        } else if (id == R.id.nav_feedback) {
//
//        } else if (id == R.id.nav_about) {
//
//        } else if (id == R.id.nav_logout) {
//
//        }
//
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setTitle("FragmentTasks");

        FragmentTasks fragmentTasks = new FragmentTasks();
        FragmentTransaction fragmentTransactionTasks = getSupportFragmentManager().beginTransaction();
        fragmentTransactionTasks.replace(R.id.frame_layout, fragmentTasks);
        fragmentTransactionTasks.commit();

    }
}


