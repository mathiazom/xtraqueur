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
import android.text.Html;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

// TASK CLASS METHODS
import static com.xtraqueur.mzom.xtraqueur.Task.clearTask;
import static com.xtraqueur.mzom.xtraqueur.Task.clearTasks;
import static com.xtraqueur.mzom.xtraqueur.Task.editTask;
import static com.xtraqueur.mzom.xtraqueur.Task.getTaskProp;
import static com.xtraqueur.mzom.xtraqueur.Task.newTask;
import static com.xtraqueur.mzom.xtraqueur.Task.loadTasks;
import static com.xtraqueur.mzom.xtraqueur.Task.totalCount;

public class IndexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
    }
    private Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

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

            case R.id.action_edittasks:
                editMode(loadTasks(this));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected void onStart(){

//        clearTasks(this);
//
//        newTask(this,"Terminator","#212121",10,10);
//
//        editTask(this,0,"count","12");
//
//       clearTask(this,tasksArray,0);
//
//        tasksArray = loadTasks(this);
        if(((LinearLayout) findViewById(R.id.tasksLayout)).getChildCount() == 0){buildTasks(loadTasks(this));}

        ((TextView) findViewById(R.id.totalCount)).setText(String.valueOf(totalCount(this)) + "kr");

        super.onStart();
    }

    public void commitCount(TextView textCount, int taskNum, String sign)   {

        String taskName = getTaskProp(this,taskNum,"name");
        String taskCount = getTaskProp(this,taskNum,"count");

        String time = new SimpleDateFormat("hh:mm dd.MM").format(Calendar.getInstance().getTime());
        String timeD = new SimpleDateFormat("hh:mm:ss dd.MM").format(Calendar.getInstance().getTime());
        String histItem;
        String histDetail;

        histItem = taskName + " (" + time + ")";
        histDetail = "Type: " + taskName + "\n" + "Tid: " + timeD;

        if(sign.equals("+")){
            taskCount = String.valueOf(Integer.parseInt(taskCount)+1);
            packHistory(histItem,histDetail);
        }else{
            taskCount = String.valueOf(Integer.parseInt(taskCount)-1);
        }

        editTask(this,taskNum,"count",taskCount);

        textCount.setText(taskCount);

//        if(taskNum == 5){
//            histItem = taskName + " (" + taskInputInt + "kr)" + " (" + time + ")";
//            histDetail = "Type: " + taskName + "\n" + taskInputInt + "kr" + "\n" + "Tid: " + timeD;
//        }

        TextView totalCountView = (TextView) findViewById(R.id.totalCount);
        totalCountView.setText(String.valueOf(totalCount(this)) + "kr");
    }

    public void buildTasks(JSONArray tasksArray){

        final Context context = this;

        for(int h = 0;h<tasksArray.length();h++) {

            final int id = h;

            String taskName = getTaskProp(this,h,"name");
            String taskColor = getTaskProp(this,h,"col");
            String taskCount = getTaskProp(this,h,"count");

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
            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        }
    }

    private void addTaskType(){
        final Context context = this;

        JSONArray tasksArray = loadTasks(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("Ny oppgave");
        View dialogView = LayoutInflater.from(context).inflate(R.layout.add_task_type,null);
        final EditText tne = (EditText) dialogView.findViewById(R.id.taskName);
        final EditText tfe = (EditText) dialogView.findViewById(R.id.taskFee);
        final Button cpb = (Button) dialogView.findViewById(R.id.pickColourButton);
        cpb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                colorPicker(cpb);
        }});
        builder.setView(dialogView);
        builder.setPositiveButton(Html.fromHtml("<font color='#FF7F27'>Legg til</font>"),
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

                        newTask(context,taskName,taskCol,0,taskFee);

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
        builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void colorPicker(Button button){
        final Context context = this;
        final Button cpb = button;

        cpb.setTag("");

        ColorPickerDialogBuilder
                .with(context)
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
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    public void editTaskType(Context context,int taskNum){
        final Context contextFin = context;

        final JSONArray tasksArray = loadTasks(context);
        final int id = taskNum;

        String taskName = getTaskProp(this,id,"name");
        String taskFee = getTaskProp(this,id,"fee");
        final String taskCol = getTaskProp(this,id,"col");

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
        builder.setPositiveButton(Html.fromHtml("<font color='#FF7F27'>Lagre</font>"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String taskName = tne.getEditableText().toString();
                        taskName = taskName.substring(0, 1).toUpperCase() + taskName.substring(1).toLowerCase();
                        int taskFee = Integer.parseInt(tfe.getEditableText().toString());
                        String newtaskCol = String.valueOf(cpb.getTag());
                        if(newtaskCol.equals("null")){
                            newtaskCol = taskCol;
                        }

                        editTask(contextFin,id,"name",taskName);
                        editTask(contextFin,id,"col",newtaskCol);
                        editTask(contextFin,id,"fee",String.valueOf(taskFee));

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNeutralButton(Html.fromHtml("<font color='#FF7F27'>Slett type</font>"),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println(id);
                clearTask(contextFin,tasksArray,id);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button dP = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        dP.setTextColor(Color.parseColor("#f44242"));
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

    public void editMode(JSONArray tasksArray){

        final Context context = this;

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.tasksLayout);
        insertPoint.removeAllViews();

        for(int h = 0;h<tasksArray.length();h++) {

            final int id = h;

            String taskName = getTaskProp(this, h, "name");
            String taskColor = getTaskProp(this, h, "col");

            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.edit_task, null);

            TextView textView = (TextView) v.findViewById(R.id.task0Head);
            textView.setText(taskName);

            ImageButton editButton = (ImageButton) v.findViewById(R.id.editButton0);
            editButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    editTaskType(context,id);
                }
            });

            RelativeLayout taskLayout = (RelativeLayout) v.findViewById(R.id.task0Layout);
            taskLayout.setBackgroundColor(Color.parseColor(taskColor));
            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
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
}

