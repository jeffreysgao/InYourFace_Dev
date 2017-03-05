package com.example.jeffrey_gao.inyourface_dev;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;


@SuppressWarnings("deprecation")
public class BackgroundService extends Service {

    private static final String TAG = "CAMERA";
    private static boolean isRunning = false;
    private MyBinder myBinder;
    private Handler handler;
    private boolean isBind = false;



    private static SurfaceView mSurfaceView;
    private static SurfaceHolder mSurfaceHolder;
    private Camera mCamera =  null;
    private Camera.Parameters params;

    ActivityManager am;


    //TODO: pass the photo to RecognizeService
    //TODO: pass the photo to AnalyzeService
    Camera.PictureCallback callbackForRaw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken accessed for RAW");

            SharedPreferences settings = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());

            if (settings.getBoolean("auth_preference", false)) {
                Intent recognizeIntent = new Intent(getApplicationContext(), RecognizeService.class);
                recognizeIntent.putExtra(RecognizeService.INPUT_TYPE, RecognizeService.BYTE_DATA);
                recognizeIntent.putExtra(RecognizeService.IMAGE_DATA, data);
                startService(recognizeIntent);
            }

            else {
                if(settings.getBoolean("emotions_pref", false)) {

                }

                else if (settings.getBoolean("attention_pref", false)) {

                }
            }




            camera.stopPreview();
            camera.release();
        }
    };

    Camera.PictureCallback callbackForJPG = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken accessed for JPG");

            mCamera.startPreview();

            SharedPreferences settings = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());

            if (settings.getBoolean("auth_preference", false)) {
                Intent recognizeIntent = new Intent(getApplicationContext(), RecognizeService.class);
                recognizeIntent.putExtra(RecognizeService.INPUT_TYPE, RecognizeService.BYTE_DATA);
                recognizeIntent.putExtra(RecognizeService.IMAGE_DATA, data);
                startService(recognizeIntent);
            }

            else {
                if(settings.getBoolean("emotions_pref", false)) {

                }

                else if (settings.getBoolean("attention_pref", false)) {

                }
            }

//            camera.stopPreview();
//            camera.release();
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
        super.onCreate();
        Log.d("SERVICE CREATED", "SERVICE CREATED");

        isRunning = true;
        handler = null;
        myBinder = new MyBinder();
        am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        mSurfaceView = new SurfaceView(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "Surface Created!");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "Surface Changed!");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "Surface Destroyed!");
            }
        });

        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void onDestroy() {
        isRunning = false;
        //shouldContinueThread = false;

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
            params = mCamera.getParameters();
            mCamera.setParameters(params);
            Camera.Parameters p = mCamera.getParameters();

            final List<Size> listPreviewSize = p.getSupportedPreviewSizes();
            for (Size size : listPreviewSize) {
                Log.d(TAG, String.format("Supported Preview Size (%d, %d)", size.width, size.height));
            }

            Size previewSize = listPreviewSize.get(0);
            p.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(p);
            Log.d(TAG, "Camera Parameters Set Successfully");
        }
        catch (Exception e) {
            Log.e(TAG, "Camera Parameters Error");
            e.printStackTrace();
        }

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            Log.d(TAG, "Preview Display set and preview started");
        }
        catch(Exception e) {
            Log.e(TAG, "Error in starting Preview");
            e.printStackTrace();
        }

        try {
            mCamera.takePicture(null, null, callbackForJPG);
            Log.d(TAG, "Picture successfully taken");
        }
        catch (Exception e) {
            Log.e(TAG, "Error taking picture");
            e.printStackTrace();
        }

//        repeatService();

        //shouldContinueThread = true;
        //startThread();

        return super.onStartCommand(intent, flags, startId);

    }

    //TODO: alarm manager for calling the service over time in settingsfrag with global alarm manager

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
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);

        packageName = foregroundTaskInfo.topActivity.getPackageName();
        Log.d("PACKAGE NAME", packageName);
        return packageName;

    }


    /*
    checks foreground app every five seconds, this is just here to test the package
    get method, kill this once you get AlarmManager
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
    */

}
