package com.example.jeffrey_gao.inyourface_dev;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.rvalerio.fgchecker.AppChecker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


@SuppressWarnings("deprecation")
public class BackgroundService extends Service {

    private static final String TAG = "CAMERA";
    private static boolean isRunning = false;
    private MyBinder myBinder;
    private Handler handler;
    private boolean isBind = false;
    private int interval;



    private static SurfaceView mSurfaceView;
    private static SurfaceHolder mSurfaceHolder;
    private Camera mCamera =  null;
    private Camera.Parameters params;
    private String currentPackageName = "";

    private boolean shouldContinueThread = false;
    private boolean isTimerRunning = false;

    private String mCameraId;

    ActivityManager am;

    TimerTask timerTask;

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
        shouldContinueThread = false;
        isTimerRunning = false;

        Log.d("SERVICE DESTROYED", "SERVICE DESTROYED");



        super.onDestroy();
    }

    public class MyBinder extends Binder {

        public void setMessageHandler(Handler messageHandler) {handler = messageHandler;}

        public void setInterval(int inter_val) {interval = inter_val;}

        public void setShouldContinueBoolean(boolean shouldContinue) {shouldContinueThread = shouldContinue;}

        public void stopRepeatService() {
            if (timerTask!= null) {
                timerTask.cancel();
                isTimerRunning = false;
            }}

        public void startRepeatService() {repeatService(); isTimerRunning = true;}

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
        takePhoto();

//        repeatService();

        //shouldContinueThread = true;
        //startThread();

        return super.onStartCommand(intent, flags, startId);

    }

    // Sets up the camera and takes the photo, passing it to the services
    // TODO: Handle the exception that gets thrown when camera has been opened before
    private void takePhoto() {
        setUpCamera();

        openCamera();

        Log.d("jeff", "all calls made");
    }

    //TODO: alarm manager for calling the service over time in settingsfrag with global alarm manager

    public void repeatService() {
        new Thread() {
            public void run() {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Intent myIntent = new Intent(MainActivity.mContext, BackgroundService.class);
                        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.mContext, 0, myIntent, 0);
//                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                        Calendar mCal = Calendar.getInstance();
//                        mCal.setTimeInMillis(System.currentTimeMillis());
//                        mCal.add(Calendar.SECOND, 10);
                    }
                }, 0, 5000);

            }
        }.run();
    }

    public String getForegroundActivityPackage() {
        String packageName = "";
        AppChecker appChecker = new AppChecker();
        packageName = appChecker.getForegroundApp(this);

        currentPackageName = packageName;
        Log.d("PACKAGE NAME", packageName);
        return packageName;


    }


    /*
     * Code for launching the camera in the background and taking a photo - Jeff
     * http://stackoverflow.com/questions/28003186/capture-picture-without-preview-using-camera2-api
     * https://github.com/googlesamples/android-Camera2Basic/blob/master/Application/src/main/java/com/example/android/camera2basic/Camera2BasicFragment.java
     *
     */

    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;

    private void setUpCamera() {
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                if (characteristics.get(CameraCharacteristics.LENS_FACING)
                        != CameraCharacteristics.LENS_FACING_FRONT)
                    continue;

                mCameraId = cameraId;

                imageReader = ImageReader.newInstance(100, 100, ImageFormat.JPEG, 1);
                imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);

            }
        } catch (CameraAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.openCamera(mCameraId, cameraStateCallback, backgroundHandler);
        } catch (CameraAccessException | SecurityException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback
            = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice device) {
            Log.d("jeff", "camera opened");
            cameraDevice = device;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.d("jeff", "camera disconnected");
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            Log.d("jeff", "camera error");
        }
    };

    private void createCaptureSession() {
        List<Surface> outputSurfaces = new LinkedList<>();
        outputSurfaces.add(imageReader.getSurface());

        try {
            cameraDevice.createCaptureSession(outputSurfaces,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            cameraCaptureSession = session;
                            createCaptureRequest();
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {}
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final ImageReader.OnImageAvailableListener onImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Log.d("jeff", "image captured" + bytes.length);

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            if (settings.getBoolean("auth_preference", false)) {
                Intent recognizeIntent = new Intent(getApplicationContext(), RecognizeService.class);
                recognizeIntent.putExtra(RecognizeService.INPUT_TYPE, RecognizeService.BYTE_DATA);
                recognizeIntent.putExtra(RecognizeService.FACE_IMAGE, bytes);

                startService(recognizeIntent);
            } else if (settings.getBoolean("emotions_pref", false)
                    || settings.getBoolean("attention_pref", false)) {
                // TODO: Send byte array to analyze service

                Intent analyzeIntent = new Intent(getApplicationContext(), AnalyzeService.class);
                analyzeIntent.putExtra(AnalyzeService.INPUT_TYPE, AnalyzeService.BYTE_DATA);
                analyzeIntent.putExtra(AnalyzeService.IMAGE_DATA, bytes);

                startService(analyzeIntent);
            }

            image.close();
        }
    };

    private void createCaptureRequest() {
        try {
            CaptureRequest.Builder requestBuilder = cameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            requestBuilder.addTarget(imageReader.getSurface());

            // Set the focus of the camera
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Set the orientation for the camera based on device's orientation
            WindowManager windowService = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int currentRotation = windowService.getDefaultDisplay().getRotation();
            Log.d("jeff", "" + currentRotation);
            requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, currentRotation * 90 + 90);

            cameraCaptureSession.capture(requestBuilder.build(), cameraCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback cameraCallback
            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };





    //checks foreground app every five seconds, this is just here to test the package
    //get method, kill this once you get AlarmManager
    /*public void startThread() {
        new Thread() {

            public void run() {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Intent myIntent = new Intent(MainActivity.mContext, BackgroundService.class);
                        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.mContext, 0, myIntent, 0);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        Calendar mCal = Calendar.getInstance();
                        mCal.setTimeInMillis(System.currentTimeMillis());
                        mCal.add(Calendar.SECOND, 10);
                        alarmManager.setRepeating(AlarmManager.RTC, mCal.getTimeInMillis(), 10000, pendingIntent);
                    }
                }, 0, 5000);
            }

        }.run();
    }*/



}
