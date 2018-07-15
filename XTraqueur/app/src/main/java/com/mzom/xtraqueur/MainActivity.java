package com.mzom.xtraqueur;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        FragmentLoader.FragmentLoadable,
        XDataRetriever.XDataFeedable,
        XDataUploader.XDataUploaderable,
        TasksFragment.TasksFragmentListener {

    // Tag used for debugging
    private static final String TAG = "XTQ-MainActivity";

    private static final int DATA_CHANGED_IN_SETTINGS_REQUEST_CODE = 100;

    private TasksFragment mTasksFragment;

    // Google sign in account
    private GoogleSignInAccount mGoogleSignInAccount;

    // Google Drive Clients
    private DriveResourceClient mDriveResourceClient;

    // Data set storing all user tasks
    private ArrayList<XTask> tasks;
    private ArrayList<XPayment> payments;
    private ArrayList<XTaskCompletion> instantCompletions;

    private boolean firstLoadingOfTasksData = false;

    // Activity creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG,"MainActivity onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //showActivityProgressBar();

        // Retrieve all data saved before configuration changes (onSaveInstanceState)
        if (!restoreFromSavedInstanceState(savedInstanceState)) {
            // Get google sign in object and account photo from SignInActivity
            getGoogleSignIn();
        }

        // Set listener for changes to the BackStack
        onBackStackChangedListener();

    }

    // Save visible fragment and tasks data to restore after activity destruction
    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        // Save tasks data
        String tasks_data = new Gson().toJson(tasks);
        if (tasks_data != null && !tasks_data.equals("")) {
            outState.putString("tasks_data", tasks_data);
        }

        // Save payments data
        String payments_data = new Gson().toJson(payments);
        if (payments_data != null && !payments_data.equals("")) {
            outState.putString("payments_data", payments_data);
        }

        String instant_completions_data = new Gson().toJson(instantCompletions);
        if (instant_completions_data != null && !instant_completions_data.equals("")) {
            outState.putString("instant_completions_data", instant_completions_data);
        }

        // Save Google Sign-in Account
        if (mGoogleSignInAccount != null) {
            outState.putParcelable("googleSignInAccount", mGoogleSignInAccount);
        }

    }

    // Restore tasks data and fragments that were saved before activity destruction
    private boolean restoreFromSavedInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            return false;
        }

        // Restore tasks data
        String temp_tasks_data = savedInstanceState.getString("tasks_data", null);
        if (temp_tasks_data != null) {
            tasks = new Gson().fromJson(temp_tasks_data, new TypeToken<ArrayList<XTask>>() {
            }.getType());
        } else {
            return false;
        }

        // Restore payments data
        String temp_payments_data = savedInstanceState.getString("payments_data", null);
        if (temp_payments_data != null) {
            payments = new Gson().fromJson(temp_payments_data, new TypeToken<ArrayList<XPayment>>() {
            }.getType());
        } else {
            return false;
        }

        // Restore instant completions data
        String temp_instant_completions_data = savedInstanceState.getString("instant_completions_data", null);
        if (temp_instant_completions_data != null) {
            instantCompletions = new Gson().fromJson(temp_instant_completions_data, new TypeToken<ArrayList<XTaskCompletion>>() {
            }.getType());
        } else {
            return false;
        }

        // Restore Google Sign-in Account
        if (savedInstanceState.getParcelable("googleSignInAccount") != null) {

            mGoogleSignInAccount = savedInstanceState.getParcelable("googleSignInAccount");

            if (mGoogleSignInAccount != null) {
                mDriveResourceClient = Drive.getDriveResourceClient(this, mGoogleSignInAccount);
            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;

    }

    // Google account sign in from SignInActivity
    private void getGoogleSignIn() {

        // Get intent from SignInActivity
        Intent signInIntent = getIntent();

        if (signInIntent != null) {

            // Sign in account
            GoogleSignInAccount googleSignInAccount = signInIntent.getParcelableExtra("GOOGLE_SIGN_IN_ACCOUNT");
            if (googleSignInAccount != null) {
                mGoogleSignInAccount = googleSignInAccount;

                mDriveResourceClient = Drive.getDriveResourceClient(this, googleSignInAccount);

                Log.i(TAG,"Sign in process complete");

                getLatestTasksDataFromDrive();
            }
        }

    }


    ArrayList<String> dataNotFound;

    // Use XDataRetriever to get latest tasks and payments data
    private void getLatestTasksDataFromDrive() {

        if (mGoogleSignInAccount == null) {
            return;
        }

        final Context context = this;

        dataNotFound = new ArrayList<>();

        XDataRetriever.retrieveData(context, XDataConstants.TASKS_DATA_FILE_NAME,XDataConstants.PAYMENTS_DATA_FILE_NAME, XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME);

    }

    @Override
    public void onTasksDataRetrieved(@NonNull ArrayList<XTask> tasks) {
        this.tasks = tasks;

        loadTasksFragmentIfReady();
    }

    @Override
    public void onPaymentsDataRetrieved(@NonNull ArrayList<XPayment> payments) {
        this.payments = payments;

        loadTasksFragmentIfReady();
    }

    @Override
    public void onInstantCompletionsDataRetrieved(@NonNull ArrayList<XTaskCompletion> instantCompletions) {

        this.instantCompletions = instantCompletions;

        loadTasksFragmentIfReady();
    }

    private void loadTasksFragmentIfReady(){

        if(tasks == null || payments == null || instantCompletions == null) return;

        mTasksFragment = TasksFragment.newInstance(tasks,payments, instantCompletions);
        FragmentLoader.loadFragment(mTasksFragment,this);

        if(!firstLoadingOfTasksData){

            Log.i(TAG,"firstLoadingOfTasksData");
            firstLoadingOfTasksData = true;

            hideActivityProgressBar();


        }
    }

    @Override
    public void onDataNotFound(String dataFileName) {
        // TODO: Clean up no data found-message
        displayNoDataFoundDialog(dataFileName);

    }

    @Override
    public ArrayList<XTask> getTasks() {
        return tasks;
    }

    @Override
    public ArrayList<XTaskCompletion> getInstantCompletions() {
        return instantCompletions;
    }

    @Override
    public ArrayList<XPayment> getPayments() {
        return payments;
    }

    private void displayNoDataFoundDialog(final String dataFileName){

        final Context context = this;

        new AlertDialog.Builder(context).setTitle("No previous data found on drive").setMessage("Do you wish to continue and start fresh? " + dataFileName).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "Google Drive API: Could not find user data, creating blank file");

                switch (dataFileName){

                    case XDataConstants.TASKS_DATA_FILE_NAME:

                        tasks = new ArrayList<>();

                        // Create new file on drive
                        XDataUploader.uploadData(
                                dataFileName, tasks, context, new OnSuccessListener<DriveFile>() {
                                    @Override
                                    public void onSuccess(DriveFile driveFile) {
                                        getLatestTasksDataFromDrive();
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Snackbar.make(findViewById(R.id.main_frame_layout), R.string.error_could_not_setup_data_directory,Snackbar.LENGTH_LONG);
                                    }
                                });

                    case XDataConstants.PAYMENTS_DATA_FILE_NAME:

                        payments = new ArrayList<>();

                        // Create new file on drive
                        XDataUploader.uploadData(
                                dataFileName, payments, context, new OnSuccessListener<DriveFile>() {
                                    @Override
                                    public void onSuccess(DriveFile driveFile) {
                                        getLatestTasksDataFromDrive();
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Snackbar.make(findViewById(R.id.main_frame_layout), R.string.error_could_not_setup_data_directory,Snackbar.LENGTH_LONG);
                                    }
                                });

                    case XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME:

                        instantCompletions = new ArrayList<>();

                        // Create new file on drive
                        XDataUploader.uploadData(
                                dataFileName, instantCompletions, context, new OnSuccessListener<DriveFile>() {
                                    @Override
                                    public void onSuccess(DriveFile driveFile) {
                                        getLatestTasksDataFromDrive();
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Snackbar.make(findViewById(R.id.main_frame_layout), R.string.error_could_not_setup_data_directory,Snackbar.LENGTH_LONG);
                                    }
                                });


                }


            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();

    }


    @Override
    public void loadSettingsActivity() {

        // Show settings activity
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("GOOGLE_SIGN_IN_ACCOUNT", mGoogleSignInAccount);

        final String tasks_data = new Gson().toJson(tasks);
        final String instant_completions_data = new Gson().toJson(instantCompletions);

        intent.putExtra("TASKS_DATA", tasks_data);
        intent.putExtra("INSTANT_COMPLETIONS_DATA",instant_completions_data);

        startActivityForResult(intent,DATA_CHANGED_IN_SETTINGS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == DATA_CHANGED_IN_SETTINGS_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){

                // Instant completions data
                final String instantCompletionsData = data.getStringExtra("INSTANT_COMPLETIONS_DATA");

                ArrayList<XTaskCompletion> intentInstantCompletions = new Gson().fromJson(instantCompletionsData, new TypeToken<ArrayList<XTaskCompletion>>() {
                }.getType());

                if (intentInstantCompletions != null){
                    instantCompletions = intentInstantCompletions;
                    if(mTasksFragment != null){
                        mTasksFragment.setInstantCompletions(instantCompletions);
                    }


                }

                // Tasks data
                final String tasksDataJson = data.getStringExtra("TASKS_DATA");

                ArrayList<XTask> intentTasks = new Gson().fromJson(tasksDataJson, new TypeToken<ArrayList<XTask>>() {
                }.getType());

                if (intentTasks != null) {
                    tasks = intentTasks;
                    if(mTasksFragment != null){
                        mTasksFragment.setTasks(tasks);
                    }

                }

                FragmentLoader.loadFragment(TasksFragment.newInstance(tasks,payments,instantCompletions),this);



            }
        }

    }

    // Listen to BackStack changes to update TaskFragment
    private void onBackStackChangedListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                // Index of last entry in backStack
                int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
                // Do nothing if backStack is empty
                if (index < 0) {
                    return;
                }

                // Name of current fragment in use
                String fragName = getSupportFragmentManager().getBackStackEntryAt(index).getName();
                // Update TasksFragment when gone back to
                if (fragName.equals(TasksFragment.class.getSimpleName()) && mTasksFragment != null && mTasksFragment.isAdded()) {
                    mTasksFragment.setTasks(tasks);
                    mTasksFragment.loadTasks();
                }
            }
        });
    }

    @Override
    public void onFragmentBackPressed() {
        onBackPressed();
    }

    // Handle back presses
    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {

            // Finish activity if backstack is empty of fragments on back button press
            finish();

            return;
        }

        // Check if any visible fragment extend BaseEditFragment
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (!fragment.isVisible()) continue;

                if (fragment instanceof BaseEditFragment && ((BaseEditFragment) fragment).onBackPressed()) {
                    // Fragment handles back press, no further actions from this activity
                    return;
                }
            }
        }

        // Otherwise let super handle the back press
        super.onBackPressed();
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
        if(dataMap.get(XDataConstants.PAYMENTS_DATA_FILE_NAME) != null){
            payments = (ArrayList<XPayment>) dataMap.get(XDataConstants.TASKS_DATA_FILE_NAME);
        }
        if(dataMap.get(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME) != null){
            instantCompletions = (ArrayList<XTaskCompletion>) dataMap.get(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME);
        }

    }

    @Override
    public int getFragmentFrameResId() {
        return R.id.main_frame_layout;
    }

    // Activity ProgressBar
    private void showActivityProgressBar() {
        ConstraintLayout container = findViewById(R.id.main_activity_progress_bar_container);
        container.setVisibility(View.VISIBLE);
    }

    private void hideActivityProgressBar() {
        ConstraintLayout container = findViewById(R.id.main_activity_progress_bar_container);
        container.setVisibility(View.GONE);
    }


}
