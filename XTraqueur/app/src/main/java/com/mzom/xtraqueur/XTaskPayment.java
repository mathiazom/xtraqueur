package com.mzom.xtraqueur;

import java.util.ArrayList;

class XTaskPayment {

    private final ArrayList<XTaskCompletion> completions;
    private double paymentValue;
    private long paymentDate;

    XTaskPayment(ArrayList<XTaskCompletion> completions,double paymentValue, long paymentDate){
        this.completions = completions;
        this.paymentValue = paymentValue;
        this.paymentDate = paymentDate;
    }

    long getPaymentDate(){
        return this.paymentDate;
    }

    ArrayList<XTaskCompletion> getCompletions(){
        return this.completions;
    }

    double getPaymentValue(){
        return this.paymentValue;
    }

    void setPaymentDate(long paymentDate){
        this.paymentDate = paymentDate;
    }

}
