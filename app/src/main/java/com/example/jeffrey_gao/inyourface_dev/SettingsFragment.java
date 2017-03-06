package com.example.jeffrey_gao.inyourface_dev;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;

/**
 * Created by jinnan on 2/25/17.
 *
 * This class is for the setting page.
 */

//edited by Tong on 3/6/17.


public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, ServiceConnection{

    private boolean isBind = false;
    private BackgroundService.MyBinder binder;
    private MessageHandler handler;
    private int mInterval = 3000;

    private static boolean connectionInitialized = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        handler = new MessageHandler();


        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_fragment);

        Preference startButton = findPreference("start_button");
        startButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity().getApplicationContext(), BackgroundService.class);

                if (!BackgroundService.isRunning()) {
                    getActivity().startService(intent);

                    //Bind to the service so we can send messages to it
                    if (!isBind) {
                        getActivity().getApplicationContext().bindService(intent, SettingsFragment.this , 0);
                        isBind = true;
                    }
                }

                return false;
            }
        });

        Preference stopButton = findPreference("stop_button");
        stopButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity().getApplicationContext(), BackgroundService.class);

                if (BackgroundService.isRunning()) {
                    if (isBind) {
                        getActivity().getApplicationContext().unbindService(SettingsFragment.this);
                        isBind = false;
                    }

                    getActivity().stopService(intent);
                    connectionInitialized = false;


                }

                return false;
            }
        });

       ListPreference prefs = (ListPreference) findPreference("interval_preference");

        String intervalChoice = prefs.getValue();

        switch (intervalChoice) {
            case "3 sec":
                mInterval = 3000;
                break;
            case "5 sec":
                mInterval = 5000;
                break;
            case "10 sec":
                mInterval = 10000;
                break;
            case "15 sec":
                mInterval = 15000;
                break;
            case "30 sec":
                mInterval = 30000;
                break;
            case "1 min":
                mInterval = 60000;
                break;

            default:
                mInterval = 3000;
                break;
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            connectionInitialized = savedInstanceState.getBoolean("INITIALIZED");
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {

        binder = (BackgroundService.MyBinder) service;
        binder.setMessageHandler(handler);


        binder.setInterval(mInterval);

        if (!connectionInitialized) {
            binder.stopRepeatService();
            binder.startRepeatService();
            connectionInitialized = true;
        }


        binder.setShouldContinueBoolean(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

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
                //do stuff with binder
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
        } else if (s.equals("interval_preference")) {
            String intervalChoice = sharedPreferences.getString("interval_preference", "10 sec");
            switch (intervalChoice) {
                case "3 sec":
                    mInterval = 3000;

                    break;
                case "5 sec":
                    mInterval = 5000;

                    break;
                case "10 sec":
                    mInterval = 10000;
                    break;
                case "15 sec":
                    mInterval = 15000;
                    break;
                case "30 sec":
                    mInterval = 30000;
                    break;
                case "1 min":
                    mInterval = 60000;
                    break;

                default:
                    mInterval = 3000;
                    break;
            }

            if (isBind && binder != null) {
                binder.setInterval(mInterval);
                binder.stopRepeatService();
                binder.startRepeatService();
            }
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (!isBind) {
            Intent startIntent = new Intent(getActivity(), BackgroundService.class);
            getActivity().getApplicationContext().bindService(startIntent, this, 0);
            isBind = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isBind) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            getActivity().getApplicationContext().unbindService(this);
            isBind = false;
        }
    }

    @Override
    public void onDestroy() {
        if (getActivity().isFinishing()) {

        }

        super.onDestroy();
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("INITIALIZED", connectionInitialized);
    }

}

