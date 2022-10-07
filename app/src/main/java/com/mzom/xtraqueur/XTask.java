package com.mzom.xtraqueur;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

class XTask implements Serializable {

    // Object storing all task attributes (e.g. name, fee and color)
    private XTaskIdentity taskIdentity;

    // Task payments
    private ArrayList<XTaskCompletion> completions;


    XTask(@NonNull XTaskIdentity taskIdentity) {
        this.taskIdentity = taskIdentity;
        this.completions = new ArrayList<>();
    }

    XTask(String name, double fee, int color){
        this.taskIdentity = new XTaskIdentity(name, fee, color);
    }


    XTaskIdentity getTaskIdentity(){
        return this.taskIdentity;
    }

    ArrayList<XTaskCompletion> getCompletions() {

        return this.completions;
    }

    int getCompletionsCount() {

        if(completions == null) return 0;

        return this.completions.size();
    }

    double getValue() {
        return this.taskIdentity.getFee() * this.getCompletionsCount();
    }


    void setTaskIdentity(XTaskIdentity taskIdentity){
        this.taskIdentity = taskIdentity;
    }

    void setCompletions(@NonNull ArrayList<XTaskCompletion> completions){

        this.completions = completions;
    }

    // Register new completion of this task
    void registerCompletion(){
        registerCompletion(new Date());
    }

    void registerCompletion(Date date) {

        // Create tasks payments list if it doesn't exist
        if (this.completions == null) {
            this.completions = new ArrayList<>();
        }

        // Add new completion to list
        XTaskCompletion completion = new XTaskCompletion(date.getTime(), taskIdentity, false);
        this.completions.add(completion);
    }

    // Remove a registered completion from this task
    void removeCompletion(@NonNull XTaskCompletion completion){
        completions.remove(completion);
    }

    @Override
    public String toString() {

        return super.toString() + " - Name: " + getTaskIdentity().getName() + ", Fee: " + getTaskIdentity().getFee() + ", Color: " + getTaskIdentity().getColor() + ", Completions: " + completions.toString();
    }
}
