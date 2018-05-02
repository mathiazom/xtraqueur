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

    private int customViewResId;

    private ArrayList<Boolean> selectionArray;

    private boolean selectionMode;

    private static final int NO_SELECTION_LOCK = 0;
    static final int ALWAYS_SELECTING = 100;
    private static final int NEVER_SELECTING = 200;

    private final int selectionModeLock;

    private final TimelineAdapterListener timelineAdapterListener;

    interface TimelineAdapterListener {

        TimelineItem getTimelineItem(int pos);

        void onItemClick(int pos,float y);

        void onDatasetChanged();

        void onSelectionModeToggled(boolean isSelecting);

        void onSelectionChanged(ArrayList<Boolean> updatedSelectionArray);

        void deleteItemData(int index);

        int getItemCount();

    }

    interface TimelineAdapterCustomViewListener extends TimelineAdapterListener{

        ConstraintLayout getCustomView(ViewGroup parent);

        void displayCustomItemData(@NonNull final ViewHolder holder, final TimelineItem timelineItem);

    }

    private RecyclerView mRecyclerView;


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }


    TimelineAdapter(boolean selectionMode, TimelineAdapterListener timelineAdapterListener) {
        this(selectionMode,NO_SELECTION_LOCK,timelineAdapterListener);
    }

    TimelineAdapter(int selectionModeLock, TimelineAdapterListener timelineAdapterListener){
        this(selectionModeLock == ALWAYS_SELECTING,selectionModeLock,timelineAdapterListener);
    }

    private TimelineAdapter(boolean selectionMode, int selectionModeLock, TimelineAdapterListener timelineAdapterListener){
        this.timelineAdapterListener = timelineAdapterListener;
        this.selectionMode = selectionMode;
        this.selectionModeLock = selectionModeLock;
        initSelectionArray();
    }




    static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout mItemLayout;
        final ConstraintLayout mMainItemLayout;

        ViewHolder(ConstraintLayout v) {
            super(v);
            mItemLayout = v;
            mMainItemLayout = v.findViewById(R.id.item_container);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ConstraintLayout v;

        if(timelineAdapterListener instanceof TimelineAdapterCustomViewListener){
            v = ((TimelineAdapterCustomViewListener) timelineAdapterListener).getCustomView(parent);
        }else{
            v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.template_timeline_item, parent, false);
        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        Log.i("Timeline","OnBindViewHolder");

        // Current item
        final TimelineItem timelineItem = timelineAdapterListener.getTimelineItem(position);

        // Item date
        final Date itemDate = new Date(timelineItem.getDate());

        // Display item title, date and color
        if(timelineAdapterListener instanceof TimelineAdapterCustomViewListener){
            ((TimelineAdapterCustomViewListener) timelineAdapterListener).displayCustomItemData(holder, timelineItem);
        }else{
            displayItemData(holder,timelineItem);
        }

        // Additional date header if item starts new date
        setItemDateHeader(holder,itemDate);

        // Change layout to mark item selection state
        markSelection(holder,position);

        // Set item click- and hold listener
        setItemListeners(holder);
    }

    private void displayItemData(@NonNull final ViewHolder holder, final TimelineItem timelineItem){

        // Item title
        final TextView itemTitleView = holder.mItemLayout.findViewById(R.id.timeline_item_title);
        itemTitleView.setText(timelineItem.getTitle());

        // Item date
        final TextView itemDateView = holder.mItemLayout.findViewById(R.id.timeline_item_date);
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE d MMM yyyy HH:mm", Locale.getDefault());
        final Date itemDate = new Date(timelineItem.getDate());
        itemDateView.setText(simpleDateFormat.format(itemDate));

        // Item background
        Drawable itemBackground = holder.mMainItemLayout.getBackground();
        itemBackground.setColorFilter(timelineItem.getColor(), PorterDuff.Mode.SRC_ATOP);
        holder.mMainItemLayout.setBackground(itemBackground);
    }

    private void setItemDateHeader(@NonNull final ViewHolder holder,final Date itemDate){

        // Marks if item layout needs an additional date header to mark a new date
        boolean needsDateHeader;

        // If item is not first in list, check if item needs a date header
        if (holder.getAdapterPosition() != 0) {

            // Time of current item
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(itemDate);

            // Time of previous item
            final Date prevDate = new Date(timelineAdapterListener.getTimelineItem(holder.getAdapterPosition()-1).getDate());
            final Calendar prevCalendar = Calendar.getInstance();
            prevCalendar.setTime(prevDate);

            // Check if item has different date than previous item and needs date header
            needsDateHeader = calendar.get(Calendar.DAY_OF_YEAR) != prevCalendar.get(Calendar.DAY_OF_YEAR) || calendar.get(Calendar.YEAR) != prevCalendar.get(Calendar.YEAR);

        }else {
            // item is first item on the list, therefore also needs a date header
            needsDateHeader = true;
        }


        final TextView dateHeader = holder.mItemLayout.findViewById(R.id.timeline_item_date_header);

        if (needsDateHeader) {
            // Date formatting pattern
            final SimpleDateFormat newDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

            // Set date header text
            dateHeader.setText(newDateFormat.format(itemDate));

            // Show date header
            dateHeader.setVisibility(View.VISIBLE);
        } else {

            // Hide date header
            dateHeader.setVisibility(View.GONE);
        }
    }

    private void setItemListeners(@NonNull final ViewHolder holder){

        // Item on click listener
        holder.mMainItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Fire long click listener if selection mode is enabled
                if (selectionMode) {
                    toggleItemSelection(holder);
                    return;
                }

                // Get view screen location for edit fragment animation
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                timelineAdapterListener.onItemClick(holder.getAdapterPosition(),location[1]);
            }
        });

        // Item on hold listener
        holder.mMainItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                toggleItemSelection(holder);

                return true;
            }
        });
    }

    void timelineItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart,itemCount);
        startLayoutAnimation();
    }

    private void startLayoutAnimation(){
        mRecyclerView.scheduleLayoutAnimation();
    }


    // Create new selection array with no items selected
    private void initSelectionArray(){
        selectionArray = new ArrayList<>(Arrays.asList(new Boolean[getItemCount()]));
        Collections.fill(selectionArray,false);
    }

    // Apply predefined selection
    void setSelectionArray(ArrayList<Boolean> newSelectionArray){

        if(newSelectionArray.size() != getItemCount()){
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
    private void setSelecting(boolean selectionMode){

        boolean oldSelecting = this.selectionMode;

        switch (selectionModeLock){
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
        if(oldSelecting != selectionMode){
            // If so, notify fragment to update selection based UI
            timelineAdapterListener.onSelectionModeToggled(this.selectionMode);
        }
    }

    // Get total number of selected adapter items
    int getTotalSelected(){

        // If selectionArray does not exist, then no items are selected (return 0)
        if(selectionArray == null){
            initSelectionArray();
            return 0;
        }

        int total_selected = 0;
        for(int b = 0; b < getItemCount();b++){
            if(selectionArray.get(b)){
                total_selected++;
            }
        }
        return total_selected;
    }


    private void toggleItemSelection(final ViewHolder holder) {

        if(selectionArray == null || selectionArray.size() == 0){
            initSelectionArray();
        }

        // Get correct ViewHolder position
        int position = holder.getAdapterPosition();

        if(position == -1) return;

        // Toggle selection array value for current position
        selectionArray.set(position, !selectionArray.get(position));

        // Notify fragment that selection has changed (to update selection based UI)
        timelineAdapterListener.onSelectionChanged(selectionArray);

        // Stop selection if nothing is selected
        setSelecting(getTotalSelected() > 0);

        // Change item layout based on selection
        markSelection(holder,position);
    }

    // Apply a common selection state to all adapter items
    void setUniversalItemSelection(boolean selected) {

        // Set all selectionArray values to true
        for (int b = 0;b<getItemCount();b++){
            selectionArray.set(b,selected);
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


    // Delete item from adapter with animation
    private void deleteItem(final int index){

        selectionArray.remove(index);

        // Removed item from adapter with animation
        notifyItemRemoved(index);

        // Update item above to keep date header (use same index because of index shifting after item deletion)
        notifyItemChanged(index);
    }

    void deleteItemRange(int position, int itemCount){

        initSelectionArray();

        notifyItemRangeRemoved(position,itemCount);

        startLayoutAnimation();

    }

    void deleteSelectedItems() {

        ArrayList<Integer> selectedIndices = getSelectedIndices();

        // Make sure that array is sorted from largest to smallest index
        Collections.sort(selectedIndices,new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1,o2);
            }
        });

        for (int x = selectedIndices.size()-1;x >= 0;x--) {

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
}
