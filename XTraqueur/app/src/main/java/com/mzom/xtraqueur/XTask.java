package com.mzom.xtraqueur;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

class XTask implements Serializable {

    // Task display name
    private String name;

    // Task fee for every completion
    private double fee;

    // Task color used in the app UI
    private int color;

    // Task completions
    private ArrayList<Long> completionsList;


    XTask(String name, double fee, int color) {
        this.name = name;
        this.fee = fee;
        this.color = color;
        this.completionsList = new ArrayList<>();
    }


    String getName() {
        return this.name;
    }

    double getFee() {
        return this.fee;
    }

    int getColor() {
        return this.color;
    }

    ArrayList<Long> getCompletions() {
        return this.completionsList;
    }

    int getCompletionsCount() {
        return this.completionsList.size();
    }

    double getValue() {
        return this.fee * this.getCompletionsCount();
    }

    void setName(String name) {
        this.name = name;
    }

    void setFee(double fee) {
        this.fee = fee;
    }

    void setColor(int color) {
        this.color = color;
    }

    void setCompletionsList(ArrayList<Long> completionsList) {
        this.completionsList = completionsList;
    }

    void addToCompletions() {

        // Create tasks completions list if it doesn't exist
        if (this.completionsList == null) {
            this.completionsList = new ArrayList<>();
        }

        // Add new completion to list
        Long date = new Date().getTime();
        this.completionsList.add(date);
    }

    void removeCompletion(Long completion){
        this.completionsList.remove(completion);
    }
}
