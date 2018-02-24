package com.mzom.xtraqueur;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by elev on 23.02.2018.
 */

public class TimelineFragment extends Fragment {

    // Fragment main views
    private View view;

    private Toolbar mToolbar;

    ArrayList<XTask> tasks;

    // RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    TimelineFragmentListener mTimelineFragmentListener;

    interface TimelineFragmentListener{
        void popBackStackFromFragment();
    }

    public static TimelineFragment newInstance(ArrayList<XTask> tasks) {

        TimelineFragment fragment = new TimelineFragment();
        fragment.tasks = tasks;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_timeline,container,false);

        initToolbar();

        loadTimeline();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mTimelineFragmentListener = (TimelineFragmentListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TimelineFragmentListener");
        }
    }

    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Initialize toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_timeline_fragment);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimelineFragmentListener.popBackStackFromFragment();
            }
        });
    }

    private void loadTimeline(){

        // Load recycler view
        mRecyclerView = view.findViewById(R.id.timeline_recycler);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<XTaskCompletion> completions = new ArrayList<>();

        for(XTask t : tasks){
            for(Long l : t.getCompletionsList()){
                completions.add(new XTaskCompletion(l,t.getName(),t.getColor()));
            }
        }

        Collections.sort(completions,new Comparator<XTaskCompletion>() {
            @Override
            public int compare(XTaskCompletion xTaskCompletion, XTaskCompletion t1) {
                return Long.compare(t1.getDate(),xTaskCompletion.getDate());
            }
        });

        mAdapter = new TimelineAdapter(completions);
        mRecyclerView.setAdapter(mAdapter);

    }
}
