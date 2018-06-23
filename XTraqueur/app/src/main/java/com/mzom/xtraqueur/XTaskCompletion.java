package com.mzom.xtraqueur;

import android.support.annotation.NonNull;
import android.util.Log;

class XTaskCompletion {

    // Time for when this completion was registered
    private long date;

    // Attributes of the completion's associated task
    private XTaskIdentity taskIdentity;

    XTaskCompletion(long date, final XTaskIdentity taskIdentity) {
        this.date = date;
        this.taskIdentity = taskIdentity;
    }


    long getDate() {
        return this.date;
    }

    XTaskIdentity getTaskIdentity(){
        return this.taskIdentity;
    }


    void setDate(Long date){
        this.date = date;
    }

    void setTaskIdentity(@NonNull XTaskIdentity taskIdentity){

        this.taskIdentity = taskIdentity;

    }

}
