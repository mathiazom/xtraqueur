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
            Log.i(TAG, "SharedPreferences: Getting tasks data from device");
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("TASKS_DATA_ON_DEVICE", 0);
            String json = sharedPreferences.getString("TASKS_DATA_" + mGoogleSignInAccount.getId(), null);

            if (json != null) {
                tasks = new Gson().fromJson(json, new TypeToken<ArrayList<XTask>>() {
                }.getType());
                loadTasksFragment();
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

        /*// TasksFragment
        if (mTasksFragment != null && mTasksFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, TASKS_FRAGMENT_NAME, mTasksFragment);
        }

        // EditTaskFragment
        else if (mEditTaskFragment != null && mEditTaskFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, EDITTASK_FRAGMENT_NAME, mEditTaskFragment);
        }

        // NewTaskFragment
        else if (mNewTaskFragment != null && mNewTaskFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, NEWTASK_FRAGMENT_NAME, mNewTaskFragment);
        }

        // SettingsFragment
        else if (mSettingsFragment != null && mSettingsFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, SETTINGS_FRAGMENT_NAME, mSettingsFragment);
        }

        // CompletionsFragment
        else if (mCompletionsFragment != null && mCompletionsFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, TIMELINE_FRAGMENT_NAME, mCompletionsFragment);
        }

        // EarningsFragment
        else if (mEarningsFragment != null && mEarningsFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, EARNINGS_FRAGMENT_NAME, mEarningsFragment);
        }

        // NewPaymentFragment
        else if (mNewPaymentFragment != null && mNewPaymentFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, NEWPAYMENT_FRAGMENT_NAME, mNewPaymentFragment);
        }

        // PaymentsFragment
        else if (mPaymentsFragment != null && mPaymentsFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, PAYMENTS_FRAGMENT_NAME, mPaymentsFragment);
        }

        // WelcomeFragment
        else if (mWelcomeFragment != null && mWelcomeFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, WELCOME_FRAGMENT_NAME, mWelcomeFragment);
        }*/
    }

    // Restore tasks data and fragments that were saved before activity destruction
    private boolean restoreFromSavedInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            Log.i(TAG, "No saved instance states");
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

            //mGoogleSignInClient = buildGoogleSignInClient();
        }


        return restoreFragmentsFromInstanceState(savedInstanceState);

    }

    private boolean restoreFragmentsFromInstanceState(Bundle savedInstanceState) {

        if (getSupportFragmentManager().getFragment(savedInstanceState, TasksFragment.class.getSimpleName()) != null) {
            mTasksFragment = (TasksFragment) getSupportFragmentManager().getFragment(savedInstanceState, TasksFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mTasksFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EditTaskFragment.class.getSimpleName()) != null) {
            mEditTaskFragment = (EditTaskFragment) getSupportFragmentManager().getFragment(savedInstanceState, EditTaskFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mEditTaskFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, NewTaskFragment.class.getSimpleName()) != null) {
            mNewTaskFragment = (NewTaskFragment) getSupportFragmentManager().getFragment(savedInstanceState, NewTaskFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mNewTaskFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, CompletionsFragment.class.getSimpleName()) != null) {
            mCompletionsFragment = (CompletionsFragment) getSupportFragmentManager().getFragment(savedInstanceState, CompletionsFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mCompletionsFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EarningsFragment.class.getSimpleName()) != null) {
            mEarningsFragment = (EarningsFragment) getSupportFragmentManager().getFragment(savedInstanceState, EarningsFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mEarningsFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, NewPaymentFragment.class.getSimpleName()) != null) {
            mNewPaymentFragment = (NewPaymentFragment) getSupportFragmentManager().getFragment(savedInstanceState, NewPaymentFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mNewPaymentFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, PaymentsFragment.class.getSimpleName()) != null) {
            mPaymentsFragment = (PaymentsFragment) getSupportFragmentManager().getFragment(savedInstanceState, PaymentsFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mPaymentsFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EditCompletionFragment.class.getSimpleName()) != null) {
            mEditCompletionFragment = (EditCompletionFragment) getSupportFragmentManager().getFragment(savedInstanceState, EditCompletionFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mEditCompletionFragment).commit();
        } else if (getSupportFragmentManager().getFragment(savedInstanceState, EditPaymentFragment.class.getSimpleName()) != null) {
            mEditPaymentFragment = (EditPaymentFragment) getSupportFragmentManager().getFragment(savedInstanceState, EditPaymentFragment.class.getSimpleName());
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mEditPaymentFragment).commit();
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
        });

        dataRetriever.execute(mDriveResourceClient);

    }

    @Override
    public void updatePaymentsDataOnDrive(XTaskPayment payment) {

        payments.add(payment);
        updatePaymentsDataOnDrive(payments);

    }

    @Override
    public void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments) {

        this.payments = payments;

        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(this, mGoogleSignInAccount);

        XTasksDataPackage dataPackage = new XTasksDataPackage(null, payments);

        XTasksDataUploader uploader = new XTasksDataUploader(mGoogleSignInAccount, driveResourceClient, getApplicationContext());
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
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mTasksFragment).addToBackStack(TasksFragment.class.getSimpleName()).commit();
    }

    // Fragment to create new tasks
    @Override
    public void loadNewTaskFragment() {
        mNewTaskFragment = NewTaskFragment.newInstance(tasks, getRandomMaterialColor());
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top).replace(R.id.main_frame_layout, mNewTaskFragment).addToBackStack(NewTaskFragment.class.getSimpleName()).commit();
    }

    // Fragment to edit the task passed in as an argument
    @Override
    public void loadEditTaskFragment(XTask task, int index) {
        mEditTaskFragment = EditTaskFragment.newInstance(tasks, task, index);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame_layout, mEditTaskFragment).addToBackStack(EditTaskFragment.class.getSimpleName()).commit();
    }

    @Override
    public void loadSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("GOOGLE_SIGN_IN_ACCOUNT", mGoogleSignInAccount);
        startActivity(intent);
    }

    // Fragment to show completions timeline
    @Override
    public void loadCompletionsFragment() {
        mCompletionsFragment = CompletionsFragment.newInstance(tasks);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom).replace(R.id.main_frame_layout, mCompletionsFragment).addToBackStack(CompletionsFragment.class.getSimpleName()).commit();
    }

    @Override
    public void loadCompletionsFragment(ArrayList<XTask> tasks, XTask filterTask) {
        mCompletionsFragment = CompletionsFragment.newInstance(tasks, filterTask);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom).replace(R.id.main_frame_layout, mCompletionsFragment).addToBackStack(CompletionsFragment.class.getSimpleName()).commit();
    }

    // Fragment to see unpaid earnings and to launch NewPaymentFragment
    @Override
    public void loadEarningsFragment() {
        mEarningsFragment = EarningsFragment.newInstance(tasks, payments);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom).replace(R.id.main_frame_layout, mEarningsFragment).addToBackStack(EarningsFragment.class.getSimpleName()).commit();
    }

    // Fragment to register new payments
    @Override
    public void loadNewPaymentFragment() {
        mNewPaymentFragment = NewPaymentFragment.newInstance(tasks);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.main_frame_layout, mNewPaymentFragment).addToBackStack(NewPaymentFragment.class.getSimpleName()).commit();
    }

    @Override
    public void loadNewPaymentFragment(ArrayList<Boolean> selectionArray) {
        mNewPaymentFragment = NewPaymentFragment.newInstance(tasks, selectionArray);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.main_frame_layout, mNewPaymentFragment).addToBackStack(NewPaymentFragment.class.getSimpleName()).commit();
    }

    @Override
    public void loadEditCompletionFragment(XTaskCompletion completion) {
        mEditCompletionFragment = EditCompletionFragment.newInstance(tasks, completion);
        replaceMainFragment(mEditCompletionFragment);
    }

    @Override
    public void loadPaymentsFragment() {
        mPaymentsFragment = PaymentsFragment.newInstance(payments);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.main_frame_layout, mPaymentsFragment).addToBackStack(PaymentsFragment.class.getSimpleName()).commit();
    }

    @Override
    public void loadEditPaymentFragment(XTaskPayment payment) {
        mEditPaymentFragment = EditPaymentFragment.newInstance(payment, payments);
        replaceMainFragment(mEditPaymentFragment);
    }


    private void replaceMainFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, newFragment).addToBackStack(newFragment.getClass().getSimpleName()).commit();
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
