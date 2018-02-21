package com.mzom.xtraqueur;

import java.util.ArrayList;
import java.util.Date;

class XTask {

    // Task display name
    private String name;

    // Task completions count
    private int completions;

    // Task fee for every completion
    private int fee;

    // Task color used in the app UI
    private int color;

    // Task completions
    private ArrayList<Long> completionsList;


    XTask(String name, int completions, int fee, int color) {
        this.name = name;
        this.completions = completions;
        this.fee = fee;
        this.color = color;
    }


    String getName() {
        return this.name;
    }

    int getFee() {
        return this.fee;
    }

    int getColor() {
        return this.color;
    }

    ArrayList<Long> getCompletionsList() {
        return this.completionsList;
    }

    int getCompletions() {
        return this.completions;
    }

    int getValue() {
        return this.fee * this.completions;
    }


    void setName(String name) {
        this.name = name;
    }

    void setFee(int fee) {
        this.fee = fee;
    }

    void setColor(int color) {
        this.color = color;
    }

    void setCompletionsList(ArrayList<Long> completionsList) {
        this.completionsList = completionsList;
        this.completions = completionsList.size();
    }


    void addToCompletions() {

        // Create tasks completions list if it doesn't exist
        if (this.completionsList == null) {
            this.completionsList = new ArrayList<>();
        }

        // Add new completion to list
        Long date = new Date().getTime();
        this.completionsList.add(date);

        // Update completions count
        this.completions = completionsList.size();
    }

    void removeCompletions() {
        this.completions = 0;
    }
}
