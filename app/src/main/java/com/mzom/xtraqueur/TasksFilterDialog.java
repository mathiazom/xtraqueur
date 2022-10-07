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

    private XTaskIdentity filterTaskIdentity;

    private ArrayList<XTaskIdentity> allTaskIdentities;
    private ArrayList<XTaskIdentity> completedTaskIdentities;

    private TasksFilterDialogListener mCallback;

    interface TasksFilterDialogListener {
        void onFilter(XTaskIdentity filterIdentity);
    }

    TasksFilterDialog(Context context, XTaskIdentity filterTaskIdentity, ArrayList<XTaskIdentity> allTaskIdentities, ArrayList<XTaskIdentity> completedTaskIdentities, TasksFilterDialogListener callback) {
        super(context);

        this.filterTaskIdentity = filterTaskIdentity;

        this.completedTaskIdentities = completedTaskIdentities;

        this.allTaskIdentities = allTaskIdentities;
        sortTaskIdentitiesByCompletion();

        this.mCallback = callback;

    }

    @Override
    public void show() {

        // Filter dialog base views
        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout tasksContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.module_colors_dialog, scrollView, false);
        scrollView.addView(tasksContainer);

        // Filter dialog item to remove any filter from list (i.e. show all completions)
        final ConstraintLayout taskLayoutAll = createTaskFilterItem(null, getContext().getString(R.string.all_completions), getContext().getResources().getColor(R.color.colorPrimary), true, tasksContainer);
        tasksContainer.addView(taskLayoutAll);


        // Create items for each of the task identities and add them to the filter dialog container
        for (final XTaskIdentity taskIdentity : allTaskIdentities) {

            final ConstraintLayout taskLayout = createTaskFilterItem(taskIdentity, taskIdentity.getName(), taskIdentity.getColor(), completedTaskIdentities.contains(taskIdentity), tasksContainer);

            tasksContainer.addView(taskLayout);
        }

        // Show actual filter dialog
        this.setView(scrollView);

        super.show();
    }

    private ConstraintLayout createTaskFilterItem(@Nullable final XTaskIdentity taskIdentity, final String name, final int color, final boolean active, final LinearLayout parent) {

        final ConstraintLayout taskLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_dialog_color, parent, false);

        final TextView titleView = taskLayout.findViewById(R.id.dialog_color_title);
        titleView.setText(name);

        final ImageButton selected = taskLayout.findViewById(R.id.dialog_color_selected);
        selected.setColorFilter(getContext().getResources().getColor(R.color.colorAccent));

        final LinearLayout marker = taskLayout.findViewById(R.id.dialog_color_marker);
        int markerColor = active ? color : Color.argb(100, Color.red(color), Color.green(color), Color.blue(color));
        ColorUtilities.setViewBackgroundColor(marker, markerColor);

        // If items represents current filter, apply accent color to signalize this
        if (taskIdentity == filterTaskIdentity) {
            titleView.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
            selected.setVisibility(View.VISIBLE);
        } else {

            int textColor = active ? getContext().getResources().getColor(R.color.colorPrimary) : Color.argb(100, 0, 0, 0);
            titleView.setTextColor(textColor);

            selected.setVisibility(View.GONE);
        }

        taskLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Stop filtering with uncompleted tasks
                if (!active) return;

                // Hide filter dialog
                dismiss();

                // Set filter
                filterTaskIdentity = taskIdentity;

                // Update adapter and UI in fragment
                mCallback.onFilter(filterTaskIdentity);
            }
        });

        return taskLayout;
    }

    // Sort taskIdentities list to show completed tasks on top and non-completed tasks at the bottom of the dialog
    private void sortTaskIdentitiesByCompletion() {
        Collections.sort(allTaskIdentities, new Comparator<XTaskIdentity>() {
            @Override
            public int compare(XTaskIdentity o1, XTaskIdentity o2) {
                return Boolean.compare(completedTaskIdentities.contains(o2), completedTaskIdentities.contains(o1));
            }
        });
    }


}
