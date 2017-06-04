package com.xtraqueur.mzom.xtraqueur;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.vstechlab.easyfonts.EasyFonts;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class IndexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addTaskType:
                addTaskType();
                return true;

            case R.id.action_history:
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected void onStart(){

        final int totalTasks = 5;

        for(int f=1;f<totalTasks+1;f++){
            String headFontId = "task" + f + "Head";
            TextView headFont= (TextView) findViewById(getResources().getIdentifier(headFontId, "id", getPackageName()));
            headFont.setTypeface(EasyFonts.robotoLight(this));

            String countFontId = "task" + f + "Count";
            TextView countFont = (TextView) findViewById(getResources().getIdentifier(countFontId, "id", getPackageName()));
            countFont.setTypeface(EasyFonts.robotoLight(this));
        }

        for(int i=1;i<totalTasks+1;i++){
            final int taskNum = i;
            final String countId = "task" + i + "Count";

            String addButtonId = "addButton" + i;
            String subButtonId = "subButton" + i;

            if(i!=5){
                ImageButton addTask = (ImageButton) findViewById(getResources().getIdentifier(addButtonId, "id", getPackageName()));
                addTask.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        TextView taskCount = (TextView) findViewById(getResources().getIdentifier(countId, "id", getPackageName()));
                        int intCount = Integer.parseInt(taskCount.getText().toString());
                        intCount = intCount + 1;
                        String sign = "+";
                        commitCount(taskCount,intCount,taskNum,totalTasks,sign,0);
                    }
                });
                ImageButton subTask = (ImageButton) findViewById(getResources().getIdentifier(subButtonId, "id", getPackageName()));
                subTask.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        TextView taskCount = (TextView) findViewById(getResources().getIdentifier(countId, "id", getPackageName()));
                        int intCount = Integer.parseInt(taskCount.getText().toString());
                        if(intCount > 0){
                            intCount = intCount - 1;
                            String sign = "-";
                            commitCount(taskCount,intCount,taskNum,totalTasks,sign,0);
                        }

                    }
                });
            }else{
                ImageButton addTask = (ImageButton) findViewById(getResources().getIdentifier(addButtonId, "id", getPackageName()));
                addTask.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        EditText taskInput = (EditText) findViewById(R.id.taskInput);
                        String taskInputValue = taskInput.getText().toString();
                        if(!taskInputValue.matches("")){
                            int taskInputInt = Integer.parseInt(taskInputValue);
                            TextView taskCount = (TextView) findViewById(getResources().getIdentifier(countId, "id", getPackageName()));
                            int intCount = Integer.parseInt(taskCount.getText().toString());
                            intCount = intCount + taskInputInt;
                            String sign = "+";
                            commitCount(taskCount,intCount,taskNum,totalTasks,sign,taskInputInt);
                            taskInput.setText("", TextView.BufferType.EDITABLE);
                        }
                    }
                });
                ImageButton subTask = (ImageButton) findViewById(getResources().getIdentifier(subButtonId, "id", getPackageName()));
                subTask.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        EditText taskInput = (EditText) findViewById(R.id.taskInput);
                        String taskInputValue = taskInput.getText().toString();
                        if(!taskInputValue.matches("")){
                        int taskInputInt = Integer.parseInt(taskInputValue);
                        TextView taskCount = (TextView) findViewById(getResources().getIdentifier(countId, "id", getPackageName()));
                        int intCount = Integer.parseInt(taskCount.getText().toString());
                        if(intCount >= taskInputInt){
                            intCount = intCount - taskInputInt;
                            String sign = "-";
                            commitCount(taskCount,intCount,taskNum,totalTasks,sign,taskInputInt);
                        }
                        taskInput.setText("", TextView.BufferType.EDITABLE);}
                    }
                });
            }


        }

        SharedPreferences countStorage = PreferenceManager.getDefaultSharedPreferences(this);

        for(int t=0;t<totalTasks+1;t++){
            String storageId = "SPC" + t;
            if(countStorage.getInt(storageId,-1) == -1){
                SharedPreferences.Editor editor = countStorage.edit();
                editor.putInt(storageId,0);
                editor.apply();
            }
        }

        totalDisplay(totalTasks);

        super.onStart();
    }

    public void commitCount(TextView taskCount,int intCount, int taskNum, int totalTasks, String sign,int taskInputInt){
        SharedPreferences countStorage = PreferenceManager.getDefaultSharedPreferences(this);

        String[] taskHeads = new String[6];

        taskHeads[0] = "";
        taskHeads[1] = "Vaskemaskin";
        taskHeads[2] = "Oppvask";
        taskHeads[3] = "Bad";
        taskHeads[4] = "Annet";
        taskHeads[5] = "Manuelt";

        taskCount.setText(String.valueOf(intCount));
        String storageId = "SPC" + taskNum;
        SharedPreferences.Editor editor = countStorage.edit();
        editor.putInt(storageId,intCount);
        editor.apply();

        String time = new SimpleDateFormat("hh:mm dd.MM").format(Calendar.getInstance().getTime());
        String timeD = new SimpleDateFormat("hh:mm:ss dd.MM").format(Calendar.getInstance().getTime());
        String histItem;
        String histDetail;

        if(taskNum == 5){
            histItem = taskHeads[taskNum] + " (" + taskInputInt + "kr)" + " (" + time + ")";
            histDetail = "Type: " + taskHeads[taskNum] + "\n" + taskInputInt + "kr" + "\n" + "Tid: " + timeD;
        }else{
            histItem = taskHeads[taskNum] + " (" + time + ")";
            histDetail = "Type: " + taskHeads[taskNum] + "\n" + "Tid: " + timeD;
        }

        totalDisplay(totalTasks);

        if(sign.equals("+")){
            packHistory(histItem,histDetail);
        }
    }

    public void totalDisplay(int totalTasks){
        SharedPreferences countStorage = PreferenceManager.getDefaultSharedPreferences(this);

        int totalCount = 0;

        int[] taskFees = new int[totalTasks+1];

        taskFees[0]=0;
        taskFees[1]=10;
        taskFees[2]=10;
        taskFees[3]=50;
        taskFees[4]=10;
        taskFees[5]=1;

        for(int j=1;j<totalTasks+1;j++){
            String storageId = "SPC" + j;
            String countId = "task" + j + "Count";
            int spCount = countStorage.getInt(storageId,-1);
            TextView taskCount = (TextView) findViewById(getResources().getIdentifier(countId, "id", getPackageName()));
            taskCount.setText(String.valueOf(spCount));
            totalCount = totalCount + (spCount*taskFees[j]);
        }

        TextView totalCountView = (TextView) findViewById(R.id.totalCount);
        totalCountView.setText(String.valueOf(totalCount) + "kr");
    }

    public void loadHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void packHistory(String histItem,String histDetail) {
        SharedPreferences countStorage = PreferenceManager.getDefaultSharedPreferences(this);
        String histStorage = countStorage.getString("histStorage","");
        String histDetailStorage = countStorage.getString("histDetailStorage","");

        List<String> histList = new ArrayList<>(Arrays.asList(histStorage.split(",")));
        List<String> histDetailList = new ArrayList<>(Arrays.asList(histDetailStorage.split(",")));

        histList.add(histItem);
        histDetailList.add(histDetail);

        histStorage = histItem + "," + histStorage;
        histDetailStorage = histDetail + "," + histDetailStorage;

        SharedPreferences.Editor editor = countStorage.edit();
        editor.putString("histStorage", histStorage);
        editor.putString("histDetailStorage", histDetailStorage);
        editor.apply();

    }

    private void addTaskType(){
        final Context context = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("Legg til en ny oppgave-type");
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.add_task_type,null);
        Button cpb = (Button) view.findViewById(R.id.pickColourButton);
        System.out.println(cpb.getBackground());
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#FF7F27'>Legg til</font>"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void colorPicker(View v){
        System.out.println("test");
        /*final Context context = this;

        ColorPickerDialogBuilder
                .with(context)
                .setTitle("Velg farge")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("Velg", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                    }
                })
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();*/
    }


}


