package com.example.jeffrey_gao.inyourface_dev;

import android.app.Application;

import com.github.orangegangsters.lollipin.lib.managers.LockManager;

/**
 * Created by RaunakBhojwani on 3/5/17.
 */

public class CustomApplication extends Application {

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
        super.onCreate();

        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, CustomPinActivity.class);
        lockManager.getAppLock().setLogoId(R.drawable.security_lock);
    }


}
