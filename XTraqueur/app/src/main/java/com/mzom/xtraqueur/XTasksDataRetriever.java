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
    private static final String TASKS_DATA_FILE_NAME = "tasks_data.txt";

    // Google Drive payments data file name
    private static final String PAYMENTS_DATA_FILE_NAME = "payments_data.txt";

    private XTasksDataPackage dataPackage;

    static final int RETRIEVE_ALL_DATA = 0;
    private static final int RETRIEVE_TASKS_ONLY = 1;
    private static final int RETRIEVE_PAYMENTS_ONLY = 2;

    private final int dataToRetrieve;

    interface XTasksDataRetrieverListener {
        void onDataRetrieved(XTasksDataPackage dataPackage);
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
            getLatestTasksDataFromDrive();
        } else if (dataToRetrieve == RETRIEVE_PAYMENTS_ONLY) {
            getLatestPaymentsDataFromDrive();
        } else if (dataToRetrieve == RETRIEVE_ALL_DATA) {
            getLatestTasksDataFromDrive();
            getLatestPaymentsDataFromDrive();
        }


        Log.i(TAG, "Datapackage: " + "Tasks: " + dataPackage.getTasks() + ", Payments: " + dataPackage.getPayments());

        return dataPackage;
    }

    private void getLatestTasksDataFromDrive() {

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        appFolderTask.addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {

                Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, TASKS_DATA_FILE_NAME))
                        .build();


                Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(appFolderTask.getResult(), query);

                queryTask
                        .addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadata) {
                                DriveFile driveFile;
                                try {
                                    driveFile = metadata.get(0).getDriveId().asDriveFile();
                                    Log.i(TAG, "Google Drive API: tasks_data.txt acquired");
                                } catch (Exception e) {
                                    /*Log.e(TAG, "Google Drive API: Could not find tasks_data.txt, creating new");

                                    if (tasks == null) {
                                        tasks = new ArrayList<>();
                                    }

                                    updateTasksDataOnDrive(tasks);
                                    getLatestTasksDataFromDrive();*/
                                    return;
                                }

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

                                            ArrayList<XTask> tasks = new Gson().fromJson(builder.toString(), new TypeToken<ArrayList<XTask>>() {
                                            }.getType());
                                            dataPackage.setTasks(tasks);

                                            if (dataToRetrieve == RETRIEVE_TASKS_ONLY)
                                                mXTasksDataRetrieverListener.onDataRetrieved(dataPackage);
                                        }

                                        return mDriveResourceClient.discardContents(contents);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
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

    private void getLatestPaymentsDataFromDrive() {

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        appFolderTask.addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {

                Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, PAYMENTS_DATA_FILE_NAME))
                        .build();


                Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(appFolderTask.getResult(), query);

                queryTask
                        .addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadata) {
                                DriveFile driveFile;
                                try {
                                    driveFile = metadata.get(0).getDriveId().asDriveFile();
                                    Log.i(TAG, "Google Drive API: payments_data.txt acquired");
                                } catch (Exception e) {
                                    /*Log.e(TAG, "Google Drive API: Could not find payments_data.txt, creating new");

                                    if (payments == null) {
                                        payments = new ArrayList<>();
                                    }

                                    updatePaymentsDataOnDrive(payments);
                                    getLatestPaymentsDataFromDrive();*/
                                    return;
                                }

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

                                            ArrayList<XTaskPayment> payments = new Gson().fromJson(builder.toString(), new TypeToken<ArrayList<XTaskPayment>>() {
                                            }.getType());
                                            dataPackage.setPayments(payments);

                                            mXTasksDataRetrieverListener.onDataRetrieved(dataPackage);

                                            //updatePaymentsDataOnDevice(payments);

                                            //loadTasksFragment();
                                        }

                                        return mDriveResourceClient.discardContents(contents);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                            }
                                        });
                                metadata.release();
                            }
                        })


                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Google Drive API: Unable to retrieve payments data", e);
                            }
                        });
            }
        });
    }


    @Override
    protected void onPostExecute(XTasksDataPackage xTasksDataPackage) {
        super.onPostExecute(xTasksDataPackage);
    }
}
