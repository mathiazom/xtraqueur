package com.mzom.xtraqueur;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// AsyncTask that retrieves image bitmap from url string
// Uses an interface to return the result
class AsyncURLImageRetriever extends AsyncTask<String,Void,File> {

    private final static String TAG = "Xtraqueur-ImageFromUrl";

    // Where to store downloaded image file
    private final String path;

    private final AsyncURLImageRetrieverListener mAsyncURLImageRetrieverListener;

    // Interface to return result file
    interface AsyncURLImageRetrieverListener{
        void onTaskFinished(File file);
    }

    AsyncURLImageRetriever(AsyncURLImageRetrieverListener asyncURLImageRetrieverListener,String path){
        this.mAsyncURLImageRetrieverListener = asyncURLImageRetrieverListener;
        this.path = path;
    }

    @Override
    protected File doInBackground(String... strings) {

        // Get photo url from string array argument
        String url = strings[0];

        Bitmap imageBitmap;

        try {
            // Open stream with InputStream and decode result with BitmapFactory
            InputStream stream = new URL(url).openStream();
            imageBitmap = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            Log.e(TAG,"Exception while retrieving bitmap from url",e);
            return null;
        }

        // Get bitmap byte array to store on device
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,0,bos);
        byte[] bitmapdata = bos.toByteArray();

        File file = new File(path);

        // Store bitmap data inside file on device
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bitmapdata);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return reference to downloaded image file
        return file;

    }

    // Notify listener when task is complete
    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);

        mAsyncURLImageRetrieverListener.onTaskFinished(file);
    }
}
