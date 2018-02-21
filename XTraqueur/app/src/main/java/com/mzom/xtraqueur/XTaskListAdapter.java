package com.mzom.xtraqueur;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;

class XTaskListAdapter extends ArrayAdapter<XTask> {

    private final Context ctx;

    // Data set holding all tasks
    private final ArrayList<XTask> tasks;

    private ViewGroup parent;

    private final XTaskListAdapterListener xTaskListAdapterListener;

    private static final String TAG = "Xtraqueur-ListAdapter";

    public interface XTaskListAdapterListener {
        void onUpdateTasks(ArrayList<XTask> tasks);

        void updateTasksDataOnDrive(ArrayList<XTask> tasks);

        boolean useDarkText(int color);
    }

    XTaskListAdapter(Context ctx, ArrayList<XTask> tasks, XTaskListAdapterListener xTaskListAdapterListener) {
        super(ctx, -1, tasks);
        this.ctx = ctx;
        this.tasks = tasks;

        this.xTaskListAdapterListener = xTaskListAdapterListener;
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
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.template_xtask, parent, false);
        }

        // Set item background according to task color
        Drawable drawable = new ColorDrawable(darkenColor(task.getColor()));
        convertView.findViewById(R.id.xtask).setBackground(drawable);

        // Determine if task color fits dark or light text
        boolean useDarkText = useDarkText(task.getColor());
        int contrastColor = useDarkText ? getContext().getResources().getColor(R.color.colorPrimary) : Color.parseColor("#eeeeee");

        // Task name
        TextView tv_name = convertView.findViewById(R.id.xtask_name);
        tv_name.setText(task.getName());
        tv_name.setTextColor(contrastColor);

        // Task completions count
        TextView tv_completions = (convertView.findViewById(R.id.xtask_completions));
        tv_completions.setText(String.valueOf(task.getCompletions()));
        tv_completions.setTextColor(contrastColor);

        // Addition button
        ImageButton button_add = convertView.findViewById(R.id.xtask_button_add);

        // Subtraction button
        ImageButton button_subtract = convertView.findViewById(R.id.xtask_button_subtract);

        // Button background color
        int contrastValue = useDarkText ? 33 : 255;

        // Subtraction button background
        Drawable button_drawable1 = button_add.getBackground();
        button_drawable1.setColorFilter(Color.argb(51, contrastValue, contrastValue, contrastValue), PorterDuff.Mode.MULTIPLY);
        button_subtract.setBackground(button_drawable1);

        // Addition button background
        Drawable button_drawable2 = button_subtract.getBackground();
        button_drawable2.setColorFilter(Color.argb(51, contrastValue, contrastValue, contrastValue), PorterDuff.Mode.MULTIPLY);
        button_add.setBackground(button_drawable2);

        // Store item view in final variable to use in listeners
        final View taskView = convertView;

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

                CompletionsDialog completionsDialog = new CompletionsDialog(getContext(), tasks, task, position);
                completionsDialog.setOnCompletionDeletedListener(new CompletionsDialog.OnCompletionDeletedListener() {
                    @Override
                    public void onCompletionDeleted(ArrayList<XTask> updated_tasks) {
                        xTaskListAdapterListener.onUpdateTasks(updated_tasks);
                        ((TextView) taskView.findViewById(R.id.xtask_completions)).setText(String.valueOf(updated_tasks.get(position).getCompletions()));
                    }
                });
                completionsDialog.show();
            }
        });

        return convertView;
    }

    // UPDATE TASKLIST FOR NEXT LISTVIEW UPDATE
    private void updateTask(XTask t, View v) {
        // GOOGLE DRIVE
        xTaskListAdapterListener.updateTasksDataOnDrive(tasks);

        // TEMPORARY CHANGE
        ((TextView) v.findViewById(R.id.xtask_completions)).setText(String.valueOf(t.getCompletions()));
        Activity activity = (Activity) ctx;

        // UPDATE TOTAL VALUE TEXTVIEW
        int total = 0;
        for (XTask task : tasks) {
            total += task.getValue();
        }

        String total_string = String.valueOf(total) + " kr";
        ((TextView) activity.findViewById(R.id.tasks_total_value)).setText(total_string);

    }

    private void updateToalValue(){

    }

    private boolean useDarkText(int color) {
        double r = Color.red(color);
        double g = Color.green(color);
        double b = Color.blue(color);

        return (r * 0.299 + g * 0.587 + b * 0.114) > 186;
    }


    // MISC
    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
