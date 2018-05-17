package com.mzom.xtraqueur;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        TasksFragment.TasksFragmentListener,
        NewTaskFragment.NewTaskFragmentListener,
        CompletionsFragment.CompletionsFragmentListener,
        EarningsFragment.EarningsFragmentListener,
        NewPaymentFragment.NewPaymentFragmentListener,
        PaymentsFragment.PaymentsFragmentListener,
        BaseEditFragment.BaseEditFragmentListener,
        CompletionsPieFragment.CompletionsPieFragmentListener{

    // Tag used for debugging
    private static final String TAG = "XTQ-MainActivity";

    private TasksFragment mTasksFragment;

    // Google sign in account
    private GoogleSignInAccount mGoogleSignInAccount;

    // Google Drive Clients
    private DriveResourceClient mDriveResourceClient;

    // Data set storing all user tasks
    private ArrayList<XTask> tasks;
    private ArrayList<XTaskPayment> payments;

    // Activity creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve all data saved before configuration changes (onSaveInstanceState)
        if (!restoreFromSavedInstanceState(savedInstanceState)) {
            // Get google sign in object and account photo from SignInActivity
            getGoogleSignIn();
        }

        // Set listener for changes to the BackStack
        onBackStackChangedListener();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        getLatestTasksDataFromDrive();
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

        // Save Google Sign-in Account
        if (mGoogleSignInAccount != null) {
            outState.putParcelable("googleSignInAccount", mGoogleSignInAccount);
        }

        // Save currently used fragment
        //saveFragmentsToInstanceState(outState);

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
        }else{
            return false;
        }

        // Restore payments data
        String temp_payments_data = savedInstanceState.getString("payments_data", null);
        if (temp_payments_data != null) {
            payments = new Gson().fromJson(temp_payments_data, new TypeToken<ArrayList<XTaskPayment>>() {
            }.getType());
        }else{
            return false;
        }

        // Restore Google Sign-in Account
        if (savedInstanceState.getParcelable("googleSignInAccount") != null) {

            mGoogleSignInAccount = savedInstanceState.getParcelable("googleSignInAccount");

            if (mGoogleSignInAccount != null) {
                mDriveResourceClient = Drive.getDriveResourceClient(this, mGoogleSignInAccount);
            }else{
                return false;
            }
        }else{
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

                getLatestTasksDataFromDrive();
            }
        }

    }

    // Use XTasksDataRetriever to get latest tasks and payments data
    private void getLatestTasksDataFromDrive() {

        if (mGoogleSignInAccount == null) {
            return;
        }

        final Context context = this;

        XTasksDataRetriever dataRetriever = new XTasksDataRetriever(XTasksDataRetriever.RETRIEVE_ALL_DATA, new XTasksDataRetriever.XTasksDataRetrieverListener() {

            @Override
            public void onDataRetrieved(XTasksDataPackage dataPackage) {
                ArrayList<XTask> retrievedTasks = dataPackage.getTasks();
                if (retrievedTasks != null) {
                    tasks = retrievedTasks;

                    loadTasksFragment();
                }

                ArrayList<XTaskPayment> retrievedPayments = dataPackage.getPayments();
                if (retrievedPayments != null) {
                    payments = retrievedPayments;
                }
            }

            @Override
            public void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments) {

                XTasksDataUploader dataUploader = new XTasksDataUploader(mGoogleSignInAccount, mDriveResourceClient, getApplicationContext());
                XTasksDataPackage dataPackage = new XTasksDataPackage(null, payments);
                dataUploader.execute(dataPackage);

            }

            @Override
            public void onNoTasksDataFound() {
                new AlertDialog.Builder(context).setTitle("No tasks data found on drive").setMessage("Do you wish to continue and start fresh?").setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "Google Drive API: Could not find tasks_data.txt, creating blank file");

                        tasks = new ArrayList<>();

                        // Create new file on drive
                        updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                getLatestTasksDataFromDrive();
                            }
                        });

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
            }

            @Override
            public void onNoPaymentsDataFound() {
                new AlertDialog.Builder(context).setTitle("No payments data found on drive").setMessage("Do you wish to continue and start fresh?").setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "Google Drive API: Could not find payments_data.txt, creating new");

                        payments = new ArrayList<>();

                        // Create new file on drive
                        updatePaymentsDataOnDrive(payments);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
            }
        });

        dataRetriever.execute(mDriveResourceClient);

    }

    @Override
    public void updatePaymentsDataOnDrive(XTaskPayment payment, OnSuccessListener<DriveFile> onSuccessListener) {

        payments.add(payment);
        updatePaymentsDataOnDrive(payments, onSuccessListener);

    }

    @Override
    public void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments){
        updatePaymentsDataOnDrive(payments, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        });
    }

    @Override
    public void updateTasksDataOnDrive(ArrayList<XTask> tasks, OnSuccessListener<DriveFile> onSuccessListener) {
        this.tasks = tasks;

        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(this, mGoogleSignInAccount);

        XTasksDataPackage dataPackage = new XTasksDataPackage(tasks, null);

        XTasksDataUploader uploader = new XTasksDataUploader(mGoogleSignInAccount, driveResourceClient, getApplicationContext(), onSuccessListener);
        uploader.execute(dataPackage);
    }

    public void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments, OnSuccessListener<DriveFile> onSuccessListener) {

        this.payments = payments;

        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(this, mGoogleSignInAccount);

        XTasksDataPackage dataPackage = new XTasksDataPackage(null, payments);

        XTasksDataUploader uploader = new XTasksDataUploader(mGoogleSignInAccount, driveResourceClient, getApplicationContext(), onSuccessListener);
        uploader.execute(dataPackage);

    }

    @Override
    public void updateTasksDataOnDrive(ArrayList<XTask> tasks) {
        updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        });
    }


    // Fragment listing all the tasks
    // Each task has buttons to add and subtract completions
    // Clicking on a task item will open a EditTaskFragment for this task
    @Override
    public void loadTasksFragment() {
        mTasksFragment = TasksFragment.newInstance(tasks);
        replaceMainFragment(mTasksFragment);
    }

    // Fragment to create new tasks
    @Override
    public void loadNewTaskFragment() {
        loadNewTaskFragment(false);
    }

    @Override
    public void loadNewTaskFragment(boolean instantCompletion) {
        NewTaskFragment mNewTaskFragment = NewTaskFragment.newInstance(tasks, getRandomMaterialColor(), instantCompletion);
        replaceMainFragment(mNewTaskFragment,R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top,true);
    }

    // Fragment to edit the task passed in as an argument
    @Override
    public void loadEditTaskFragment(XTask task, int index) {
        EditTaskFragment mEditTaskFragment = EditTaskFragment.newInstance(tasks, task, index);
        replaceMainFragment(mEditTaskFragment);
    }

    // Fragment to show completions timeline
    @Override
    public void loadCompletionsFragment() {
        CompletionsFragment mCompletionsFragment = CompletionsFragment.newInstance(tasks);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
                .replace(R.id.main_frame_layout, mCompletionsFragment)
                .addToBackStack(CompletionsFragment.class.getSimpleName())
                .commit();
    }

    @Override
    public void loadCompletionsFragment(ArrayList<XTask> tasks, XTask filterTask) {
        CompletionsFragment mCompletionsFragment = CompletionsFragment.newInstance(tasks, filterTask);
        replaceMainFragment(mCompletionsFragment,R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom,true);
    }

    @Override
    public void loadCompletionsPieFragment() {
        CompletionsPieFragment mCompletionsPieFragment = CompletionsPieFragment.newInstance(tasks);
        replaceMainFragment(mCompletionsPieFragment,true);
    }

    // Fragment to see unpaid earnings and to launch NewPaymentFragment
    @Override
    public void loadEarningsFragment() {
        EarningsFragment mEarningsFragment = EarningsFragment.newInstance(tasks, payments);
        replaceMainFragment(mEarningsFragment,R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom,true);
    }

    // Fragment to register new payments
    @Override
    public void loadNewPaymentFragment() {
        NewPaymentFragment mNewPaymentFragment = NewPaymentFragment.newInstance(tasks);
        replaceMainFragment(mNewPaymentFragment,R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right,true);
    }

    @Override
    public void loadNewPaymentFragment(ArrayList<Boolean> selectionArray) {
        NewPaymentFragment mNewPaymentFragment = NewPaymentFragment.newInstance(tasks, selectionArray);
        replaceMainFragment(mNewPaymentFragment,R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right,true);
    }

    @Override
    public void loadEditCompletionFragment(XTaskCompletion completion) {
        EditCompletionFragment mEditCompletionFragment = EditCompletionFragment.newInstance(tasks, completion);
        replaceMainFragment(mEditCompletionFragment, 0,0,0,0,true);
    }

    @Override
    public void loadPaymentsFragment(boolean addToBackStack) {
        PaymentsFragment mPaymentsFragment = PaymentsFragment.newInstance(payments);
        replaceMainFragment(mPaymentsFragment,R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right,addToBackStack);
    }

    @Override
    public void loadEditPaymentFragment(XTaskPayment payment) {
        EditPaymentFragment mEditPaymentFragment = EditPaymentFragment.newInstance(payment, payments);
        replaceMainFragment(mEditPaymentFragment);
    }


    private void replaceMainFragment(Fragment fragment){
        replaceMainFragment(fragment,true);
    }

    // Fragment transaction without animation
    private void replaceMainFragment(Fragment newFragment, boolean addToBackStack) {

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, newFragment);

        if(addToBackStack){
            transaction.addToBackStack(newFragment.getClass().getSimpleName());
        }

        transaction.commit();
    }

    // Fragment transaction with animation
    private void replaceMainFragment(Fragment newFragment, int enter, int exit, int popEnter, int popExit, boolean addToBackStack){

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(R.id.main_frame_layout, newFragment);

        if(addToBackStack){
            transaction.addToBackStack(newFragment.getClass().getSimpleName());
        }

        transaction.commit();

    }


    @Override
    public void loadSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("GOOGLE_SIGN_IN_ACCOUNT", mGoogleSignInAccount);
        intent.putExtra("TASKS_DATA",tasks);
        startActivity(intent);
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
                    mTasksFragment.loadTasks(tasks);
                }
            }
        });
    }

    // Handle back presses
    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {

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

    private int getRandomMaterialColor() {
        int arrayId = getResources().getIdentifier("mdcolor_700_light_text", "array", getPackageName());
        if (arrayId == 0) return Color.BLACK;

        TypedArray typedColors = getResources().obtainTypedArray(arrayId);

        int randIndex = (int) (Math.random() * typedColors.length());

        int randColor = typedColors.getColor(randIndex, 0);

        typedColors.recycle();

        return randColor;
    }

}
