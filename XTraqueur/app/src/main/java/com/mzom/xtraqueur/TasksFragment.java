package com.mzom.xtraqueur;

import android.animation.Animator;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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

    interface TasksFragmentListener {
        void loadSummaryFragment();

        void loadNewTaskFragment(boolean fromPreEdit);

        void loadEditTaskFragment(XTask task, int index);

        void loadSettingsFragment();

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
                }
                return false;
            }
        });
    }

    private void initListeners() {
        // Summary button
        final TextView total_value = view.findViewById(R.id.tasks_total_value);
        total_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // View that acts as a drawable expanding and covering the whole screen
                final View scaleView = new View(getContext());

                // Add elevation to make scaleView appear over all other views and cover the whole screen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    scaleView.setElevation(maxElevation);
                }

                // Set drawable color to task color
                scaleView.setBackground(new ColorDrawable(Color.parseColor("#eeeeee")));

                // Add view to the fragment root view (get access to ViewGroup method addView() by casting to ConstraintLayout)
                ((ConstraintLayout) view).addView(scaleView);

                // Calculate center on y-axis
                float y = (total_value.getTop() + total_value.getBottom())/2;

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
                        // Once the expand animation has finished, tell MainActivity to switch to SummaryFragment
                        tasksFragmentListener.loadSummaryFragment();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                scaleView.startAnimation(expand_animation);

                /*// Circular reveal
                View scaleView = new View(getContext());
                scaleView.setBackground(new ColorDrawable(Color.parseColor("#eeeeee")));
                ((ConstraintLayout) view).addView(scaleView);

                int x = (total_value.getLeft() + total_value.getRight()) / 2;
                int y = (total_value.getTop() + total_value.getBottom()) / 2;

                int startRadius = 0;
                int endRadius = Math.max(view.getWidth(), view.getHeight());

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Animator anim = ViewAnimationUtils.createCircularReveal(scaleView, x, y, startRadius, endRadius);
                    anim.setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                tasks_total_value_container.setElevation(0);
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            tasksFragmentListener.loadSummaryFragment();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    anim.start();
                }*/


            }
        });

        view.findViewById(R.id.new_task_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tasksFragmentListener.loadNewTaskFragment(false);

            }
        });

        view.findViewById(R.id.pre_new_task_name_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tasksFragmentListener.loadNewTaskFragment(true);
            }
        });

    }

    // Fill ListView with items representing the tasks
    @SuppressLint("ClickableViewAccessibility")
    void loadTasks() {

        // Create empty ArrayList if data set is null
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        // DragSortListView to host the task items
        final DragSortListView xtask_list = view.findViewById(R.id.xtask_container);

        // ListView ArrayAdapter
        final XTaskListAdapter mListAdapter = new XTaskListAdapter(getContext(), tasks, new XTaskListAdapter.XTaskListAdapterListener() {
            @Override
            public void onUpdateTasks(ArrayList<XTask> tasks) {
                // Save the scroll position
                int index = xtask_list.getFirstVisiblePosition();
                View v = xtask_list.getChildAt(0);
                int top = (v == null) ? 0 : (v.getTop() - xtask_list.getPaddingTop());

                updateTasks(tasks);

                // Restore the scroll position
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
