package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class CompletionsFragment extends Fragment {

    // Fragment main views
    private View view;

    private ArrayList<XTask> tasks;
    private ArrayList<XTaskCompletion> completions;

    private TimelineAdapter mAdapter;

    private ArrayList<Boolean> selectionArray;

    private XTask filterTask;

    private Toolbar mToolbar;
    private Toolbar mSelectionModeToolbar;

    private RecyclerView mRecyclerView;

    private CompletionsFragmentListener mCompletionsFragmentListener;

    interface CompletionsFragmentListener {
        void onBackPressed();

        void loadNewPaymentFragment(ArrayList<Boolean> selectionArray);

        void loadEditCompletionFragment(XTaskCompletion completion);

        void updateTasksDataOnDrive(ArrayList<XTask> tasks);
    }

    public static CompletionsFragment newInstance(ArrayList<XTask> tasks) {

        CompletionsFragment fragment = new CompletionsFragment();
        fragment.tasks = tasks;
        return fragment;
    }

    public static CompletionsFragment newInstance(ArrayList<XTask> tasks, XTask filterTask) {

        CompletionsFragment fragment = new CompletionsFragment();
        fragment.tasks = tasks;
        fragment.filterTask = filterTask;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_completions, container, false);

        initToolbar();

        loadCompletions();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCompletionsFragmentListener = (CompletionsFragmentListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CompletionsFragmentListener");
        }
    }

    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Load both regular and selection mode toolbar (only regular toolbar visible at startup)

        // Initialize toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_timeline_fragment);

        // Menu items
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.timeline_icon_task_filter:
                        // Display dialog for filtering

                        ArrayList<String> titles_temp = new ArrayList<>();
                        final ArrayList<XTask> titles_task = new ArrayList<>();
                        titles_temp.add(getString(R.string.timeline_filter_all_tasks));
                        titles_task.add(null);

                        for(XTask t : tasks){
                            if(t.getCompletions() > 0){
                                titles_temp.add(t.getName());
                                titles_task.add(t);
                            }
                        }

                        CharSequence[] titles = titles_temp.toArray(new CharSequence[titles_temp.size()]);

                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.timeline_filter_by_task)
                                .setSingleChoiceItems(titles, filterTask != null ? titles_task.indexOf(filterTask) : 0,new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadCompletions(titles_task.get(which));
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                }
                return false;
            }
        });

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCompletionsFragmentListener.onBackPressed();
            }
        });


        // Selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);

        // Hide when not using selection mode
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Add action buttons to toolbar from menu resource
        mSelectionModeToolbar.inflateMenu(R.menu.menu_timeline_fragment_selection_mode);

        // Selection icons listener
        mSelectionModeToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                ArrayList<Boolean> oldSelection = selectionArray;

                disableSelectionMode();

                switch (item.getItemId()){
                    case R.id.timeline_selection_mode_icon_register_payment:
                        mCompletionsFragmentListener.loadNewPaymentFragment(oldSelection);
                        break;
                    case R.id.timeline_selection_mode_icon_delete:
                        deleteSelectedCompletions(oldSelection);
                        break;
                }

                return false;
            }
        });

        // Cancel selection icon listener
        mSelectionModeToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableSelectionMode();
            }
        });
    }

    // Use SparseBooleanArray to get RecyclerView's currently selected completions
    private ArrayList<XTaskCompletion> getSelectedCompletions(){

        ArrayList<XTaskCompletion> selectedCompletions = new ArrayList<>();

        for(int c = 0;c<completions.size();c++){
            if(selectionArray.get(c)){
                selectedCompletions.add(completions.get(c));
            }
        }
        return selectedCompletions;
    }

    private void loadCompletions(XTask filterTask){
        this.filterTask = filterTask;
        loadCompletions();
    }

    // Display all completions sorted from newest to oldest
    private void loadCompletions() {

        // Load recycler view
        mRecyclerView = view.findViewById(R.id.completions_recycler);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        completions = new ArrayList<>();

        // Filtered for single task (from EditTaskFragment)
        if(filterTask != null){
            if(filterTask.getCompletionsList() != null){
                for (Long l : filterTask.getCompletionsList()) {
                    completions.add(new XTaskCompletion(l, filterTask));
                }
            }
        }

        // No filters, showing all task completions
        else{
            for (XTask t : tasks) {
                if (t.getCompletionsList() != null)
                    for (Long l : t.getCompletionsList()) {
                        completions.add(new XTaskCompletion(l, t));
                    }
            }
        }

        // Sort completions based on recency
        Collections.sort(completions, new Comparator<XTaskCompletion>() {
            @Override
            public int compare(XTaskCompletion xTaskCompletion, XTaskCompletion t1) {
                return Long.compare(t1.getDate(), xTaskCompletion.getDate());
            }
        });

        // Display completions in RecyclerView with Adapter
        mAdapter = new TimelineAdapter(false,new TimelineAdapter.TimelineAdapterListener() {
            @Override
            public void onItemClick(int pos,float y) {
                Log.i("Edit completion",String.valueOf(y));
                editCompletion(pos,y);
            }

            @Override
            public int getItemCount() {
                return completions.size();
            }

            @Override
            public boolean onSelectionChanged(ArrayList<Boolean> updatedSelectionArray) {
                // Update selection array
                selectionArray = updatedSelectionArray;

                updateSelectionToolbar();

                // Enable/disable selection mode based on number of selections
                if(totalSelected() > 0){
                    enableSelectionMode();
                    return true;
                }else{
                    disableSelectionMode();
                    return false;
                }
            }

            @Override
            public TimelineItem getTimelineItem(int pos) {

                String title = completions.get(pos).getTask().getName();
                int color = completions.get(pos).getTask().getColor();
                long date = completions.get(pos).getDate();

                return new TimelineItem(title,color,date);
            }
        });

        mRecyclerView.setAdapter(mAdapter);

    }

    private void refreshTimeline(){

        // Display completions in RecyclerView with Adapter
        mRecyclerView.getAdapter().notifyDataSetChanged();

    }

    // Animate launch of EditTaskFragment with the selected task
    private void editCompletion(final int pos, float y) {

        // View that acts as a drawable with task color expanding and covering the whole screen
        final View scaleView = new View(getContext());

        // Add elevation to make scaleView appear over all other views and cover the whole screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scaleView.setElevation(100);
        }

        // Set drawable color to task color
        scaleView.setBackground(new ColorDrawable(completions.get(pos).getTask().getColor()));

        // Add view to the fragment root view (get access to ViewGroup method addView() by casting to ConstraintLayout)
        ((ConstraintLayout) view).addView(scaleView);

        // Expand animation to fill the whole screen with task color
        final ScaleAnimation expand_animation = new ScaleAnimation(1f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.ABSOLUTE, y);

        expand_animation.setDuration(200);

        // Keep the transformation after animation has finished
        expand_animation.setFillAfter(true);

        expand_animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Once the expand animation has finished, tell MainActivity to switch to an EditTaskFragment in the FrameLayout
                mCompletionsFragmentListener.loadEditCompletionFragment(completions.get(pos));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        scaleView.startAnimation(expand_animation);
    }

    // Calculate how many completions have been selected
    private int totalSelected(){
        return mAdapter.getTotalSelected();
    }

    private void updateSelectionToolbar(){

        // Update toolbar title based on number of selected
        int total_selected = totalSelected();
        String toolbar_title;
        if(total_selected == 0){
            toolbar_title = getString(R.string.select_completions);
        }else{
            toolbar_title = String.valueOf(total_selected) + " " + getString(R.string.selected);
        }

        mSelectionModeToolbar.setTitle(toolbar_title);
    }

    private void enableSelectionMode(){

        // Hide regular toolbar
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.GONE);

        // Make selection mode toolbar visible
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.VISIBLE);

        updateSelectionToolbar();
    }

    private void disableSelectionMode(){
        // Selection array with none selected
        selectionArray = new ArrayList<>(Arrays.asList(new Boolean[completions.size()]));
        Collections.fill(selectionArray,false);

        // Hide selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Make regular toolbar visible
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);

        refreshTimeline();
        Log.i("Timeline","Loaded timeline");
    }

    private void deleteSelectedCompletions(ArrayList<Boolean> selection){

        Log.i("Timeline","Starting deletion");

        ArrayList<XTaskCompletion> toRemove = new ArrayList<>();

        for(int c = completions.size()-1;c>=0;c--){
            Log.i("Timeline","Selection for " + String.valueOf(c) + " - " + String.valueOf(selection.get(c)));
            if(selection.get(c)){
                XTaskCompletion completion = completions.get(c);
                tasks.get(tasks.indexOf(completion.getTask())).removeCompletion(completion.getDate());
                toRemove.add(completion);
                mAdapter.deleteItem(c);
                Log.i("Timeline",String.valueOf(c) + " will be removed");
            }
        }

        Log.i("Timeline","To be removed " + toRemove.toString());

        /*for(XTaskCompletion completion : toRemove){

            Log.i("Timeline", "Removing");

            mAdapter.deleteItem(completions.indexOf(completion));

            Log.i("Timeline","II Removed " + String.valueOf(completions.indexOf(completion)));

            completions.remove(completion);
        }*/

        completions.removeAll(toRemove);

        mCompletionsFragmentListener.updateTasksDataOnDrive(tasks);

    }

}
