package com.mzom.xtraqueur;

import android.support.annotation.NonNull;

class XTaskCompletion {

    // Time for when this completion was registered
    private long date;

    // Attributes of the completion's associated task
    private XTaskFields taskFields;

    XTaskCompletion(long date, final XTaskFields taskFields) {
        this.date = date;
        this.taskFields = taskFields;
    }


    long getDate() {
        return this.date;
    }

    XTaskFields getTaskFields(){
        return this.taskFields;
    }


    void setDate(Long date){
        this.date = date;
    }

    void setTaskFields(@NonNull XTaskFields taskFields){

        this.taskFields = taskFields;

    }

}
