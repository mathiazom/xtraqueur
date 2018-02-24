package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TasksFragment.TasksFragmentListener, SummaryFragment.SummaryFragmentListener, NewTaskFragment.NewTaskFragmentListener, EditTaskFragment.EditTaskFragmentListener, SettingsFragment.SettingsFragmentListener, TimelineFragment.TimelineFragmentListener, WelcomeFragment.WelcomeFragmentListener {

    // Data set storing all the tasks
    private ArrayList<XTask> tasks;

    // Fragment fields
    private TasksFragment tasksFragment;
    private SummaryFragment summaryFragment;
    private EditTaskFragment editTaskFragment;
    private NewTaskFragment newTaskFragment;
    private SettingsFragment settingsFragment;
    private TimelineFragment timelineFragment;
    private WelcomeFragment welcomeFragment;

    // Fragment identifiers used by the BackStack and instance saving
    private static final String TASKS_FRAGMENT_NAME = "TasksFragment";
    private static final String SUMMARY_FRAGMENT_NAME = "SummaryFragment";
    private static final String NEWTASK_FRAGMENT_NAME = "NewTaskFragment";
    private static final String EDITTASK_FRAGMENT_NAME = "EditTaskFragment";
    private static final String SETTINGS_FRAGMENT_NAME = "SettingsFragment";
    private static final String TIMELINE_FRAGMENT_NAME = "TimelineFragment";
    private static final String WELCOME_FRAGMENT_NAME = "WelcomeFragment";

    // Google Drive tasks data file name
    private static final String TASKS_DATA_FILE_NAME = "tasks_data.txt";

    // Tag used for debugging
    private static final String TAG = "Xtraqueur-MainActivity";


    // Google Drive SignIn request code
    private static final int REQUEST_CODE_SIGN_IN = 1337;

    // Google sign in account
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;
    private Bitmap mGoogleAccountPhoto;

    // Google Drive Clients
    private DriveResourceClient mDriveResourceClient;
    private DriveClient mDriveClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve all data saved before configuration changes (onSaveInstanceState)
        boolean cont = handleSavedInstanceState(savedInstanceState);

        // Set listener for changes to the backStack
        onBackStackChangedListener();

        // Check if any Google accounts are signed in to the app
        boolean signedIn = GoogleSignIn.getLastSignedInAccount(this) != null;

        if (!signedIn) {
            // No Google accounts signed in, load WelcomeFragment to let the user sign in
            Log.i(TAG, "No signed in account, loading welcome-page");
            loadWelcomeFragment();
        }else if(cont){
            // Google Drive API client sign in
            signIn();
        }
    }

    // Restore tasks data and fragments that were saved before activity destruction
    private boolean handleSavedInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            Log.i(TAG, "No saved instance states");
            return true;
        }

        boolean cont = false;

        // Get tasks Arraylist from savedInstanceState
        String temp_tasks_data = savedInstanceState.getString("tasks_data", null);
        if (temp_tasks_data != null) {
            tasks = new Gson().fromJson(temp_tasks_data, new TypeToken<ArrayList<XTask>>() {
            }.getType());
        }

        if (savedInstanceState.getParcelable("googleSignInAccount") != null) {
            mGoogleSignInAccount = savedInstanceState.getParcelable("googleSignInAccount");
            mGoogleSignInClient = buildGoogleSignInClient();
        }

        if (getSupportFragmentManager().getFragment(savedInstanceState, TASKS_FRAGMENT_NAME) != null) {
            Log.i(TAG, "SavedInstanceState: " + TASKS_FRAGMENT_NAME);
            tasksFragment = (TasksFragment) getSupportFragmentManager().getFragment(savedInstanceState, TASKS_FRAGMENT_NAME);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, tasksFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, SUMMARY_FRAGMENT_NAME) != null) {
            Log.i(TAG, "SavedInstanceState: " + SUMMARY_FRAGMENT_NAME);
            summaryFragment = (SummaryFragment) getSupportFragmentManager().getFragment(savedInstanceState, SUMMARY_FRAGMENT_NAME);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, summaryFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EDITTASK_FRAGMENT_NAME) != null) {
            Log.i(TAG, "SavedInstanceState: " + EDITTASK_FRAGMENT_NAME);
            editTaskFragment = (EditTaskFragment) getSupportFragmentManager().getFragment(savedInstanceState, EDITTASK_FRAGMENT_NAME);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, editTaskFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, NEWTASK_FRAGMENT_NAME) != null) {
            Log.i(TAG, "SavedInstanceState: " + NEWTASK_FRAGMENT_NAME);
            newTaskFragment = (NewTaskFragment) getSupportFragmentManager().getFragment(savedInstanceState, NEWTASK_FRAGMENT_NAME);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, newTaskFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, SETTINGS_FRAGMENT_NAME) != null) {
            Log.i(TAG, "SavedInstanceState: " + SETTINGS_FRAGMENT_NAME);
            settingsFragment = (SettingsFragment) getSupportFragmentManager().getFragment(savedInstanceState, SETTINGS_FRAGMENT_NAME);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, settingsFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, TIMELINE_FRAGMENT_NAME) != null) {
            Log.i(TAG, "SavedInstanceState: " + TIMELINE_FRAGMENT_NAME);
            timelineFragment = (TimelineFragment) getSupportFragmentManager().getFragment(savedInstanceState, TIMELINE_FRAGMENT_NAME);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, timelineFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, WELCOME_FRAGMENT_NAME) != null) {
            Log.i(TAG, "SavedInstanceState: " + WELCOME_FRAGMENT_NAME);
            welcomeFragment = (WelcomeFragment) getSupportFragmentManager().getFragment(savedInstanceState, WELCOME_FRAGMENT_NAME);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, welcomeFragment).commit();
        } else {
            cont = true;
        }

        return cont;

    }


    // Start Google Drive sign in
    @Override
    public void signIn() {
        Log.i(TAG, "Google Drive API: Start sign in");
        mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
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

    // Handle Google Drive sign in request
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:

                Log.i(TAG, "Google Drive API: Sign in request code");

                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Google Drive API: Signed in successfully.");

                    // Use the last signed in account
                    mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
                    if (mGoogleSignInAccount == null) {
                        Log.e(TAG, "Google Drive API: GoogleSignInAccount is null");
                        return;
                    }

                    Log.i(TAG, "Google Drive API: Account: " + mGoogleSignInAccount.getDisplayName() + ", " + mGoogleSignInAccount.getEmail());

                    mDriveClient = Drive.getDriveClient(this, mGoogleSignInAccount);

                    mDriveResourceClient = Drive.getDriveResourceClient(this, mGoogleSignInAccount);

                    // Sync required after app reinstalling
                    mDriveClient.requestSync()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "Google Drive API: Sync successful");

                                    if(welcomeFragment != null && welcomeFragment.isAdded()){
                                        getSupportFragmentManager().beginTransaction().remove(welcomeFragment).commit();
                                    }

                                    getLatestTasksDataFromDrive();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Google Drive API: Sync failed", e);
                                    Toast.makeText(MainActivity.this, "Google Drive API request rate limit exceeded, getting local data", Toast.LENGTH_LONG).show();

                                    getLatestTasksDataFromDevice();
                                }
                            });
                } else {
                    Log.e(TAG, "Google Drive API: Sign in failed. Result code: " + resultCode);
                }
                break;
        }
    }

    // Get task data from Google Drive account
    private void getLatestTasksDataFromDrive() {

        showActivityProgressBar();

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
                                    Log.e(TAG, "Google Drive API: Could not find tasks_data.txt, creating new");

                                    if (tasks == null) {
                                        tasks = new ArrayList<>();
                                    }

                                    updateTasksDataOnDrive(tasks);
                                    getLatestTasksDataFromDrive();
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

                                            tasks = new Gson().fromJson(builder.toString(), new TypeToken<ArrayList<XTask>>() {
                                            }.getType());

                                            loadTasksFragment();
                                        }

                                        hideActivityProgressBar();

                                        return mDriveResourceClient.discardContents(contents);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    // Update tasks data on Google Drive account
    @Override
    public void updateTasksDataOnDrive(ArrayList<XTask> tasks) {
        updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        });
    }

    public void updateTasksDataOnDrive(ArrayList<XTask> tasks, OnSuccessListener<DriveFile> onSuccessListener) {

        if (mDriveResourceClient == null || mDriveClient == null) return;

        Log.i(TAG, "Google Drive API: Tasks data update started");

        if (tasks == null) {
            Log.e(TAG, "Google Drive API: Tasks data was null, update terminated");
            return;
        }

        // Update data locally
        updateTasksDataOnDevice(tasks);

        this.tasks = tasks;

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
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(final DriveFile driveFile) {
                                Log.i(TAG, "Google Drive API: Tasks data on drive updated successfully");
                            }
                        })
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Google Drive API: Unable to update task data on drive", e);
                    }
                });
    }

    // Danger zone: Delete all completions
    @Override
    public void deleteAllCompletions() {
        for (XTask t : tasks) {
            t.setCompletionsList(new ArrayList<Long>());
        }

        updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                settingsFragment = SettingsFragment.newInstance(tasks, mGoogleSignInAccount, mGoogleAccountPhoto);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, settingsFragment).commit();
            }
        });
    }

    // Danger zone: Delete all tasks
    @Override
    public void deleteAllTasks() {
        tasks = new ArrayList<>();

        updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                settingsFragment = SettingsFragment.newInstance(tasks, mGoogleSignInAccount, mGoogleAccountPhoto);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, settingsFragment).commit();
            }
        });


    }

    @Override
    public void signOut() {
        if (mGoogleSignInClient == null) {
            Log.e(TAG, "Sign in client is null");
            return;
        }

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        restartActivity();
                    }
                });
    }

    public void restartActivity() {
        Intent mIntent = getIntent();
        finish();
        startActivity(mIntent);
    }


    // Update tasks data in SharedPreferences (stored locally on the device)
    private void updateTasksDataOnDevice(ArrayList<XTask> tasks) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("TASKS_DATA_ON_DEVICE", 0);
        String json = new Gson().toJson(tasks);
        String id = mGoogleSignInAccount.getId();
        sharedPreferences.edit().putString("TASKS_DATA_" + id, json).apply();
        Log.i(TAG, "SharedPreferences: Local tasks data updated for id " + id);
    }

    // Get task data from SharedPreferences (stored locally on the device)
    private void getLatestTasksDataFromDevice() {
        Log.i(TAG, "SharedPreferences: Getting tasks data from device");
        SharedPreferences sharedPreferences = this.getSharedPreferences("TASKS_DATA_ON_DEVICE", 0);
        String json = sharedPreferences.getString("TASKS_DATA_" + mGoogleSignInAccount.getId(), null);

        if (json != null) {
            tasks = new Gson().fromJson(json, new TypeToken<ArrayList<XTask>>() {
            }.getType());
            loadTasksFragment();
        } else {
            Log.e(TAG, "SharedPreferences: No tasks data on device");
        }
    }

    // Update Google sign in account photo
    @Override
    public void setAccountPhoto(Bitmap bitmap) {
        this.mGoogleAccountPhoto = bitmap;
    }

    @Override
    public void popBackStackFromFragment() {
        onBackPressed();
    }

    // Fragment listing all the tasks
    // Each task has buttons to add and subtract completions
    // Clicking on a task item will open a EditTaskFragment with the task as an argument
    @Override
    public void loadTasksFragment() {
        tasksFragment = TasksFragment.newInstance(tasks);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, tasksFragment).addToBackStack(TASKS_FRAGMENT_NAME).commit();
    }

    // Fragment giving an overview of the tasks values and the total value
    @Override
    public void loadSummaryFragment() {
        summaryFragment = SummaryFragment.newInstance(tasks);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, summaryFragment).addToBackStack(SUMMARY_FRAGMENT_NAME).commit();
    }

    // Fragment to create new tasks
    @Override
    public void loadNewTaskFragment(boolean fromPreEdit) {
        newTaskFragment = NewTaskFragment.newInstance(tasks, getRandomMatColor("700"), fromPreEdit);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top).replace(R.id.main_frame_layout, newTaskFragment).addToBackStack(NEWTASK_FRAGMENT_NAME).commit();
    }

    // Fragment to edit the task passed in as an argument
    @Override
    public void loadEditTaskFragment(XTask task, int index) {
        editTaskFragment = EditTaskFragment.newInstance(tasks, task, index);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame_layout, editTaskFragment).addToBackStack(EDITTASK_FRAGMENT_NAME).commit();
    }

    // Fragment to change app settings (not developed yet)
    @Override
    public void loadSettingsFragment() {
        settingsFragment = SettingsFragment.newInstance(tasks, mGoogleSignInAccount, mGoogleAccountPhoto);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom).replace(R.id.main_frame_layout, settingsFragment).addToBackStack(SETTINGS_FRAGMENT_NAME).commit();
    }

    @Override
    public void loadTimelineFragment() {
        timelineFragment = TimelineFragment.newInstance(tasks);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom).replace(R.id.main_frame_layout, timelineFragment).addToBackStack(TIMELINE_FRAGMENT_NAME).commit();
    }

    public void loadWelcomeFragment() {
        welcomeFragment = new WelcomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, welcomeFragment).commit();
    }


    // Handle user back presses
    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 2) {

            if (editTaskFragment != null && editTaskFragment.isAdded() && editTaskFragment.hasUnsavedChanges()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.discard_changes_dialog_title)
                        .setMessage(R.string.discard_changes_dialog_message)
                        .setPositiveButton(R.string.discard_option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getSupportFragmentManager().popBackStack();
                            }
                        })
                        .setNegativeButton(R.string.cancel_option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
                return;
            }

            getSupportFragmentManager().popBackStack();

        } else if(getSupportFragmentManager().getBackStackEntryCount() == 1){
            finish();
        } else{
            super.onBackPressed();
        }
    }

    // Handle BackStack changes
    private void onBackStackChangedListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
                if (index < 0) {
                    return;
                }
                String fragName = getSupportFragmentManager().getBackStackEntryAt(index).getName();
                if (fragName.equals(TASKS_FRAGMENT_NAME) && tasksFragment != null && tasksFragment.isAdded()) {
                    tasksFragment.loadTasks(tasks);
                }
            }
        });
    }

    // Save visible fragment and tasks data to restore after activity destruction
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String tasks_data = new Gson().toJson(tasks);
        outState.putString("tasks_data", tasks_data);
        outState.putParcelable("googleSignInAccount", mGoogleSignInAccount);

        if (tasksFragment != null && tasksFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, TASKS_FRAGMENT_NAME, tasksFragment);
        } else if (summaryFragment != null && summaryFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, SUMMARY_FRAGMENT_NAME, summaryFragment);
        } else if (editTaskFragment != null && editTaskFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, EDITTASK_FRAGMENT_NAME, editTaskFragment);
        } else if (newTaskFragment != null && newTaskFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, NEWTASK_FRAGMENT_NAME, newTaskFragment);
        } else if (settingsFragment != null && settingsFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, SETTINGS_FRAGMENT_NAME, settingsFragment);
        } else if (timelineFragment != null && timelineFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, TIMELINE_FRAGMENT_NAME, timelineFragment);
        } else if (welcomeFragment != null && welcomeFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, WELCOME_FRAGMENT_NAME, welcomeFragment);
        }


    }

    // Activity ProgressBar
    void showActivityProgressBar() {
        Log.i(TAG, "Show progress");
        ConstraintLayout container = findViewById(R.id.main_activity_progress_bar_container);
        container.setVisibility(View.VISIBLE);

        final CircularFillableLoaders photo = findViewById(R.id.loading_google_account_photo);

        if (mGoogleAccountPhoto == null) {

            // Get account photo URL
            String photoUrl = mGoogleSignInAccount.getPhotoUrl().toString();

            // Get photo bitmap from URL and add it to ImageView;
            new AsyncImageFromURL(new AsyncImageFromURL.AsyncImageFromURLListener() {
                @Override
                public void onTaskFinished(Bitmap bitmap) {
                    mGoogleAccountPhoto = bitmap;
                    photo.setImageBitmap(mGoogleAccountPhoto);
                }
            }).execute(photoUrl);
        } else {
            photo.setImageBitmap(mGoogleAccountPhoto);
        }

    }

    void hideActivityProgressBar() {
        Log.i(TAG, "Hide progress");
        ConstraintLayout container = findViewById(R.id.main_activity_progress_bar_container);
        container.setVisibility(View.GONE);
    }

    // Get random Google Material color, parameter "typeColor" is color shade
    private int getRandomMatColor(String typeColor) {
        int returnColor = Color.BLACK;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return returnColor;
    }

    // Determine if text should be light or dark base on background color
    @Override
    public boolean useDarkText(int color) {
        double r = Color.red(color);
        double g = Color.green(color);
        double b = Color.blue(color);

        return (r * 0.299 + g * 0.587 + b * 0.114) > 186;
    }
}
