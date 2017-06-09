package com.xtraqueur.mzom.xtraqueur;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.vstechlab.easyfonts.EasyFonts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        invalidateOptionsMenu();
    }
    private Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        if(loadTasks().length() == 0){
            menu.findItem(R.id.action_edittasks).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_addTaskType:
                addTaskType();
                return true;

            case R.id.action_history:
                loadHistory();
                return true;

            case R.id.action_edittasks:
                editMode();
                return true;

            case R.id.action_editdone:
                reload();
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected void onStart(){

        if(((LinearLayout) findViewById(R.id.tasksLayout)).getChildCount() == 0){buildTasks();}

        ((TextView) findViewById(R.id.totalCount)).setText(String.valueOf(totalCount()) + "kr");

        super.onStart();
    }

    private void commitCount(TextView textCount, int taskNum, String sign)   {

        String taskName = getTaskProp(taskNum,"name");
        String taskCount = getTaskProp(taskNum,"count");

        String time = new SimpleDateFormat("hh:mm dd.MM").format(Calendar.getInstance().getTime());
        String timeD = new SimpleDateFormat("hh:mm:ss dd.MM").format(Calendar.getInstance().getTime());
        String histItem = taskName + " (" + time + ")";
        String histDetail = "Type: " + taskName + "\n" + "Tid: " + timeD;

        if(sign.equals("+")){
            taskCount = String.valueOf(Integer.parseInt(taskCount)+1);
            packHistory(histItem,histDetail);
        }else{
            taskCount = String.valueOf(Integer.parseInt(taskCount)-1);
        }

        editTask(taskNum,"count",taskCount);
        textCount.setText(taskCount);
        totalDisplay();
//        if(taskNum == 5){
//            histItem = taskName + " (" + taskInputInt + "kr)" + " (" + time + ")";
//            histDetail = "Type: " + taskName + "\n" + taskInputInt + "kr" + "\n" + "Tid: " + timeD;
//        }

    }

    private void buildTasks(){

        for(int h = 0;h<loadTasks().length();h++) {

            final int id = h;

            String taskName = getTaskProp(h,"name");
            String taskColor = getTaskProp(h,"col");
            String taskCount = getTaskProp(h,"count");

            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.new_task, null);

            TextView textView = (TextView) v.findViewById(R.id.task0Head);
            textView.setText(taskName);

            RelativeLayout taskLayout = (RelativeLayout) v.findViewById(R.id.task0Layout);
            taskLayout.setBackgroundColor(Color.parseColor(taskColor));

            TextView textCount = (TextView) v.findViewById(R.id.task0Count);
            textCount.setText(taskCount);

            final TextView textCountFin = textCount;

//          CLICK-LISTENERS

            ImageButton addTask = (ImageButton) v.findViewById(R.id.addButton0);
            addTask.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String sign = "+";
                commitCount(textCountFin,id,sign);
             }
            });

            ImageButton subTask = (ImageButton) v.findViewById(R.id.subButton0);
            subTask.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String sign = "-";
                    commitCount(textCountFin,id,sign);
                }
            });

            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.tasksLayout);
            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        }
    }

    private void addTaskType(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Ny oppgave");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_task_type,null);
        final EditText tne = (EditText) dialogView.findViewById(R.id.taskName);
        final EditText tfe = (EditText) dialogView.findViewById(R.id.taskFee);
        final Button cpb = (Button) dialogView.findViewById(R.id.pickColourButton);
        cpb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                colorPicker(cpb);
        }});
        builder.setView(dialogView);
        builder.setPositiveButton(("Legg til"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String taskName = tne.getEditableText().toString();
                        taskName = taskName.substring(0, 1).toUpperCase() + taskName.substring(1).toLowerCase();
                        int taskFee = Integer.parseInt(tfe.getEditableText().toString());
                        String taskCol = String.valueOf(cpb.getTag());
                        if(taskCol.equals("null")){
                            taskCol = "#0277bd";
                        }

                        newTask(taskName,taskCol,taskFee);

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);

//                        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                        View v = vi.inflate(R.layout.new_task, null);
//
//                        TextView textView = (TextView) v.findViewById(R.id.task0Head);
//                        textView.setText(taskName);
//
//                        textView.setOnClickListener(new View.OnClickListener() {
//                            public void onClick(View v) {
//                                editTaskType(context,id);
//                            }
//                        });
//
//                        RelativeLayout taskLayout = (RelativeLayout) v.findViewById(R.id.task0Layout);
//                        taskLayout.setBackgroundColor(Color.parseColor(taskCol));
//
//                        TextView textCount = (TextView) v.findViewById(R.id.task0Count);
//                        textCount.setText("0");
//
//                        final TextView textCountFin = textCount;
//
////                        ONCLICK-LISTENERS
//
//                        ImageButton addTask = (ImageButton) v.findViewById(R.id.addButton0);
//                        addTask.setOnClickListener(new View.OnClickListener() {
//                            public void onClick(View v) {
//                                String sign = "+";
//                                commitCount(textCountFin,id,sign);
//                            }
//                        });
//
//                        ImageButton subTask = (ImageButton) v.findViewById(R.id.subButton0);
//                        subTask.setOnClickListener(new View.OnClickListener() {
//                            public void onClick(View v) {
//                                String sign = "-";
//                                commitCount(textCountFin,id,sign);
//                            }
//                        });
//
//                        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.tasksLayout);
//                        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
                    }
                });
        builder.setNegativeButton(R.string.negDialogB, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void colorPicker(Button button){
        final Button cpb = button;

        cpb.setTag("");

        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Velg farge")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("Velg", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        cpb.setBackgroundColor(selectedColor);
                        String tagColor = "#" + Integer.toHexString(selectedColor);
                        cpb.setTag(tagColor);
                    }
                })
                .setNegativeButton(R.string.negDialogB, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    private void editTaskType(int taskNum){
        final Context context = this;

        final JSONArray tasksArray = loadTasks();
        final int id = taskNum;

        String taskName = getTaskProp(id,"name");
        String taskFee = getTaskProp(id,"fee");
        final String taskCol = getTaskProp(id,"col");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("Rediger oppgave");
        View dialogView = LayoutInflater.from(context).inflate(R.layout.edit_task_type,null);
        final EditText tne = (EditText) dialogView.findViewById(R.id.taskName);
        tne.setText(taskName, TextView.BufferType.EDITABLE);
        final EditText tfe = (EditText) dialogView.findViewById(R.id.taskFee);
        tfe.setText(taskFee, TextView.BufferType.EDITABLE);
        final Button cpb = (Button) dialogView.findViewById(R.id.pickColourButton);
        cpb.setBackgroundColor(Color.parseColor(taskCol));
        cpb.setTag(taskCol);
        cpb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                colorPicker(cpb);
            }});
        builder.setView(dialogView);
        builder.setPositiveButton(("Lagre"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String taskName = tne.getEditableText().toString();
                        taskName = taskName.substring(0, 1).toUpperCase() + taskName.substring(1).toLowerCase();
                        String taskFee = tfe.getEditableText().toString();
                        String newtaskCol = String.valueOf(cpb.getTag());
                        if(newtaskCol.equals("null")){
                            newtaskCol = taskCol;
                        }

                        editTask(id,"name",taskName);
                        editTask(id,"col",newtaskCol);
                        editTask(id,"fee",taskFee);
                        reload();
                    }
                });
        builder.setNegativeButton(R.string.negDialogB, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNeutralButton(("Slett type"),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearTask(tasksArray,id);
                reload();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button dP = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        dP.setTextColor(getResources().getColor(R.color.deny));
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

    private void loadHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void editMode(){

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.tasksLayout);
        insertPoint.removeAllViews();

        for(int h = 0;h<loadTasks().length();h++) {

            final int id = h;

            View v = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.edit_task, null);

            TextView textView = (TextView) v.findViewById(R.id.task0Head);
            textView.setText(getTaskProp(h, "name"));

            ImageButton editButton = (ImageButton) v.findViewById(R.id.editButton0);
            editButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    editTaskType(id);
                }
            });

            RelativeLayout taskLayout = (RelativeLayout) v.findViewById(R.id.task0Layout);
            taskLayout.setBackgroundColor(Color.parseColor(getTaskProp(h, "col")));
            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        menu.findItem(R.id.action_addTaskType).setVisible(false);
        menu.findItem(R.id.action_history).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_edittasks).setVisible(false);
        menu.findItem(R.id.action_editdone).setVisible(true);
    }
    //for(int i=1;i<totalTasks+1;i++){
