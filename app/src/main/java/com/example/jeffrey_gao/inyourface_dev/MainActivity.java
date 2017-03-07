package com.example.jeffrey_gao.inyourface_dev;

import java.util.*;

import android.Manifest;
import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.TabLayout;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;

import static com.rvalerio.fgchecker.Utils.hasUsageStatsPermission;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener
{
    private static final long DRAWER_CLOSE_DELAY_MS = 350;
    private static final String NAV_ITEM_ID = "navItemId";
    private static final String IS_FIRST_ID = "isFirst";

    private SettingsFragment settingsFragment = new SettingsFragment();
    // private AttentionFragment attentionFragment = new AttentionFragment();
    private EmotionsFragment emotionsFragment = new EmotionsFragment();
    private final Handler drawerActionHandler = new Handler();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int navItemId;

    public static DevicePolicyManager dpm;
    public static ComponentName compName;

    public static Context mContext;

    public static boolean isFirst = true;
    public static boolean broughtFromForeground =  false;

    private static final int REQUEST_CODE_ENABLE = 11;

    //code from here: http://stackoverflow.com/questions/4414171/how-to-detect-when-an-android-app-goes-to-the-background-and-come-back-to-the-fo

    public class MemoryBoss implements ComponentCallbacks2 {
        @Override
        public void onConfigurationChanged(final Configuration newConfig) {
        }

        @Override
        public void onLowMemory() {
        }

        @Override
        public void onTrimMemory(final int level) {
            if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
                broughtFromForeground = true;
            }

        }
    }

    MemoryBoss memoryBoss;

    /**
     * When main activity is created, the main function
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memoryBoss = new MemoryBoss();
        registerComponentCallbacks(memoryBoss);



        // ask to set a PIN to lock the app
        SharedPreferences sharedPreferences = this.getSharedPreferences("main", 0);
        boolean isPincodeSet = sharedPreferences.getBoolean("IS_PINCODE_SET", false);

        if (isFirst) {

            Intent intent = new Intent(MainActivity.this, CustomPinActivity.class);
            if (!isPincodeSet){
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
            }
            startActivityForResult(intent, REQUEST_CODE_ENABLE);

            isFirst = false;
        }

//        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
//        lockManager.enableAppLock(this, CustomPinActivity.class);

//        AppLockManager.getInstance().enableDefaultAppLockIfAvailable(getApplication());

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

        requestUsageStatsPermission();

        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        compName = new ComponentName(this, admin.class);

        Intent adminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        adminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        adminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "explanation");
        startActivityForResult(adminIntent, 3);


        /*
         * Testing Kairos Services - Jeff
         */

//        AppLockManager.getInstance().enableDefaultAppLockIfAvailable(getApplication());
//        KairosTest.testAnalyze(this);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("main", 0);
        sharedPreferences.edit().putBoolean("IS_PINCODE_SET", true).apply();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 0);
        }
    }

    @Override
    public void onResume() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("main", 0);
        boolean isPincodeSet = sharedPreferences.getBoolean("IS_PINCODE_SET", false);

        if (broughtFromForeground) {

            Intent intent = new Intent(MainActivity.this, CustomPinActivity.class);
            if (!isPincodeSet){
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
            }
            startActivityForResult(intent, REQUEST_CODE_ENABLE);

            broughtFromForeground = false;
        }


        super.onResume();
    }

    @Override
    public void onPause() {


        super.onPause();
    }

    private void requestUsageStatsPermission() {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !hasUsageStatsPermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
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
        outState.putBoolean(IS_FIRST_ID, isFirst);
        outState.putBoolean("brought_from_foreground", broughtFromForeground);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        isFirst = savedInstanceState.getBoolean(IS_FIRST_ID);
        broughtFromForeground = savedInstanceState.getBoolean("brought_from_foreground");
    }

}

