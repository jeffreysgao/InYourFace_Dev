package com.example.jeffrey_gao.inyourface_dev;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kairos.KairosListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by jeffreygao on 3/2/17.
 */

public class AnalyzeService extends Service {
    public static final String FACE_IMAGE = "face_image";
    private KairosListener kairosListener;

    @Override
    public void onCreate() {
        super.onCreate();

        kairosListener = new KairosListener() {
            @Override
            public void onSuccess(String s) {
                Log.d("KAIROS MEDIA", s);
                JsonObject response = new JsonParser().parse(s).getAsJsonObject();

                if (response.getAsJsonObject().get("frames") != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray() != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray().get(0) != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray().get(0).getAsJsonObject()
                        .get("people") != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray().get(0).getAsJsonObject()
                        .get("people").getAsJsonArray() != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray().get(0).getAsJsonObject()
                        .get("people").getAsJsonArray().get(0) != null) {

                    // Create emotions object from returned JSON data
                    JsonObject emotions = response.getAsJsonObject()
                            .get("frames").getAsJsonArray().get(0).getAsJsonObject()
                            .get("people").getAsJsonArray().get(0).getAsJsonObject()
                            .get("emotions").getAsJsonObject();

                    if (emotions != null) {
                        JsonElement anger = emotions.get("anger");
                        JsonElement fear = emotions.get("fear");
                        JsonElement joy = emotions.get("joy");
                        JsonElement sadness = emotions.get("sadness");
                        JsonElement surprise = emotions.get("surprise");


                        String displayString = "ANGER: " + anger.toString() +
                                " FEAR: " + fear.toString() +
                                " JOY: " + joy.toString() +
                                " SADNESS: " + sadness.toString() +
                                " SURPRISE: " + surprise.toString();

                        String emotString = anger.toString() + "," + fear.toString() + "," + joy.toString()
                                + "," + sadness.toString() + "," + surprise.toString() + "\n";

                        Log.d("EMOTIONS", displayString);
                        Toast.makeText(getApplicationContext(), displayString, Toast.LENGTH_LONG).show();

                        try {
                            FileOutputStream fos = openFileOutput("emotions.csv", MODE_APPEND);
                            OutputStreamWriter writer = new OutputStreamWriter(fos);
                            writer.append(emotString);
                            writer.flush();
                            writer.close();
                            EmotionsFragment.refresh();

                        } catch (IOException i) {
                            i.printStackTrace();
                        }
                    }
                } else {
                    // If response only contains success message
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
        postMedia(faceImage);

        return super.onStartCommand(intent, flags, startId);
    }

    private void postMedia(String faceImage) {
        try {
            KairosHelper.postMedia(getApplicationContext(), faceImage, kairosListener);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getMedia(String id) {
        try {
            KairosHelper.getMedia(getApplicationContext(), id, kairosListener);
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
