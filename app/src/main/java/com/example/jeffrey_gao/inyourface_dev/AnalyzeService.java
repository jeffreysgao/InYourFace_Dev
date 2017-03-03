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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by jeffreygao on 3/2/17.
 */

public class AnalyzeService extends Service {
    public static final String FACE_IMAGE = "face_image";
    private Kairos myKairos;
    private KairosListener kairosListener;


    @Override
    public void onCreate() {
        super.onCreate();

        kairosListener = new KairosListener() {
            @Override
            public void onSuccess(String s) {
                Log.d("KAIROS MEDIA", s);
//
                JsonObject response = new JsonParser().parse(s).getAsJsonObject();

                JsonElement frames = response.getAsJsonObject().get("frames");

                if (frames != null) {
                    JsonArray framesAsJsonArray = frames.getAsJsonArray();


                    if (framesAsJsonArray != null) {
                        JsonObject element = framesAsJsonArray.get(0).getAsJsonObject();

                        if (element != null) {
                            JsonArray people = element.get("people").getAsJsonArray();
                            if (people != null) {
                                JsonObject peopleAsObject = people.get(0).getAsJsonObject();

                                if (peopleAsObject != null) {
                                    JsonObject emotions = peopleAsObject.get("emotions").getAsJsonObject();

                                    if (emotions != null) {
                                        JsonElement anger = emotions.get("anger");
                                        JsonElement fear = emotions.get("fear");
                                        JsonElement joy = emotions.get("joy");
                                        JsonElement sadness = emotions.get("sadness");
                                        JsonElement surprise = emotions.get("surprise");

                                        String string = anger.toString() + "," + fear.toString() + "," + joy.toString()
                                                + "," + sadness.toString() + "," + surprise.toString();

                                        try {
                                            FileOutputStream fos = openFileOutput("emotions.csv", MODE_APPEND);
                                            OutputStreamWriter writer = new OutputStreamWriter(fos);
                                            writer.write(string);
                                            writer.flush();
                                            writer.close();
                                        } catch (IOException i) {
                                            i.printStackTrace();
                                        }

                                    }
                                }
                            }
                        }




                    }
                }


                stopSelf();
            }

            @Override
            public void onFail(String s) {
                Log.d("KAIROS MEDIA", s);
                stopSelf();
            }
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("jeff", "service started");

        String faceImage = intent.getStringExtra(FACE_IMAGE);
        media(faceImage);

        return super.onStartCommand(intent, flags, startId);
    }

    private void media(String faceImage) {
        try {
            KairosHelper.media(getApplication().getApplicationContext(), faceImage, kairosListener);
        } catch (IOException e) {
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
