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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public abstract class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    /*

    * Abstract methods to enable class inheritance for use in classes like CompletionsTimelineAdapter and PaymentsTimelineAdapter

     */

    abstract void onSelectionChanged(final int totalSelected, final boolean isSelecting);

    abstract void displayItemData(@NonNull ViewHolder holder, final int position);

    abstract void displaySelectionState(@NonNull ViewHolder holder, boolean selected);

    abstract void deleteItemData(final int position);

    abstract void onItemsDataChanged();

    abstract void onItemClicked(final int position, final int yPos);

    abstract Date getItemDate(final int position);

    abstract String getItemDateString(final int position);

    abstract int getItemsDataSize();


    /*

    * end of abstract methods

     */

    private static final String TAG = "XTQ-TimelineAdapter";

    private ArrayList<Boolean> selectionArray;

    private boolean selectionMode;

    private static final int NO_SELECTION_LOCK = 0;
    static final int ALWAYS_SELECTING = 100;
    private static final int NEVER_SELECTING = 200;

    private final int selectionModeLock;

    private RecyclerView mRecyclerView;

    private final int itemViewResId;


    interface TimelineAdapterListener extends TimelineAdapterLockListener{

        void onItemsDataChanged();

        void onItemClicked(int position, int yPos);

    }

    interface TimelineAdapterLockListener{

        void onSelectionChanged(int totalSelected, final boolean isSelecting);

    }



    TimelineAdapter(final int itemViewResId, boolean selectionMode) {
        this(itemViewResId, selectionMode, NO_SELECTION_LOCK);
    }

    TimelineAdapter(final int itemViewResId, int selectionModeLock) {
        this(itemViewResId, selectionModeLock == ALWAYS_SELECTING, selectionModeLock);
    }

    private TimelineAdapter(final int itemViewResId, boolean selectionMode, int selectionModeLock) {

        this.itemViewResId = itemViewResId;

        this.selectionMode = selectionMode;
        this.selectionModeLock = selectionModeLock;

        initSelectionArray();
    }




    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
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

        if(viewHolder == null){
            return new ViewHolder(itemBaseLayout);
        }
        else{
            return viewHolder;
        }
    }

    abstract ViewHolder onCreateViewHolder(ConstraintLayout itemBaseLayout);



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        // Display item data
        displayItemData(holder, holder.getAdapterPosition());

        // Show mark state (marked or not marked)
        boolean selected = selectionArray.size() != 0 && selectionArray.get(position);
        displaySelectionState(holder, selected);

        // Additional date header if item starts new date
        displayDateHeaderIfNeeded(holder,holder.getAdapterPosition());

        // Set item click- and hold listeners
        setItemListeners(holder);
    }


    private void setItemListeners(@NonNull final ViewHolder holder) {

        // Item on click listener
        holder.itemDataLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Fire long click listener if selection mode is enabled
                if (selectionMode) {
                    toggleItemSelection(holder);
                    return;
                }

                // Get view screen location for edit fragment animation
                onItemClicked(holder.getAdapterPosition(),getScreenCoordinates(v)[1]);
            }
        });

        // Item on hold listener
        holder.itemDataLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

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


    void setItemBackgroundColor(final ViewHolder holder, final int color){

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
        onSelectionChanged(getTotalSelected(selectionArray), selectionMode);
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
            onSelectionChanged(getTotalSelected(selectionArray), selectionMode);
        }
    }


    private void toggleItemSelection(final ViewHolder holder) {

        if (selectionArray == null || selectionArray.size() == 0) {
            initSelectionArray();
        }

        // Get correct TaskViewHolder position
        int position = holder.getAdapterPosition();

        if (position == -1) return;

        // Toggle value stored in selection
        boolean selected = !selectionArray.get(position);

        // Toggle selection array value for current position
        selectionArray.set(position, selected);

        // Notify fragment that selection has changed (to update selection based UI)
        onSelectionChanged(getTotalSelected(selectionArray), selectionMode);

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

        onSelectionChanged(getTotalSelected(selectionArray), selectionMode);

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

    private int getTotalSelected(ArrayList<Boolean> selectionArray){

        int totalSelected = 0;

        for(Boolean b : selectionArray){
            totalSelected += b ? 1 : 0;
        }

        return totalSelected;

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

            deleteItemData(index);

            deleteItem(index);

        }

        onItemsDataChanged();

        setSelecting(false);

    }

    // Additional date header if item starts new date
    private void displayDateHeaderIfNeeded(@NonNull ViewHolder holder, final int position){

        if (firstOfDate(holder.getAdapterPosition())) {

            // Date formatting pattern
            final SimpleDateFormat newDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

            // Set date header text
            holder.itemDateHeader.setText(getItemDateString(holder.getAdapterPosition()));

            // Show date header
            holder.itemDateHeader.setVisibility(View.VISIBLE);
        } else {

            // Hide date header
            holder.itemDateHeader.setVisibility(View.GONE);
        }
    }

    // Check if item of specified position is the first item of its date
    private boolean firstOfDate(final int position){

        if(position == 0) return true;

        final Date currentDate = getItemDate(position);

        final Date prevDate = getItemDate(position-1);

        // Time of current item
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Time of previous item
        final Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.setTime(prevDate);

        // Check if item has different date than previous item and needs date header
        return calendar.get(Calendar.DAY_OF_YEAR) != prevCalendar.get(Calendar.DAY_OF_YEAR) || calendar.get(Calendar.YEAR) != prevCalendar.get(Calendar.YEAR);

    }

    // Get total number of adapter items
    @Override
    public int getItemCount() {
        return getItemsDataSize();
    }

    // int direction : 0 for x-component, 1 for y-component
    private int[] getScreenCoordinates(View v){
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        return location;
    }
}
