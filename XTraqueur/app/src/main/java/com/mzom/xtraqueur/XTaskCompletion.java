package com.mzom.xtraqueur;

class XTaskCompletion {

    private long date;
    private final XTask task;
    private final int index;

    XTaskCompletion(long date, XTask task) {
        this.date = date;
        this.task = task;
        this.index = task.getCompletions().indexOf(date);
    }

    long getDate() {
        return this.date;
    }

    XTask getTask(){
        return this.task;
    }

    int getIndex() {
        return this.index;
    }

    void setDate(Long date){
        this.date = date;
    }

}
