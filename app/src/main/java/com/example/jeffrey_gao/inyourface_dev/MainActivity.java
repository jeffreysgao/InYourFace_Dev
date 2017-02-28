package com.example.jeffrey_gao.inyourface_dev;

import java.util.*;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.TabLayout;
import android.app.Fragment;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity
{
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragmentArray;
    private TabsViewPagerAdapter viewPageAdapter;   // self-defined adapter

    public static DevicePolicyManager dpm;
    public static ComponentName compName;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create the instance of the tablayout from the main layout
        tabLayout = (TabLayout) findViewById(R.id.tab);         // defined in main xml
        viewPager = (ViewPager) findViewById(R.id.viewpager);   // defined in main xml

        // create the array of fragments
        fragmentArray = new ArrayList<Fragment>();
        fragmentArray.add(new SettingFragment());
        fragmentArray.add(new AuthenticationFragment());
        fragmentArray.add(new EmotionsFragment());

        // bind the tab layout to the viewpager
        viewPageAdapter = new TabsViewPagerAdapter(getFragmentManager(), fragmentArray);
        viewPager.setAdapter(viewPageAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        checkPermissions();

        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        compName = new ComponentName(this, admin.class);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "explanation");
        startActivityForResult(intent, 3);


    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 0);
        }
    }

}

