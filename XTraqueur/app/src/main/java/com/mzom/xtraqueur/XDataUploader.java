package com.mzom.xtraqueur;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

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
import java.util.HashMap;
import java.util.Map;

class XDataUploader extends AsyncTask<HashMap<String,ArrayList<?>>, Void, Void> {

    private static final String TAG = "XTQ-XDataUploader";

    private final DriveResourceClient mDriveResourceClient;

    private final OnSuccessListener<DriveFile> onSuccessListener;
    private final OnFailureListener onFailureListener;

    interface XDataUploaderable {

        DriveResourceClient getDriveResourceClient();

        void dataUploading(HashMap<String,ArrayList<?>> dataMap);
    }

    static void uploadData(@NonNull String fileName,@NonNull ArrayList<?> data, Context context){
        uploadData(fileName,data, context, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    static void uploadData(@NonNull String fileName,@NonNull ArrayList<?> data, Context context,@NonNull OnSuccessListener<DriveFile> onSuccessListener){
        uploadData(fileName,data, context, onSuccessListener, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    static void uploadData(@NonNull String fileName,@NonNull ArrayList<?> data, Context context, @NonNull OnSuccessListener<DriveFile> onSuccessListener, @NonNull OnFailureListener onFailureListener){

        if(context == null) return;

        HashMap<String,ArrayList<?>> dataMap = new HashMap<>();
        dataMap.put(fileName,data);

        XDataUploader uploader = new XDataUploader(context, dataMap, onSuccessListener, onFailureListener);
        uploader.execute(dataMap);


    }

    private XDataUploader(@NonNull Context context, @NonNull HashMap<String,ArrayList<?>> dataMap, @NonNull OnSuccessListener<DriveFile> onSuccessListener, @NonNull OnFailureListener onFailureListener){

        final XDataUploaderable mXTaskDataMapUploaderInterface = getActivityAsInterface(context);

        mXTaskDataMapUploaderInterface.dataUploading(dataMap);

        this.mDriveResourceClient = mXTaskDataMapUploaderInterface.getDriveResourceClient();

        this.onSuccessListener = onSuccessListener;

        this.onFailureListener = onFailureListener;
    }

    private XDataUploaderable getActivityAsInterface(Context activity){

        try {
            return (XDataUploaderable) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement XDataUploaderable");
        }
    }

    @Override
    protected Void doInBackground(HashMap<String,ArrayList<?>>... hashMaps) {

        final HashMap<String,ArrayList<?>> map = hashMaps[0];

        for(Map.Entry<String,ArrayList<?>> entry : map.entrySet()){

            final String entryKey = entry.getKey();
            final ArrayList<?> entryValue = entry.getValue();

            if(entryKey != null && entryValue != null){

                String json = new Gson().toJson(entryValue);

                updateDataFileOnDrive(json,entryKey);

            }

        }

        return null;
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
                .addOnFailureListener(onFailureListener)

                // Delete any outdated files on drive
                .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {

                        Log.i(TAG, "Google Drive API: " + fileTitle + " updated successfully");

                        // Remove any existing versions of file from drive
                        deleteOutdatedDriveFiles(fileTitle);
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
    private void deleteOutdatedDriveFiles(final String fileTitle) {

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

}
