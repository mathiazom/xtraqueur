package com.mzom.xtraqueur;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class XTasksDataUploader extends AsyncTask<XTasksDataPackage,Void,Void> {

    private static final String TAG = "XTQ-XTasksDataUploader";

    private GoogleSignInAccount mGoogleSignInAccount;

    private DriveResourceClient mDriveResourceClient;

    private SharedPreferences sharedPreferencesTasks;
    private SharedPreferences sharedPreferencesPayments;

    private OnSuccessListener<DriveFile> onSuccessListener;

    // Google Drive tasks data file name
    private static final String TASKS_DATA_FILE_NAME = "tasks_data.txt";

    // Google Drive payments data file name
    private static final String PAYMENTS_DATA_FILE_NAME = "payments_data.txt";

    XTasksDataUploader(GoogleSignInAccount googleSignInAccount, DriveResourceClient driveResourceClient, Context context, OnSuccessListener<DriveFile> onSuccessListener){
        this.mGoogleSignInAccount = googleSignInAccount;
        this.mDriveResourceClient = driveResourceClient;
        this.sharedPreferencesTasks = context.getSharedPreferences("TASKS_DATA_ON_DEVICE", 0);
        this.sharedPreferencesPayments = context.getSharedPreferences("TASKS_DATA_ON_DEVICE", 0);
        this.onSuccessListener = onSuccessListener;
    }

    XTasksDataUploader(GoogleSignInAccount googleSignInAccount, DriveResourceClient driveResourceClient, Context context){
        this(googleSignInAccount, driveResourceClient, context, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        });
    }

    @Override
    protected Void doInBackground(XTasksDataPackage... xTasksDataPackages) {

        ArrayList<XTask> tasks = xTasksDataPackages[0].getTasks();

        if(tasks != null){
            updateTasksDataOnDrive(tasks,onSuccessListener);
        }

        ArrayList<XTaskPayment> payments = xTasksDataPackages[0].getPayments();

        if(payments != null){
            updatePaymentsDataOnDrive(payments);
        }

        return null;
    }

    private void updateTasksDataOnDrive(ArrayList<XTask> tasks, OnSuccessListener<DriveFile> onSuccessListener) {

        Log.i(TAG, "Google Drive API: Tasks data update started");

        if (tasks == null) {
            Log.e(TAG, "Google Drive API: Tasks data was null, update terminated");
            return;
        }

        // Update data locally
        updateTasksDataOnDevice(tasks);

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        final String tasks_data = new Gson().toJson(tasks);

        // Save JSON of tasks data (XTask) to Google Drive when tasks (not XTask) have finished
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write(tasks_data);
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(TASKS_DATA_FILE_NAME)
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();

                        return mDriveResourceClient.createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(final DriveFile driveFile) {
                                Log.i(TAG, "Google Drive API: Tasks data on drive updated successfully");
                            }
                        })
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Google Drive API: Unable to update task data on drive", e);
                    }
                });
    }

    // Update tasks data in SharedPreferences (stored locally on the device)
    private void updateTasksDataOnDevice(ArrayList<XTask> tasks) {
        String json = new Gson().toJson(tasks);
        String id = mGoogleSignInAccount.getId();
        sharedPreferencesTasks.edit().putString("TASKS_DATA_" + id, json).apply();
        Log.i(TAG, "SharedPreferences: Local tasks data updated for id " + id);
        Log.i(TAG, json);
    }


    private void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments){
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        final String payments_data = new Gson().toJson(payments);

        // Save JSON of tasks data (XTask) to Google Drive when tasks (not XTask) have finished
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write(payments_data);
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(PAYMENTS_DATA_FILE_NAME)
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();

                        return mDriveResourceClient.createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(final DriveFile driveFile) {
                                Log.i(TAG, "Google Drive API: Payments data on drive updated successfully");
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Google Drive API: Unable to update payments data on drive", e);
                    }
                });
    }


}
