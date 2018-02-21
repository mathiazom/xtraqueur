package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

class CompletionsDialog extends AlertDialog {

    private final ArrayList<XTask> tasks;
    private final XTask task;
    private final int index;
    private ArrayList<Long> completionsList;

    private ConstraintLayout completionsModule;

    private OnCompletionDeletedListener onCompletionDeletedListener;

    CompletionsDialog(Context context, ArrayList<XTask> tasks, XTask task, int index) {
        super(context);
        this.index = index;
        this.task = task;
        this.tasks = tasks;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) return;
        completionsModule = (ConstraintLayout) layoutInflater.inflate(R.layout.module_completions, null, false);
        setContentView(completionsModule);

        loadDialog();
    }

    interface OnCompletionDeletedListener {
        void onCompletionDeleted(ArrayList<XTask> updated_tasks);
    }

    void setOnCompletionDeletedListener(OnCompletionDeletedListener onCompletionDeletedListener) {
        this.onCompletionDeletedListener = onCompletionDeletedListener;
    }

    private void loadDialog() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) return;

        completionsList = task.getCompletionsList();

        if (completionsList == null || completionsList.size() == 0) {
            return;
        }

        this.setMessage(getContext().getResources().getString(R.string.completions_message));
        this.setTitle(R.string.completions_title);

        final LinearLayout completionsContainer = completionsModule.findViewById(R.id.completions_container);

        // TINT BACKGROUND
        /*Drawable containerBackground = new ColorDrawable(Color.TRANSPARENT);
        containerBackground.setColorFilter(getContext().getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        completionsContainer.setBackground(containerBackground);*/

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE d MMM yyyy HH:mm", Locale.getDefault());

        for (final Long l : completionsList) {
            final ConstraintLayout completion = (ConstraintLayout) layoutInflater.inflate(R.layout.template_completion, completionsContainer, false);
            Date d = new Date(l);
            String date = simpleDateFormat.format(d);
            Date now = new Date();

            String time_since_msg;

            int minutes_ago = (int) TimeUnit.MINUTES.convert(now.getTime() - d.getTime(), TimeUnit.MILLISECONDS);

            if (minutes_ago > 59) {
                int hours_ago = (int) TimeUnit.HOURS.convert(now.getTime() - d.getTime(), TimeUnit.MILLISECONDS);
                if (hours_ago > 23) {
                    int days_ago = (int) TimeUnit.DAYS.convert(now.getTime() - d.getTime(), TimeUnit.MILLISECONDS);
                    time_since_msg = String.valueOf(days_ago) + " " + getContext().getResources().getString(R.string.days_ago);
                } else {
                    time_since_msg = String.valueOf(hours_ago) + " " + getContext().getResources().getString(R.string.hours_ago);
                }
            } else if (minutes_ago == 0) {
                time_since_msg = getContext().getResources().getString(R.string.just_now);
            } else {
                time_since_msg = String.valueOf(minutes_ago) + " " + getContext().getResources().getString(R.string.minutes_ago);
            }

            //String days_ago_msg = days_ago > 0 ? String.valueOf(days_ago) + " " + getContext().getResources().getString(R.string.days_ago) : (getContext().getResources().getString(R.string.today));
            ((TextView) completion.findViewById(R.id.completion_days_ago)).setText(time_since_msg);

            ((TextView) completion.findViewById(R.id.completion_date)).setText(date);

            completion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog.Builder datePickerDialog = new DatePickerDialog.Builder(getContext());
                    datePickerDialog.create();
                    datePickerDialog.show();
                }
            });

            completion.findViewById(R.id.delete_completion).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteCompletion(completionsModule, completion, l);
                }
            });

            // TINT COMPLETION
            Drawable completionBackground = completion.getBackground();
            if(completionBackground == null){
                completionBackground = new ColorDrawable(darkenColor(task.getColor()));
            }
            completionBackground.setColorFilter(darkenColor(task.getColor()), PorterDuff.Mode.SRC_ATOP);
            completion.setBackground(completionBackground);

            completionsContainer.addView(completion, 0);
        }
    }

    private void deleteCompletion(ConstraintLayout completionsModule, ConstraintLayout completion_layout, Long l) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) return;

        final LinearLayout completionsContainer = completionsModule.findViewById(R.id.completions_container);

        completionsList.remove(l);
        completionsContainer.removeView(completion_layout);

        // CLOSE DIALOG IF COMPLETION LIST IS EMPTY
        if (completionsList.size() == 0) {
            this.dismiss();
        }

        XTask new_task = tasks.get(index);
        new_task.setCompletionsList(completionsList);
        tasks.set(index, new_task);

        if (onCompletionDeletedListener != null) {
            onCompletionDeletedListener.onCompletionDeleted(tasks);
        }


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
