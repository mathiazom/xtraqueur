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
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends android.support.v4.app.Fragment {

    // Fragment root view
    private View view;

    private ArrayList<XTask> tasks;

    // Google sign in account
    private GoogleSignInAccount mGoogleSignInAccount;
    private Bitmap mAccountPhoto;

    // Listener to communicate with MainActivity
    private SettingsFragmentListener settingsFragmentListener;

    // Log tag for debugging
    private static final String TAG = "Xtraqueur-Settings";

    interface SettingsFragmentListener {

        void signOut();

        void deleteAllCompletions();

        void deleteAllTasks();

        void onFragmentBackPressed();
    }

    // Custom constructor to pass Google sign in account arguments
    public static SettingsFragment newInstance(ArrayList<XTask> tasks, GoogleSignInAccount googleSignInAccount, Bitmap acccountPhoto) {

        SettingsFragment fragment = new SettingsFragment();
        fragment.tasks = tasks;
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
        Button sign_out_button = view.findViewById(R.id.button_account_sign_out);
        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsFragmentListener.signOut();
            }
        });

        // Delete all completions button
        Button delete_completions_button = view.findViewById(R.id.button_delete_all_completions);

        int completions = 0;
        for(XTask t : tasks){
            completions += t.getCompletionsCount();
        }

        // Disable button if user doesn't have any task completions
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
                                    settingsFragmentListener.deleteAllCompletions();
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
                                    settingsFragmentListener.deleteAllTasks();
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

    private void disableButton(Button btn){
        // Disable button
        btn.setEnabled(false);

        // Disabled button background
        btn.setBackground(new ColorDrawable(Color.parseColor("#0CFFFFFF")));

        // Disabled button text
        btn.setTextColor(Color.parseColor("#1EFFFFFF"));
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
