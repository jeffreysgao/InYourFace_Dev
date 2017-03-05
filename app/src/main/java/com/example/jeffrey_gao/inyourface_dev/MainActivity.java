package com.example.jeffrey_gao.inyourface_dev;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener
{
    private static final long DRAWER_CLOSE_DELAY_MS = 350;
    private static final String NAV_ITEM_ID = "navItemId";

    public static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 0;

    private SettingsFragment settingsFragment = new SettingsFragment();
//    private AttentionFragment attentionFragment = new AttentionFragment();
    private EmotionsFragment emotionsFragment = new EmotionsFragment();
    private final Handler drawerActionHandler = new Handler();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int navItemId;

    public static DevicePolicyManager dpm;
    public static ComponentName compName;

    public static Context mContext;

    /**
     * When main activity is created, the main function
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        // replace the content with the gridview in GridViewFragment
        getFragmentManager().beginTransaction().replace(R.id.content,
                                                        new SettingsFragment()).commit();
        // the drawer that pops out on the left of the screen
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // if it exists, then load saved navigation state
        if (null == savedInstanceState) {
            navItemId = R.id.drawer_item_1;
        }
        else
        {
            navItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }

        // Find the navigation events, then listen to it
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        // select the correct navigation drawer item
        navigationView.getMenu().findItem(navItemId).setChecked(true);

        // set up the "==" icon to open and close the drawer
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open,
                R.string.close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // start navigating
        navigate(navItemId);

        checkPermissions();


        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        compName = new ComponentName(this, admin.class);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "explanation");
        startActivityForResult(intent, 3);


        /*
         * Testing Kairos Services - Jeff
         */

//        KairosTest.testAnalyze(this);
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 0);
        }
    }




    /**
     * Do the actual navigation activity.
     */
    private void navigate(final int itemId) {
        switch (itemId) {
            case R.id.drawer_item_1:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, settingsFragment)
                        .commit();
                break;
            case R.id.drawer_item_2:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, emotionsFragment)
                        .commit();
                break;
            default:
                break;              // ignore
        }
    }

    /**
     * Handles clicks on the navigation menu.
     */
    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // choose selected item in the navigation menu
        menuItem.setChecked(true);
        navItemId = menuItem.getItemId();

        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        drawerLayout.closeDrawer(GravityCompat.START);
        drawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(menuItem.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }

    /**
     * Receive a call to the current activity.
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * When user chooses an item.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
            return drawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * When back item is clicked
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Save the current state.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, navItemId);
    }



}

