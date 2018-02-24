package com.mzom.xtraqueur;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private final ArrayList<XTaskCompletion> completions;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout mConstraintLayout;

        ViewHolder(ConstraintLayout v) {
            super(v);
            mConstraintLayout = v;
        }
    }

    TimelineAdapter(ArrayList<XTaskCompletion> completions) {
        this.completions = completions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.template_timeline_completion, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Completion object
        XTaskCompletion completion = completions.get(position);

        TextView new_date = holder.mConstraintLayout.findViewById(R.id.timeline_new_date_title);

        if(position != 0){
            XTaskCompletion prevCompletion = completions.get(position-1);

            // New date title
            Date prevDate = new Date(prevCompletion.getDate());
            Date date = new Date(completion.getDate());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            Calendar prevCalendar = Calendar.getInstance();
            prevCalendar.setTime(prevDate);

            boolean newDate = calendar.get(Calendar.DAY_OF_YEAR) != prevCalendar.get(Calendar.DAY_OF_YEAR) || calendar.get(Calendar.YEAR) != prevCalendar.get(Calendar.YEAR);

            if(newDate){
                final SimpleDateFormat newDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
                new_date.setText(newDateFormat.format(date));
                new_date.setVisibility(View.VISIBLE);
            }else{
                new_date.setVisibility(View.GONE);
            }
        }
        else{
            final SimpleDateFormat newDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            new_date.setText(newDateFormat.format(new Date(completion.getDate())));
            new_date.setVisibility(View.VISIBLE);
        }

        // Task name
        String completionTask = completion.getTaskName();
        TextView completionTaskView = holder.mConstraintLayout.findViewById(R.id.timeline_completion_task);
        completionTaskView.setText(completionTask);

        // Completion date
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE d MMM yyyy HH:mm", Locale.getDefault());
        String completionDate = simpleDateFormat.format(new Date(completion.getDate()));
        TextView completionDateView = holder.mConstraintLayout.findViewById(R.id.timeline_completion_date);
        completionDateView.setText(completionDate);

        // Task color
        Drawable completionBackground = holder.mConstraintLayout.findViewById(R.id.completion).getBackground();
        completionBackground.setColorFilter(completion.getTaskColor(),PorterDuff.Mode.SRC_ATOP);
        holder.mConstraintLayout.findViewById(R.id.completion).setBackground(completionBackground);


    }

    @Override
    public int getItemCount() {
        return completions.size();
    }
}
