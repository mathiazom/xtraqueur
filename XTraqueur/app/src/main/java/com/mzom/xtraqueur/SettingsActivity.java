package com.mzom.xtraqueur;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.util.ArrayList;


public class SettingsActivity extends AppCompatActivity implements SettingsFragment.SettingsFragmentListener {

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

        loadData();
    }

    private void loadData(){

       /* SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("TASKS_DATA_ON_DEVICE", 0);
        String json = sharedPreferences.getString("TASKS_DATA_" + mGoogleSignInAccount.getId(), null);

        if (json != null) {
            tasks = new Gson().fromJson(json, new TypeToken<ArrayList<XTask>>() {
            }.getType());
            loadSettingsFragment();
        } else {
            Log.e(TAG, "SharedPreferences: No tasks data on device");
        }*/

       ArrayList<XTask> intentTasks = (ArrayList<XTask>) getIntent().getSerializableExtra("TASKS_DATA");
       if(intentTasks == null) return;

       tasks = intentTasks;
       loadSettingsFragment();

    }

    private void getGoogleSignIn(){

        // Get intent from SignInActivity
        Intent signInIntent = getIntent();

        if(signInIntent != null){

            // Sign in account
            GoogleSignInAccount googleSignInAccount = signInIntent.getParcelableExtra("GOOGLE_SIGN_IN_ACCOUNT");
            if(googleSignInAccount != null){
                mGoogleSignInAccount = googleSignInAccount;

                mDriveResourceClient = Drive.getDriveResourceClient(this,googleSignInAccount);
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
        new AsyncURLImageRetriever(new AsyncURLImageRetriever.AsyncURLImageRetrieverListener() {
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

    private void updateTasksDataOnDrive(){
        XTasksDataUploader dataUploader = new XTasksDataUploader(mGoogleSignInAccount, mDriveResourceClient, getApplicationContext(), new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                loadSettingsFragment();
            }
        });
        XTasksDataPackage newDataPackage = new XTasksDataPackage(tasks,null);
        dataUploader.execute(newDataPackage);
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

                        Intent intent = new Intent(context,SignInActivity.class);
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

    private void notifyTasksDataChanged(){
        Intent intent = new Intent();
        intent.setAction("TASKS_DATA_CHANGED_IN_SETTINGS");
        intent.putExtra("TASKS_DATA",tasks);
        sendBroadcast(intent);
    }



    @Override
    public void onFragmentBackPressed() {
        super.onBackPressed();
    }

}
