package com.example.jeffrey_gao.inyourface_dev;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;

@SuppressWarnings("deprecation")
public class BackgroundService extends Service {

    private static final String TAG = "CAMERA";

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

        return super.onStartCommand(intent, flags, startId);

    }
}
