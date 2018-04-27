package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
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

class XTaskListAdapter extends ArrayAdapter<XTask> {

    private final LayoutInflater mLayoutInflater;

    private ViewGroup parent;

    private final XTaskListAdapterListener xTaskListAdapterListener;

    // Data set holding all tasks
    private final ArrayList<XTask> tasks;

    private static final String TAG = "Xtraqueur-ListAdapter";

    public interface XTaskListAdapterListener {
        void onUpdateTasks(ArrayList<XTask> tasks);

        // Show task completions in timeline
        void loadTimeline(ArrayList<XTask> tasks,XTask filterTask);
    }

    XTaskListAdapter(Context ctx, ArrayList<XTask> tasks, XTaskListAdapterListener xTaskListAdapterListener) {
        super(ctx, -1, tasks);
        this.mLayoutInflater = LayoutInflater.from(ctx);
        this.xTaskListAdapterListener = xTaskListAdapterListener;

        this.tasks = tasks;

    }

    static class ViewHolder{
        final TextView mTaskName;
        final TextView mTaskCompletions;
        final ImageButton mAddButton;
        final ImageButton mSubtractButton;

        ViewHolder(ConstraintLayout v){
            mTaskName = v.findViewById(R.id.xtask_name);
            mTaskCompletions = v.findViewById(R.id.xtask_completions);
            mAddButton = v.findViewById(R.id.xtask_button_add);
            mSubtractButton = v.findViewById(R.id.xtask_button_subtract);
        }
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {

        // Save parent for later use
        if(this.parent == null) this.parent = parent;

        // Get task object from current position
        final XTask task = tasks.get(position);

        // Task view inflation
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.template_xtask, parent, false);
        }

        final ViewHolder holder = new ViewHolder((ConstraintLayout) convertView);

        // Determine if task color fits dark or light text
        int textColor = Color.parseColor("#eeeeee");

        // Task name
        TextView tv_name = holder.mTaskName;
        tv_name.setText(task.getName());
        tv_name.setTextColor(textColor);

        // Task completions count
        TextView tv_completions = holder.mTaskCompletions;
        tv_completions.setText(String.valueOf(task.getCompletions()));
        tv_completions.setTextColor(textColor);

        // Addition button
        ImageButton button_add = holder.mAddButton;

        // Subtraction button
        ImageButton button_subtract = holder.mSubtractButton;

        // Subtraction button background
        Drawable button_drawable1 = button_add.getBackground();
        button_drawable1.setColorFilter(Color.argb(51, 255, 255, 255), PorterDuff.Mode.MULTIPLY);
        button_subtract.setBackground(button_drawable1);

        // Addition button background
        Drawable button_drawable2 = button_subtract.getBackground();
        button_drawable2.setColorFilter(Color.argb(51, 255, 255, 255), PorterDuff.Mode.MULTIPLY);
        button_add.setBackground(button_drawable2);

        // Addition button listener
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.addToCompletions();
                xTaskListAdapterListener.onUpdateTasks(tasks);
            }
        });

        // Subtraction listener
        button_subtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (task.getCompletionsList().size() == 0) return;

                xTaskListAdapterListener.loadTimeline(tasks,task);

                /*CompletionsDialog completionsDialog = new CompletionsDialog(getContext(), tasks, task, position,new CompletionsDialog.OnCompletionDeletedListener() {
                    @Override
                    public void onCompletionDeleted(ArrayList<XTask> updated_tasks) {
                        xTaskListAdapterListener.onUpdateTasks(updated_tasks);
                        holder.mTaskCompletions.setText(String.valueOf(updated_tasks.get(position).getCompletions()));
                    }
                });
                completionsDialog.show();*/
            }
        });

        // Set task view background according to task color
        //convertView.setBackground(new ColorDrawable(darkenColor(task.getColor())));
        convertView.setBackground(new ColorDrawable(task.getColor()));

        return convertView;
    }
}
