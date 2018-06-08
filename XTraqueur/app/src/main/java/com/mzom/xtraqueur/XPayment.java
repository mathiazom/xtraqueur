package com.mzom.xtraqueur;

import java.util.ArrayList;

class XPayment {

    private final ArrayList<XTaskCompletion> completions;

    private final double paymentValue;

    private long date;


    XPayment(ArrayList<XTaskCompletion> completions, double paymentValue, long date){

        this.completions = completions;
        this.paymentValue = paymentValue;
        this.date = date;
    }


    long getDate(){
        return this.date;
    }

    ArrayList<XTaskCompletion> getCompletions(){
        return this.completions;
    }

    double getPaymentValue(){
        return this.paymentValue;
    }


    void setDate(long date){
        this.date = date;
    }

}
