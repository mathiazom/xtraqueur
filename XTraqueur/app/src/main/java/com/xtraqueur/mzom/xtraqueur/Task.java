package com.xtraqueur.mzom.xtraqueur;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mathias on 06.06.2017.
 */

public class Task {

    public static JSONArray loadTasks(Context context) {
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(context);
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

    public static void newTask(Context context, String name, String col, int count, int fee){
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(context);
        JSONArray tasksArray = loadTasks(context);

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


        tasksArray.put(newtask);

        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", tasksArray.toString());
        prefsEditor.commit();
    }

    public static void editTask(Context context,int taskNum, String taskProp,String propVal){
        SharedPreferences appStorage = PreferenceManager.getDefaultSharedPreferences(context);
        JSONArray tasksArray = loadTasks(context);

        JSONObject task = null;
        try {
            task = tasksArray.getJSONObject(taskNum);
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

        System.out.println(tasksArray);

        SharedPreferences.Editor prefsEditor = appStorage.edit();
        prefsEditor.putString("TaskStorage", tasksArray.toString());
        prefsEditor.commit();
    }

    public static String getTaskProp(Context context,int objectPos,String prop){
        JSONArray tasksArray = loadTasks(context);

        JSONObject testtask = null;
        try {
            testtask = tasksArray.getJSONObject(objectPos);
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

    public static int totalCount(Context context){
        JSONArray tasksArray = loadTasks(context);

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
            if (k != taskNum) {
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

}
