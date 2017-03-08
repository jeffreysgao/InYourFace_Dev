package com.example.jeffrey_gao.inyourface_dev;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.rvalerio.fgchecker.AppChecker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;


@SuppressWarnings("deprecation")
public class BackgroundService extends Service {

    public final String photoPath = "background_photo.png";
    public static final String TIME_INTERVAL = "time_interval";
    private static final String TAG = "CAMERA";
    public static final String PACKAGE_NAME = "package_name";
    private static boolean isRunning = false;
    private MyBinder myBinder;
    private Handler handler;
    private boolean isBind = false;
    private int interval;

    private String currentPackageName = "No activity";

    private boolean shouldContinueThread = true;
    private boolean isTimerRunning = false;

    private String mCameraId;

    ActivityManager am;
    NotificationManager notificationManager;
    ScreenLockReceiver screenLockReceiver;

    StartTimerTask startTimerTask;
    TimerTask timerTask;

    public BackgroundService() {
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BACKGROUND SERVICE", "SERVICE CREATED");

        screenLockReceiver = new ScreenLockReceiver();

        isRunning = true;
        handler = null;
        myBinder = new MyBinder();
        am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        shouldContinueThread = false;
        isTimerRunning = false;

        notificationManager.cancel(0);

        unregisterReceiver(screenLockReceiver);

        Log.d("SERVICE DESTROYED", "SERVICE DESTROYED");

        super.onDestroy();
    }

    public class MyBinder extends Binder {

        public void setMessageHandler(Handler messageHandler) {handler = messageHandler;}

        public void setInterval(int inter_val) {interval = inter_val;}

        public void setShouldContinueBoolean(boolean shouldContinue) {shouldContinueThread = shouldContinue;}

        public void stopRepeatService() {
            if (startTimerTask != null && !startTimerTask.isCancelled()) {
                shouldContinueThread = false;
                isTimerRunning = false;
            }}

        public void startRepeatService() {
            repeatService();
            isTimerRunning = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        setUpNotification();
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenLockReceiver, intentFilter);

        if (intent != null) {
            interval = intent.getIntExtra(TIME_INTERVAL, 10000);
        }

        repeatService();
        isTimerRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    // Sets up the camera and takes the photo, passing it to the services
    private void takePhoto() {
        setUpCamera();

        openCamera();

        Log.d("jeff", "all calls made");
    }

    //TODO: alarm manager for calling the service over time in settingsfrag with global alarm manager

    public void repeatService() {
        shouldContinueThread = true;
        startTimerTask = new StartTimerTask();
        startTimerTask.execute();
    }

    public String getForegroundActivityPackage() {
        String packageName = "";
        AppChecker appChecker = new AppChecker();
        packageName = appChecker.getForegroundApp(this);

        currentPackageName = packageName;
        Log.d("PACKAGE NAME", packageName);

        return packageName;
    }

    public void setUpNotification() {
        String title = "In Your Face!";
        String text = "Recording your face now";

        Intent intent = new Intent(this, MainActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent)
                .build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    private class StartTimerTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            if (interval == 0) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String intervalChoice = settings.getString("interval_preference", "10 sec");
                switch (intervalChoice) {
                    case "3 sec":
                        interval = 3000;
                        break;
                    case "5 sec":
                        interval = 5000;

                        break;
                    case "10 sec":
                        interval = 10000;
                        break;
                    case "15 sec":
                        interval = 15000;
                        break;
                    case "30 sec":
                        interval = 30000;
                        break;
                    case "1 min":
                        interval = 60000;
                        break;
                    default:
                        interval = 3000;
                        break;
                }
            }

            Log.d("BACKGROUND SERVICE", "interval " + interval);
            if (shouldContinueThread)
                Log.d("BACKGROUND SERVICE", "should continue thread");

            new Timer().scheduleAtFixedRate(timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (shouldContinueThread) {
                        // This code get's called every _interval_ seconds
                        Log.d("BACKGROUND SERVICE", "take photo");
                        backgroundHandler = new Handler(Looper.getMainLooper());
                        takePhoto();
                        getForegroundActivityPackage();
                    }
                }
            }, 0, interval);

            while (shouldContinueThread) {

            }
            timerTask.cancel();
            Log.d("BACKGROUND SERVICE", "closing camera task");

            return null;
        }
    }

    public class ScreenLockReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.d("BACKGROUND SERVICE", "phone locked");
                shouldContinueThread = false;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.d("BACKGROUND SERVICE", "phone unlocked");
                repeatService();
            }
        }
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
    private CameraCaptureSession captureSession;
    private SurfaceTexture mDummyPreview = new SurfaceTexture(1);
    private Surface mDummySurface = new Surface(mDummyPreview);

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
        outputSurfaces.add(mDummySurface);

        try {
            cameraDevice.createCaptureSession(outputSurfaces,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            captureSession = session;
                            createPreviewRequest();
                            try {
                                sleep(1000);
                                captureSession.stopRepeating();
                            } catch (InterruptedException | CameraAccessException e) {
                                e.printStackTrace();
                            }
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
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            savePic(bmp);

            if (settings.getBoolean("auth_preference", false)) {
                Intent recognizeIntent = new Intent(getApplicationContext(), RecognizeService.class);
                recognizeIntent.putExtra(RecognizeService.FACE_IMAGE, photoPath);
                if (settings.getBoolean("emotions_pref", false)
                        || settings.getBoolean("attention_pref", false)) {
                    recognizeIntent.putExtra(RecognizeService.PACKAGE_NAME, currentPackageName);
                }
                startService(recognizeIntent);
            } else if (settings.getBoolean("emotions_pref", false)
                    || settings.getBoolean("attention_pref", false)) {
                Intent analyzeIntent = new Intent(getApplicationContext(), AnalyzeService.class);
                analyzeIntent.putExtra(AnalyzeService.FACE_IMAGE, photoPath);
                analyzeIntent.putExtra(PACKAGE_NAME, currentPackageName);


                startService(analyzeIntent);
            }

            Log.d("CAMERA TASK", "closing");
            image.close();
            captureSession.close();
            cameraDevice.close();
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

            captureSession.capture(requestBuilder.build(), cameraCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createPreviewRequest() {
        try {
            CaptureRequest.Builder requestBuilder = cameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(mDummySurface);
            requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureSession.setRepeatingRequest(requestBuilder.build(), null, backgroundHandler);
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

    private void savePic(Bitmap image) {
        try {
            FileOutputStream f = openFileOutput(photoPath, MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, f);
            f.flush();
            f.close();
            Log.d("jeff", "pic saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
