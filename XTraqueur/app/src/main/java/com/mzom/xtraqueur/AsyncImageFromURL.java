package com.mzom.xtraqueur;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// AsyncTask that retrieves image bitmap from url string
// Uses an interface to return the result
class AsyncImageFromURL extends AsyncTask<String,Void,Bitmap> {

    private final static String TAG = "Xtraqueur-ImageFromUrl";

    private AsyncImageFromURLListener mAsyncImageFromURLListener;

    interface AsyncImageFromURLListener{
        void onTaskFinished(Bitmap bitmap);
    }

    AsyncImageFromURL(AsyncImageFromURLListener asyncImageFromURLListener){
        this.mAsyncImageFromURLListener = asyncImageFromURLListener;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        // Get photo url from string array argument
        String url = strings[0];

        Bitmap imageBitmap = null;

        try {
            // Open stream with InputStream and decode result with BitmapFactory
            InputStream stream = new URL(url).openStream();
            imageBitmap = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            Log.e(TAG,"Exception while retrieving bitmap from url",e);
        }

        // Return variable will be passed to onPostExecute()
        return imageBitmap;
    }

    // Notify listener when task is complete
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        mAsyncImageFromURLListener.onTaskFinished(bitmap);
    }
}
