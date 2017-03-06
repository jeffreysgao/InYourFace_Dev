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
    public static final String INPUT_TYPE = "input_type";
    public static final int STRING_DATA = 0;
    public static final int BYTE_DATA = 1;
    public static final String GALLERY_ID = "users";
    private Kairos myKairos;
    private KairosListener kairosListener;


    @Override
    public void onCreate() {
        super.onCreate();
        myKairos = new Kairos();
        String appId = getResources().getString(R.string.kairos_app_id);
        String appKey = getResources().getString(R.string.kairos_app_key);
        myKairos.setAuthentication(this, appId, appKey);

        kairosListener = new KairosListener() {
            @Override
            public void onSuccess(String s) {
                Log.d("KAIROS RECOGNIZE", s);
                JsonObject response = new JsonParser().parse(s).getAsJsonObject();
                JsonElement element = response.getAsJsonObject().get("images");
                JsonArray images = null;

                if (element != null) {
                    images = element.getAsJsonArray();
                } else {

                    Toast.makeText(getApplicationContext(), "No faces identified",
                            Toast.LENGTH_LONG).show();
                    Log.d("KAIROS RECOGNIZE", "no faces");
                }

                if (images != null && images.size() > 0) {
                    JsonObject transaction = images
                            .get(0)
                            .getAsJsonObject()
                            .get("transaction")
                            .getAsJsonObject();

                    if (transaction != null) {
                        JsonElement status = transaction.get("status");

                        if (status != null && status.getAsString().equals("success")) {
                            Toast.makeText(getApplicationContext(), "Valid user identified",
                                    Toast.LENGTH_LONG).show();
                            Log.d("KAIROS RECOGNIZE", "success");

                        } else if (status != null && status.getAsString().equals("failure")) {
                            Toast.makeText(getApplicationContext(), "Invalid user identified!",
                                    Toast.LENGTH_LONG).show();
                            Log.d("KAIROS RECOGNIZE", "failure");
                            SharedPreferences settings = PreferenceManager
                                    .getDefaultSharedPreferences(getApplicationContext());
                            if (settings.getBoolean("lock_preference", false)
                                    && MainActivity.dpm.isAdminActive(MainActivity.compName)) {
                                MainActivity.dpm.lockNow();
                            }
                        }
                    }
                }
                stopSelf();
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
        Log.d("jeff", "service started");

        if (intent.getIntExtra(INPUT_TYPE, 0) == 0) {
            String faceImage = intent.getStringExtra(FACE_IMAGE);
            recognize(faceImage);
        }
        else if (intent.getIntExtra(INPUT_TYPE, 0) == 1) {
            byte [] imageData = intent.getByteArrayExtra(FACE_IMAGE);
            recognize(imageData);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Helper function to save and recognize the picture in Kairos.
     * @param faceImage - string of the path of the image
     */
    private void recognize(String faceImage) {
        try {
            FileInputStream fis = openFileInput(faceImage);
            Bitmap image = BitmapFactory.decodeStream(fis);
            String selector = "FULL";
            String threshold = "0.75";
            String minHeadScale = null;
            String maxNumResults = "25";
            myKairos.recognize(image,
                    GALLERY_ID,
                    selector,
                    threshold,
                    minHeadScale,
                    maxNumResults,
                    kairosListener);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper function to save and recognize the picture in Kairos.
     * @param byteImage - a byte array representing the image
     */
    private void recognize(byte[] byteImage) {
        try {
            if (byteImage != null) {
                Bitmap image = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                String selector = "FULL";
                String threshold = "0.75";
                String minHeadScale = null;
                String maxNumResults = "25";
                myKairos.recognize(image,
                        GALLERY_ID,
                        selector,
                        threshold,
                        minHeadScale,
                        maxNumResults,
                        kairosListener);
            } else {
                Log.d("RECOGNIZE", "empty byte array");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
