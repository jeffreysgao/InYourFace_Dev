package com.example.jeffrey_gao.inyourface_dev;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.rvalerio.fgchecker.AppChecker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

@SuppressWarnings("deprecation")
public class BackgroundService extends Service {

    private static final String TAG = "CAMERA";
    private static boolean isRunning = false;
    private MyBinder myBinder;
    private Handler handler;
    private boolean isBind = false;
    public final String photoPath = "service_pic.png";
    ActivityManager am;
    private boolean shouldContinueThread = false;



    private Camera mCamera =  null;
    private Camera.Parameters params;

    private String currentPackageName = "";



    private boolean safeToTakePicture = false;

    Camera.PictureCallback callbackForRaw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken accessed for RAW");


            camera.stopPreview();
            camera.release();

            safeToTakePicture = true;
        }
    };

    Camera.PictureCallback callbackForJPG = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken accessed for JPG");
            camera.stopPreview();
            camera.release();
        }
    };

    Camera.ShutterCallback callbackForShutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutter accessed");
        }
    };

    public BackgroundService() {
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onCreate() {

        Log.d("SERVICE CREATED", "SERVICE CREATED");
        isRunning = true;

        handler = null;

        myBinder = new MyBinder();


        am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        shouldContinueThread = false;

        Log.d("SERVICE DESTROYED", "SERVICE DESTROYED");
        super.onDestroy();
    }

    public class MyBinder extends Binder {

        public void setMessageHandler(Handler messageHandler) {handler = messageHandler;}

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        isBind = true;
        return myBinder;
    }

    @Override
    public boolean onUnbind (Intent intent) {
        handler = null;
        isBind = false;
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            mCamera = Camera.open();
            Log.d(TAG, "Camera Opened Successfully");
        }
        catch (RuntimeException e) {
            Log.e(TAG, "Camera Unavailable for Opening");
            e.printStackTrace();
        }

        try {
            params = mCamera.getParameters();
            params.setPreviewFormat(ImageFormat.NV21);
            //params.setPreviewSize(1, 1);
            mCamera.setParameters(params);
            Log.d(TAG, "Camera Parameters Set Successfully");
        }
        catch (Exception e) {
            Log.e(TAG, "Camera Parameters Error");
            e.printStackTrace();
        }

        try {
            SurfaceView surfaceView = new SurfaceView(this);
            surfaceView.setVisibility(View.GONE);
            SurfaceHolder holder = surfaceView.getHolder();
            Log.d(TAG, "Got View Holder");
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    Log.d(TAG, "Surface Created!");
                    try {
                        mCamera.setPreviewDisplay(holder);
                        Log.d(TAG, "Set Preview Display");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    mCamera.startPreview();
                    safeToTakePicture = true;
                    Log.d(TAG, "Started Preview");
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {}
            });

//            WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
//            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(
//                    1, 1, //Must be at least 1x1
//                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//                    0,
//                    //Don't know if this is a safe default
//                    PixelFormat.UNKNOWN);
//
//            //Don't set the preview visibility to GONE or INVISIBLE
//            windowManager.addView(surfaceView, windowParams);

            Log.d(TAG, "View successfully set");
        }
        catch(Exception e) {
            Log.e(TAG, "Error in setting Preview");
            e.printStackTrace();
        }



        try {

            if (safeToTakePicture) {
                mCamera.takePicture(null, null, callbackForRaw);
                safeToTakePicture = false;
                Log.d(TAG, "Picture successfully taken");
            }

        }
        catch (Exception e) {
            Log.e(TAG, "Error taking picture");
            e.printStackTrace();
        }



        repeatService();

        shouldContinueThread = true;
        startThread();

        return super.onStartCommand(intent, flags, startId);

    }

    //TODO: alarm manager for calling the service over time

    public void repeatService() {
        new Thread() {
            public void run() {
                Intent myIntent = new Intent(MainActivity.mContext, BackgroundService.class);
                PendingIntent pendingIntent = PendingIntent.getService(MainActivity.mContext, 0, myIntent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar mCal = Calendar.getInstance();
                mCal.setTimeInMillis(System.currentTimeMillis());
                mCal.add(Calendar.SECOND, 10);
                alarmManager.setRepeating(AlarmManager.RTC, mCal.getTimeInMillis(), 10000, pendingIntent);
            }
        }.run();
    }


    //TODO: get the activity running in the foreground
    //Don't think this is possible
    //THIS DOES NOT WORK
    public String getForegroundActivityPackage() {
        String packageName = "";

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);

            long time = System.currentTimeMillis();

            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 1000*3, time);


            if (stats != null) {
                SortedMap<Long, UsageStats> sortedMap = new TreeMap<Long, UsageStats>();

                int i = 0;
                int size = stats.size();
                Log.d("SIZE", Integer.toString(size));

                while (i < size) {
                    sortedMap.put(stats.get(i).getLastTimeUsed(), stats.get(i));
                }

                Log.d("INSIDE LOOP", "INSIDE LOOP");

                if (size > 0) {

                    UsageStats mostRecent = sortedMap.get(sortedMap.lastKey());
                    packageName = mostRecent.getPackageName();
                    Log.d("PACKAGE NAME", packageName);
                }
            }

        } else {
            ActivityManager.RunningTaskInfo foregroundInfo = am.getRunningTasks(1).get(0);
            packageName = foregroundInfo.topActivity.getPackageName();
            Log.d("PACKAGE NAME", packageName);
        }*/

        AppChecker appChecker = new AppChecker();
        packageName = appChecker.getForegroundApp(this);

        Log.d("PACKAGE NAME", packageName);

        currentPackageName = packageName;

        return packageName;

    }



    //checks foreground app every five seconds, this is just here to test the package
    //get method, kill this once you get AlarmManager
    public void startThread() {
        new Thread() {

            public void run() {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (shouldContinueThread) {
                            getForegroundActivityPackage();
                        }
                    }
                }, 0, 5000);
            }

        }.run();
    }


    //TODO: pass the photo to RecognizeService

    //TODO: pass the photo to AnalyzeService
}
