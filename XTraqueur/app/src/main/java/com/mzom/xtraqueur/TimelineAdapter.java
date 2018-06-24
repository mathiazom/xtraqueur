package com.mzom.xtraqueur;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


/**
 * TimelineAdapter functionality:
 * ITEM SORTING: Sorts items of a data set by date
 * <p>
 * ITEM VIEW HOLDER: Delivers a view holder to add a visual item representation including item title, item date and item color
 * <p>
 * DATE HEADERS: Adds a date header to group items of the same calendar date
 * <p>
 * ITEM CLICK LISTENER: Catches users item clicks
 * <p>
 * ITEM SELECTION: Logic to select items from list and store currently selected items
 * <p>
 * VISUAL ITEM DELETION: Notifies item deletion to create visual animations
 * <p>
 * <p>
 * Functionality needed from extender of this class (using abstract methods from this class):
 * DATA SET: This class only facilitates a list to store items and their data. The extender must therefore provide that data.
 * <p>
 * SELECTION UI: This class only holds information about selected items (indexes) and does not provide any selection UI.
 * <p>
 * ACTUAL ITEM DELETION FROM DATA SET: This class tries to visually present item deletion. For this to work,
 * the extender has to actually remove the item from the data set for this to work properly.
 * <p>
 * SAVING DATA SET CHANGES: This class does not save any changes made to the data set (e.g. item deletion)
 * <p>
 * REACTION TO ITEM CLICKS: This class only alerts extender that an item
 * has been clicked, the extender decides how to react to this.
 * <p>
 * SELECTION INFORMATION: This class operates inside the RecyclerView, and therefore gives
 * no selection indicators outside this view
 **/


public abstract class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {


    private static final String TAG = "XTQ-TimelineAdapter";

    // Stores boolean selection status for every item in list
    private ArrayList<Boolean> selectionArray;

    // Used by various UI components to decide what information to show
    private boolean isSelecting;

    private static final int NO_SELECTION_LOCK = 0;
    static final int ALWAYS_SELECTING = 100;
    private static final int NEVER_SELECTING = 200;

    // Used to keep selection toolbar constantly visible if needed
    private final int selectionLock;

    // RecyclerView holding an instance of this adapter
    private RecyclerView mRecyclerView;

    // Visual representation of item data in list
    private final int itemViewResId;


    TimelineAdapter(final int itemViewResId, boolean isSelecting) {
        this(itemViewResId, isSelecting, NO_SELECTION_LOCK);
    }

    TimelineAdapter(final int itemViewResId, int selectionLock) {
        this(itemViewResId, selectionLock == ALWAYS_SELECTING, selectionLock);
    }

    private TimelineAdapter(final int itemViewResId, boolean isSelecting, int selectionLock) {

        this.itemViewResId = itemViewResId;

        this.isSelecting = isSelecting;
        this.selectionLock = selectionLock;

        initSelectionArray();
    }


    interface TimelineAdapterListener {

        void onItemsDataChanged();

        void onItemClicked(int position, int yPos);

        void onSelectionChanged(int totalSelected, final boolean isSelecting);

    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        final ConstraintLayout itemBaseLayout;
        final ConstraintLayout itemDataLayout;
        final TextView itemDateHeader;

        ViewHolder(ConstraintLayout itemBaseLayout) {
            super(itemBaseLayout);

            // Required views
            this.itemBaseLayout = itemBaseLayout;
            this.itemDataLayout = itemBaseLayout.findViewById(R.id.item_container);
            this.itemDateHeader = itemBaseLayout.findViewById(R.id.timeline_item_date_header);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final ConstraintLayout itemBaseLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.template_timeline_item, parent, false);

        final ConstraintLayout itemDataLayout = itemBaseLayout.findViewById(R.id.item_container);

        final View itemView = LayoutInflater.from(itemDataLayout.getContext()).inflate(itemViewResId, itemDataLayout, false);
        itemDataLayout.addView(itemView);

        ViewHolder viewHolder = onCreateViewHolder(itemBaseLayout);

        if (viewHolder == null) {
            return new ViewHolder(itemBaseLayout);
        } else {
            return viewHolder;
        }
    }

    abstract ViewHolder onCreateViewHolder(ConstraintLayout itemBaseLayout);

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        // Display item data
        displayItemData(holder, holder.getAdapterPosition());
        setItemBackgroundColor(holder, getItemColor(position));

        // Show mark state (marked or not marked)
        boolean selected = selectionArray.size() != 0 && selectionArray.get(position);
        displaySelectionState(holder, selected);

        // Additional date header if item starts new date
        displayDateHeaderIfNeeded(holder, holder.getAdapterPosition());

        // Set item click- and hold listeners
        initItemListeners(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }


