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

    int completionsOfTask(final XTask task){

        int total = 0;

        for(XTaskCompletion completion : getCompletions()){

            if(completion.getTask().getName().equals(task.getName())){
                total += 1;
            }

        }

        return total;
    }

    ArrayList<XTask> getTasks(){
        ArrayList<XTask> tasks = new ArrayList<>();

        for(XTaskCompletion completion : getCompletions()){

            boolean notAdded = true;

            for(XTask task : tasks){
                if(task.getName().equals(completion.getTask().getName())){
                    notAdded = false;
                    break;
                }
            }

            if(notAdded){
                tasks.add(completion.getTask());
            }

        }

        return tasks;
    }

    double getPaymentValue(){
        return this.paymentValue;
    }

    void setPaymentDate(long paymentDate){
        this.paymentDate = paymentDate;
    }

}
