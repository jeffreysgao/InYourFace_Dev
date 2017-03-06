package com.example.jeffrey_gao.inyourface_dev;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kairos.KairosListener;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by jeffreygao on 3/2/17.
 *
 * Emotion analysis of the image.
 */

public class AnalyzeService extends Service {
    public static final String FACE_IMAGE = "face_image";
    private KairosListener kairosListener;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        this.context = this;

        kairosListener = new KairosListener() {
            @Override
            public void onSuccess(String s) {
                Log.d("KAIROS MEDIA", s);
                JsonObject response = new JsonParser().parse(s).getAsJsonObject();

                if (response.getAsJsonObject().get("frames").getAsJsonArray().get(0).getAsJsonObject()
                        .get("people") == null) {
                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "No faces identified", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if (response.getAsJsonObject().get("frames") != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray() != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray().size() > 0
                        && response.getAsJsonObject().get("frames").getAsJsonArray().get(0).getAsJsonObject()
                        .get("people") != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray().get(0).getAsJsonObject()
                        .get("people").getAsJsonArray() != null
                        && response.getAsJsonObject().get("frames").getAsJsonArray().get(0).getAsJsonObject()
                        .get("people").getAsJsonArray().size() > 0) {

                    // Create emotions object from returned JSON data
                    JsonObject emotions = response.getAsJsonObject()
                            .get("frames").getAsJsonArray().get(0).getAsJsonObject()
                            .get("people").getAsJsonArray().get(0).getAsJsonObject()
                            .get("emotions").getAsJsonObject();

                    JsonObject tracking = response.getAsJsonObject()
                            .get("frames").getAsJsonArray().get(0).getAsJsonObject()
                            .get("people").getAsJsonArray().get(0).getAsJsonObject()
                            .get("tracking").getAsJsonObject();

                    if (emotions != null && tracking != null) {
                        JsonElement anger = emotions.get("anger");
                        JsonElement fear = emotions.get("fear");
                        JsonElement disgust = emotions.get("disgust");
                        JsonElement joy = emotions.get("joy");
                        JsonElement sadness = emotions.get("sadness");
                        JsonElement surprise = emotions.get("surprise");


                        JsonElement attention = tracking.get("attention");



                        String displayString = "ANGER: " + anger.toString() +
                                " FEAR: " + fear.toString() +
                                " DISGUST: " + disgust.toString() +
                                " JOY: " + joy.toString() +
                                " SADNESS: " + sadness.toString() +
                                " SURPRISE: " + surprise.toString() +
                                " ATTENTION: " + attention.toString();

                        String emotString = anger.toString() + "," + fear.toString() + ","
                                + disgust.toString() + "," + joy.toString() + ","
                                + sadness.toString() + "," + surprise.toString() + "\n";

                        Log.d("EMOTIONS", displayString);
                        Toast.makeText(getApplicationContext(), displayString, Toast.LENGTH_LONG).show();

                        if (response.get("id") != null) {
                            // Delete the uploaded photo
                            deleteMedia(response.get("id").getAsString());
                        }

                        /*try {
                            //FileOutputStream fos = openFileOutput("emotions.csv", MODE_APPEND);
                            FileOutputStream fos = openFileOutput("emotionz.csv", MODE_APPEND);
                            OutputStreamWriter writer = new OutputStreamWriter(fos);
                            writer.append(emotString);
                            writer.flush();
                            writer.close();
                            EmotionsFragment.refresh();

                        } catch (IOException i) {
                            i.printStackTrace();
                        }*/

                        DataPoint dataPoint = new DataPoint(context);
                        dataPoint.setActivity("");
                        dataPoint.setAnger(Float.parseFloat(anger.toString()));
                        dataPoint.setFear(Float.parseFloat(fear.toString()));
                        dataPoint.setDisgust(Float.parseFloat(disgust.toString()));
                        dataPoint.setJoy(Float.parseFloat(joy.toString()));
                        dataPoint.setSadness(Float.parseFloat(sadness.toString()));
                        dataPoint.setSurprise(Float.parseFloat(surprise.toString()));
                        dataPoint.setAttention(Float.parseFloat(attention.toString()));

                        DataSource source = new DataSource(context);
                        source.open();
                        source.insertDataPoint(dataPoint);
                        source.close();

                        EmotionsFragment.refresh();


                    }
                } else if (response.get("status_message") != null) {
                    Log.d("KAIROS MEDIA", "status message: " + response.get("status_message").getAsString());
                    if (response.get("status_message").getAsString().equals("Analyzing")
                            && response.get("id") != null) {
                        getMedia(response.get("id").getAsString());
                    }
                } else if (response.get("id") != null) {
                    Log.d("KAIROS MEDIA", "information not returned");
                    getMedia(response.get("id").getAsString());
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

    private void deleteMedia(String id) {
        try {
            KairosHelper.deleteMedia(getApplicationContext(), id, kairosListener);
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
