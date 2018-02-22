package com.mzom.xtraqueur;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;

class AsyncImageFromURL extends AsyncTask<String,Void,Bitmap> {

    private AsyncImageFromURLListener mAsyncImageFromURLListener;

    interface AsyncImageFromURLListener{
        void onTaskFinished(Bitmap bitmap);
    }

    AsyncImageFromURL(AsyncImageFromURLListener asyncImageFromURLListener){
        this.mAsyncImageFromURLListener = asyncImageFromURLListener;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        String url = strings[0];

        Bitmap imageBitmap = null;

        try {
            InputStream stream = new java.net.URL(url).openStream();
            imageBitmap = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        mAsyncImageFromURLListener.onTaskFinished(bitmap);
    }
}
