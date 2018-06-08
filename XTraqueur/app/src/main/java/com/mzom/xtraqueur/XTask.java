package com.mzom.xtraqueur;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

class XTask implements Serializable {

    // Object storing all task attributes (e.g. name, fee and color)
    private final XTaskFields taskFields;

    // Task payments
    private ArrayList<XTaskCompletion> completions;


    XTask(@NonNull XTaskFields taskFields) {
        this.taskFields = taskFields;
        this.completions = new ArrayList<>();
    }


    XTaskFields getTaskFields(){
        return this.taskFields;
    }

    ArrayList<XTaskCompletion> getCompletions() {

        return this.completions;
    }

    int getCompletionsCount() {

        if(completions == null) return 0;

        return this.completions.size();
    }

    double getValue() {
        return this.taskFields.getFee() * this.getCompletionsCount();
    }


    void setCompletions(@NonNull ArrayList<XTaskCompletion> completions){

        this.completions = completions;
    }


    // Register new completion of this task
    void newCompletion() {

        // Create tasks payments list if it doesn't exist
        if (this.completions == null) {
            this.completions = new ArrayList<>();
        }

        // Add new completion to list
        XTaskCompletion completion = new XTaskCompletion(new Date().getTime(), taskFields);
        this.completions.add(completion);
    }

    // Remove a registered completion from this task
    void removeCompletion(@NonNull XTaskCompletion completion){
        completions.remove(completion);
    }
}
