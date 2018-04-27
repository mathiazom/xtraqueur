package com.mzom.xtraqueur;

class TimelineItem {

    private String title;
    private int color;
    private long date;

    TimelineItem(String title, int color, long date){
        this.title = title;
        this.color = color;
        this.date = date;
    }

    String getTitle(){
        return this.title;
    }

    int getColor(){
        return this.color;
    }

    long getDate(){
        return this.date;
    }

    void setTitle(String title){
        this.title = title;
    }

    void setColor(int color){
        this.color = color;
    }

    void setDate(long date){
        this.date = date;
    }
}

