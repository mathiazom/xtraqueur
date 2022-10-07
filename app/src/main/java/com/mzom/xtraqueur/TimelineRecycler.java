package com.mzom.xtraqueur;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class TimelineRecycler extends RecyclerView {

    public TimelineRecycler(Context context, AttributeSet attrs){
        super(context,attrs);

        initRecycler();
    }

    public TimelineRecycler(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);

        initRecycler();
    }

    public TimelineRecycler(Context context) {

        super(context);

        initRecycler();
    }

    private void initRecycler(){
        setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        setLayoutManager(mLayoutManager);
    }

    public void setAdapter(TimelineAdapter timelineAdapter){
        // Adapter to manage item data visualizing
        super.setAdapter(timelineAdapter);
    }

    public void startLayoutAnimation(){
        scheduleLayoutAnimation();
    }

}
