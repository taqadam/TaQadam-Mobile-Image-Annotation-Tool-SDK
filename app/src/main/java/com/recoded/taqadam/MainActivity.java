package com.recoded.taqadam;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.recoded.taqadam.models.db.JobDbHandler;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private User user;
    private View drawerHeader;
    private LockableViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthorized();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupBottomNavigation();

        setupNavigationDrawer(toolbar);

    }

    private void setupNavigationDrawer(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerHeader = navigationView.inflateHeaderView(R.layout.activity_main_drawer);
        drawerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
    }

    private void setupBottomNavigation() {
        pager = findViewById(R.id.main_pager);
        pager.setLocked(true);
        pager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));

        BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_jobs:
                        setTitle(item.getTitle());
                        /*FragmentJobs fragmentJobs = new FragmentJobs();
                        FragmentTransaction fragmentTransactionTasks = getSupportFragmentManager().beginTransaction();
                        fragmentTransactionTasks.replace(R.id.frame_layout, fragmentJobs);
                        fragmentTransactionTasks.commit();*/
                        pager.setCurrentItem(0);

                        return true;
                    case R.id.navigation_qa:
                        setTitle(item.getTitle());
                        /*FragmentQA fragmentQA = new FragmentQA();
                        FragmentTransaction fragmentTransactionQa = getSupportFragmentManager().beginTransaction();
                        fragmentTransactionQa.replace(R.id.frame_layout, fragmentQA);
                        fragmentTransactionQa.commit();*/
                        pager.setCurrentItem(1);

                        return true;
                    case R.id.navigation_cash_out:
                        setTitle(item.getTitle());
                       /* FragmentCashOut fragmentCashOut = new FragmentCashOut();
                        FragmentTransaction fragmentTransactionCashout = getSupportFragmentManager().beginTransaction();
                        fragmentTransactionCashout.replace(R.id.frame_layout, fragmentCashOut);
                        fragmentTransactionCashout.commit();*/
                        pager.setCurrentItem(2);

                        return true;
                    case R.id.navigation_discuss:
                        setTitle(item.getTitle());
                        /*FragmentDiscuss fragmentDiscuss = new FragmentDiscuss();
                        FragmentTransaction fragmentTransactionDiscuss = getSupportFragmentManager().beginTransaction();
                        fragmentTransactionDiscuss.replace(R.id.frame_layout, fragmentDiscuss);
                        fragmentTransactionDiscuss.commit();*/
                        pager.setCurrentItem(3);

                        return true;
                }
                return false;
            }
        };

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(listener);

        /*FragmentJobs fragmentJobs = new FragmentJobs();
        FragmentTransaction fragmentTransactionTasks = getSupportFragmentManager().beginTransaction();
        fragmentTransactionTasks.replace(R.id.frame_layout, fragmentJobs);
        fragmentTransactionTasks.commit();
        setTitle("Tasks");*/
        navigation.setSelectedItemId(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initHeader();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_notification) {
            // Handle the camera action
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_feedback) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_logout) {
            UserAuthHandler.getInstance().signOut();
            startActivity(new Intent(this, SigninActivity.class));
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkAuthorized() {
        if (UserAuthHandler.getInstance().getCurrentUser() == null) {
            user = new User();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error);
            builder.setIcon(R.drawable.ic_error);
            builder.setMessage(R.string.not_logged_in);
            builder.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(MainActivity.this, SigninActivity.class));
                    finish();
                }
            });
            builder.setNegativeButton(R.string.register, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        } else {
            user = UserAuthHandler.getInstance().getCurrentUser();
        }
    }

    private void initHeader() {
        TextView tv = drawerHeader.findViewById(R.id.tv_display_name);
        ImageView iv = drawerHeader.findViewById(R.id.iv_display_image);
        Picasso.with(this).load(user.getPicturePath()).into(iv);
        tv.setText(user.getDisplayName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
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
        } else if (id == R.id.action_discussion) {
            return true;
        } else if (id == R.id.action_notification) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (JobDbHandler.getInstance().getOnJobsChangedLister() != null) {
            JobDbHandler.getInstance().setOnJobsChangedListener(null);
        }
    }
}


