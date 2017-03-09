package com.example.jeffrey_gao.inyourface_dev;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kairos.Kairos;
import com.kairos.KairosListener;

import org.json.JSONException;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by jeffreygao on 2/25/17.
 *
 * Analyze the picture uploaded, built on top of Kairos APIs.
 */

public class RecognizeService extends Service {
    public static final String FACE_IMAGE = "face_image";
    public static final String GALLERY_ID = "users";
    public static final String PACKAGE_NAME = "package_name";
    public String faceImage;
    public String currentPackageName;
//    private Kairos myKairos;
    private KairosListener kairosListener;

    @Override
    public void onCreate() {
        super.onCreate();
//        myKairos = new Kairos();
//        String appId = getResources().getString(R.string.kairos_app_id);
//        String appKey = getResources().getString(R.string.kairos_app_key);
//        myKairos.setAuthentication(this, appId, appKey);

        kairosListener = new KairosListener() {
            @Override
            public void onSuccess(String s) {
                Log.d("KAIROS RECOGNIZE", s);
                final JsonObject response = new JsonParser().parse(s).getAsJsonObject();

                new Thread() {

                    public void run() {
                        JsonElement element = response.getAsJsonObject().get("images");
                        JsonArray images = null;

                        if (element != null)
                        {
                            images = element.getAsJsonArray();
                        } else

                        {
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            if (settings.getBoolean("toast_pref", false)) {
                                Toast.makeText(getApplicationContext(), "No faces identified",
                                        Toast.LENGTH_SHORT).show();
                            }

                            Log.d("KAIROS RECOGNIZE", "no faces");
                            if (settings.getBoolean("lock_preference", false)
                                    && MainActivity.dpm.isAdminActive(MainActivity.compName)) {
                                MainActivity.dpm.lockNow();
                            }
                        }
                        if (images != null && images.size() > 0)
                        {
                            JsonObject transaction = images
                                    .get(0)
                                    .getAsJsonObject()
                                    .get("transaction")
                                    .getAsJsonObject();

                            if (transaction != null) {
                                JsonElement status = transaction.get("status");

                                if (status != null && status.getAsString().equals("success")) {
                                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    if (settings.getBoolean("toast_pref", false)) {
                                        Toast.makeText(getApplicationContext(), "Valid user identified",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    Log.d("KAIROS RECOGNIZE", "success");

                                    /*
                                     * Start the analyze if the user is successfully recognized
                                     */
                                    if (settings.getBoolean("emotions_pref", false)
                                            || settings.getBoolean("attention_pref", false)) {
                                        Log.d("KAIROS RECOGNIZE", "authentication success - starting " + currentPackageName);
                                        if (currentPackageName != null) {
                                            Intent analyzeIntent = new Intent(getApplicationContext(), AnalyzeService.class);
                                            analyzeIntent.putExtra(AnalyzeService.FACE_IMAGE, faceImage);
                                            analyzeIntent.putExtra(BackgroundService.PACKAGE_NAME, currentPackageName);

                                            startService(analyzeIntent);
                                        }
                                    }

                                } else if (status != null && status.getAsString().equals("failure")) {
                                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    if (settings.getBoolean("toast_pref", false)) {
                                        Toast.makeText(getApplicationContext(), "Invalid user identified!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    Log.d("KAIROS RECOGNIZE", "failure");
                                    if (settings.getBoolean("lock_preference", false)
                                            && MainActivity.dpm.isAdminActive(MainActivity.compName)) {
                                        MainActivity.dpm.lockNow();
                                    }
                                }
                            }
                        }

                        stopSelf();
                    }
                }.run();

            }

            @Override
            public void onFail(String s) {
                Log.d("KAIROS RECOGNIZE", s);
                stopSelf();
            }
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("RECOGNIZE SERVICE", "service started");

        if (intent != null) {
            faceImage = intent.getStringExtra(FACE_IMAGE);
            recognize(faceImage);

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (settings.getBoolean("emotions_pref", false)
                    || settings.getBoolean("attention_pref", false)) {
                currentPackageName = intent.getStringExtra(PACKAGE_NAME);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Helper function to save and recognize the picture in Kairos.
     * @param faceImage - string of the path of the image
     */
    private void recognize(final String faceImage) {

        new Thread() {

            public void run() {
                try

                {

                    FileInputStream fis = openFileInput(faceImage);
                    Bitmap image = BitmapFactory.decodeStream(fis);
                    String selector = "FULL";
                    String threshold = "0.75";
                    String minHeadScale = null;
                    String maxNumResults = "25";
                    KairosHelper.recognize(getApplicationContext(),
                            image,
                            GALLERY_ID,
                            selector,
                            threshold,
                            minHeadScale,
                            maxNumResults,
                            kairosListener);
                    fis.close();
                } catch (
                        IOException e
                        )

                {
                    e.printStackTrace();
                } catch (
                        JSONException e
                        )

                {
                    e.printStackTrace();
                }
            }
        }.run();
    }

    /**
     * Bind the service.
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
