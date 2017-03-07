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
 *
 * Helper class to enable more specific calls to Kairos to retrieve emotion and attention information
 */

public class KairosHelper {

    /**
     * Uploads image to Kairos for analysis
     */
    public static void postMedia(Context context, String image, final KairosListener callback)
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
                if (errorResponse != null) {
                    String responseString = new String(errorResponse);
                    callback.onFail(responseString);
                } else {
                    callback.onFail("post media failed");
                }
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

        client.addHeader("app_id", context.getResources().getString(R.string.kairos_app_id));
        client.addHeader("app_key", context.getResources().getString(R.string.kairos_app_key));
        Log.d("jeff", "posting");
        client.post(context, "http://api.kairos.com/v2/media", requestParams, responseHandler);
    }

    /**
     * Gets detailed information on a previously uploaded image
     */
    public static void getMedia(Context context, String id, final KairosListener callback)
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
                if (errorResponse != null) {
                    String responseString = new String(errorResponse);
                    callback.onFail(responseString);
                } else {
                    callback.onFail("get media failed");
                }
            }

            public void onRetry(int retryNo) {
            }
        };

        client.addHeader("app_id", context.getResources().getString(R.string.kairos_app_id));
        client.addHeader("app_key", context.getResources().getString(R.string.kairos_app_key));
        client.get(context, "http://api.kairos.com/v2/media/" + id, responseHandler);
    }

    /*
     * Checks if user matches registered user
     */
    public static void recognize(Context context, Bitmap image, String galleryId, String selector, String threshold, String minHeadScale, String maxNumResults, final KairosListener callback)
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
                if (errorResponse != null) {
                    String responseString = new String(errorResponse);
                    callback.onFail(responseString);
                } else {
                    callback.onFail("Recognize failed");
                }
            }

            public void onRetry(int retryNo) {
            }
        };
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("image", base64FromBitmap(image));
        jsonParams.put("gallery_name", galleryId);
        if(selector != null) {
            jsonParams.put("selector", selector);
        }

        if(minHeadScale != null) {
            jsonParams.put("minHeadScale", minHeadScale);
        }

        if(threshold != null) {
            jsonParams.put("threshold", threshold);
        }

        if(maxNumResults != null) {
            jsonParams.put("max_num_results", maxNumResults);
        }

        StringEntity entity = new StringEntity(jsonParams.toString());
        client.addHeader("app_id", context.getResources().getString(R.string.kairos_app_id));
        client.addHeader("app_key", context.getResources().getString(R.string.kairos_app_key));
        client.post(context, "http://api.kairos.com/recognize", entity, "application/json", responseHandler);
    }

    /**
     * Deletes uploaded image once analysis is complete
     */
    public static void deleteMedia(Context context, String id, final KairosListener callback)
            throws JSONException, UnsupportedEncodingException{
        AsyncHttpClient client = new AsyncHttpClient();
        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
            public void onStart() {
            }

            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                callback.onSuccess(responseString);
            }

            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                if (errorResponse != null) {
                    String responseString = new String(errorResponse);
                    callback.onFail(responseString);
                } else {
                    callback.onFail("delete failed");
                }
            }

            public void onRetry(int retryNo) {
            }
        };

        client.addHeader("app_id", context.getResources().getString(R.string.kairos_app_id));
        client.addHeader("app_key", context.getResources().getString(R.string.kairos_app_key));
        client.delete(context, "http://api.kairos.com/v2/media/" + id, responseHandler);
    }

    /*
     * Retrieves summary data on an uploaded image
     */
    public static void analytics(Context context, String id, final KairosListener callback)
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
                if (errorResponse != null) {
                    String responseString = new String(errorResponse);
                    callback.onFail(responseString);
                } else {
                    callback.onFail("analytics failed");
                }
            }

            public void onRetry(int retryNo) {
            }
        };

        client.addHeader("app_id", context.getResources().getString(R.string.kairos_app_id));
        client.addHeader("app_key", context.getResources().getString(R.string.kairos_app_key));
        client.get(context, "http://api.kairos.com/v2/analytics/" + id, responseHandler);
    }

    protected static String base64FromBitmap(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, 0);
        return encoded;
    }
}
