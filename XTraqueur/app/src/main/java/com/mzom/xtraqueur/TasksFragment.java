package com.mzom.xtraqueur;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private View view;

    private ArrayList<XTask> tasks;

    private static int maxElevation;

    private TasksFragmentListener tasksFragmentListener;

    private final static String TAG = "Xtraqueur-TasksFrag";

    interface TasksFragmentListener {

        void loadNewTaskFragment(boolean fromPreEdit);

        void loadEditTaskFragment(XTask task, int index);

        void loadSettingsFragment();

        void loadTimelineFragment();

        void updateTasksDataOnDrive(ArrayList<XTask> tasks);

        boolean useDarkText(int color);
    }

    public static TasksFragment newInstance(ArrayList<XTask> tasks) {
        TasksFragment fragment = new TasksFragment();
        fragment.tasks = tasks;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        this.view = inflater.inflate(R.layout.fragment_tasks, container, false);

        initListeners();

        // Get maxElevation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int childCount = ((ViewGroup) view).getChildCount();
            for (int c = 0; c < childCount; c++) {
                View child = ((ViewGroup) view).getChildAt(c);
                int elevation = (int) child.getElevation();
                maxElevation = elevation > maxElevation ? elevation : maxElevation;
            }
        }

        return view;
    }

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

        // Load listview items
        loadTasks();

        super.onActivityCreated(savedInstanceState);
    }

    void initToolbar() {
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        toolbar.inflateMenu(R.menu.menu_tasks_fragment);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tasks_settings_icon:
                        tasksFragmentListener.loadSettingsFragment();
                        break;
                    case R.id.tasks_timeline_icon:
                        tasksFragmentListener.loadTimelineFragment();
                }
                return false;
            }
        });
    }

    private void initListeners() {
        // ImageButton to load NewTaskFragment
        view.findViewById(R.id.new_task_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tasksFragmentListener.loadNewTaskFragment(false);

            }
        });

        // EditText to load NewTaskFragment
        view.findViewById(R.id.pre_new_task_name_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tasksFragmentListener.loadNewTaskFragment(true);
            }
        });

    }

    // Update dataset before loading tasks
    void loadTasks(ArrayList<XTask> tasks){
        this.tasks = tasks;
        loadTasks();
    }

    // Fill ListView with items representing the tasks
    @SuppressLint("ClickableViewAccessibility")
    void loadTasks() {

        Log.i(TAG,"Loading tasks: " + tasks.toString());

        // Create empty ArrayList if data set is null
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        // Display message to user if there are no tasks to load
        if(tasks.size() == 0){
            // Display message
            view.findViewById(R.id.xtask_container).setVisibility(View.GONE);
            view.findViewById(R.id.no_tasks_container).setVisibility(View.VISIBLE);
            view.findViewById(R.id.tasks_total_value_container).setVisibility(View.GONE);
        }else{
            // Hide message
            view.findViewById(R.id.xtask_container).setVisibility(View.VISIBLE);
            view.findViewById(R.id.no_tasks_container).setVisibility(View.GONE);
            view.findViewById(R.id.tasks_total_value_container).setVisibility(View.VISIBLE);
        }

        // DragSortListView to host the task items
        // Enables drag and sort functionality to the list
        final DragSortListView xtask_list = view.findViewById(R.id.xtask_container);

        // ListView ArrayAdapter
        final XTaskListAdapter mListAdapter = new XTaskListAdapter(getContext(), tasks, new XTaskListAdapter.XTaskListAdapterListener() {
            @Override
            public void onUpdateTasks(ArrayList<XTask> tasks) {

                // Save the scroll position before refresh
                int index = xtask_list.getFirstVisiblePosition();
                View v = xtask_list.getChildAt(0);
                int top = (v == null) ? 0 : (v.getTop() - xtask_list.getPaddingTop());

                // Refresh ListView
                updateTasks(tasks);

                // Restore the scroll position after refresh
                xtask_list.setSelectionFromTop(index, top);
            }

            @Override
            public void updateTasksDataOnDrive(ArrayList<XTask> tasks) {
                tasksFragmentListener.updateTasksDataOnDrive(tasks);
            }

            @Override
            public boolean useDarkText(int color) {
                return tasksFragmentListener.useDarkText(color);
            }
        });

        // Set ArrayAdapter to DragSortListView
        xtask_list.setAdapter(mListAdapter);

        // Listener for task drop
        xtask_list.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int i, int i1) {
                if (i != i1) {
                    XTask item = mListAdapter.getItem(i);
                    mListAdapter.remove(item);
                    mListAdapter.insert(item, i1);

                    tasks.remove(item);
                    tasks.add(i1, item);
                    tasksFragmentListener.updateTasksDataOnDrive(tasks);
                }
            }
        });

        // Controller used to handle touch events and calculate which ListView items to drag
        final DragSortController controller = new XTaskDragSortController(xtask_list, new XTaskDragSortController.XTaskDragSortControllerListener() {
            @Override
            public void onEditTask(int index, float y) {
                editTask(index, y);
            }
        });

        controller.setDragHandleId(R.id.xtask);
        controller.setRemoveEnabled(false);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_LONG_PRESS);

        xtask_list.setFloatViewManager(controller);
        xtask_list.setOnTouchListener(controller);
        xtask_list.setDragEnabled(true);

        // Update TextView displaying the tasks total value
        updateTotalValue();
    }

    // Animate launch of EditTaskFragment with the selected task
    private void editTask(final int index, float y) {
        // View that acts as a drawable with task color expanding and covering the whole screen
        final View scaleView = new View(getContext());

        // Add elevation to make scaleView appear over all other views and cover the whole screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scaleView.setElevation(maxElevation);
        }

        // Set drawable color to task color
        scaleView.setBackground(new ColorDrawable(darkenColor(tasks.get(index).getColor())));

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
                tasksFragmentListener.loadEditTaskFragment(tasks.get(index), index);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        scaleView.startAnimation(expand_animation);
    }

    // Update Google Drive tasks_data.txt and update TextView displaying the tasks total value
    private void updateTasks(ArrayList<XTask> tasks) {

        // Update fragment field variable for the tasks data set
        this.tasks = tasks;

        // Update tasks_data.txt stored with Google Drive
        tasksFragmentListener.updateTasksDataOnDrive(tasks);

        // Update TextView displaying the tasks total value
        updateTotalValue();

        // "Reload" ListView to make changes take effect
        loadTasks();
    }

    // Update TextView displaying the tasks total value
    private void updateTotalValue() {

        if (tasks == null) {
            return;
        }

        // Total value of all tasks
        int total = 0;

        // Loop trough data set and add task value to total value
        for (XTask task : tasks) {
            total += task.getValue();
        }

        // String concatenation with bold number and currency
        String totalString = getString(R.string.you_have_earned) + " <b>" + String.valueOf(total) + " " + String.valueOf(Currency.getInstance(Locale.getDefault()).getSymbol()) + "</b>";

        // Update total value TextView
        ((TextView) view.findViewById(R.id.tasks_total_value)).setText(Html.fromHtml(totalString));
    }

    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
