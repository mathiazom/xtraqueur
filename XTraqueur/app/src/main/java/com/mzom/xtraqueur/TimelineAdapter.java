package com.mzom.xtraqueur;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private ArrayList<Boolean> selectionArray;

    private final TimelineAdapterListener timelineAdapterListener;

    private boolean selectionMode;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout mItemLayout;
        final ConstraintLayout mMainItemLayout;

        ViewHolder(ConstraintLayout v) {
            super(v);
            mItemLayout = v;
            mMainItemLayout = v.findViewById(R.id.item_container);
        }
    }

    interface TimelineAdapterListener {
        void onItemClick(int pos,float y);

        int getItemCount();

        boolean onSelectionChanged(ArrayList<Boolean> updatedSelectionArray);

        TimelineItem getTimelineItem(int pos);
    }


    TimelineAdapter(boolean selectionMode, TimelineAdapterListener timelineAdapterListener) {
        this.timelineAdapterListener = timelineAdapterListener;
        this.selectionMode = selectionMode;
        initSelectionArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.template_timeline_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        TimelineItem timelineItem = timelineAdapterListener.getTimelineItem(position);

        TextView new_date = holder.mItemLayout.findViewById(R.id.timeline_new_date_title);

        // Date formatting pattern
        final SimpleDateFormat newDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

        final Date itemDate = new Date(timelineItem.getDate());

        if (holder.getAdapterPosition() != 0) {

            // New date title
            Date prevDate = new Date(timelineAdapterListener.getTimelineItem(holder.getAdapterPosition()-1).getDate());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(itemDate);

            Calendar prevCalendar = Calendar.getInstance();
            prevCalendar.setTime(prevDate);

            boolean newDate = calendar.get(Calendar.DAY_OF_YEAR) != prevCalendar.get(Calendar.DAY_OF_YEAR) || calendar.get(Calendar.YEAR) != prevCalendar.get(Calendar.YEAR);

            if (newDate) {
                new_date.setText(newDateFormat.format(itemDate));
                new_date.setVisibility(View.VISIBLE);
            } else {
                new_date.setVisibility(View.GONE);
            }
        } else {
            new_date.setText(newDateFormat.format(itemDate));
            new_date.setVisibility(View.VISIBLE);
        }

        // Item on click
        holder.mMainItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(holder.getAdapterPosition() == -1) return;

                // Fire long click listener if selection mode is enabled
                if (selectionMode) {
                    onItemSelected(holder);
                    return;
                }

                timelineAdapterListener.onItemClick(holder.getAdapterPosition(),v.getY());
            }
        });

        // Item on hold
        holder.mMainItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                onItemSelected(holder);

                return true;
            }
        });

        // Task name
        String itemTitle = timelineItem.getTitle();

        TextView itemTitleView = holder.mItemLayout.findViewById(R.id.timeline_item_title);
        itemTitleView.setText(itemTitle);

        // Item date
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE d MMM yyyy HH:mm", Locale.getDefault());
        String itemDateString = simpleDateFormat.format(itemDate);
        TextView itemDateView = holder.mItemLayout.findViewById(R.id.timeline_item_date);
        itemDateView.setText(itemDateString);

        // Update selection layout only reachable from adapter
        updateSelectionLayout(holder);
    }

    private void onItemSelected(ViewHolder holder) {

        Log.i("Selection",selectionArray.toString());

        // Get correct ViewHolder position
        int position = holder.getAdapterPosition();

        if(position == -1) return;

        // Toggle selection array value for current position
        selectionArray.set(position, !selectionArray.get(position));

        selectionMode = timelineAdapterListener.onSelectionChanged(selectionArray);

        // Change item layout based on selection
        updateSelectionLayout(holder);

        Log.i("Selection",selectionArray.toString());
    }

    void setAllItemsSelection(boolean selected) {

        // Set all selectionArray values to true
        for (int b = 0;b<getItemCount();b++){
            selectionArray.set(b,selected);
        }

        // Update items layout
        notifyDataSetChanged();

        // Update selection toolbar
        timelineAdapterListener.onSelectionChanged(selectionArray);
    }

    private void initSelectionArray(){
        selectionArray = new ArrayList<>(Arrays.asList(new Boolean[getItemCount()]));
        Collections.fill(selectionArray,false);
    }

    // Change item layout based on selection
    private void updateSelectionLayout(ViewHolder holder) {

        if(selectionArray == null || selectionArray.size() == 0){
            initSelectionArray();
        }

        TimelineItem timelineItem = timelineAdapterListener.getTimelineItem(holder.getAdapterPosition());

        // Selected
        if (selectionArray.get(holder.getAdapterPosition())) {
            // Selected item background
            Drawable itemBackground = holder.mMainItemLayout.getBackground();
            itemBackground.setColorFilter(darkenColor(timelineItem.getColor()), PorterDuff.Mode.SRC_ATOP);
            holder.mMainItemLayout.setBackground(itemBackground);

            // Show selection check mark
            holder.mMainItemLayout.findViewById(R.id.timeline_item_selected_mark).setVisibility(View.VISIBLE);
        }
        // Not selected
        else {
            // Regular item background
            Drawable itemBackground = holder.mMainItemLayout.getBackground();
            itemBackground.setColorFilter(timelineItem.getColor(), PorterDuff.Mode.SRC_ATOP);
            holder.mMainItemLayout.setBackground(itemBackground);

            // Hide selection check mark
            holder.mMainItemLayout.findViewById(R.id.timeline_item_selected_mark).setVisibility(View.GONE);
        }
    }

    // Remotely select items
    void selectItems(ArrayList<Boolean> selections){

        for(int b = 0; b < getItemCount();b++){
            if(selections.get(b)){
                selectionArray.set(b,true);
            }
        }

        // Update items layout
        notifyDataSetChanged();

        // Update selection toolbar
        timelineAdapterListener.onSelectionChanged(selectionArray);
    }

    void setSelection(int position, boolean selected){
        selectionArray.set(position,selected);
    }

    int getTotalSelected(){

        if(selectionArray == null) initSelectionArray();

        int total_selected = 0;
        for(int b = 0; b < getItemCount();b++){
            if(selectionArray.get(b)){
                total_selected++;
            }
        }
        return total_selected;
    }

    void deleteItem(int index){

        selectionArray.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, getItemCount());

        Log.i("TimelineAdapter","Selection: " + selectionArray.toString());

    }

    @Override
    public int getItemCount() {
        return timelineAdapterListener.getItemCount();
    }

    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
