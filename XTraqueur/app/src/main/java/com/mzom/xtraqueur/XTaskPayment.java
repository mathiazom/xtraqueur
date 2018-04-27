package com.mzom.xtraqueur;

import java.util.ArrayList;

class XTaskPayment {

    private final ArrayList<XTaskCompletion> completions;

    private long paymentDate;

    XTaskPayment(ArrayList<XTaskCompletion> completions,long paymentDate){
        this.completions = completions;
        this.paymentDate = paymentDate;
    }

    long getPaymentDate(){
        return this.paymentDate;
    }

    ArrayList<XTaskCompletion> getCompletions(){
        return this.completions;
    }

    void setPaymentDate(long paymentDate){
        this.paymentDate = paymentDate;
    }

}
