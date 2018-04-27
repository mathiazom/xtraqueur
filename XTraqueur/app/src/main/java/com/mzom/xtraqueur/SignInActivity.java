package com.mzom.xtraqueur;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;

public class SignInActivity extends AppCompatActivity implements WelcomeFragment.WelcomeFragmentListener {

    private static final String TAG = "XTQ-SignInActivity";


    private static final String WELCOME_FRAGMENT_NAME = "WelcomeFragment";

    // Google Drive SignIn request code
    private static final int REQUEST_CODE_SIGN_IN = 1337;

    private GoogleSignInAccount mGoogleSignInAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        handleSignIn();
    }

    // Handle Google account sign in on activity creation
    private void handleSignIn() {
        // Check if any Google accounts are signed in to the app
        boolean signedIn = GoogleSignIn.getLastSignedInAccount(this) != null;

        if (!signedIn) {
            // No Google accounts signed in, load WelcomeFragment to let the user sign in
            Log.i(TAG, "No signed in account, loading welcome-page");
            loadWelcomeFragment();
        } else {
            // Google Drive API client sign in
            signIn();
        }
    }

    // Start Google Drive sign in
    public void signIn() {
        Log.i(TAG, "Google Drive API: Start sign in");
        GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();
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

                // Start ProgressBar loading
                showActivityProgressBar();

                // Called after user is signed in
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Google Drive API: Signed in successfully.");

                    // Use the last signed in account
                    mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
                    if (mGoogleSignInAccount == null) {
                        Log.e(TAG, "Google Drive API: GoogleSignInAccount is null");
                        return;
                    }

                    Log.i(TAG, "Google Drive API: Account: " + mGoogleSignInAccount.getDisplayName() + ", " + mGoogleSignInAccount.getEmail());

                    DriveClient mDriveClient = Drive.getDriveClient(this, mGoogleSignInAccount);

                    // Sync required after app reinstalling
                    mDriveClient.requestSync()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Log.i(TAG, "Google Drive API: Sync successful");

                                    hideActivityProgressBar();

                                    launchMainActivity();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.e(TAG, "Google Drive API: Sync failed", e);
                                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                                    hideActivityProgressBar();

                                    launchMainActivity();
                                }
                            });
                } else {
                    Log.e(TAG, "Google Drive API: Sign in failed. Result code: " + resultCode);

                    // Stop ProgressBar loading
                    hideActivityProgressBar();

                    // Go back to log in to let user try again
                    loadWelcomeFragment();
                }
                break;
        }
    }

    // Fragment that lets the user sign in to a Google account to use with the app
    private void loadWelcomeFragment() {
        WelcomeFragment mWelcomeFragment = new WelcomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.sign_in_frame_layout, mWelcomeFragment).commit();
    }

    // Activity ProgressBar
    private void showActivityProgressBar() {
        Log.i(TAG, "START LOADING");
        ConstraintLayout container = findViewById(R.id.sign_in_activity_progress_bar_container);
        container.setVisibility(View.VISIBLE);
    }

    private void hideActivityProgressBar() {
        Log.i(TAG, "STOP LOADING");
        ConstraintLayout container = findViewById(R.id.sign_in_activity_progress_bar_container);
        container.setVisibility(View.GONE);
    }


    // Start MainActivity with sign in object and account photo
    private void launchMainActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("GOOGLE_SIGN_IN_ACCOUNT", mGoogleSignInAccount);
        //intent.putExtra("GOOGLE_SIGN_IN_ACCOUNT_PHOTO",mGoogleAccountPhoto);
        startActivity(intent);

        Log.i(TAG, "MainActivity launched");
        finish();
    }
}
