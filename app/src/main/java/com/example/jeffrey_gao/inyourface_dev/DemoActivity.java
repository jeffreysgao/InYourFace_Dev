package com.example.jeffrey_gao.inyourface_dev;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * This class is used to test & demo the photo analysis
 */
public class DemoActivity extends Activity implements Button.OnClickListener{
    /*
     * For testing/demoing analysis of photos - Jeff
     */
    public int photoType;
    public static final int RECOGNIZE_PHOTO = 0;
    public static final int ANALYZE_PHOTO = 1;
    public final String photoPath = "demo_photo.png";
    /*
     * End of test code - Jeff
     */


    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        Button verifyButton = (Button)findViewById(R.id.verify_button);
        verifyButton.setOnClickListener(this);
        Button analyzeButton = (Button)findViewById(R.id.analyze_button);
        analyzeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.verify_button) {
            photoType = RECOGNIZE_PHOTO;
        } else if (view.getId() == R.id.analyze_button) {
            photoType = ANALYZE_PHOTO;
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");

                savePic(photo);

                // Launch service to process photo taken
                Log.d("jeff", "launching service: " + photoType);
                Intent demoIntent;
                if (photoType == RECOGNIZE_PHOTO)
                    demoIntent = new Intent(this, RecognizeService.class);
                else
                    demoIntent = new Intent(this, AnalyzeService.class);

                demoIntent.putExtra(RecognizeService.FACE_IMAGE, photoPath);
                startService(demoIntent);

                finish();
            }
        }

        MainActivity.broughtFromForeground = false;
    }

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