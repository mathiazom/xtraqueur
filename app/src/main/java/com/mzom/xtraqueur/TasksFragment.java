package com.mzom.xtraqueur;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TasksFragment extends XFragment {

    // Fragment root view
    private View view;

    // Fragment toolbar view
    private Toolbar mToolbar;

    // Tasks dataset
    private ArrayList<XTask> tasks;

    private ArrayList<XPayment> payments;

    private ArrayList<XTaskCompletion> instantCompletions;

    private TasksListAdapter mListAdapter;

    // Interface instance to communicate with MainActivity
    private TasksFragmentListener tasksFragmentListener;

    // Interface class to communicate with MainActivity
    interface TasksFragmentListener {
        void loadSettingsActivity();
    }

    // Custom constructor to pass required fragment variables
    public static TasksFragment newInstance(@NonNull ArrayList<XTask> tasks,@NonNull ArrayList<XPayment> payments, @NonNull ArrayList<XTaskCompletion> instantCompletions) {

        TasksFragment fragment = new TasksFragment();
        fragment.tasks = tasks;
        fragment.payments = payments;
        fragment.instantCompletions = instantCompletions;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Save the fragment state on configuration changes (mainly screen rotations)
        setRetainInstance(true);

        // Fragment root view
        this.view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Add listeners to fragment views
        initListeners();

        return view;
    }

    // Initialize listener when activity is available
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            tasksFragmentListener = (TasksFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TasksFragmentListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        // Show fragment toolbar with appropriate item listeners
        initToolbar();

        // Load listView items
        loadTasks();

        super.onActivityCreated(savedInstanceState);
    }

    private void initToolbar() {

        mToolbar = view.findViewById(R.id.toolbar);

        mToolbar.inflateMenu(R.menu.menu_tasks_fragment);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tasks_settings_icon:
                        tasksFragmentListener.loadSettingsActivity();
                        break;
                    case R.id.tasks_timeline_icon:

                        FragmentLoader.loadFragment(TasksCompletionsFragment.newInstance(tasks,payments,instantCompletions),getContext(),R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom, true);

                        break;
                }
                return false;
            }
        });

    }

    private void initListeners() {

        // TextView to load NewTaskFragment
        view.findViewById(R.id.new_task_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoader.loadFragment(NewTaskFragment.newInstance(tasks, payments,instantCompletions,false,ColorUtilities.getRandomMaterialColor(getContext())),getContext(), R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top,true);
            }
        });

        view.findViewById(R.id.new_instant_completion_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoader.loadFragment(NewTaskFragment.newInstance(tasks,payments,instantCompletions,true,ColorUtilities.getRandomMaterialColor(getContext())),getContext(), R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top,true);
            }
        });

        view.findViewById(R.id.tasks_total_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoader.loadFragment(EarningsFragment.newInstance(tasks,payments,instantCompletions),getContext(),R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom, true);

            }
        });
    }

    // Update dataset before loading tasks
    void setTasks(ArrayList<XTask> tasks){
        this.tasks = tasks;
    }


    void setInstantCompletions(ArrayList<XTaskCompletion> instantCompletions){
        this.instantCompletions = instantCompletions;
    }

    // Fill ListView with items representing the tasks
    @SuppressLint("ClickableViewAccessibility")
    void loadTasks() {

        // Create empty ArrayList if data set is null
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        for(XTask task : tasks){
            for(XTaskCompletion completion : task.getCompletions()){
                completion.setTaskIdentity(task.getTaskIdentity());
            }
        }

        // Display message to user if there are no tasks to load
        handleTaskAbsence();

        // Timeline action button enable/disable based on total completions count
        handleTimelineActionButton();

        // Update TextView displaying the tasks total value
        updateTotalValue();

        // DragSortListView to host the task items
        // Enables drag and sort functionality to the list
        final DragSortListView tasksListView = view.findViewById(R.id.xtask_container);

        // ListView ArrayAdapter
        mListAdapter = new TasksListAdapter(getContext(), tasks, new TasksListAdapter.XTaskListAdapterListener() {
            @Override
            public void onTasksUpdated(ArrayList<XTask> updatedTasks) {

                // Save the scroll position before refresh
                int index = tasksListView.getFirstVisiblePosition();
                View v = tasksListView.getChildAt(0);
                int top = (v == null) ? 0 : (v.getTop() - tasksListView.getPaddingTop());

                // Update fragment field variable for the tasks data set
                tasks = updatedTasks;

                // "Reload" ListView to make changes take effect
                loadTasks();

                // Restore the scroll position after refresh
                tasksListView.setSelectionFromTop(index, top);
            }
        });

        // Set ArrayAdapter to DragSortListView
        tasksListView.setAdapter(mListAdapter);

        // Listener for task drop
        tasksListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int i, int i1) {
                if (i != i1) {
                    XTask item = mListAdapter.getItem(i);
                    mListAdapter.remove(item);
                    mListAdapter.insert(item, i1);

                    tasks.remove(item);
                    tasks.add(i1, item);

                    // Update tasks order changes to drive
                    XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME,tasks,getContext());
                }
            }
        });

        // Controller used to handle touch events and calculate which ListView items to drag
        // Uses custom class to handle both normal click and long click
        final DragSortController controller = new XTaskDragSortController(tasksListView, new XTaskDragSortController.XTaskDragSortControllerListener() {
            @Override
            public void onEditTask(int index, float y) {
                editTask(index, y);
            }
        });

        if(getContext() != null) controller.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));

        controller.setDragHandleId(R.id.xtask);
        controller.setRemoveEnabled(false);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_LONG_PRESS);

        tasksListView.setFloatViewManager(controller);
        tasksListView.setOnTouchListener(controller);
        tasksListView.setDragEnabled(true);

    }

    // Animate launch of EditTaskFragment with the selected task
    private void editTask(final int index, float y) {

        final XTask task = tasks.get(index);

        startScaleAnimation(task.getTaskIdentity().getColor(), y, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                FragmentLoader.loadFragment(EditTaskFragment.newInstance(tasks, payments,instantCompletions,task, index),getContext());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void startScaleAnimation(int color, float y, final Animation.AnimationListener animationListener){
        // View that acts as a drawable with task color expanding and covering the whole screen
        final View scaleView = new View(getContext());

        // Add elevation to make scaleView appear over all other views and cover the whole screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scaleView.setElevation(100);
        }

        // Set drawable color to task color
        scaleView.setBackground(new ColorDrawable(color));

        // Add view to the fragment root view (get access to ViewGroup method addView() by casting to ConstraintLayout)
        ((ConstraintLayout) view).addView(scaleView);

        // Expand animation to fill the whole screen with task color
        final ScaleAnimation expand_animation = new ScaleAnimation(1f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.ABSOLUTE, y);

        expand_animation.setDuration(200);

        // Keep the transformation after animation has finished
        expand_animation.setFillAfter(true);

        expand_animation.setAnimationListener(animationListener);

        scaleView.startAnimation(expand_animation);
    }

    // Update TextView displaying the tasks total value
    private void updateTotalValue() {

        if (tasks == null) {
            return;
        }

        // Total value of all tasks
        double total = 0;

        // Loop trough data set and add task value to total value
        for (XTask task : tasks) {
            total += task.getValue();
        }
        for(XTaskCompletion instantCompletion : instantCompletions){
            total += instantCompletion.getTaskIdentity().getFee();
        }

        // Get currency format
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String totalString = nf.format(total);

        // Update total value TextView
        ((TextView) view.findViewById(R.id.tasks_total_value)).setText(Html.fromHtml(totalString));
    }

    // Display message to user if there are no tasks to load
    private void handleTaskAbsence(){

        int vis = tasks.size() != 0 ? View.VISIBLE : View.GONE;
        int aVis = vis == View.VISIBLE ? View.GONE : View.VISIBLE;

        view.findViewById(R.id.xtask_container).setVisibility(vis);
        view.findViewById(R.id.no_tasks_background_container).setVisibility(aVis);
        view.findViewById(R.id.no_tasks_text_container).setVisibility(aVis);

    }

    // Timeline action button enable/disable based on total completions count
    private void handleTimelineActionButton(){

        int totalCompletions = 0;
        for(XTask t:tasks){
            totalCompletions += t.getCompletionsCount();
        }
        totalCompletions += instantCompletions.size();

        MenuItem timeline_icon = mToolbar.getMenu().findItem(R.id.tasks_timeline_icon);
        timeline_icon.setEnabled(totalCompletions > 0);
        if(totalCompletions > 0){
            timeline_icon.getIcon().setAlpha(255);
        }else{
            timeline_icon.getIcon().setAlpha(30);
        }

    }
}
