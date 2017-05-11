package com.xtraqueur.mzom.xtraqueur;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class IndexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        final SharedPreferences countStorage = PreferenceManager.getDefaultSharedPreferences(this);

        ImageButton addTask1 = (ImageButton) findViewById(R.id.addButton1);
        addTask1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView taskCount = (TextView) findViewById(R.id.task1Count);
                int intCount = Integer.parseInt(taskCount.getText().toString());
                intCount = intCount + 1;
                System.out.println(intCount);

                taskCount.setText(String.valueOf(intCount));

                storeCount1(intCount);

                totalDisplay();
            }
        });

        ImageButton subTask1 = (ImageButton) findViewById(R.id.subButton1);
        subTask1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView taskCount = (TextView) findViewById(R.id.task1Count);
                int intCount = Integer.parseInt(taskCount.getText().toString());
                if (intCount > 0) {
                    intCount = intCount - 1;
                }
                taskCount.setText(String.valueOf(intCount));

                storeCount1(intCount);

                totalDisplay();
            }
        });

        ImageButton addTask2 = (ImageButton) findViewById(R.id.addButton2);
        addTask2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView taskCount = (TextView) findViewById(R.id.task2Count);
                int intCount = Integer.parseInt(taskCount.getText().toString());
                intCount = intCount + 1;
                taskCount.setText(String.valueOf(intCount));

                storeCount2(intCount);

                totalDisplay();
            }
        });

        ImageButton subTask2 = (ImageButton) findViewById(R.id.subButton2);
        subTask2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView taskCount = (TextView) findViewById(R.id.task2Count);
                int intCount = Integer.parseInt(taskCount.getText().toString());
                if (intCount > 0) {
                    intCount = intCount - 1;
                }
                taskCount.setText(String.valueOf(intCount));

                storeCount2(intCount);

                totalDisplay();
            }
        });}

        public void totalDisplay(){
            TextView taskCount1 = (TextView) findViewById(R.id.task1Count);
            TextView taskCount2 = (TextView) findViewById(R.id.task2Count);
            int intCount1 = Integer.parseInt(taskCount1.getText().toString());
            int intCount2 = Integer.parseInt(taskCount2.getText().toString());
            int totalCount = (intCount1 + intCount2)*10;
            TextView totalCountView = (TextView) findViewById(R.id.totalCount);
            totalCountView.setText(String.valueOf(totalCount));
    }
    public void storeCount1(int intCount){
        SharedPreferences countStorage = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = countStorage.edit();
        editor.putInt("SPC1",intCount);
        editor.apply();
    }
    public void storeCount2(int intCount){
        SharedPreferences countStorage = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = countStorage.edit();
        editor.putInt("SPC2",intCount);
        editor.apply();
    }
    protected void onStart(){
        final SharedPreferences countStorage = PreferenceManager.getDefaultSharedPreferences(this);
        if(countStorage.getInt("SPC1",-1)%1 != 0){
            SharedPreferences.Editor editor = countStorage.edit();
            editor.putInt("SPC1",9);
            editor.putInt("SPC2",0);
            editor.apply();
        }

        int spCount1 = countStorage.getInt("SPC1",-1);
        System.out.println(spCount1);
        int spCount2 = countStorage.getInt("SPC2",-1);
        System.out.println(spCount2);

        TextView task1Count = (TextView) findViewById(R.id.task1Count);
        TextView task2Count = (TextView) findViewById(R.id.task2Count);

        task1Count.setText(String.valueOf(spCount1));
        task2Count.setText(String.valueOf(spCount2));

        totalDisplay();

        super.onStart();
    }
    }