    private View.OnClickListener getItemOnClickListener(@NonNull final ViewHolder holder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Fire long click listener if selection mode is enabled
                if (isSelecting) {
                    toggleItemSelection(holder);
                    return;
                }

                // Get view screen location for edit fragment animation
                onItemClicked(holder.getAdapterPosition(), getScreenCoordinates(v)[1]);
            }
        };
    }

    private View.OnLongClickListener getItemOnLongClickListener(@NonNull final ViewHolder holder) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                toggleItemSelection(holder);

                return true;
            }
        };
    }

    private void initItemListeners(@NonNull final ViewHolder holder) {

        // Item on click listener
        holder.itemDataLayout.setOnClickListener(getItemOnClickListener(holder));

        // Item on hold listener
        holder.itemDataLayout.setOnLongClickListener(getItemOnLongClickListener(holder));
    }


    void timelineItemRangeInserted(int itemCount) {

        for (int i = 0; i < itemCount; i++) {
            selectionArray.add(i, false);
        }

        notifyItemRangeInserted(0, itemCount);

        startLayoutAnimation();
    }

    private void setItemBackgroundColor(final ViewHolder holder, final int color) {

        Drawable itemDataLayoutDrawable = holder.itemDataLayout.getBackground();
        itemDataLayoutDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        holder.itemDataLayout.setBackground(itemDataLayoutDrawable);

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
        onSelectionChanged(getTotalSelected(selectionArray), isSelecting);
    }

    // Set isSelecting (controlled by selectionLock)
    private void setSelecting(boolean isSelecting) {

        // Check if any selection lock prohibits change to isSelecting
        if (selectionLock == ALWAYS_SELECTING || selectionLock == NEVER_SELECTING) return;

        // Make sure selection state is different from current selection state
        if (isSelecting == this.isSelecting) return;


        // Apply given state
        this.isSelecting = isSelecting;

        // Notify fragment that selection state has changed
        onSelectionChanged(getTotalSelected(selectionArray), isSelecting);


    }

    private void toggleItemSelection(final ViewHolder holder) {

        if (selectionArray == null || selectionArray.size() == 0) {
            initSelectionArray();
        }

        // Get correct TaskViewHolder position
        int position = holder.getAdapterPosition();

        // Make sure the holder exists in adapter
        if (position == -1) return;

        // Work out new item selection value by toggling current value
        boolean selected = !selectionArray.get(position);

        // Apply new item selection value
        selectionArray.set(position, selected);

        // Notify fragment that selection has changed (to update selection based UI)
        onSelectionChanged(getTotalSelected(selectionArray), isSelecting);

        // Stop selection if nothing is selected
        setSelecting(getTotalSelected(selectionArray) > 0);

        // Change item layout based on selection
        displaySelectionState(holder, selected);
    }

    // Apply a common selection state to all adapter items
    void setUniversalItemSelection(boolean selected) {

        // Set all selectionArray values to true
        for (int b = 0; b < getItemCount(); b++) {
            selectionArray.set(b, selected);
        }

        // Update items layout
        notifyDataSetChanged();

        onSelectionChanged(getTotalSelected(selectionArray), isSelecting);

        setSelecting(selected);
    }

    // Use Boolean Array to get RecyclerView's currently selected payments
    private ArrayList<Integer> getSelectedIndices() {

        ArrayList<Integer> selectedIndices = new ArrayList<>();

        for (int c = 0; c < getItemCount(); c++) {
            if (selectionArray.get(c)) {
                selectedIndices.add(c);
            }
        }
        return selectedIndices;
    }

    ArrayList<Boolean> getSelectionArray() {

        return selectionArray;
    }

    private int getTotalSelected(ArrayList<Boolean> selectionArray) {

        int totalSelected = 0;

        for (Boolean b : selectionArray) {
            totalSelected += b ? 1 : 0;
        }

        return totalSelected;

    }


    // Remove selected items from list AND tell extender to delete them from data set
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

            // Delete data from data set
            deleteItemData(index);

            // Visually remove item from list
            removeItem(index);

        }

        onItemsDataChanged();

        setSelecting(false);

    }

    // Removes item from list (does NOT delete it from the data set)
    private void removeItem(final int index) {

        selectionArray.remove(index);

        // Removed item from adapter with animation
        notifyItemRemoved(index);

        // Update item above to keep date header (use same index because of index shifting after item deletion)
        notifyItemChanged(index);
    }


    private void startLayoutAnimation() {
        mRecyclerView.scheduleLayoutAnimation();
    }


    // Additional date header if item starts new date
    private void displayDateHeaderIfNeeded(@NonNull ViewHolder holder, final int position) {

        if (isFirstOfDate(holder.getAdapterPosition())) {

            // Set date header text
            holder.itemDateHeader.setText(DateFormatter.formatDate(getItemDate(position).getTime()));

            // Show date header
            holder.itemDateHeader.setVisibility(View.VISIBLE);
        } else {

            // Hide date header
            holder.itemDateHeader.setVisibility(View.GONE);
        }
    }

    // Check if item of specified position is the first item of its date
    private boolean isFirstOfDate(final int position) {

        if (position == 0) return true;

        final Date currentDate = getItemDate(position);

        final Date prevDate = getItemDate(position - 1);

        // Time of current item
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Time of previous item
        final Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.setTime(prevDate);

        // Check if item has different date than previous item and needs date header
        return calendar.get(Calendar.DAY_OF_YEAR) != prevCalendar.get(Calendar.DAY_OF_YEAR) || calendar.get(Calendar.YEAR) != prevCalendar.get(Calendar.YEAR);

    }


    // int direction : 0 for x-component, 1 for y-component
    private int[] getScreenCoordinates(View v) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        return location;
    }


    abstract void onSelectionChanged(final int totalSelected, final boolean isSelecting);

    abstract void displayItemData(@NonNull ViewHolder holder, final int position);

    abstract void displaySelectionState(@NonNull ViewHolder holder, boolean selected);

    abstract void deleteItemData(final int position);

    abstract void onItemsDataChanged();

    abstract void onItemClicked(final int position, final int yPos);

    abstract Date getItemDate(final int position);

    abstract int getItemColor(final int position);


}
