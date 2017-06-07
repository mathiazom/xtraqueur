package com.xtraqueur.mzom.xtraqueur;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.zip.Inflater;

/**
 * Created by Mathias on 06.06.2017.
 */

public class Task {
    String name;
    String col;
    int count;
    int fee;

//    MAIN

    public static JSONArray loadTasks(Context context) {
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(context);
        String jsontemp = appStorage.getString("TaskStorage","");
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsontemp);
        } catch (JSONException e) {
            jsonArray = new JSONArray();
            e.printStackTrace();
        }

        return jsonArray;
    }

    public static void newTask(Context context, String name, String col, int count, int fee){
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(context);
        String jsontemp = appStorage.getString("TaskStorage","");
        JSONArray jsonArray = null;
        if(jsontemp.equals("")){
            jsonArray = new JSONArray();
        }else {
            try {
                jsonArray = new JSONArray(jsontemp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject newtask = new JSONObject();
        try {
            newtask.put("name",name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            newtask.put("count",count);
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


        jsonArray.put(newtask);

        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", jsonArray.toString());
        prefsEditor.commit();
    }

    public static void editTask(Context context,int taskNum, String taskProp,String propVal){
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(context);
        String jsontemp = appStorage.getString("TaskStorage","");
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsontemp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject task = null;
        try {
            task = jsonArray.getJSONObject(taskNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(taskProp.equals("count")){
            int propCount = Integer.parseInt(propVal);

            try {
                task.put(taskProp,propCount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                task.put(taskProp,propVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", jsonArray.toString());
        prefsEditor.commit();

    }

    public static int totalCount(Context context,JSONArray jsonArray){
        int totalVal = 0;

        for(int y=0;y<jsonArray.length();y++){
            JSONObject temptask = null;
            try {
                temptask = jsonArray.getJSONObject(y);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int tempcountval = 0;
            try {
                tempcountval = Integer.parseInt(temptask.getString("count"))*Integer.parseInt(temptask.getString("fee"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            totalVal = totalVal + tempcountval;
        }

        return totalVal;


    }

    public static void clearTasks(Context context){
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", "");
        prefsEditor.commit();
    }

    public static void clearTask(Context context,JSONArray taskArray,int taskNum){

        JSONArray taskArrayTemp = new JSONArray();

        for (int k = 0; k < taskArray.length(); k++)   {
            if (k != 0) {
                try {
                    taskArrayTemp.put(taskArray.get(k));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        taskArray = taskArrayTemp;

        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", taskArray.toString());
        prefsEditor.commit();
    }

//    MISC

    public static String getTaskProp(JSONArray myTasks,int objectPos,String prop){
        JSONObject testtask = null;
        try {
            testtask = myTasks.getJSONObject(objectPos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String taskProp = "";
        try {
            taskProp = testtask.getString(prop);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return taskProp;
    }


}
