package com.example.jeffrey_gao.inyourface_dev;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
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
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class RegisterService extends Service {
    public static final String USER_FACE_IMAGE = "user_face_image";
    public static final String USER_NAME = "user_name";
    public static final String ACTION = "action";
    private Kairos myKairos;
    private KairosListener kairosListener;

    public RegisterService() {
    }

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
                Log.d("KAIROS ENROLL", s);
                JsonObject response = new JsonParser().parse(s).getAsJsonObject();
                if (response.get("gallery_ids") != null) {
                    JsonArray galleries = response.get("gallery_ids").getAsJsonArray();
                    Iterator<JsonElement> elements = galleries.iterator();
                    while(elements.hasNext()) {
                        JsonElement gallery = elements.next();
                        deleteGallery(gallery.getAsString());
                    }
                } else if (response.get("images") != null) {
                    JsonArray images = response.get("images").getAsJsonArray();
                    JsonObject transaction = images
                        .get(0)
                        .getAsJsonObject()
                        .get("transaction")
                        .getAsJsonObject();
                    if (transaction != null) {
                        JsonElement status = transaction.get("status");
                        if (status != null && status.getAsString().equals("success")) {
                            JsonElement name = transaction.get("subject_id");
                            JsonElement gallery = transaction.get("gallery_name");

                            Toast.makeText(getApplicationContext(),
                                    name + " registered in " + gallery + ".",
                                    Toast.LENGTH_LONG).show();
                            Log.d("KAIROS RECOGNIZE", name + " registered in " + gallery + ".");
                        } else {
                            Toast.makeText(getApplicationContext(), "Unable to register." ,
                                    Toast.LENGTH_LONG).show();
                            Log.d("KAIROS RECOGNIZE", "Unable to register.");
                        }
                    }
                }

                stopSelf();
            }

            @Override
            public void onFail(String s) {
                Log.d("KAIROS ENROLL", s);
                stopSelf();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("jeff", "service started");

        String action = intent.getStringExtra(ACTION);
        if (action.equals("register")) {
            String faceImage = intent.getStringExtra(USER_FACE_IMAGE);
            String userName = intent.getStringExtra(USER_NAME);
            register(faceImage, userName);
        } else if (action.equals("delete")) {
            String userName = intent.getStringExtra(USER_NAME);
            // delete the user
        } else if (action.equals("clear")) {
            listGalleries();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void register(String faceImage, String name) {
        try {
            FileInputStream fis = openFileInput(faceImage);
            Bitmap image = BitmapFactory.decodeStream(fis);
            String subjectId = name;
            String galleryId = RecognizeService.GALLERY_ID;
            String selector = "FULL";
            String multipleFaces = "false";
            String minHeadScale = "0.25";
            myKairos.enroll(image,
                    subjectId,
                    galleryId,
                    selector,
                    multipleFaces,
                    minHeadScale,
                    kairosListener);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void listGalleries() {
        try {
            myKairos.listGalleries(kairosListener);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void deleteGallery(String name) {
        try {
            myKairos.deleteGallery(name, kairosListener);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}