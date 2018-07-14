package com.mzom.xtraqueur;

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

abstract class BaseCompletionsFragment extends XFragment {



    abstract void deleteCompletion(XTaskCompletion completion);

    abstract int getSelectionMenuResId();

    abstract Toolbar.OnMenuItemClickListener getSelectionMenuItemClickListener();

    abstract void onDataSetChanged();

    abstract void onCompletionClicked(XTaskCompletion completion, int yPos);



    private static final String TAG = "XTQ-BaseCompletions";

    // Fragment main views
    private View view;

    private ArrayList<XTaskIdentity> allTaskIdentities;

    private ArrayList<XTaskCompletion> allCompletions;
    private ArrayList<XTaskCompletion> filteredCompletions;

    private CompletionsTimelineAdapter mAdapter;

    private XTaskIdentity filterTaskIdentity;

    private Toolbar mToolbar;

    private Toolbar mSelectionModeToolbar;

    private CompletionsTimelineAdapter.CompletionsTimelineAdapterListener completionsTimelineAdapterListener = new CompletionsTimelineAdapter.CompletionsTimelineAdapterListener() {
        @Override
        public void onSelectionChanged(int totalSelected, final boolean isSelecting) {

            if(getContext() == null) return;

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

        @Override
        public void onDeleteCompletion(XTaskCompletion completion) {

            deleteCompletion(completion);

        }

        @Override
        public void onItemsDataChanged() {

            onDataSetChanged();

        }

        @Override
        public void onItemClicked(int position, int yPos) {

            XTaskCompletion completion = filteredCompletions.get(position);

            onCompletionClicked(completion,yPos);

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_completions, container, false);

        initToolbar();

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

                final ArrayList<XTaskIdentity> completedTaskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(allCompletions);

                new TasksFilterDialog(getContext(), filterTaskIdentity, allTaskIdentities, completedTaskIdentities, new TasksFilterDialog.TasksFilterDialogListener() {
                    @Override
                    public void onFilter(XTaskIdentity filterIdentity) {

                        // Update task identity filter
                        filterTaskIdentity = filterIdentity;

                        // Reload completions adapter to reflect filter change
                        loadCompletions();

                        // Reflect filter change in UI elements
                        displayCurrentTasksFilter();


                    }
                }).show();
            }
        });

        // Init filter UI
        displayCurrentTasksFilter();

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

        if(allCompletions.size() == 0){
            FragmentLoader.reverseLoading(getContext());
            return;
        }

        // Load recycler view
        final TimelineRecycler mRecyclerView = view.findViewById(R.id.completions_recycler);

        // Filtered for single task (from EditTaskFragment)
        if (filterTaskIdentity != null) {

            filteredCompletions = filterTaskIdentity.findCompletions(allCompletions);

            if(filteredCompletions.size() == 0){

                // Update task identity filter
                filterTaskIdentity = null;

                // Reload completions adapter to reflect filter change
                loadCompletions();

                // Reflect filter change in UI elements
                displayCurrentTasksFilter();

                return;
            }

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
        mAdapter = new CompletionsTimelineAdapter(filteredCompletions, completionsTimelineAdapterListener);

        mRecyclerView.setAdapter(mAdapter);

        // Item loading animation
        mRecyclerView.startLayoutAnimation();

    }

    ArrayList<XTaskCompletion> getFilteredCompletions() {
        return filteredCompletions;
    }

    // Use selectionArray to get RecyclerView's currently selected completions
    ArrayList<XTaskCompletion> getSelectedCompletions() {

        final ArrayList<XTaskCompletion> filteredCompletions = getFilteredCompletions();

        ArrayList<XTaskCompletion> selectedCompletions = new ArrayList<>();

        for (int c = 0; c < filteredCompletions.size(); c++) {
            if (getAdapter().getSelectionArray().get(c)) {
                selectedCompletions.add(filteredCompletions.get(c));
            }
        }
        return selectedCompletions;
    }

    // Visually display current tasks filter
    private void displayCurrentTasksFilter() {

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

    private void displaySelectionUI() {

        // Hide regular toolbar
        mToolbar.setVisibility(View.GONE);

        // Make selection mode toolbar visible
        mSelectionModeToolbar.setVisibility(View.VISIBLE);
    }

    final void hideSelectionUI() {

        // Hide selection mode toolbar
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Make regular toolbar visible
        mToolbar.setVisibility(View.VISIBLE);
    }

}