//final int taskNum = i;
//final String countId = "task" + i + "Count";
//
//        String addButtonId = "addButton" + i;
//        String subButtonId = "subButton" + i;
//
//        if(i!=5){}
//          else{
//        ImageButton addTask = (ImageButton) findViewById(getResources().getIdentifier(addButtonId, "id", getPackageName()));
//        addTask.setOnClickListener(new View.OnClickListener() {
//public void onClick(View v) {
//        EditText taskInput = (EditText) findViewById(R.id.taskInput);
//        String taskInputValue = taskInput.getText().toString();
//        if(!taskInputValue.matches("")){
//        int taskInputInt = Integer.parseInt(taskInputValue);
//        TextView taskCount = (TextView) findViewById(getResources().getIdentifier(countId, "id", getPackageName()));
//        int intCount = Integer.parseInt(taskCount.getText().toString());
//        intCount = intCount + taskInputInt;
//        String sign = "+";
//        commitCount(taskCount,intCount,taskNum,totalTasks,sign,taskInputInt);
//        taskInput.setText("", TextView.BufferType.EDITABLE);
//        }
//        }
//        });
//        ImageButton subTask = (ImageButton) findViewById(getResources().getIdentifier(subButtonId, "id", getPackageName()));
//        subTask.setOnClickListener(new View.OnClickListener() {
//public void onClick(View v) {
//        EditText taskInput = (EditText) findViewById(R.id.taskInput);
//        String taskInputValue = taskInput.getText().toString();
//        if(!taskInputValue.matches("")){
//        int taskInputInt = Integer.parseInt(taskInputValue);
//        TextView taskCount = (TextView) findViewById(getResources().getIdentifier(countId, "id", getPackageName()));
//        int intCount = Integer.parseInt(taskCount.getText().toString());
//        if(intCount >= taskInputInt){
//        intCount = intCount - taskInputInt;
//        String sign = "-";
//        commitCount(taskCount,intCount,taskNum,totalTasks,sign,taskInputInt);
//        }
//        taskInput.setText("", TextView.BufferType.EDITABLE);}
//        }
//        });
//        }
//
//        tasksArray = loadTasks(this);
//
//        }
    private void totalDisplay(){
        TextView totalCountView = (TextView) findViewById(R.id.totalCount);
        totalCountView.setText(String.valueOf(totalCount()) + "kr");
    }

    private void reload(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private JSONArray loadTasks() {
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(this);
        String jsontemp = appStorage.getString("TaskStorage","");
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsontemp);
        } catch (JSONException e) {
            jsonArray = new JSONArray();
            e.printStackTrace();
        }

        return jsonArray;
    }

    private void newTask(String name, String col, int fee){
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(this);
        JSONArray tasksArray = loadTasks();

        JSONObject newtask = new JSONObject();
        try {
            newtask.put("name",name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            newtask.put("count",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            newtask.put("col",col);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            newtask.put("fee",fee);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        tasksArray.put(newtask);

        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", tasksArray.toString());
        prefsEditor.commit();
    }

    private void editTask(int taskNum, String taskProp,String propVal){
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(this);
        JSONArray tasksArray = loadTasks();

        JSONObject task = null;
        try {
            task = tasksArray.getJSONObject(taskNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(taskProp.equals("count")){
            int propCount = Integer.parseInt(propVal);

            try {
                assert task != null;
                task.put(taskProp,propCount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                assert task != null;
                task.put(taskProp,propVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", tasksArray.toString());
        prefsEditor.commit();
    }

    private String getTaskProp(int objectPos,String prop){
        JSONArray tasksArray = loadTasks();

        JSONObject task = null;
        try {
            task = tasksArray.getJSONObject(objectPos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String taskProp = "";
        try {
            assert task != null;
            taskProp = task.getString(prop);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return taskProp;
    }

    private int totalCount(){
        JSONArray tasksArray = loadTasks();

        int totalVal = 0;

        for(int y=0;y<tasksArray.length();y++){
            JSONObject temptask = null;
            try {
                temptask = tasksArray.getJSONObject(y);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int tempcountval = 0;
            try {
                assert temptask != null;
                tempcountval = Integer.parseInt(temptask.getString("count"))*Integer.parseInt(temptask.getString("fee"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            totalVal = totalVal + tempcountval;
        }

        return totalVal;


    }

    public void clearTasks(){
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", "");
        prefsEditor.commit();
    }

    private void clearTask(JSONArray taskArray,int taskNum){

        JSONArray taskArrayTemp = new JSONArray();

        for (int k = 0; k < taskArray.length(); k++)   {
            if (k != taskNum) {
                try {
                    taskArrayTemp.put(taskArray.get(k));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        taskArray = taskArrayTemp;

        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", taskArray.toString());
        prefsEditor.commit();
    }
}

