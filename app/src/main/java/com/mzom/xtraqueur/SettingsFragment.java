package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends android.support.v4.app.Fragment {

    // Fragment root view
    private View view;

    private ArrayList<XTask> tasks;

    private ArrayList<XTaskCompletion> instantCompletions;

    // Google sign in account
    private GoogleSignInAccount mGoogleSignInAccount;
    private Bitmap mAccountPhoto;

    // Listener to communicate with MainActivity
    private SettingsFragmentListener settingsFragmentListener;

    // Log tag for debugging
    private static final String TAG = "XTQ-SettingsFrag";

    interface SettingsFragmentListener {

        void signOut();

        void onFragmentBackPressed();
    }

    // Custom constructor to pass Google sign in account arguments
    public static SettingsFragment newInstance(ArrayList<XTask> tasks, ArrayList<XTaskCompletion> instantCompletions, GoogleSignInAccount googleSignInAccount, Bitmap acccountPhoto) {

        SettingsFragment fragment = new SettingsFragment();
        fragment.tasks = tasks;
        fragment.instantCompletions = instantCompletions;
        fragment.mGoogleSignInAccount = googleSignInAccount;
        fragment.mAccountPhoto = acccountPhoto;

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        // Get fragment root view
        this.view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize toolbar field variable and add action buttons with listeners
        initToolbar();

        // Button listeners
        initListeners();

        // Fill views with account info
        loadAccountInfo();

        return view;
    }

    // Init listener when activity is available
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            settingsFragmentListener = (SettingsFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SettingsFragmentListener");
        }
    }

    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Initialize toolbar field variable
        Toolbar mToolbar = view.findViewById(R.id.toolbar);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsFragmentListener.onFragmentBackPressed();
            }
        });
    }

    // Button listeners
    private void initListeners(){

        // Google account sign out button
        ImageButton sign_out_button = view.findViewById(R.id.button_account_sign_out);
        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsFragmentListener.signOut();
            }
        });

        deleteAllCompletionsListener();

        deleteAllTasksListener();

    }

    void deleteAllCompletionsListener(){

        // Delete all completions button
        Button delete_completions_button = view.findViewById(R.id.button_delete_all_completions);

        int completions = 0;
        for(XTask t : tasks){
            completions += t.getCompletionsCount();
        }
        completions += instantCompletions.size();

        // Disable button if user doesn't have any task payments
        if(completions == 0){
            disableButton(delete_completions_button);
        }else{
            delete_completions_button.setEnabled(true);

            delete_completions_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext(),R.style.AlertDialogTheme)
                            .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteAllCompletions();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create();
                    alertDialog.setTitle(getString(R.string.delete_all_completions_title));
                    alertDialog.setMessage(getString(R.string.delete_all_completions_message));

                    alertDialog.show();
                }
            });
        }
    }

    void setTasks(ArrayList<XTask> tasks){
        this.tasks = tasks;
    }

    void setInstantCompletions(ArrayList<XTaskCompletion> instantCompletions){
        this.instantCompletions = instantCompletions;
    }

    void deleteAllTasksListener(){

        // Delete all tasks button
        Button delete_tasks_button = view.findViewById(R.id.button_delete_all_tasks);

        // Disable button if user doesn't have any tasks
        if(tasks.size() == 0){
            disableButton(delete_tasks_button);
        }else{
            delete_tasks_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext(),R.style.AlertDialogTheme)
                            .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteAllTasks();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create();

                    alertDialog.setTitle(getString(R.string.delete_all_tasks_title));
                    alertDialog.setMessage(getString(R.string.delete_all_tasks_message));
                    alertDialog.show();
                }
            });
        }
    }

    // Danger zone: Delete all completions
    public void deleteAllCompletions() {

        // Delete all completions from tasks data
        for (XTask t : tasks) {
            t.setCompletions(new ArrayList<XTaskCompletion>());
        }
        instantCompletions = new ArrayList<>();

        deleteAllCompletionsListener();

        // Upload updated tasks data
        updateTasksDataOnDrive();
    }

    // Danger zone: Delete all tasks
    public void deleteAllTasks() {

        tasks = new ArrayList<>();

        setTasks(tasks);

        deleteAllTasksListener();

        // Upload updated tasks data
        updateTasksDataOnDrive();
    }

    private void updateTasksDataOnDrive() {

        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME,tasks, getContext());

        XDataUploader.uploadData(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME,instantCompletions, getContext());
    }

    private void disableButton(Button btn){

        // Disable button
        btn.setEnabled(false);

        // Disabled button background
        ColorUtilities.setViewBackgroundColor(btn,Color.parseColor("#AAFFFFFF"));
    }

    // Fill views with account info
    private void loadAccountInfo(){

        // Check if account if available
        if(mGoogleSignInAccount == null){
            Log.e(TAG,"GoogleSignInAccount is null");
            return;
        }

        // Account name
        TextView accountNameView = view.findViewById(R.id.settings_google_account_name);
        String accountName = mGoogleSignInAccount.getDisplayName();
        accountNameView.setText(accountName);

        // Account email
        TextView accountEmailView = view.findViewById(R.id.settings_google_account_email);
        String accountEmail = mGoogleSignInAccount.getEmail();
        accountEmailView.setText(accountEmail);

        //Account photo
        final CircleImageView accountPhotoView = view.findViewById(R.id.settings_google_account_photo);
        if(mAccountPhoto != null) accountPhotoView.setImageBitmap(mAccountPhoto);

    }



}
