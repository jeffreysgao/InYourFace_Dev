package com.example.jeffrey_gao.inyourface_dev;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jeffreygao on 3/3/17.
 *  Methods used for testing the Services for communication with Kairos
 */

public class KairosTest {
    public static void testAnalyze(Context context) {
        createProfPic(context);
        Intent intent = new Intent(context, AnalyzeService.class);
        intent.putExtra(AnalyzeService.FACE_IMAGE, "test_photo.png");
        context.startService(intent);
    }

    public static void createProfPic(Context context) {
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.liz);
        try {
            FileOutputStream f = context.openFileOutput("test_photo.png", context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, f);
            f.flush();
            f.close();
            Log.d("jeff", "test_pic created");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
