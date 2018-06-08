package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class CompletionsFragment extends XFragment {

    // Fragment main views
    private View view;

    private ArrayList<XTask> allTasks;
    private ArrayList<XTaskFields> allTasksFields;
    private ArrayList<XTaskCompletion> allCompletions;
    private ArrayList<XTaskCompletion> filteredCompletions;

    private CompletionsTimelineAdapter mAdapter;

    private XTaskFields filterTaskFields;

    private Toolbar mToolbar;
    private Toolbar mSelectionModeToolbar;

    private CompletionsFragmentListener mCompletionsFragmentListener;

    interface CompletionsFragmentListener {
        void onBackPressed();

        void loadNewPaymentFragment(ArrayList<Boolean> selectionArray);

        void loadEditCompletionFragment(XTaskCompletion completion);

        void updateTasksDataOnDrive(ArrayList<XTask> tasks);
    }

    public static CompletionsFragment newInstanceFromTasks(ArrayList<XTask> tasks) {
        return newInstanceFromTasks(tasks, null);
    }

    public static CompletionsFragment newInstanceFromTasks(ArrayList<XTask> tasks, @Nullable XTaskFields filterTaskFields) {

        CompletionsFragment fragment = new CompletionsFragment();
        fragment.allTasks = tasks;
        fragment.allTasksFields = fragment.getTasksFieldsFromTasks(tasks);
        fragment.allCompletions = fragment.getCompletionsFromTasks(tasks);
        fragment.filterTaskFields = filterTaskFields;
        return fragment;
    }

    static CompletionsFragment newInstanceFromCompletions(ArrayList<XTaskCompletion> completions) {

        CompletionsFragment fragment = new CompletionsFragment();
        fragment.allCompletions = completions;
        fragment.allTasksFields = fragment.getTasksFieldsFromCompletions(completions);
        return fragment;
    }

    // Iterate allTasks array to retrieve all filteredCompletions
    private ArrayList<XTaskCompletion> getCompletionsFromTasks(ArrayList<XTask> tasks) {

        ArrayList<XTaskCompletion> retrievedCompletions = new ArrayList<>();

        for (XTask task : tasks) {
            retrievedCompletions.addAll(task.getCompletions());
        }

        // Sort filteredCompletions based on recency
        Collections.sort(retrievedCompletions, new Comparator<XTaskCompletion>() {
            @Override
            public int compare(XTaskCompletion c1, XTaskCompletion c2) {
                return Long.compare(c2.getDate(), c1.getDate());
            }
        });

        return retrievedCompletions;

    }

    private ArrayList<XTaskFields> getTasksFieldsFromTasks(ArrayList<XTask> tasks){

        ArrayList<XTaskFields> tasksFields = new ArrayList<>();

        for(XTask task : tasks){
            tasksFields.add(task.getTaskFields());
        }

        return tasksFields;

    }

    private ArrayList<XTaskFields> getTasksFieldsFromCompletions(ArrayList<XTaskCompletion> completions){

        ArrayList<XTaskFields> tasksFields = new ArrayList<>();

        for(XTaskCompletion completion : completions){

            XTaskFields taskFields = completion.getTaskFields();

            if(tasksFields.indexOf(taskFields) == -1){
                tasksFields.add(taskFields);
            }

        }

        return tasksFields;

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

        // Initialize regular toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_completions_fragment);

        // Completions task filter UI
        final LinearLayout taskFilterContainer = mToolbar.findViewById(R.id.task_filter_field);
        taskFilterContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterTaskDialog();
            }
        });
        // Init filter UI
        onTasksFiltered(filterTaskFields);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCompletionsFragmentListener.onBackPressed();
            }
        });

        /*// Menu items
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.timeline_icon_task_filter:
                        // Display dialog for filtering
                        filterTaskDialog();
                        break;
                    case R.id.completions_icon_pie_chart:
                        mCompletionsFragmentListener.loadCompletionsPieFragment();
                        break;

                }
                return false;
            }
        });

        // Pie action button enable/disable
        int total_completions = 0;
        for(XTask t:allTasks){
            if(t.getCompletions() != null)
                total_completions += t.getCompletions().size();
        }

        MenuItem pie_icon = mToolbar.getMenu().findItem(R.id.completions_icon_pie_chart);
        pie_icon.setEnabled(total_completions > 0);
        if(total_completions > 0){
            pie_icon.getIcon().setAlpha(255);
        }else{
            pie_icon.getIcon().setAlpha(30);
        }*/


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

                switch (item.getItemId()) {
                    case R.id.timeline_selection_mode_icon_register_payment:
                        mCompletionsFragmentListener.loadNewPaymentFragment(mAdapter.getSelectionArray());
                        hideSelectionUI();
                        break;
                    case R.id.timeline_selection_mode_icon_delete:

                        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                                .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mAdapter.deleteSelectedItems();
                                        hideSelectionUI();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .create();

                        alertDialog.setTitle(getString(R.string.delete_completions_confirmation_title));
                        alertDialog.setMessage(getString(R.string.delete_completions_confirmation_message));
                        alertDialog.show();

                        break;
                }

                return false;
            }
        });

        // Cancel selection icon listener
        mSelectionModeToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.setUniversalItemSelection(false);
            }
        });
    }

    private AlertDialog filterTaskDialog;

    private void filterTaskDialog() {

        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout tasksContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.module_colors_dialog, scrollView, false);
        scrollView.addView(tasksContainer);

        final ConstraintLayout taskLayoutAll = createTaskFilterItem(null, getString(R.string.all_completions), getResources().getColor(R.color.colorWhite), tasksContainer);
        tasksContainer.addView(taskLayoutAll);

        for (final XTaskFields taskFields : allTasksFields) {

            final ConstraintLayout taskLayout = createTaskFilterItem(taskFields, taskFields.getName(), taskFields.getColor(), tasksContainer);

            tasksContainer.addView(taskLayout);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(scrollView);
        filterTaskDialog = builder.create();
        filterTaskDialog.show();


    }

    private ConstraintLayout createTaskFilterItem(@Nullable final XTaskFields taskFields, final String name, final int color, final LinearLayout parent) {

        ConstraintLayout taskLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_dialog_color, parent, false);

        TextView titleView = taskLayout.findViewById(R.id.dialog_color_title);
        titleView.setText(name);

        LinearLayout marker = taskLayout.findViewById(R.id.dialog_color_marker);
        paintViewBackground(marker, color);

        final ImageButton selected = taskLayout.findViewById(R.id.dialog_color_selected);

        if (getContext() == null) return taskLayout;

        selected.setColorFilter(getContext().getResources().getColor(R.color.colorAccent));

        if (taskFields == filterTaskFields) {
            titleView.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
            selected.setVisibility(View.VISIBLE);
        } else {
            titleView.setTextColor(getResources().getColor(R.color.colorWhite));
            selected.setVisibility(View.GONE);
        }

        taskLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterTaskDialog.dismiss();

                // Remove old filteredCompletions
                if (filterTaskFields == null) {
                    mAdapter.deleteItemRange(0, allCompletions.size());
                } else {
                    mAdapter.deleteItemRange(0, filteredCompletions.size());
                }


                // Set filter
                filterTaskFields = taskFields;

                // Update filteredCompletions array
                filteredCompletions = new ArrayList<>();
                if (filterTaskFields == null) {
                    filteredCompletions = allCompletions;
                } else {
                    for (XTaskCompletion completion : allCompletions) {
                        if (XTaskFieldsUtilities.areEqual(completion.getTaskFields(),filterTaskFields)){
                            filteredCompletions.add(completion);
                            Log.i("XTQ-Completions",completion.getTaskFields().getName());
                        }
                    }
                }

                // Sort filteredCompletions based on recency
                Collections.sort(filteredCompletions, new Comparator<XTaskCompletion>() {
                    @Override
                    public int compare(XTaskCompletion xTaskCompletion, XTaskCompletion t1) {
                        return Long.compare(t1.getDate(), xTaskCompletion.getDate());
                    }
                });

                mAdapter.setCompletions(filteredCompletions);

                // Add new filteredCompletions to adapter
                mAdapter.timelineItemRangeInserted(0, filteredCompletions.size());

                // Reflect filter change
                onTasksFiltered(filterTaskFields);
            }
        });

        return taskLayout;
    }

    private void onTasksFiltered(@Nullable XTaskFields filterTaskFields) {

        // Completions task filter
        final LinearLayout taskFilterContainer = mToolbar.findViewById(R.id.task_filter_field);

        final LinearLayout taskFilterCircle = taskFilterContainer.findViewById(R.id.task_filter_circle);
        final TextView taskFilterName = taskFilterContainer.findViewById(R.id.task_filter_name);

        String name;
        int color;

        // If filtered task is null, no filter is applied (all filteredCompletions visible)
        if (filterTaskFields == null) {
            color = getResources().getColor(R.color.colorWhite);
            name = getString(R.string.all_completions);
        }
        // Get data from filtered task
        else {
            color = filterTaskFields.getColor();
            name = filterTaskFields.getName();
        }

        // Task filter circle
        paintViewBackground(taskFilterCircle, color);

        // Task filter name
        taskFilterName.setText(name);
    }

    // Display all filteredCompletions sorted from newest to oldest
    private void loadCompletions() {

        // Load recycler view
        final TimelineRecycler mRecyclerView = view.findViewById(R.id.completions_recycler);

        // Filtered for single task (from EditTaskFragment)
        if (filterTaskFields != null) {

            filteredCompletions = getFilteredCompletions(filterTaskFields);

            // Sort filteredCompletions based on recency
            Collections.sort(filteredCompletions, new Comparator<XTaskCompletion>() {
                @Override
                public int compare(XTaskCompletion xTaskCompletion, XTaskCompletion t1) {
                    return Long.compare(t1.getDate(), xTaskCompletion.getDate());
                }
            });
        } else {
            filteredCompletions = allCompletions;
        }

        // Display filteredCompletions in RecyclerView with Adapter
        mAdapter = new CompletionsTimelineAdapter(filteredCompletions, new CompletionsTimelineAdapter.CompletionsTimelineAdapterNoLockListener() {

            @Override
            public void onSelectionChanged(int totalSelected, final boolean isSelecting) {

                updateSelectionUI(totalSelected, isSelecting);

            }

            @Override
            public void deleteCompletionData(XTaskCompletion completion) {

                XTask task = XTaskFieldsUtilities.getTaskFromCompletion(completion, allTasks);

                if(task == null) return;

                allTasks = XTaskFieldsUtilities.removeCompletion(completion,allTasks);

                filteredCompletions.remove(completion);

                if (filteredCompletions.size() == 0) {
                    mCompletionsFragmentListener.onBackPressed();
                }

            }

            @Override
            public void onItemsDataChanged() {

                mCompletionsFragmentListener.updateTasksDataOnDrive(allTasks);

            }

            @Override
            public void onItemClicked(int position, int yPos) {

                mCompletionsFragmentListener.loadEditCompletionFragment(filteredCompletions.get(position));

            }
        });

        mRecyclerView.setAdapter(mAdapter);

        // Item loading animation
        mRecyclerView.startLayoutAnimation();

    }

    private ArrayList<XTaskCompletion> getFilteredCompletions(XTaskFields taskFields){

        ArrayList<XTaskCompletion> filteredCompletions = new ArrayList<>();

        for(XTaskCompletion completion : allCompletions){

            if(XTaskFieldsUtilities.areEqual(completion.getTaskFields(),taskFields)){
                filteredCompletions.add(completion);
            }

        }

        return filteredCompletions;

    }


    // Animate launch of EditTaskFragment with the selected task
    /*
    private void editCompletion(final int pos, float y) {

        // View that acts as a drawable with task color expanding and covering the whole screen
        final View scaleView = new View(getContext());

        // Add elevation to make scaleView appear over all other views and cover the whole screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scaleView.setElevation(100);
        }

        // Set drawable color to task color
        scaleView.setBackground(new ColorDrawable(filteredCompletions.get(pos).getTask().getColor()));

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
                mCompletionsFragmentListener.loadEditCompletionFragment(filteredCompletions.get(pos));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        scaleView.startAnimation(expand_animation);

        mCompletionsFragmentListener.loadEditCompletionFragment(filteredCompletions.get(pos));
    }*/

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

    private void hideSelectionUI() {

        // Hide selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Make regular toolbar visible
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);
    }


    private void paintViewBackground(@NonNull View view, int color) {
        Drawable background = view.getBackground();
        background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        view.setBackground(background);
    }

}
