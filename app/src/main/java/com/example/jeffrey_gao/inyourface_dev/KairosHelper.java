package com.example.jeffrey_gao.inyourface_dev;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.kairos.KairosListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by jeffreygao on 3/2/17.
 * Helper class to enable more specific calls to Kairos to retrieve emotion and attention information
 */

public class KairosHelper {
    public static void media(Context context, String image, final KairosListener callback)
            throws JSONException, UnsupportedEncodingException {
        AsyncHttpClient client = new AsyncHttpClient();
        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
            public void onStart() {
            }

            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                callback.onSuccess(responseString);
            }

            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                String responseString = new String(errorResponse);
                callback.onFail(responseString);
            }

            public void onRetry(int retryNo) {
            }
        };

        Log.d("jeff", context.getFilesDir() + "/" + image);
        File source = new File(context.getFilesDir() + "/" + image);
        RequestParams requestParams = new RequestParams();
        try {
            requestParams.put("source", source);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        client.addHeader("app_id", Globals.APP_ID);
        client.addHeader("app_key", Globals.APP_KEY);
        Log.d("jeff", "posting");
        client.post(context, "http://api.kairos.com/v2/media", requestParams, responseHandler);
    }

    protected static String base64FromBitmap(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, 0);
        return encoded;
    }
}
