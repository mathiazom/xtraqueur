package com.xtraqueur.mzom.xtraqueur;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import static com.xtraqueur.mzom.xtraqueur.R.id.settingsConstraint;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Innstillinger");

        toggleListener();
    }

    private void toggleListener(){
        Switch toggle = (Switch) findViewById(R.id.themeSwitch);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    darkMode();
                } else {
                    lightMode();
                }
            }
        });
    }

    private void darkMode(){
        (findViewById(settingsConstraint)).setBackgroundColor(Color.parseColor("#000000"));
    }

    private void lightMode(){
        (findViewById(settingsConstraint)).setBackgroundColor(Color.parseColor("#FFFFFF"));
    }
}
