package com.example.jeffrey_gao.inyourface_dev;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class DemoActivity extends Activity implements Button.OnClickListener{

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        Button verifyButton = (Button) findViewById(R.id.verify_button);
        verifyButton.setOnClickListener(this);



        /* how to delete all galleries */
//        Intent registerIntent = new Intent(this, RegisterService.class);
//        registerIntent.putExtra(RegisterService.ACTION, "clear");
//        startService(registerIntent);

        /* how to register a user */
//        Intent registerIntent = new Intent(this, RegisterService.class);
//        registerIntent.putExtra(RegisterService.ACTION, "register");
//        registerIntent.putExtra(RegisterService.USER_NAME, "liz");
//        // settings user photo is saved as profile_photo.png
//        // need to figure out what the temp URI is for the verify photo
//        registerIntent.putExtra(RegisterService.USER_FACE_IMAGE, "profile_photo.png");
//        startService(registerIntent);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.verify_button) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 1);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");

                createProfPic(photo);

                /* how to verify a user */
                Intent recognizeIntent = new Intent(this, RecognizeService.class);
                // change profile_photo.png to the path of the temp photo taken
                recognizeIntent.putExtra(RecognizeService.FACE_IMAGE, "verify_photo.png");
                startService(recognizeIntent);

                finish();
            }
        }
    }

    private void createProfPic(Bitmap image) {
        try {
            FileOutputStream f = openFileOutput("verify_photo.png", MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, f);
            f.flush();
            f.close();
            Log.d("jeff", "prof_pic created");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}