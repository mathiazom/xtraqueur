package com.mzom.xtraqueur;

import android.app.Activity;
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
        FragmentLoader.FragmentLoadable,
        SettingsFragment.SettingsFragmentListener,
        XDataUploader.XDataUploaderable {

    // Tag used for debugging
    private static final String TAG = "XTQ-SettingsActivity";

    private ArrayList<XTask> tasks;

    private ArrayList<XTaskCompletion> instantCompletions;

    // Google sign in account
    private GoogleSignInAccount mGoogleSignInAccount;
    private Bitmap mGoogleAccountPhoto;

    // Google Drive Clients
    private DriveResourceClient mDriveResourceClient;

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getGoogleSignIn();

        loadData(getIntent());
    }

    private void loadData(Intent intent) {

        // Tasks data
        final String tasksData = intent.getStringExtra("TASKS_DATA");

        ArrayList<XTask> intentTasks = new Gson().fromJson(tasksData, new TypeToken<ArrayList<XTask>>() {
        }.getType());

        if (intentTasks != null) tasks = intentTasks;

        // Instant completions data
        final String instantCompletionsData = intent.getStringExtra("INSTANT_COMPLETIONS_DATA");

        ArrayList<XTaskCompletion> intentInstantCompletions = new Gson().fromJson(instantCompletionsData, new TypeToken<ArrayList<XTaskCompletion>>() {
        }.getType());

        if (intentTasks != null) instantCompletions = intentInstantCompletions;


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
        new GoogleAccountPhotoRetriever(new GoogleAccountPhotoRetriever.AsyncURLImageRetrieverListener() {
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
        settingsFragment = SettingsFragment.newInstance(tasks, instantCompletions, mGoogleSignInAccount, mGoogleAccountPhoto);
        FragmentLoader.loadFragment(settingsFragment, this);
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

                        overridePendingTransition(0,0);
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

    @Override
    public int getFragmentFrameResId() {
        return R.id.settings_frame_layout;
    }

    @Override
    public void onFragmentBackPressed() {
        onBackPressed();
    }

    @Override
    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void dataUploading(HashMap<String, ArrayList<?>> dataMap) {

        if(dataMap.get(XDataConstants.TASKS_DATA_FILE_NAME) != null){
            tasks = (ArrayList<XTask>) dataMap.get(XDataConstants.TASKS_DATA_FILE_NAME);
        }
        if(dataMap.get(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME) != null){
            instantCompletions = (ArrayList<XTaskCompletion>) dataMap.get(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME);
        }

    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {

            //notifyTasksDataChanged();

            Intent resultIntent = new Intent();

            final String tasks_data = new Gson().toJson(tasks);
            resultIntent.putExtra("TASKS_DATA", tasks_data);

            final String instant_completions_data = new Gson().toJson(instantCompletions);
            resultIntent.putExtra("INSTANT_COMPLETIONS_DATA", instant_completions_data);

            setResult(Activity.RESULT_OK,resultIntent);

            // Finish activity if backstack is empty of fragments on back button press
            finish();

            return;
        }

        super.onBackPressed();
    }
}
