package com.mzom.xtraqueur;

import java.util.ArrayList;

/**
 * Class used when retrieving tasks data from drive
 *
 *  Since AsyncTask only allows one return value,
 *  this makes it possible to return both tasks and payments
 *
 ***/

public class XTasksDataPackage {

    private ArrayList<XTask> tasks;
    private ArrayList<XPayment> payments;

    XTasksDataPackage(){

    }

    XTasksDataPackage(ArrayList<XTask> tasks, ArrayList<XPayment> payments){
        this.tasks = tasks;
        this.payments = payments;
    }

    void setTasks(ArrayList<XTask> tasks){
        this.tasks = tasks;
    }

    void setPayments(ArrayList<XPayment> payments){
        this.payments = payments;
    }

    ArrayList<XTask> getTasks(){
        return this.tasks;
    }

    ArrayList<XPayment> getPayments(){
        return this.payments;
    }

}
