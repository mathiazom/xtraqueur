package com.mzom.xtraqueur;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private static final String TAG = "XTQ-TimelineAdapter";

    private ViewHolder customViewHolder;

    private boolean hasCustomViewHolder;
    /*
    private int customLayoutRes;
    private int customTitleViewResId;
    private int customDateViewResId;
    private int customMarkerResId;
*/
    private ArrayList<Boolean> selectionArray;

    private boolean selectionMode;

    private static final int NO_SELECTION_LOCK = 0;
    static final int ALWAYS_SELECTING = 100;
    private static final int NEVER_SELECTING = 200;

    private final int selectionModeLock;

    private final TimelineAdapterListener timelineAdapterListener;

    interface TimelineAdapterListener {

        TimelineItem getTimelineItem(int pos);

        void onItemClick(int pos, float y);

        void onDatasetChanged();

        void onSelectionModeToggled(boolean isSelecting);

        void onSelectionChanged(ArrayList<Boolean> updatedSelectionArray);

        void deleteItemData(int index);

        int getItemCount();

    }

    private TimelineCustomViewListener customViewHolderListener;

    interface TimelineCustomViewListener {

        ViewHolder getCustomViewHolder(ConstraintLayout itemBase);

        void displayItemData(@NonNull final ViewHolder holder, final TimelineItem timelineItem);

        void markSelection(final ViewHolder holder, final TimelineItem timelineItem, final int position, final boolean isSelected);

    }

    private RecyclerView mRecyclerView;


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }


    TimelineAdapter(boolean selectionMode, TimelineAdapterListener timelineAdapterListener) {
        this(selectionMode, NO_SELECTION_LOCK, timelineAdapterListener);
    }

    TimelineAdapter(int selectionModeLock, TimelineAdapterListener timelineAdapterListener) {
        this(selectionModeLock == ALWAYS_SELECTING, selectionModeLock, timelineAdapterListener);
    }

    private TimelineAdapter(boolean selectionMode, int selectionModeLock, TimelineAdapterListener timelineAdapterListener) {
        this.timelineAdapterListener = timelineAdapterListener;
        this.selectionMode = selectionMode;
        this.selectionModeLock = selectionModeLock;
        initSelectionArray();
    }


    // Base view holder for both default and custom view
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout itemBaseLayout;
        ConstraintLayout itemLayout;
        final TextView itemDateHeader;

        final TextView itemTitleView;
        final TextView itemDateView;
        final ImageView itemSelectedMark;

        private ViewHolder(ConstraintLayout itemContainer) {
            super(itemContainer);

            // Required views
            this.itemBaseLayout = itemContainer;
            this.itemLayout = itemContainer.findViewById(R.id.item_default_view);
            this.itemDateHeader = itemContainer.findViewById(R.id.timeline_item_date_header);

            // Optional views
            this.itemTitleView = itemContainer.findViewById(R.id.timeline_item_title);
            this.itemDateView = itemContainer.findViewById(R.id.timeline_item_date);
            this.itemSelectedMark = itemContainer.findViewById(R.id.timeline_item_selected_mark);
        }

        ViewHolder(ConstraintLayout itemContainer,@NonNull ConstraintLayout customLayout,
                   @NonNull TextView itemTitleView,
                   @NonNull TextView itemDateView,
                   @NonNull ImageView itemSelectedMark) {

            super(itemContainer);

            // Required views
            this.itemBaseLayout = itemContainer;
            this.itemLayout = customLayout;
            this.itemDateHeader = itemBaseLayout.findViewById(R.id.timeline_item_date_header);

            // Optional views
            this.itemTitleView = itemTitleView;
            this.itemDateView = itemDateView;
            this.itemSelectedMark = itemSelectedMark;

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final ConstraintLayout itemBase = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.template_timeline_item, parent, false);

        if(hasCustomViewHolder){
            return customViewHolderListener.getCustomViewHolder(itemBase);
        }

        // Base item layout (both for custom and default layout)
        final ConstraintLayout layoutContainer = itemBase.findViewById(R.id.item_container);


        // Use default layout
        final ConstraintLayout defaultLayout = (ConstraintLayout) LayoutInflater.from(itemBase.getContext()).inflate(R.layout.template_timeline_default_item, itemBase, false);
        layoutContainer.addView(defaultLayout);

        // Use default view holder
        return new ViewHolder(itemBase);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        // Current item
        final TimelineItem timelineItem = timelineAdapterListener.getTimelineItem(position);

        // Display any custom data in custom views
        if (hasCustomViewHolder & customViewHolderListener != null) {
            customViewHolderListener.displayItemData(holder, timelineItem);
        }

        // Display item title, date and color with default layout
        displayItemData(holder, timelineItem);

        // Change layout to mark item selection state
        markSelection(holder, position);

        // Additional date header if item starts new date
        setItemDateHeader(holder, new Date(timelineItem.getDate()));

        // Set item click- and hold listener
        setItemListeners(holder);
    }


    void setCustomViewHolder(TimelineCustomViewListener customViewHolderListener){

        if(customViewHolderListener == null) return;

        Log.i(TAG,"hasCustomViewHolder");
        this.hasCustomViewHolder = true;
        this.customViewHolderListener = customViewHolderListener;

    }

    private void displayItemData(@NonNull final ViewHolder holder, final TimelineItem timelineItem) {

        // Item title
        final TextView itemTitleView = holder.itemTitleView;
        itemTitleView.setText(timelineItem.getTitle());

        // Item date
        final TextView itemDateView = holder.itemDateView;
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE d MMM yyyy HH:mm", Locale.getDefault());
        final Date itemDate = new Date(timelineItem.getDate());
        itemDateView.setText(simpleDateFormat.format(itemDate));

        // Item background
        Drawable itemBackground = holder.itemLayout.getBackground();
        itemBackground.setColorFilter(timelineItem.getColor(), PorterDuff.Mode.SRC_ATOP);
        holder.itemLayout.setBackground(itemBackground);
    }

    private void setItemDateHeader(@NonNull final ViewHolder holder, final Date itemDate) {

        // Marks if item layout needs an additional date header to mark a new date
        boolean needsDateHeader;

        // If item is not first in list, check if item needs a date header
        if (holder.getAdapterPosition() != 0) {

            // Time of current item
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(itemDate);

            // Time of previous item
            final Date prevDate = new Date(timelineAdapterListener.getTimelineItem(holder.getAdapterPosition() - 1).getDate());
            final Calendar prevCalendar = Calendar.getInstance();
            prevCalendar.setTime(prevDate);

            // Check if item has different date than previous item and needs date header
            needsDateHeader = calendar.get(Calendar.DAY_OF_YEAR) != prevCalendar.get(Calendar.DAY_OF_YEAR) || calendar.get(Calendar.YEAR) != prevCalendar.get(Calendar.YEAR);

        } else {
            // item is first item on the list, therefore also needs a date header
            needsDateHeader = true;
        }

        if (needsDateHeader) {

            // Date formatting pattern
            final SimpleDateFormat newDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

            Log.i(TAG,"NeedsDateHeader " + newDateFormat.format(itemDate));

            // Set date header text
            holder.itemDateHeader.setText(newDateFormat.format(itemDate));

            // Show date header
            holder.itemDateHeader.setVisibility(View.VISIBLE);
        } else {

            // Hide date header
            holder.itemDateHeader.setVisibility(View.GONE);
        }
    }

    private void setItemListeners(@NonNull final ViewHolder holder) {

        // Item on click listener
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "OnClick");

                // Fire long click listener if selection mode is enabled
                if (selectionMode) {
                    toggleItemSelection(holder);
                    return;
                }

                // Get view screen location for edit fragment animation
                timelineAdapterListener.onItemClick(holder.getAdapterPosition(), getScreenCoordinates(v)[1]);
            }
        });

        // Item on hold listener
        holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Log.i(TAG, "OnLong");

                toggleItemSelection(holder);

                return true;
            }
        });
    }

    void timelineItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart, itemCount);
        startLayoutAnimation();
    }

    private void startLayoutAnimation() {
        mRecyclerView.scheduleLayoutAnimation();
    }


    // Create new selection array with no items selected
    private void initSelectionArray() {
        selectionArray = new ArrayList<>(Arrays.asList(new Boolean[getItemCount()]));
        Collections.fill(selectionArray, false);
    }

    // Apply predefined selection
    void setSelectionArray(ArrayList<Boolean> newSelectionArray) {

        if (newSelectionArray.size() != getItemCount()) {
            initSelectionArray();
            return;
        }

        this.selectionArray = newSelectionArray;

        // Update items layout
        notifyDataSetChanged();

        // Update selection toolbar
        timelineAdapterListener.onSelectionChanged(selectionArray);
    }

    // Set selectionMode (controlled by selectionModeLock)
    private void setSelecting(boolean selectionMode) {

        boolean oldSelecting = this.selectionMode;

        switch (selectionModeLock) {
            case ALWAYS_SELECTING:
                this.selectionMode = true;
                break;
            case NEVER_SELECTING:
                this.selectionMode = false;
                break;
            case NO_SELECTION_LOCK:
            default:
                // Set given selection if no locks exist
                this.selectionMode = selectionMode;
                break;
        }

        // Check if selection state has changed
        if (oldSelecting != selectionMode) {
            // If so, notify fragment to update selection based UI
            timelineAdapterListener.onSelectionModeToggled(this.selectionMode);
        }
    }

    // Get total number of selected adapter items
    int getTotalSelected() {

        // If selectionArray does not exist, then no items are selected (return 0)
        if (selectionArray == null) {
            initSelectionArray();
            return 0;
        }

        int total_selected = 0;
        for (int b = 0; b < getItemCount(); b++) {
            if (selectionArray.get(b)) {
                total_selected++;
            }
        }
        return total_selected;
    }


    private void toggleItemSelection(final ViewHolder holder) {

        if (selectionArray == null || selectionArray.size() == 0) {
            initSelectionArray();
        }

        // Get correct ViewHolder position
        int position = holder.getAdapterPosition();

        if (position == -1) return;

        // Toggle selection array value for current position
        selectionArray.set(position, !selectionArray.get(position));

        // Notify fragment that selection has changed (to update selection based UI)
        timelineAdapterListener.onSelectionChanged(selectionArray);

        // Stop selection if nothing is selected
        setSelecting(getTotalSelected() > 0);

        // Change item layout based on selection
        markSelection(holder, position);

        // Mark selection for custom layout
        if(hasCustomViewHolder()){
            customViewHolderListener.markSelection(holder,timelineAdapterListener.getTimelineItem(position),position,selectionArray.get(position));
        }
    }

    private boolean hasCustomViewHolder(){
        return hasCustomViewHolder && customViewHolderListener != null;
    }

    // Apply a common selection state to all adapter items
    void setUniversalItemSelection(boolean selected) {

        // Set all selectionArray values to true
        for (int b = 0; b < getItemCount(); b++) {
            selectionArray.set(b, selected);
        }

        // Update items layout
        notifyDataSetChanged();

        timelineAdapterListener.onSelectionChanged(selectionArray);
    }


    // Change items visual layout based on selection state
    private void markSelection(final ViewHolder holder, final int position) {

        final TimelineItem timelineItem = timelineAdapterListener.getTimelineItem(position);

        // Selected
        if (selectionArray.size() != 0 && selectionArray.get(position)) {
            // Selected item background
            Drawable itemBackground = holder.itemLayout.getBackground();
            itemBackground.setColorFilter(darkenColor(timelineItem.getColor()), PorterDuff.Mode.SRC_ATOP);
            holder.itemLayout.setBackground(itemBackground);

            // Show selection check mark
            holder.itemSelectedMark.setVisibility(View.VISIBLE);
        }
        // Not selected
        else {
            // Regular item background
            Drawable itemBackground = holder.itemLayout.getBackground();
            itemBackground.setColorFilter(timelineItem.getColor(), PorterDuff.Mode.SRC_ATOP);
            holder.itemLayout.setBackground(itemBackground);

            // Hide selection check mark
            holder.itemSelectedMark.setVisibility(View.GONE);
        }
    }


    // Delete item from adapter with animation
    private void deleteItem(final int index) {

        selectionArray.remove(index);

        // Removed item from adapter with animation
        notifyItemRemoved(index);

        // Update item above to keep date header (use same index because of index shifting after item deletion)
        notifyItemChanged(index);
    }

    void deleteItemRange(int position, int itemCount) {

        initSelectionArray();

        notifyItemRangeRemoved(position, itemCount);

        startLayoutAnimation();

    }

    void deleteSelectedItems() {

        ArrayList<Integer> selectedIndices = getSelectedIndices();

        // Make sure that array is sorted from largest to smallest index
        Collections.sort(selectedIndices, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1, o2);
            }
        });

        for (int x = selectedIndices.size() - 1; x >= 0; x--) {

            int index = selectedIndices.get(x);

            timelineAdapterListener.deleteItemData(index);

            deleteItem(index);

        }

        timelineAdapterListener.onDatasetChanged();

        setSelecting(false);

    }

    // Use SparseBooleanArray to get RecyclerView's currently selected completions
    private ArrayList<Integer> getSelectedIndices() {

        ArrayList<Integer> selectedIndices = new ArrayList<>();

        for (int c = 0; c < getItemCount(); c++) {
            if (selectionArray.get(c)) {
                selectedIndices.add(c);
            }
        }
        return selectedIndices;
    }

    // Get total number of adapter items
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

    // int direction : 0 for x-component, 1 for y-component
    private int[] getScreenCoordinates(View v){
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        return location;
    }
}
