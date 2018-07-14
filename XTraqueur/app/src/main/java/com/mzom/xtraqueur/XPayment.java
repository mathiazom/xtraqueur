package com.mzom.xtraqueur;

import java.util.ArrayList;

class XPayment {

    private ArrayList<XTaskCompletion> completions;

    private long date;


    XPayment(ArrayList<XTaskCompletion> completions, long date){

        this.completions = completions;
        this.date = date;
    }


    long getDate(){
        return this.date;
    }

    ArrayList<XTaskCompletion> getCompletions(){
        return this.completions;
    }

    double getPaymentValue(){

        double value = 0;

        for(XTaskCompletion completion : completions){

            value += completion.getTaskIdentity().getFee();

        }

        return value;
    }

    void deleteCompletion(XTaskCompletion completion) {

        int index = completions.indexOf(completion);
        if (index == -1) return;

        completions.remove(index);
    }


    void setCompletions(ArrayList<XTaskCompletion> completions){
        this.completions = completions;
    }

    void setDate(long date){
        this.date = date;
    }

}
