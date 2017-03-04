package com.example.jeffrey_gao.inyourface_dev;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by jinnan on 2/25/17.
 */


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_fragment);
    }

    /*
     * Checks if the user activates or deactivates the authentication feature - Jeff
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // TODO: every time the user changes an aspect of how the feature runs,
        // TODO: either send message to the background service or restart it with new options
        if (s.equals("auth_preference")) {
            if (sharedPreferences.getBoolean("auth_preference", false)) {
                Log.d("PREFERENCES", "authentication activated");
            } else {
                Log.d("PREFERENCES", "authentication deactivated");
            }
        } else if (s.equals("lock_preference")) {
            if (sharedPreferences.getBoolean("lock_preference", false)) {
                Log.d("PREFERENCES", "auto-lock activated");
            } else {
                Log.d("PREFERENCES", "auto-lock deactivated");
            }
        } else if (s.equals("emotions_pref")) {
            if (sharedPreferences.getBoolean("emotions_pref", false)) {
                Log.d("PREFERENCES", "emotion tracking activated");
            } else {
                Log.d("PREFERENCES", "emotion tracking deactivated");
            }
        } else if (s.equals("attention_pref")) {
            if (sharedPreferences.getBoolean("attention_pref", false)) {
                Log.d("PREFERENCES", "attention tracking activated");
            } else {
                Log.d("PREFERENCES", "attention tracking deactivated");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}

