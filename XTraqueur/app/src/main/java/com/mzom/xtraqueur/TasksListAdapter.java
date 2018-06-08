package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

    private static final String TAG = "Xtraqueur-ListAdapter";

    public interface XTaskListAdapterListener {

        void onUpdateTasks(ArrayList<XTask> tasks);
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

        // Get task's taskFields object from current position
        final XTaskFields taskFields = task.getTaskFields();

        // Task view inflation
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.template_task, parent, false);
        }

        final TaskViewHolder holder = new TaskViewHolder((ConstraintLayout) convertView);

        // Task name
        TextView tv_name = holder.mTaskName;
        tv_name.setText(taskFields.getName());
        tv_name.setTextColor(getContext().getResources().getColor(R.color.colorWhite));

        // Task payments count
        TextView tv_completions = holder.mTaskCompletions;
        tv_completions.setText(String.valueOf(task.getCompletionsCount()));
        tv_completions.setTextColor(getContext().getResources().getColor(R.color.colorWhite));

        // Addition button
        ImageButton button_add = holder.mAddButton;

        // Subtraction button background
        Drawable button_drawable1 = button_add.getBackground();
        button_drawable1.setColorFilter(Color.argb(51, 255, 255, 255), PorterDuff.Mode.MULTIPLY);

        //button_add.setColorFilter(task.getColor());
        button_add.setColorFilter(getContext().getResources().getColor(R.color.colorWhite));

        // Addition button listener
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.newCompletion();
                xTaskListAdapterListener.onUpdateTasks(tasks);
            }
        });

        // Set task view background according to task color
        convertView.setBackground(new ColorDrawable(taskFields.getColor()));

        return convertView;
    }
}
