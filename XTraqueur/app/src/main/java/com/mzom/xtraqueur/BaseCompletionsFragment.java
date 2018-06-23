package com.mzom.xtraqueur;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

abstract class BaseCompletionsFragment extends XFragment implements CompletionsTimelineAdapter.CompletionsTimelineAdapterListener {



    abstract void deleteCompletion(XTaskCompletion completion, OnSuccessListener<DriveFile> onSuccessListener);

    abstract void editCompletionDate(XTaskCompletion completion, long completionDate, OnSuccessListener<DriveFile> onSuccessListener);

    abstract int getSelectionMenuResId();

    abstract Toolbar.OnMenuItemClickListener getSelectionMenuItemClickListener();

    abstract void onDataSetChanged(OnSuccessListener<DriveFile> onSuccessListener);



    private static final String TAG = "XTQ-BaseCompletions";

    // Fragment main views
    private View view;

    private ArrayList<XTaskIdentity> allTaskIdentities;
    private ArrayList<XTaskIdentity> completedTaskIdentities;

    private ArrayList<XTaskCompletion> allCompletions;
    private ArrayList<XTaskCompletion> filteredCompletions;

    private CompletionsTimelineAdapter mAdapter;

    private XTaskIdentity filterTaskIdentity;

    private Toolbar mToolbar;
    private Toolbar mSelectionModeToolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        Log.i(TAG,"OnCreateView");

        this.view = inflater.inflate(R.layout.fragment_completions, container, false);

        initToolbar();

        this.completedTaskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(allCompletions);

        loadCompletions();

        return view;
    }


    CompletionsTimelineAdapter getAdapter(){
        return this.mAdapter;
    }


    final void setAllCompletions(ArrayList<XTaskCompletion> completions){
        this.allCompletions = completions;
    }

    final void setAllTaskIdentities(ArrayList<XTaskIdentity> taskIdentities){
        this.allTaskIdentities = taskIdentities;
    }

    final void setFilterTaskIdentity(XTaskIdentity taskIdentity){
        this.filterTaskIdentity = taskIdentity;
    }

    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Initialize regular toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_completions_fragment);

        // Completions tasks filter dialog
        final LinearLayout taskFilterContainer = mToolbar.findViewById(R.id.task_filter_field);
        taskFilterContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TasksFilterDialog(getContext(), filterTaskIdentity, allCompletions,allTaskIdentities, completedTaskIdentities, mAdapter, new TasksFilterDialog.TasksFilterDialogListener() {
                    @Override
                    public void onFilter(XTaskIdentity filterIdentity) {
                        filterTaskIdentity = filterIdentity;
                        displayCurrentTasksFilter(filterIdentity);
                    }
                }).show();
            }
        });

        // Init filter UI
        displayCurrentTasksFilter(filterTaskIdentity);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoader.reverseLoading(getContext());
            }
        });

        // Selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);

        // Hide when not using selection mode
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Add action buttons to toolbar from menu resource
        mSelectionModeToolbar.inflateMenu(getSelectionMenuResId());

        // Selection icons listener
        mSelectionModeToolbar.setOnMenuItemClickListener(getSelectionMenuItemClickListener());

        // Cancel selection icon listener
        mSelectionModeToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.setUniversalItemSelection(false);
            }
        });
    }

    // Display all filteredCompletions sorted from newest to oldest
    final void loadCompletions() {

        Log.i(TAG,"Loading " + String.valueOf(allCompletions.size()) + " completions: " + new Gson().toJson(allCompletions));

        // Load recycler view
        final TimelineRecycler mRecyclerView = view.findViewById(R.id.completions_recycler);

        // Filtered for single task (from EditTaskFragment)
        if (filterTaskIdentity != null) {

            filteredCompletions = XTaskUtilities.getCompletionsFromTaskIdentity(filterTaskIdentity,allCompletions);

        } else {
            filteredCompletions = allCompletions;
        }

        // Sort filteredCompletions based on recency
        Collections.sort(filteredCompletions, new Comparator<XTaskCompletion>() {
            @Override
            public int compare(XTaskCompletion xTaskCompletion, XTaskCompletion t1) {
                return Long.compare(t1.getDate(), xTaskCompletion.getDate());
            }
        });

        // Display filteredCompletions in RecyclerView with Adapter
        mAdapter = new CompletionsTimelineAdapter(filteredCompletions, this);

        mRecyclerView.setAdapter(mAdapter);

        // Item loading animation
        mRecyclerView.startLayoutAnimation();

    }

    private void editCompletion(int position){

        final XTaskCompletion completion = filteredCompletions.get(position);

        FragmentLoader.loadFragment(EditCompletionFragment.newInstance(completion, new EditCompletionFragment.EditCompletionFragmentListener() {
            @Override
            public void onEditCompletionDate(XTaskCompletion completion, long editedCompletionDate, final EditCompletionFragment.OnFinishedListener onFinishedListener) {
                editCompletionDate(completion, editedCompletionDate, new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        onFinishedListener.onFinished();
                    }
                });
            }

            @Override
            public void onDeleteCompletion(XTaskCompletion completion, final EditCompletionFragment.OnFinishedListener onFinishedListener) {
                deleteCompletion(completion, new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        onFinishedListener.onFinished();
                    }
                });
            }
        }),getContext());

    }

    // Visually display current tasks filter
    private void displayCurrentTasksFilter(@Nullable XTaskIdentity filterTaskIdentity) {

        // Completions task filter
        final LinearLayout taskFilterContainer = mToolbar.findViewById(R.id.task_filter_field);

        final LinearLayout taskFilterCircle = taskFilterContainer.findViewById(R.id.task_filter_circle);
        final TextView taskFilterName = taskFilterContainer.findViewById(R.id.task_filter_name);

        String name;
        int color;

        // If filtered task is null, no filter is applied (all filteredCompletions visible)
        if (filterTaskIdentity == null) {
            color = getResources().getColor(R.color.colorPrimary);
            name = getString(R.string.all_completions);
        }
        // Get data from filtered task
        else {
            color = filterTaskIdentity.getColor();
            name = filterTaskIdentity.getName();
        }

        // Task filter circle
        ColorUtilities.setViewBackgroundColor(taskFilterCircle, color);

        // Task filter name
        taskFilterName.setText(name);
    }


    private void updateSelectionUI(int totalSelected, final boolean isSelecting) {

        // Update toolbar title based on number of selected
        String toolbar_title;
        if (totalSelected == 0 && isSelecting) {
            toolbar_title = getString(R.string.select_completions);
        } else {
            toolbar_title = String.valueOf(totalSelected) + " " + getString(R.string.selected);
        }

        mSelectionModeToolbar.setTitle(toolbar_title);

        if (isSelecting) {
            displaySelectionUI();
            return;
        }

        hideSelectionUI();
    }

    private void displaySelectionUI() {

        // Hide regular toolbar
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.GONE);

        // Make selection mode toolbar visible
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.VISIBLE);
    }

    final void hideSelectionUI() {

        // Hide selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Make regular toolbar visible
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onSelectionChanged(int totalSelected, final boolean isSelecting) {

        updateSelectionUI(totalSelected, isSelecting);

    }

    @Override
    public void onDeleteCompletion(XTaskCompletion completion) {

        deleteCompletion(completion, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        });

    }

    @Override
    public void onItemsDataChanged() {

        onDataSetChanged(new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {

            }
        });

    }

    @Override
    public void onItemClicked(final int position, int yPos) {

        editCompletion(position);

    }

}
