package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

class TasksListAdapter extends ArrayAdapter<XTask> {

    private final LayoutInflater mLayoutInflater;

    private ViewGroup parent;

    private final XTaskListAdapterListener xTaskListAdapterListener;

    // Data set holding all tasks
    private final ArrayList<XTask> tasks;

    public interface XTaskListAdapterListener {

        void onTasksUpdated(ArrayList<XTask> updatedTasks);
    }

    TasksListAdapter(Context ctx, ArrayList<XTask> tasks, XTaskListAdapterListener xTaskListAdapterListener) {
        super(ctx, -1, tasks);
        this.mLayoutInflater = LayoutInflater.from(ctx);
        this.xTaskListAdapterListener = xTaskListAdapterListener;
        this.tasks = tasks;

    }

    static class TaskViewHolder {
        final TextView mTaskName;
        final TextView mTaskCompletions;
        final ImageButton mAddButton;

        TaskViewHolder(ConstraintLayout v){
            mTaskName = v.findViewById(R.id.xtask_name);
            mTaskCompletions = v.findViewById(R.id.xtask_completions);
            mAddButton = v.findViewById(R.id.xtask_button_add);
        }
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {

        // Save parent for later use
        if(this.parent == null) this.parent = parent;

        // Get task object from current position
        final XTask task = tasks.get(position);

        // Get task's taskIdentity object from current position
        final XTaskIdentity taskIdentity = task.getTaskIdentity();

        // Task view inflation
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.template_task, parent, false);
        }

        final TaskViewHolder holder = new TaskViewHolder((ConstraintLayout) convertView);

        // Task name
        TextView tv_name = holder.mTaskName;
        tv_name.setText(taskIdentity.getName());

        // Task payments count
        TextView tv_completions = holder.mTaskCompletions;
        tv_completions.setText(String.valueOf(task.getCompletionsCount()));

        // Addition button
        ImageButton button_add = holder.mAddButton;
        button_add.setColorFilter(getContext().getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);

        // Addition button listener
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create new XTaskCompletion instance and store it inside task
                task.registerCompletion();

                // Update tasks_data.txt stored with Google Drive
                XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME,tasks,getContext());

                xTaskListAdapterListener.onTasksUpdated(tasks);
            }
        });

        /*button_add.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new RegisterCompletionDialog(getContext(), task, new RegisterCompletionDialog.RegisterCompletionDialogInterface() {
                    @Override
                    public void onCompletionRegistered(Date completionDate) {
                        task.registerCompletion(completionDate);

                        // Update tasks_data.txt stored with Google Drive
                        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME,tasks,getContext());

                        xTaskListAdapterListener.onTasksUpdated(tasks);
                    }
                }).show();

                // Consume long click to prevent firing regular on click
                return true;
            }
        });*/

        // Set task view background according to task color
        ColorUtilities.setViewBackgroundColor(convertView.findViewById(R.id.xtask),taskIdentity.getColor());

        return convertView;
    }
}
