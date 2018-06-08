package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.TimePicker;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public abstract class BaseEditFragment extends XFragment {

    // Fragment base views
    private View baseView;
    private Toolbar baseToolbar;
    private Button baseDeleteButton;

    // Dialog string resources
    private String deleteConfirmationTitle;
    private String deleteConfirmationMessage;

    private boolean discardChanges;

    private BaseEditFragmentListener mBaseEditFragmentListener;

    interface BaseEditFragmentListener{
        void updateTasksDataOnDrive(ArrayList<XTask> tasks, OnSuccessListener<DriveFile> onSuccessListener);

        void updatePaymentsDataOnDrive(ArrayList<XPayment> payments, OnSuccessListener<DriveFile> onSuccessListener);

        void loadCompletionsFragment(ArrayList<XTask> tasks,XTaskFields taskFields);

        void loadCompletionsFragment(ArrayList<XTaskCompletion> completions);

        void onBackPressed();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mBaseEditFragmentListener = (BaseEditFragmentListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement BaseEditFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Retain fragment state to save its variables
        setRetainInstance(true);

        // Root view for this fragment
        this.baseView = inflater.inflate(R.layout.fragment_base_edit, container, false);
        ConstraintLayout baseEditContainer = baseView.findViewById(R.id.base_edit_container);

        // Init base toolbar
        initToolbar();

        // Load custom layout for editing
        loadEditLayout(baseEditContainer);

        // Setup the listeners for base layout components
        initListeners();

        // Apply item color to base layout components
        applyItemColor();

        return baseView;
    }

    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Initialize toolbar field variable
        baseToolbar = baseView.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        baseToolbar.inflateMenu(R.menu.menu_base_edit_fragment);

        // Set back button as toolbar navigation icon
        baseToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDiscardChanges();
            }
        });

        // Add listeners to the other toolbar action buttons
        baseToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.base_edit_save_changes_text:
                        // Save any registered changes made to item
                        if(itemDataIsChanged()) saveChanges();
                        //returnToItemsList();
                }
                return false;
            }
        });
    }

    // Setup the listeners for the views in this fragment
    private void initListeners() {

        // If delete button has been added, attach a click listener to it
        if(baseDeleteButton != null){
            baseDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Display an alertDialog to let user confirm deletion
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext(),R.style.AlertDialogTheme)
                            .setTitle(deleteConfirmationTitle)
                            .setMessage(deleteConfirmationMessage)
                            .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Call abstract methods for deletion
                                    deleteItem();
                                    returnToItemsList();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User had second thoughts, no further actions required
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });
        }
    }

    // Get layout from class extending this
    private void loadEditLayout(ConstraintLayout baseEditContainer){
        baseEditContainer.addView(getEditLayout(baseEditContainer));
    }

    // Fill input and color field with current item values
    private void applyItemColor() {

        int itemColor = getItemColor();

        // Toolbar background
        baseToolbar.setBackground(new ColorDrawable(itemColor));

        // Delete button background
        if (getContext() != null) {
            baseDeleteButton.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            Drawable delete_drawable = baseDeleteButton.getBackground();
            delete_drawable.setColorFilter(Color.parseColor("#eeeeee"), PorterDuff.Mode.SRC_ATOP);
            baseDeleteButton.setBackground(delete_drawable);
        }

        // ScrollView background
        ScrollView scrollView = baseView.findViewById(R.id.edittask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(itemColor));
        scrollView.setBackground(scrollDrawable);
    }

    // Notify user if any unsaved changes have been made
    private void confirmDiscardChanges(){
        // Check if changes have been made and ask user to stay in fragment or discard changes
        if (itemDataIsChanged()) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.discard_changes_dialog_title)
                    .setMessage(R.string.discard_changes_dialog_message)
                    .setPositiveButton(R.string.discard_option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            discardChanges = true;
                            returnToItemsList();
                        }
                    })
                    .setNegativeButton(R.string.cancel_option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create()
                    .show();

        }
        // Go back to tasks list if no changes have been made
        else {
            returnToItemsList();
        }
    }

    BaseEditFragmentListener getBaseEditListener(){
        return mBaseEditFragmentListener;
    }

    void setItemDeleteButton(Button deleteButton, String deleteConfirmationTitle, String deleteConfirmationMessage){
        this.baseDeleteButton = deleteButton;
        this.deleteConfirmationTitle = deleteConfirmationTitle;
        this.deleteConfirmationMessage = deleteConfirmationMessage;
    }

    void newDatePicker(Date date){

        // Use calendar object to get date info
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        // Get current completion date
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        final int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);

        if (getContext() == null) return;

        // Dialog to change completion date
        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                // Set new values to calendar
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, dayOfMonth, hourOfDay, minute);

                // Get date from calendar and save changes temporarily
                final Date newDate = newCalendar.getTime();
                onDatePicked(newDate);
            }
        }, year, month, day).show();

    }

    void newTimePicker(final Date date){

        // Get current completion date
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        if (getContext() == null) return;

        // Dialog to change completion time
        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);

                final int year = c.get(Calendar.YEAR);
                final int month = c.get(Calendar.MONTH);
                final int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

                // Set new values to calendar
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, dayOfMonth, hourOfDay, minute);

                // Get date from calendar and save changes temporarily
                final Date newDate = newCalendar.getTime();
                onDatePicked(newDate);
            }
        }, hour, minute, true).show();

    }


    // Re-paint layout with updated return color of getItemColor()
    void notifyItemColorChange() {
        applyItemColor();
    }

    void returnToItemsList(){
        mBaseEditFragmentListener.onBackPressed();
    }

    // Used by activity to forward back presses
    // Return value signalizes if back press has been handled
    boolean onBackPressed(){

        if(discardChanges) return false;

        // Checks if any changes have been made to item data
        if(itemDataIsChanged()){

            // If so, prompt user to confirm discard of these changes
            confirmDiscardChanges();

            return true;
        }

        return false;

    }


    // Get a darker shade of the original task color
    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }




    @NonNull
    abstract ConstraintLayout getEditLayout(ConstraintLayout baseEditContainer);

    abstract int getItemColor();

    // Check if any properties of the task has been changed
    abstract boolean itemDataIsChanged();

    // Change task values according to inputs and update to Google drive
    abstract void saveChanges();

    // Permanently delete the task that is being edited
    abstract void deleteItem();

    abstract void onDatePicked(Date newDate);



}
