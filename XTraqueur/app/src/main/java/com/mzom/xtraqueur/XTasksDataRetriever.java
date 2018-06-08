package com.mzom.xtraqueur;

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


class XTasksDataRetriever extends AsyncTask<DriveResourceClient, Void, XTasksDataPackage> {

    private static final String TAG = "XTQ-XTasksDataRetriever";

    private final XTasksDataRetrieverListener mXTasksDataRetrieverListener;

    private DriveResourceClient mDriveResourceClient;

    // Google Drive tasks data file name
    static final String TASKS_DATA_FILE_NAME = "tasks_data.txt";

    // Google Drive payments data file name
    static final String PAYMENTS_DATA_FILE_NAME = "payments_data.txt";

    private XTasksDataPackage dataPackage;

    static final int RETRIEVE_ALL_DATA = 0;
    private static final int RETRIEVE_TASKS_ONLY = 1;
    private static final int RETRIEVE_PAYMENTS_ONLY = 2;

    private final int dataToRetrieve;

    interface XTasksDataRetrieverListener {
        void onDataRetrieved(XTasksDataPackage dataPackage);
        void updatePaymentsDataOnDrive(ArrayList<XPayment> payments);
        void onDataNotFound(String fileName);
    }

    XTasksDataRetriever(int dataToRetrieve, XTasksDataRetrieverListener xTasksDataRetrieverListener) {
        this.dataToRetrieve = dataToRetrieve;
        this.mXTasksDataRetrieverListener = xTasksDataRetrieverListener;
    }

    @Override
    protected XTasksDataPackage doInBackground(DriveResourceClient... driveResourceClients) {

        dataPackage = new XTasksDataPackage();

        mDriveResourceClient = driveResourceClients[0];

        if (dataToRetrieve == RETRIEVE_TASKS_ONLY) {
            getLatestDataFromDrive(TASKS_DATA_FILE_NAME);
        }

        else if (dataToRetrieve == RETRIEVE_PAYMENTS_ONLY) {
            getLatestDataFromDrive(PAYMENTS_DATA_FILE_NAME);
        }

        else if (dataToRetrieve == RETRIEVE_ALL_DATA) {
            getLatestDataFromDrive(TASKS_DATA_FILE_NAME);
            getLatestDataFromDrive(PAYMENTS_DATA_FILE_NAME);
        }

        return dataPackage;
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
                                    mXTasksDataRetrieverListener.onDataNotFound(dataFileName);
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
        });


    }

    private void onJSONRetrieved(String json, String fileName){

        switch (fileName){
            case TASKS_DATA_FILE_NAME:

                ArrayList<XTask> tasks = new Gson().fromJson(json, new TypeToken<ArrayList<XTask>>() {
                }.getType());
                dataPackage.setTasks(tasks);

                if (dataToRetrieve == RETRIEVE_TASKS_ONLY)
                    mXTasksDataRetrieverListener.onDataRetrieved(dataPackage);

                break;

            case PAYMENTS_DATA_FILE_NAME:

                ArrayList<XPayment> payments = new Gson().fromJson(json, new TypeToken<ArrayList<XPayment>>() {
                }.getType());
                dataPackage.setPayments(payments);

                mXTasksDataRetrieverListener.onDataRetrieved(dataPackage);

                break;

        }

    }


    @Override
    protected void onPostExecute(XTasksDataPackage xTasksDataPackage) {
        super.onPostExecute(xTasksDataPackage);
    }
}
