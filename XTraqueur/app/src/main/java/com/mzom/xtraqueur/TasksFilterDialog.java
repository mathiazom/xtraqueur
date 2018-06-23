package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class TasksFilterDialog extends AlertDialog {

    private Context context;

    private XTaskIdentity filterTaskIdentity;

    private ArrayList<XTaskIdentity> allTaskIdentities;
    private ArrayList<XTaskIdentity> completedTaskIdentities;

    private ArrayList<XTaskCompletion> allCompletions;
    private ArrayList<XTaskCompletion> filteredCompletions;

    private CompletionsTimelineAdapter mAdapter;

    private TasksFilterDialogListener mCallback;

    TasksFilterDialog(Context context, XTaskIdentity filterTaskIdentity, ArrayList<XTaskCompletion> allCompletions, ArrayList<XTaskIdentity> allTaskIdentities, ArrayList<XTaskIdentity> completedTaskIdentities, CompletionsTimelineAdapter adapter, TasksFilterDialogListener callback) {
        super(context);

        this.context = context;
        this.filterTaskIdentity = filterTaskIdentity;
        this.allCompletions = allCompletions;
        this.allTaskIdentities = allTaskIdentities;
        this.completedTaskIdentities = completedTaskIdentities;
        this.mAdapter = adapter;
        this.mCallback = callback;

        displayTasksFilterDialog();

    }

    interface TasksFilterDialogListener{
        void onFilter(XTaskIdentity filterIdentity);
    }

    private void displayTasksFilterDialog() {

        // Filter dialog base views
        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout tasksContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.module_colors_dialog, scrollView, false);
        scrollView.addView(tasksContainer);

        // Filter dialog item to remove any filter from list (i.e. show all completions)
        final ConstraintLayout taskLayoutAll = createTaskFilterItem(null, context.getString(R.string.all_completions), context.getResources().getColor(R.color.colorPrimary),true, tasksContainer);
        tasksContainer.addView(taskLayoutAll);

        // Sort taskIdentities list to show completed tasks on top and non-completed tasks at the bottom of the dialog
        Collections.sort(allTaskIdentities, new Comparator<XTaskIdentity>() {
            @Override
            public int compare(XTaskIdentity o1, XTaskIdentity o2) {
                return Boolean.compare(completedTaskIdentities.contains(o2),completedTaskIdentities.contains(o1));
            }
        });

        // Create items for each of the task identities and add them to the filter dialog container
        for (final XTaskIdentity taskIdentity : allTaskIdentities) {

            final ConstraintLayout taskLayout = createTaskFilterItem(taskIdentity, taskIdentity.getName(), taskIdentity.getColor(), completedTaskIdentities.contains(taskIdentity),tasksContainer);

            tasksContainer.addView(taskLayout);
        }

        // Show actual filter dialog
        this.setView(scrollView);
    }

    private ConstraintLayout createTaskFilterItem(@Nullable final XTaskIdentity taskIdentity, final String name, final int color, final boolean active, final LinearLayout parent) {

        final ConstraintLayout taskLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_dialog_color, parent, false);

        final TextView titleView = taskLayout.findViewById(R.id.dialog_color_title);
        titleView.setText(name);

        final ImageButton selected = taskLayout.findViewById(R.id.dialog_color_selected);
        selected.setColorFilter(getContext().getResources().getColor(R.color.colorAccent));

        final LinearLayout marker = taskLayout.findViewById(R.id.dialog_color_marker);
        int markerColor = active ? color : Color.argb(100, Color.red(color),Color.green(color),Color.blue(color));
        ColorUtilities.setViewBackgroundColor(marker,markerColor);

        // If items represents current filter, apply accent color to signalize this
        if (taskIdentity == filterTaskIdentity) {
            titleView.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
            selected.setVisibility(View.VISIBLE);
        } else {

            int textColor = active ? context.getResources().getColor(R.color.colorPrimary) : Color.argb(100,0,0,0);
            titleView.setTextColor(textColor);

            selected.setVisibility(View.GONE);
        }

        taskLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Stop filtering with uncompleted tasks
                if(!active) return;

                // Hide filter dialog
                dismiss();

                // Remove old filteredCompletions
                mAdapter.removeAllItems();

                // Set filter
                filterTaskIdentity = taskIdentity;

                // Load filteredCompletions array with completions matching filter
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

                // Update recyclerView and it's adapter with the filtered completions array
                mAdapter.loadCompletions(filteredCompletions);

                // Update UI element displaying current tasks filter
                mCallback.onFilter(filterTaskIdentity);
            }
        });

        return taskLayout;
    }


}
