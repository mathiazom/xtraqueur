package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends android.support.v4.app.Fragment {

    // Fragment root view
    private View view;

    // Google sign in account
    private GoogleSignInAccount mGoogleSignInAccount;
    private Bitmap mAccountPhoto;

    // Listener to communicate with MainActivity
    SettingsFragmentListener settingsFragmentListener;

    // Log tag for debugging
    private static final String TAG = "Xtraqueur-Settings";

    interface SettingsFragmentListener {

        void popBackStackFromFragment();

        void loadTasksFragment();
    }

    // Custom constructor to pass Google sign in account arguments
    public static SettingsFragment newInstance(GoogleSignInAccount googleSignInAccount, Bitmap acccountPhoto) {

        SettingsFragment fragment = new SettingsFragment();
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
    void initToolbar() {

        // Initialize toolbar field variable
        Toolbar mToolbar = view.findViewById(R.id.toolbar);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsFragmentListener.popBackStackFromFragment();
            }
        });
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

        // Account photo
        final CircleImageView accountPhotoView = view.findViewById(R.id.settings_google_account_photo);

        // Check account photo availability
        if(mAccountPhoto != null){
            // Use already downloaded account photo
            accountPhotoView.setImageBitmap(mAccountPhoto);
        }else{
            // Get account photo from Url
            Log.i(TAG,"Account photo is null, downloading");
            Uri photoUrl = mGoogleSignInAccount.getPhotoUrl();
            if(photoUrl == null) return;
            new AsyncImageFromURL(new AsyncImageFromURL.AsyncImageFromURLListener() {
                @Override
                public void onTaskFinished(Bitmap bitmap) {
                    Log.i(TAG,"Setting bitmap to ImageView");
                    mAccountPhoto = bitmap;
                    accountPhotoView.setImageBitmap(mAccountPhoto);
                }
            }).execute(photoUrl.toString());
        }


    }

}
