package com.example.jeffrey_gao.inyourface_dev;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class BackgroundService extends HiddenCameraService {

    private static final String TAG = "CAMERA";
    private static boolean isRunning = false;
    private MyBinder myBinder;
    private Handler handler;
    private boolean isBind = false;
    public final String photoPath = "service_pic.png";
    //private boolean shouldContinueThread = false;
    //test

    private Camera mCamera =  null;
    private Camera.Parameters params;


    //Setting camera configuration
    public CameraConfig mCamConfig = new CameraConfig().getBuilder()
    public CameraConfig mCameraConfig = new CameraConfig()
    .getBuilder(this)
            .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
    .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
    .setImageFormat(CameraImageFormat.FORMAT_JPEG)
    .build();



    ActivityManager am;

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED) {
            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                startCamera(mCameraConfig);
            } else {
                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {
            //TODO Ask your parent activity for providing runtime permission
        }

        super.onCreate();
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

       takePicture();

//        repeatService();

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        Log.d(TAG, "PICTURE TAKEN!");

    }

    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camra permission before initializing it.
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, "Your device does not have front camera.", Toast.LENGTH_LONG).show();
                break;
        }
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

    //TODO: pass the photo to RecognizeService

    //TODO: pass the photo to AnalyzeService
}
