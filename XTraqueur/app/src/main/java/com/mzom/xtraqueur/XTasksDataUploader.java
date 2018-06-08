package com.mzom.xtraqueur;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
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

class XTasksDataUploader extends AsyncTask<XTasksDataPackage, Void, Void> {

    private static final String TAG = "XTQ-XTasksDataUploader";

    private static final String TASKS_DATA_FILE_NAME = "tasks_data.txt";
    private static final String TASKS_DATA_SHARED_PREFS_PREFIX = "TASKS_DATA_";

    private static final String PAYMENTS_DATA_FILE_NAME = "payments_data.txt";
    private static final String PAYMENTS_DATA_SHARED_PREFS_PREFIX = "PAYMENTS_DATA_";

    private final GoogleSignInAccount mGoogleSignInAccount;
    private final DriveResourceClient mDriveResourceClient;

    private final SharedPreferences sharedPreferences;

    private final OnSuccessListener<DriveFile> onSuccessListener;


    XTasksDataUploader(@NonNull GoogleSignInAccount googleSignInAccount, @NonNull DriveResourceClient driveResourceClient, @NonNull Context context, @Nullable OnSuccessListener<DriveFile> onSuccessListener) {
        this.mGoogleSignInAccount = googleSignInAccount;
        this.mDriveResourceClient = driveResourceClient;
        this.sharedPreferences = context.getSharedPreferences("USER_DATA_ON_DEVICE", 0);
        this.onSuccessListener = onSuccessListener != null ? onSuccessListener : new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        };
    }

    XTasksDataUploader(@NonNull GoogleSignInAccount googleSignInAccount, @NonNull DriveResourceClient driveResourceClient, @NonNull Context context) {
        this(googleSignInAccount, driveResourceClient, context, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        });
    }

    @Override
    protected Void doInBackground(XTasksDataPackage... xTasksDataPackages) {

        final XTasksDataPackage xTasksDataPackage = xTasksDataPackages[0];

        ArrayList<XTask> tasks = xTasksDataPackage.getTasks();
        if (tasks != null) {
            updateTasksDataOnDrive(tasks);
        }

        ArrayList<XPayment> payments = xTasksDataPackage.getPayments();
        if (payments != null) {
            updatePaymentsDataOnDrive(payments);
        }

        return null;
    }

    private void updateTasksDataOnDrive(@NonNull ArrayList<XTask> tasks) {

        Log.i(TAG, "Google Drive API: Tasks data update started");

        final String tasks_data = new Gson().toJson(tasks);

        // Update tasks data locally
        updateDataFileOnDevice(tasks_data, TASKS_DATA_SHARED_PREFS_PREFIX);

        // Update tasks data on drive
        updateDataFileOnDrive(tasks_data, TASKS_DATA_FILE_NAME);

    }

    private void updatePaymentsDataOnDrive(@NonNull ArrayList<XPayment> payments) {

        final String payments_data = new Gson().toJson(payments);

        // Update data locally
        updateDataFileOnDevice(payments_data, PAYMENTS_DATA_SHARED_PREFS_PREFIX);

        // Update data on Google Drive
        updateDataFileOnDrive(payments_data, PAYMENTS_DATA_FILE_NAME);
    }


    // Update any json data to file with specified title
    private void updateDataFileOnDrive(@NonNull final String data, @NonNull final String fileTitle) {

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        // Save JSON of tasks data (XTask) to Google Drive when tasks (not XTask) have finished
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write(data);
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(fileTitle)
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();

                        return mDriveResourceClient.createFile(parent, changeSet, contents);
                    }
                })

                .addOnSuccessListener(onSuccessListener)

                // Delete any outdated files on drive
                .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {

                        // Remove any existing versions of file from drive
                        deleteOldestDriveFilesWithTitle(fileTitle);
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Google Drive API: Unable to upload " + fileTitle + " to drive", e);
                    }
                });

    }

    // Delete all files on drive with title equal to fileTitle parameter
    private void deleteOldestDriveFilesWithTitle(final String fileTitle) {

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        appFolderTask.addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {

                // Define search for files with title matching fileTitle parameter
                SortOrder sortOrder = new SortOrder.Builder().addSortDescending(SortableField.CREATED_DATE).build();
                Query query = new Query.Builder()
                        .setSortOrder(sortOrder)
                        .addFilter(Filters.eq(SearchableField.TITLE, fileTitle))
                        .build();

                // Start file search
                Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(appFolderTask.getResult(), query);
                queryTask.addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(final MetadataBuffer metadata) {

                        // Skip first result to avoid deleting most recent version
                        for (int i = 1; i < metadata.getCount(); i++) {
                            Metadata m = metadata.get(i);

                            // Get drive file from metadata
                            DriveFile driveFile;
                            try {
                                driveFile = m.getDriveId().asDriveFile();
                            } catch (Exception e) {
                                return;
                            }

                            // Permanently delete file from Google Drive
                            mDriveResourceClient.delete(driveFile)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Google Drive API: Outdated tasks data could not be deleted", e);
                                        }
                                    });
                        }
                    }
                });
            }
        });


    }


    private void updateDataFileOnDevice(@NonNull String data, @NonNull String sharedPrefsKeyPrefix) {
        String id = mGoogleSignInAccount.getId();
        String key = sharedPrefsKeyPrefix + id;
        sharedPreferences.edit().putString(key, data).apply();
        Log.i(TAG, "SharedPreferences: " + key + " updated");
    }

}
