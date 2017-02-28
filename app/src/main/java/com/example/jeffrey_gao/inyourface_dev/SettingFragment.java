package com.example.jeffrey_gao.inyourface_dev;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by jinnan on 2/25/17.
 */


public class SettingFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.setting_fragment);
        PreferenceManager preferenceManager = getPreferenceManager();
        if (preferenceManager.getSharedPreferences().getBoolean("authen_switch", true))
        {
            // Authentication switch is on
        } else {
            // Authentication switch if off
        }
    }
}

