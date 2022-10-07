package com.mzom.xtraqueur;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

/**

        Functionality of CompletionsTimelineAdapter:
     * DATA SET: Provides the data for the base adapter

     * ACTUAL ITEM DELETION FROM DATA SET: This class tries to visually present item deletion. For this to work,
     the extender has to actually remove the item from the data set for this to work properly.

     * SAVING DATA SET CHANGES: This class does not save any changes made to the data set (e.g. item deletion)

     * REACTION TO ITEM CLICKS: This class only alerts extender that an item
     has been clicked, the extender decides how to react to this.

     * SELECTION INFORMATION: This class operates inside the RecyclerView, and therefore gives
     no selection indicators outside this view

 **/

class CompletionsTimelineAdapter extends TimelineAdapter {

    private static final int completionViewResId = R.layout.template_completion;

    private ArrayList<XTaskCompletion> completions = new ArrayList<>();


    CompletionsTimelineAdapter(ArrayList<XTaskCompletion> completions, CompletionsTimelineAdapterListener completionsTimelineAdapterListener) {
        super(completionViewResId, false);

        this.completions = completions;

        this.completionsTimelineAdapterListener = completionsTimelineAdapterListener;
    }

    CompletionsTimelineAdapter(ArrayList<XTaskCompletion> completions, int selectionLock, TimelineAdapterListener completionsTimelineAdapterListener) {
        super(completionViewResId, selectionLock);

        this.completions = completions;
        this.completionsTimelineAdapterListener = completionsTimelineAdapterListener;
    }

    private final TimelineAdapterListener completionsTimelineAdapterListener;

    interface CompletionsTimelineAdapterListener extends TimelineAdapterListener{

        void onDeleteCompletion(XTaskCompletion completion);

    }


    void loadCompletions(ArrayList<XTaskCompletion> completions){
        this.completions = completions;
        timelineItemRangeInserted(completions.size());
    }


    @Override
    void onSelectionChanged(int totalSelected, final boolean isSelecting) {

        completionsTimelineAdapterListener.onSelectionChanged(totalSelected, isSelecting);

    }

    @Override
    void displayItemData(@NonNull ViewHolder holder, int position) {

        final XTaskCompletion completion = completions.get(position);
        final XTaskIdentity taskIdentity = completion.getTaskIdentity();

        CompletionViewHolder completionViewHolder = (CompletionViewHolder) holder;

        completionViewHolder.taskTitle.setText(taskIdentity.getName());

        completionViewHolder.completionDate.setText(DateFormatter.formatDateAndTime(completion.getDate()));

        if(completion.isInstantCompletion()){
            completionViewHolder.completionColorMarker.setVisibility(View.VISIBLE);
            //ColorUtilities.setViewBackgroundColor(completionViewHolder.completionColorMarker);
        }else{
            completionViewHolder.completionColorMarker.setVisibility(View.GONE);
        }

    }


    @Override
    void displaySelectionState(@NonNull ViewHolder holder, boolean selected) {

        final XTaskCompletion completion = completions.get(holder.getAdapterPosition());
        final XTaskIdentity taskIdentity = completion.getTaskIdentity();

        CompletionViewHolder completionViewHolder = (CompletionViewHolder) holder;

        // Selected
        if (selected) {
            // Selected item background
            Drawable itemBackground = holder.itemDataLayout.getBackground();
            itemBackground.setColorFilter(ColorUtilities.getDarkerColor(taskIdentity.getColor()), PorterDuff.Mode.SRC_ATOP);
            holder.itemDataLayout.setBackground(itemBackground);

            // Show selection check mark
            completionViewHolder.selectedMark.setVisibility(View.VISIBLE);
        }
        // Not selected
        else {
            // Regular item background
            Drawable itemBackground = holder.itemDataLayout.getBackground();
            itemBackground.setColorFilter(taskIdentity.getColor(), PorterDuff.Mode.SRC_ATOP);
            holder.itemDataLayout.setBackground(itemBackground);

            // Hide selection check mark
            completionViewHolder.selectedMark.setVisibility(View.GONE);
        }

    }

    @Override
    void deleteItemData(int position) {

        final XTaskCompletion completion = completions.get(position);

        completions.remove(completion);

        ((CompletionsTimelineAdapterListener) completionsTimelineAdapterListener).onDeleteCompletion(completion);

    }

    @Override
    void onItemsDataChanged() {

        completionsTimelineAdapterListener.onItemsDataChanged();

    }

    @Override
    void onItemClicked(int position, int yPos) {

        completionsTimelineAdapterListener.onItemClicked(position, yPos);

    }

    @Override
    Date getItemDate(int position) {
        return new Date(completions.get(position).getDate());
    }

    @Override
    int getItemColor(int position) {
        return completions.get(position).getTaskIdentity().getColor();
    }

    @Override
    public int getItemCount() {
        if(completions == null) return 0;
        return completions.size();
    }


    private class CompletionViewHolder extends ViewHolder {

        final TextView taskTitle;
        final TextView completionDate;
        final ImageView selectedMark;
        final View completionColorMarker;

        private CompletionViewHolder(ConstraintLayout itemBaseLayout) {
            super(itemBaseLayout);

            taskTitle = itemBaseLayout.findViewById(R.id.completion_item_title);
            completionDate = itemBaseLayout.findViewById(R.id.completion_item_date);
            selectedMark = itemBaseLayout.findViewById(R.id.completion_item_selected_mark);
            completionColorMarker = itemBaseLayout.findViewById(R.id.instant_completion_color_marker);
        }
    }

    @Override
    ViewHolder onCreateViewHolder(ConstraintLayout itemBaseLayout) {
        return new CompletionViewHolder(itemBaseLayout);
    }
}
