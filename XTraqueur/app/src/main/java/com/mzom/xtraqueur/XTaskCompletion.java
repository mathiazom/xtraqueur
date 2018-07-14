package com.mzom.xtraqueur;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

class XTaskCompletion {

    // Time for when this completion was registered
    private long date;

    // Attributes of the completion's associated task
    private XTaskIdentity taskIdentity;

    private boolean isInstantCompletion;

    XTaskCompletion(long date, final XTaskIdentity taskIdentity, boolean isInstantCompletion) {
        this.date = date;
        this.taskIdentity = taskIdentity;
        this.isInstantCompletion = isInstantCompletion;
    }


    long getDate() {
        return this.date;
    }

    XTaskIdentity getTaskIdentity(){
        return this.taskIdentity;
    }

    boolean isInstantCompletion(){
        return isInstantCompletion;
    }

    // Search tasks data set for XTask with XTaskIdentity equalling XTaskIdentity of this completion
    // (returns null if no such task is found)
    @Nullable
    XTask findTask(ArrayList<XTask> tasks){

        if(isInstantCompletion) return null;

        for(XTask task : tasks){
            if(taskIdentity.equals(task.getTaskIdentity())){
                return task;
            }
        }
        return null;
    }


    void setDate(Long date){
        this.date = date;
    }

    void setTaskIdentity(@NonNull XTaskIdentity taskIdentity){

        this.taskIdentity = taskIdentity;

    }

}
