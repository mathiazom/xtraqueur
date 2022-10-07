package com.mzom.xtraqueur;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

// TODO: Implement hashCode() since we are implementing equals()
class XTaskIdentity implements Serializable {

    // Task display name
    private String name;

    // Task fee for every completion
    private double fee;

    // Task color used in the app UI
    private int color;


    XTaskIdentity(String name, double fee, int color) {
        this.name = name;
        this.fee = fee;
        this.color = color;
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

    // Searches XTaskCompletion data set to find completions with XTaskIdentity equalling this instance
    ArrayList<XTaskCompletion> findCompletions(ArrayList<XTaskCompletion> allCompletions){

        ArrayList<XTaskCompletion> filteredCompletions = new ArrayList<>();

        for(XTaskCompletion completion : allCompletions){

            if(this.equals(completion.getTaskIdentity())){
                filteredCompletions.add(completion);
            }

        }

        return filteredCompletions;

    }

    // Searches completions data set to find completions with XTaskIdentity equalling this instance
    int getCompletionsCount(ArrayList<XTaskCompletion> completions){

        int total = 0;

        for(XTaskCompletion completion : completions){

            if(this.equals(completion.getTaskIdentity())){
                total += 1;
            }

        }

        return total;

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

    @Override
    public boolean equals(Object obj) {

        // Self check
        if (this == obj) return true;

        // Null and type check
        if (obj == null || this.getClass() != obj.getClass()) return false;

        // Argument is instance of XTaskIdentity and can be casted
        XTaskIdentity taskIdentity = (XTaskIdentity) obj;

        // Main equality check
        return Objects.equals(this.getName(), taskIdentity.getName()) &&
                Objects.equals(this.getFee(), taskIdentity.getFee()) &&
                Objects.equals(this.getColor(), taskIdentity.getColor());

    }
}
