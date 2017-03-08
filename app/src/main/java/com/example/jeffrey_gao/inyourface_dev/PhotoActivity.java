package com.example.jeffrey_gao.inyourface_dev;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Takes and saves the photos, triggered by "Enroll User" button on the
 * setting page.
 */

public class PhotoActivity extends AppCompatActivity {
    public static final String USER_DETAILS = "MyPrefs";
    private SharedPreferences myPrefs;
    private SharedPreferences.Editor myEditor;

    // Parameters for the camera function
    private static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
    private static final int REQUEST_CODE_SELECT_GALLERY = 1;
    // an integer to identify the activity in onActivityResult() as it returns
    private static final String SAVED_URI_1 = "saved_uri_1";        // prefix of unsaved URI
    private static final String SAVED_URI_2 = "saved_uri_2";
    private static final String SAVED_URI_3 = "saved_uri_3";

    // ------------ GLOBAL VARIABLES ----------------
    private Uri myImageCaptureUri;      // global image URI
    private ImageView mImageView1;       // global image view
    private ImageView mImageView2;
    private ImageView mImageView3;
//    private Uri imgUriAfterCropped1;     // if user updated the URI, then save it as
    // the new destination so that loadImage() will
    // pull out the correct image (after cropped)

    private Uri imgUriAfterCropped1;     // if user updated the URI, then save it as
    private Uri imgUriAfterCropped2;
    private Uri imgUriAfterCropped3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mImageView1 = (ImageView)findViewById(R.id.photoTaken1);
//        mImageView2 = (ImageView)findViewById(R.id.photoTaken2);
//        mImageView3 = (ImageView)findViewById(R.id.photoTaken3);


        // if the instance created is not empty
        if (savedInstanceState != null)
        {
            imgUriAfterCropped1 = savedInstanceState.getParcelable(SAVED_URI_1);
            imgUriAfterCropped2 = savedInstanceState.getParcelable(SAVED_URI_2);
            imgUriAfterCropped3 = savedInstanceState.getParcelable(SAVED_URI_3);

            if (imgUriAfterCropped1 == null)
                loadPicture(mImageView1);
            else
            {
                mImageView1.setImageURI(imgUriAfterCropped1);
            }

        }
        else
        {
            loadPicture(mImageView1);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_URI_1, imgUriAfterCropped1);
    }

    /*
     * When the "Change" button gets clicked
     */
    public void cameraIntent(View v)
    {
        // ask to take a picture, and pass that as the intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // setup temporary image path to save the newly added image
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        // add to this path as the new image URI
        myImageCaptureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, myImageCaptureUri);
        intent.putExtra("return-data", true);

        try
        {
            // getting a result from the activity: taking the picture
            startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
        }
        catch (ActivityNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void clickChangeButton1(View view)
    {
        cameraIntent(view);
    }

    public void clickDeleteButton(View view)
    {
        Intent clearIntent = new Intent(this, RegisterService.class);
            clearIntent.putExtra(RegisterService.ACTION, "clear");
            startService(clearIntent);
        mImageView1.setImageResource(0);
        mImageView1.invalidate();

        //TODO: NEED TO DELETE THE SAVED PHOTO FILE

    }


    /*
     * Upon camera activity finish
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK)        // unsuccessful results
            return;

        if(requestCode == REQUEST_CODE_TAKE_FROM_CAMERA)
        { // go crop the image
            mImageView1.setImageURI(myImageCaptureUri);
            savePicture();


            //delete galleries before registering new user
            //Note: can remove this later when we allow for multiple users/photos per user


            /*Intent clearIntent = new Intent(this, RegisterService.class);
            clearIntent.putExtra(RegisterService.ACTION, "clear");
            startService(clearIntent);*/


            //register user
            Intent registerIntent = new Intent(this, RegisterService.class);
            registerIntent.putExtra(RegisterService.ACTION, "register");
            registerIntent.putExtra(RegisterService.USER_NAME, "user");

            // settings user photo is saved as profile_photo.png
            registerIntent.putExtra(RegisterService.USER_FACE_IMAGE, getString(R.string.photo_name));
            startService(registerIntent);
        }

        MainActivity.broughtFromForeground = false;
    }

    // ----------- Helper Functions ---------------
    /*
     * Load the picture from where it is stored.
     */
    private void loadPicture(ImageView imageView)
    {
        try
        {
            FileInputStream fis = openFileInput(getString(R.string.photo_name));
            Bitmap bmap = BitmapFactory.decodeStream(fis);

            imageView.setImageBitmap(bmap);

//            mImageView1.setImageBitmap(bmap);
//            mImageView2.setImageBitmap(bmap);;

            fis.close();
        }
        catch (IOException e)   // if there is no pictures saved
        {
            // use default pictures
//            mImageView1.setImageResource(R.drawable.profile_photo);
            imageView.setImageResource(R.drawable.profile_photo);
        }
    }

    /*
     * Saved the picture to local cache.
     */
    private void savePicture()
    {
        mImageView1.buildDrawingCache();
        Bitmap bmap = mImageView1.getDrawingCache();

        try
        {
            FileOutputStream fOutStream = openFileOutput(getString(R.string.photo_name), MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);
            fOutStream.flush();
            fOutStream.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
