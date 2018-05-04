package com.mzom.xtraqueur;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
        BaseEditFragment.BaseEditFragmentListener {

    private TasksFragment mTasksFragment;
    private EditTaskFragment mEditTaskFragment;
    private NewTaskFragment mNewTaskFragment;
    private CompletionsFragment mCompletionsFragment;
    private EarningsFragment mEarningsFragment;
    private NewPaymentFragment mNewPaymentFragment;
    private PaymentsFragment mPaymentsFragment;
    private EditCompletionFragment mEditCompletionFragment;
    private EditPaymentFragment mEditPaymentFragment;

    // Google sign in account
    private GoogleSignInAccount mGoogleSignInAccount;

    // Google Drive Clients
    private DriveResourceClient mDriveResourceClient;

    // Data set storing all user tasks
    private ArrayList<XTask> tasks;
    private ArrayList<XTaskPayment> payments;


    // Tag used for debugging
    private static final String TAG = "XTQ-MainActivity";

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
    protected void onResume() {
        super.onResume();

        if (mTasksFragment != null && mTasksFragment.isAdded()) {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("TASKS_DATA_ON_DEVICE", 0);
            String json = sharedPreferences.getString("TASKS_DATA_" + mGoogleSignInAccount.getId(), null);

            if (json != null) {
                tasks = new Gson().fromJson(json, new TypeToken<ArrayList<XTask>>() {
                }.getType());
                reloadTasksFragment();
            } else {
                Log.e(TAG, "SharedPreferences: No tasks data on device");
            }
        }
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
        saveFragmentsToInstanceState(outState);

    }

    private void saveFragmentsToInstanceState(Bundle outState) {

        Fragment[] fragments = new Fragment[]{
                mTasksFragment,
                mEditTaskFragment,
                mNewTaskFragment,
                mCompletionsFragment,
                mEarningsFragment,
                mNewPaymentFragment,
                mPaymentsFragment,
                mEditCompletionFragment,
                mEditPaymentFragment
        };

        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isAdded()) {
                getSupportFragmentManager().putFragment(outState, fragment.getClass().getSimpleName(), fragment);
            }
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
        }

        // Restore payments data
        String temp_payments_data = savedInstanceState.getString("payments_data", null);
        if (temp_payments_data != null) {
            payments = new Gson().fromJson(temp_payments_data, new TypeToken<ArrayList<XTaskPayment>>() {
            }.getType());
        }

        // Restore Google Sign-in Account
        if (savedInstanceState.getParcelable("googleSignInAccount") != null) {

            mGoogleSignInAccount = savedInstanceState.getParcelable("googleSignInAccount");

            if (mGoogleSignInAccount != null) {
                mDriveResourceClient = Drive.getDriveResourceClient(this, mGoogleSignInAccount);
            }
        }


        return restoreFragmentsFromInstanceState(savedInstanceState);

    }

    private boolean restoreFragmentsFromInstanceState(Bundle savedInstanceState) {

        /*if (getSupportFragmentManager().getFragment(savedInstanceState, TasksFragment.class.getSimpleName()) != null) {
            mTasksFragment = (TasksFragment) getSupportFragmentManager().getFragment(savedInstanceState, TasksFragment.class.getSimpleName());
            replaceMainFragment(mTasksFragment,false,false);

        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EditTaskFragment.class.getSimpleName()) != null) {
            mEditTaskFragment = (EditTaskFragment) getSupportFragmentManager().getFragment(savedInstanceState, EditTaskFragment.class.getSimpleName());
            replaceMainFragment(mEditTaskFragment,false,false);

        } else if (getSupportFragmentManager().getFragment(savedInstanceState, NewTaskFragment.class.getSimpleName()) != null) {
            mNewTaskFragment = (NewTaskFragment) getSupportFragmentManager().getFragment(savedInstanceState, NewTaskFragment.class.getSimpleName());
            replaceMainFragment(mNewTaskFragment,false,false);

        } else if (getSupportFragmentManager().getFragment(savedInstanceState, CompletionsFragment.class.getSimpleName()) != null) {
            mCompletionsFragment = (CompletionsFragment) getSupportFragmentManager().getFragment(savedInstanceState, CompletionsFragment.class.getSimpleName());
            replaceMainFragment(mCompletionsFragment,false,false);

        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EarningsFragment.class.getSimpleName()) != null) {
            //mEarningsFragment = (EarningsFragment) getSupportFragmentManager().getFragment(savedInstanceState, EarningsFragment.class.getSimpleName());
            //replaceMainFragment(mEarningsFragment,false,false);

        } else if (getSupportFragmentManager().getFragment(savedInstanceState, NewPaymentFragment.class.getSimpleName()) != null) {
            mNewPaymentFragment = (NewPaymentFragment) getSupportFragmentManager().getFragment(savedInstanceState, NewPaymentFragment.class.getSimpleName());
            replaceMainFragment(mNewPaymentFragment,false,false);

        } else if (getSupportFragmentManager().getFragment(savedInstanceState, PaymentsFragment.class.getSimpleName()) != null) {
            mPaymentsFragment = (PaymentsFragment) getSupportFragmentManager().getFragment(savedInstanceState, PaymentsFragment.class.getSimpleName());
            replaceMainFragment(mPaymentsFragment,false,false);

        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EditCompletionFragment.class.getSimpleName()) != null) {
            mEditCompletionFragment = (EditCompletionFragment) getSupportFragmentManager().getFragment(savedInstanceState, EditCompletionFragment.class.getSimpleName());
            replaceMainFragment(mEditCompletionFragment,false,false);

        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EditPaymentFragment.class.getSimpleName()) != null) {
            mEditPaymentFragment = (EditPaymentFragment) getSupportFragmentManager().getFragment(savedInstanceState, EditPaymentFragment.class.getSimpleName());
            replaceMainFragment(mEditPaymentFragment,false,false);

        } else {
            return false;
        }*/

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
        });

        dataRetriever.execute(mDriveResourceClient);

    }

    @Override
    public void updatePaymentsDataOnDrive(XTaskPayment payment, OnSuccessListener onSuccessListener) {

        payments.add(payment);
        updatePaymentsDataOnDrive(payments, onSuccessListener);

    }

    @Override
    public void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments){
        updatePaymentsDataOnDrive(payments, new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {

            }
        });
    }

    public void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments, OnSuccessListener onSuccessListener) {

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

    private void updateTasksDataOnDrive(ArrayList<XTask> tasks, OnSuccessListener<DriveFile> onSuccessListener) {

        this.tasks = tasks;

        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(this, mGoogleSignInAccount);

        XTasksDataPackage dataPackage = new XTasksDataPackage(tasks, null);

        XTasksDataUploader uploader = new XTasksDataUploader(mGoogleSignInAccount, driveResourceClient, getApplicationContext(), onSuccessListener);
        uploader.execute(dataPackage);
    }

    // Fragment listing all the tasks
    // Each task has buttons to add and subtract completions
    // Clicking on a task item will open a EditTaskFragment with the task as an argument
    @Override
    public void loadTasksFragment() {
        mTasksFragment = TasksFragment.newInstance(tasks);
        replaceMainFragment(mTasksFragment);
    }

    private void reloadTasksFragment(){
        mTasksFragment = TasksFragment.newInstance(tasks);
        replaceMainFragment(mTasksFragment,false);
    }

    // Fragment to create new tasks
    @Override
    public void loadNewTaskFragment() {
        mNewTaskFragment = NewTaskFragment.newInstance(tasks, getRandomMaterialColor());
        replaceMainFragment(mNewTaskFragment,R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top,true);
    }

    // Fragment to edit the task passed in as an argument
    @Override
    public void loadEditTaskFragment(XTask task, int index) {
        mEditTaskFragment = EditTaskFragment.newInstance(tasks, task, index);
        replaceMainFragment(mEditTaskFragment);
    }

    // Fragment to show completions timeline
    @Override
    public void loadCompletionsFragment() {
        mCompletionsFragment = CompletionsFragment.newInstance(tasks);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
                .replace(R.id.main_frame_layout, mCompletionsFragment)
                .addToBackStack(CompletionsFragment.class.getSimpleName())
                .commit();
    }

    @Override
    public void loadCompletionsFragment(ArrayList<XTask> tasks, XTask filterTask) {
        mCompletionsFragment = CompletionsFragment.newInstance(tasks, filterTask);
        replaceMainFragment(mCompletionsFragment,R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom,true);
    }

    // Fragment to see unpaid earnings and to launch NewPaymentFragment
    @Override
    public void loadEarningsFragment() {
        mEarningsFragment = EarningsFragment.newInstance(tasks, payments);
        replaceMainFragment(mEarningsFragment,R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom,true);
    }

    // Fragment to register new payments
    @Override
    public void loadNewPaymentFragment() {
        mNewPaymentFragment = NewPaymentFragment.newInstance(tasks);
        replaceMainFragment(mNewPaymentFragment,R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right,true);
    }

    @Override
    public void loadNewPaymentFragment(ArrayList<Boolean> selectionArray) {
        mNewPaymentFragment = NewPaymentFragment.newInstance(tasks, selectionArray);
        replaceMainFragment(mNewPaymentFragment,R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right,true);
    }

    @Override
    public void loadEditCompletionFragment(XTaskCompletion completion) {
        mEditCompletionFragment = EditCompletionFragment.newInstance(tasks, completion);
        replaceMainFragment(mEditCompletionFragment, R.anim.fade_in,R.anim.fade_out,R.anim.fade_out,R.anim.fade_in, true);
    }

    @Override
    public void loadPaymentsFragment(boolean addToBackStack) {
        mPaymentsFragment = PaymentsFragment.newInstance(payments);
        replaceMainFragment(mPaymentsFragment,R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right,addToBackStack);
    }

    @Override
    public void loadEditPaymentFragment(XTaskPayment payment) {
        mEditPaymentFragment = EditPaymentFragment.newInstance(payment, payments);
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
            Log.i(TAG,"Added " + newFragment.getClass().getSimpleName() + " to back stack");
            transaction.addToBackStack(newFragment.getClass().getSimpleName());
        }

        transaction.commit();

        Log.i(TAG,newFragment.getClass().getSimpleName() + " is now main fragment");

    }

    // Fragment transaction with animation
    private void replaceMainFragment(Fragment newFragment, int enter, int exit, int popEnter, int popExit, boolean addToBackStack){

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(R.id.main_frame_layout, newFragment);

        if(addToBackStack){
            Log.i(TAG,"Added " + newFragment.getClass().getSimpleName() + " to back stack");
            transaction.addToBackStack(newFragment.getClass().getSimpleName());
        }

        transaction.commit();

        Log.i(TAG,newFragment.getClass().getSimpleName() + " is now main fragment");

    }


    @Override
    public void loadSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("GOOGLE_SIGN_IN_ACCOUNT", mGoogleSignInAccount);
        startActivity(intent);
    }


    // Listen to BackStack changes to update TaskFragment
    private void onBackStackChangedListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                for (int f = 0; f < getSupportFragmentManager().getBackStackEntryCount(); f++) {

                    Log.i("Backstack", getSupportFragmentManager().getBackStackEntryAt(f).toString());

                }


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
