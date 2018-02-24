package com.mzom.xtraqueur;

/**
 * Created by elev on 23.02.2018.
 */

public class XTaskCompletion {

    private long date;
    private String taskName;
    private int taskColor;

    public XTaskCompletion(long date,String taskName,int taskColor){
        this.date = date;
        this.taskName = taskName;
        this.taskColor = taskColor;
    }

    long getDate(){
        return this.date;
    }

    String getTaskName(){
        return this.taskName;
    }

    int getTaskColor(){
        return this.taskColor;
    }

}
