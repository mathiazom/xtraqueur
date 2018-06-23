package com.mzom.xtraqueur;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


class XDataRetriever extends AsyncTask<String, Void, ArrayList<?>> {

    private static final String TAG = "XTQ-XDataRetriever";

    private final XDataFeedable mXDataFeedable;

    private final DriveResourceClient mDriveResourceClient;

    private ArrayList<?> retrievedData;

    interface XDataFeedable {

        DriveResourceClient getDriveResourceClient();

        void onTasksDataRetrieved(@NonNull ArrayList<XTask> tasks);

        void onPaymentsDataRetrieved(@NonNull ArrayList<XPayment> payments);

        void onDataNotFound(String dataFileName);
    }

    private static XDataFeedable getInterfaceFromContext(Context context){
        try{
            return (XDataFeedable) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement XDataFeedable");
        }
    }

    static void retrieveData(Context context, String... fileNames){

        if(context == null) return;

        XDataRetriever retriever = new XDataRetriever(context);
        retriever.execute(fileNames);
    }

    private XDataRetriever(Context context) {

        this.mXDataFeedable = getInterfaceFromContext(context);

        this.mDriveResourceClient = mXDataFeedable.getDriveResourceClient();
    }

    @Override
    protected ArrayList<?> doInBackground(String... strings) {

        for(String fileName : strings){
            if(fileName != null) getLatestDataFromDrive(fileName);
        }

        return retrievedData;
    }

    private void getLatestDataFromDrive(@NonNull final String dataFileName){

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        appFolderTask.addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {

                // Define search for files with title matching fileTitle parameter
                // Sort result to make sure the most recent data is used
                SortOrder sortOrder = new SortOrder.Builder().addSortDescending(SortableField.CREATED_DATE).build();
                Query query = new Query.Builder()
                        .setSortOrder(sortOrder)
                        .addFilter(Filters.eq(SearchableField.TITLE, dataFileName))
                        .build();


                // Start drive file search
                Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(appFolderTask.getResult(), query);
                queryTask
                        .addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadata) {

                                // Get drive file from best suited result (most recent data)
                                DriveFile driveFile;
                                try {
                                    driveFile = metadata.get(0).getDriveId().asDriveFile();

                                } catch (Exception e) {

                                    mXDataFeedable.onDataNotFound(dataFileName);

                                    return;
                                }

                                // Read and store file contents
                                Task<DriveContents> openFileTask = mDriveResourceClient.openFile(driveFile, DriveFile.MODE_READ_ONLY);
                                openFileTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                                    @Override
                                    public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                        DriveContents contents = task.getResult();

                                        try (BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(contents.getInputStream()))) {
                                            StringBuilder builder = new StringBuilder();
                                            String line;
                                            while ((line = reader.readLine()) != null) {
                                                builder.append(line).append("\n");
                                            }

                                            onJSONRetrieved(builder.toString(),dataFileName);
                                        }

                                        return mDriveResourceClient.discardContents(contents);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Google Drive API: Unable to retrieve task data", e);
                                            }
                                        });
                                metadata.release();
                            }
                        })


                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Google Drive API: Unable to retrieve task data", e);
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Google Drive API: Unable to retrieve app folder", e);
            }
        });


    }

    private void onJSONRetrieved(String json, String fileName){

        switch (fileName){
            case XDataConstants.TASKS_DATA_FILE_NAME:

                ArrayList<XTask> tasks = new Gson().fromJson(json, new TypeToken<ArrayList<XTask>>() {
                }.getType());

                if(tasks == null) return;

                retrievedData = tasks;

                mXDataFeedable.onTasksDataRetrieved(tasks);

                break;

            case XDataConstants.PAYMENTS_DATA_FILE_NAME:

                ArrayList<XPayment> payments = new Gson().fromJson(json, new TypeToken<ArrayList<XPayment>>() {
                }.getType());

                if(payments == null) return;

                retrievedData = payments;

                mXDataFeedable.onPaymentsDataRetrieved(payments);

                break;

        }

    }


    @Override
    protected void onPostExecute(ArrayList<?> retrievedData) {
        super.onPostExecute(retrievedData);
    }
}
