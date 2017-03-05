package com.example.jeffrey_gao.inyourface_dev;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;

import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class BackgroundService extends Service {

    private static final String TAG = "CAMERA";
    private static boolean isRunning = false;

    ActivityManager am;

    Camera mCamera =  null;
    Camera.Parameters params;

    Camera.PictureCallback callbackForRaw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken accessed for RAW");
            camera.stopPreview();
            camera.release();
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
        isRunning = true;

        am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        isRunning = false;

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
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
            mCamera.setParameters(params);
            Log.d(TAG, "Camera Parameters Set Successfully");
        }
        catch (Exception e) {
            Log.e(TAG, "Camera Parameters Error");
            e.printStackTrace();
        }

        try {
            SurfaceView surfaceView = new SurfaceView(this);
            mCamera.setPreviewDisplay(surfaceView.getHolder());
            mCamera.startPreview();
            Log.d(TAG, "View successfully set");
        }
        catch(Exception e) {
            Log.e(TAG, "Error in setting Preview");
            e.printStackTrace();
        }

        try {
            mCamera.takePicture(callbackForShutter, callbackForRaw, callbackForJPG);
            Log.d(TAG, "Picture successfully taken");
        }
        catch (Exception e) {
            Log.e(TAG, "Error taking picture");
            e.printStackTrace();
        }

        startThread();

        return super.onStartCommand(intent, flags, startId);

    }

    //TODO: alarm manager for calling the service over time

    //TODO: get the activity running in the foreground

    //THIS DOES NOT WORK
    public String getForegroundActivityPackage() {
        String packageName = "";
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);

        packageName = foregroundTaskInfo.topActivity.getPackageName();
        Log.d("PACKAGE NAME", packageName);
        return packageName;

    }

    //checks foreground app every five seconds
    public void startThread() {
        new Thread() {

            public void run() {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        getForegroundActivityPackage();
                    }
                }, 0, 5000);
            }

        }.run();
    }

    //TODO: pass the photo to RecognizeService

    //TODO: pass the photo to AnalyzeService
}
