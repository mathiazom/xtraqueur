package com.mzom.xtraqueur;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class SettingsActivity extends AppCompatActivity
        implements
        SettingsFragment.SettingsFragmentListener,
        XDataUploader.XDataUploaderable {

    // Tag used for debugging
    private static final String TAG = "XTQ-SettingsActivity";

    private ArrayList<XTask> tasks;

    // Google sign in account
    private GoogleSignInAccount mGoogleSignInAccount;
    private Bitmap mGoogleAccountPhoto;

    // Google Drive Clients
    private DriveResourceClient mDriveResourceClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getGoogleSignIn();

        loadData(getIntent());
    }

    private void loadData(Intent intent) {

        final String json = intent.getStringExtra("TASKS_DATA");

        ArrayList<XTask> intentTasks = new Gson().fromJson(json, new TypeToken<ArrayList<XTask>>() {
        }.getType());

        if (intentTasks == null) return;

        tasks = intentTasks;
        loadSettingsFragment();

    }

    private void getGoogleSignIn() {

        // Get intent from SignInActivity
        Intent signInIntent = getIntent();

        if (signInIntent != null) {

            // Sign in account
            GoogleSignInAccount googleSignInAccount = signInIntent.getParcelableExtra("GOOGLE_SIGN_IN_ACCOUNT");
            if (googleSignInAccount != null) {
                mGoogleSignInAccount = googleSignInAccount;

                mDriveResourceClient = Drive.getDriveResourceClient(this, googleSignInAccount);
            }

            // Account photo
            getGoogleAccountPhoto();

        }

    }

    private void getGoogleAccountPhoto() {

        // Google Account Photo file path
        String path = getFilesDir().getPath() + File.separator + mGoogleSignInAccount.getDisplayName() + "_Photo.png";

        // Use path to check if account photo is stored on device
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null) {
            mGoogleAccountPhoto = bitmap;
            return;
        }

        // Google Account photo url
        Uri photoUrl = mGoogleSignInAccount.getPhotoUrl();
        if (photoUrl == null) {
            Log.e(TAG, "Google Account Photo url is null");
            return;
        }

        // Download bitmap with AsyncTask (from url) to store account photo on device
        new GAccountPhotoRetriever(new GAccountPhotoRetriever.AsyncURLImageRetrieverListener() {
            @Override
            public void onTaskFinished(File file) {
                if (file == null) {
                    Log.e(TAG, "Could not download Google Account Photo");
                    return;
                }

                Log.i(TAG, "Downloaded Google Account Photo to " + file.getAbsolutePath());

                // Get bitmap from downloaded file
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                if (bitmap != null) {
                    mGoogleAccountPhoto = bitmap;
                }
            }
        }, path).execute(photoUrl.toString());
    }

    // Fragment to change app settings
    private void loadSettingsFragment() {
        SettingsFragment mSettingsFragment = SettingsFragment.newInstance(tasks, mGoogleSignInAccount, mGoogleAccountPhoto);
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_frame_layout, mSettingsFragment).commit();
    }

    private void updateTasksDataOnDrive() {

        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME,tasks, this, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                loadSettingsFragment();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Snackbar.make(findViewById(R.id.main_frame_layout), "Error: Could not update tasks data", Snackbar.LENGTH_LONG);
            }
        });
    }


    @Override
    // Sign user out of Google account
    public void signOut() {

        final Context context = this;

        GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Restart activity to complete Google account sign out

                        Intent intent = new Intent(context, SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    // Build a Google SignIn client
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .requestId()
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }


    // Danger zone: Delete all completions
    @Override
    public void deleteAllCompletions() {

        // Delete all completions from tasks data
        for (XTask t : tasks) {
            t.setCompletions(new ArrayList<XTaskCompletion>());
        }

        // Upload updated tasks data
        updateTasksDataOnDrive();

        notifyTasksDataChanged();
    }

    // Danger zone: Delete all tasks
    @Override
    public void deleteAllTasks() {
        tasks = new ArrayList<>();

        // Upload updated tasks data
        updateTasksDataOnDrive();

        notifyTasksDataChanged();
    }

    private void notifyTasksDataChanged() {

        Intent intent = new Intent();
        intent.setAction("TASKS_DATA_CHANGED_IN_SETTINGS");

        final String tasks_data = new Gson().toJson(tasks);
        intent.putExtra("TASKS_DATA", tasks_data);

        sendBroadcast(intent);
    }


    @Override
    public void onFragmentBackPressed() {
        super.onBackPressed();
    }

    @Override
    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    @Override
    public void dataUploading(HashMap<String, ArrayList<?>> dataMap) {

        if(dataMap.get(XDataConstants.TASKS_DATA_FILE_NAME) != null){
            tasks = (ArrayList<XTask>) dataMap.get(XDataConstants.TASKS_DATA_FILE_NAME);
        }

    }
}
