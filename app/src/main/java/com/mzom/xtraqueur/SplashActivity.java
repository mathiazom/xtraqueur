package com.mzom.xtraqueur;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "XTQ-SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG,"SplashActivity onCreate");

        super.onCreate(savedInstanceState);

        // Check if we need to display welcome pages (first time use of app)
        if (shouldDisplayWelcomePages()) {

            // The user hasn't seen the welcome pages yet, so show it
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();

            return;

        }


        // If welcome pages are not needed, commence to sign in
        startActivity(new Intent(this,SignInActivity.class));
        finish();

    }

    private boolean shouldDisplayWelcomePages(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        return !sharedPreferences.getBoolean("COMPLETED_WELCOME_PAGES_PREF_NAME", false);

    }

}
