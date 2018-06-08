package com.mzom.xtraqueur;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

class CompletionsTimelineAdapter extends TimelineAdapter {

    private static final int completionViewResId = R.layout.template_timeline_completion_item;

    private ArrayList<XTaskCompletion> completions = new ArrayList<>();

    CompletionsTimelineAdapter(ArrayList<XTaskCompletion> completions, CompletionsTimelineAdapterNoLockListener completionsTimelineAdapterListener) {
        super(completionViewResId, false);

        this.completions = completions;
        this.completionsTimelineAdapterListener = completionsTimelineAdapterListener;
    }

    CompletionsTimelineAdapter(ArrayList<XTaskCompletion> completions, int selectionModeLock, TimelineAdapterLockListener completionsTimelineAdapterListener) {
        super(completionViewResId, selectionModeLock);

        this.completions = completions;
        this.completionsTimelineAdapterListener = completionsTimelineAdapterListener;
    }

    private final TimelineAdapterLockListener completionsTimelineAdapterListener;

    interface CompletionsTimelineAdapterNoLockListener extends TimelineAdapterListener{

        void deleteCompletionData(XTaskCompletion completion);

    }

    void setCompletions(ArrayList<XTaskCompletion> completions){
        this.completions = completions;
    }


    @Override
    void onSelectionChanged(int totalSelected, final boolean isSelecting) {

        completionsTimelineAdapterListener.onSelectionChanged(totalSelected, isSelecting);

    }

    @Override
    void displayItemData(@NonNull ViewHolder holder, int position) {

        final XTaskCompletion completion = completions.get(position);
        final XTaskFields taskFields = completion.getTaskFields();

        CompletionViewHolder completionViewHolder = (CompletionViewHolder) holder;

        setItemBackgroundColor(holder, taskFields.getColor());

        completionViewHolder.taskTitle.setText(taskFields.getName());

        completionViewHolder.completionDate.setText(DateFormatter.formatDate(completion.getDate()));

    }


    @Override
    void displaySelectionState(@NonNull ViewHolder holder, boolean selected) {

        final XTaskCompletion completion = completions.get(holder.getAdapterPosition());
        final XTaskFields taskFields = completion.getTaskFields();

        CompletionViewHolder completionViewHolder = (CompletionViewHolder) holder;

        // Selected
        if (selected) {
            // Selected item background
            Drawable itemBackground = holder.itemDataLayout.getBackground();
            itemBackground.setColorFilter(darkenColor(taskFields.getColor()), PorterDuff.Mode.SRC_ATOP);
            holder.itemDataLayout.setBackground(itemBackground);

            // Show selection check mark
            completionViewHolder.selectedMark.setVisibility(View.VISIBLE);
        }
        // Not selected
        else {
            // Regular item background
            Drawable itemBackground = holder.itemDataLayout.getBackground();
            itemBackground.setColorFilter(taskFields.getColor(), PorterDuff.Mode.SRC_ATOP);
            holder.itemDataLayout.setBackground(itemBackground);

            // Hide selection check mark
            completionViewHolder.selectedMark.setVisibility(View.GONE);
        }

    }

    @Override
    void deleteItemData(int position) {

        final XTaskCompletion completion = completions.get(position);

        completions.remove(completion);

        ((CompletionsTimelineAdapterNoLockListener) completionsTimelineAdapterListener).deleteCompletionData(completion);

    }

    @Override
    void onItemsDataChanged() {

        ((CompletionsTimelineAdapterNoLockListener) completionsTimelineAdapterListener).onItemsDataChanged();

    }

    @Override
    void onItemClicked(int position, int yPos) {

        ((CompletionsTimelineAdapterNoLockListener) completionsTimelineAdapterListener).onItemClicked(position, yPos);

    }

    @Override
    Date getItemDate(int position) {
        return new Date(completions.get(position).getDate());
    }

    @Override
    String getItemDateString(int position) {
        return DateFormatter.formatDate(completions.get(position).getDate());
    }

    @Override
    int getItemsDataSize() {
        if(completions == null) return 0;
        return completions.size();
    }


    private class CompletionViewHolder extends ViewHolder {

        final TextView taskTitle;
        final TextView completionDate;
        final ImageView selectedMark;

        private CompletionViewHolder(ConstraintLayout itemBaseLayout) {
            super(itemBaseLayout);

            taskTitle = itemBaseLayout.findViewById(R.id.completion_item_title);
            completionDate = itemBaseLayout.findViewById(R.id.completion_item_date);
            selectedMark = itemBaseLayout.findViewById(R.id.completion_item_selected_mark);
        }
    }

    @Override
    ViewHolder onCreateViewHolder(ConstraintLayout itemBaseLayout) {
        return new CompletionViewHolder(itemBaseLayout);
    }

    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
