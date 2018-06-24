package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegisterCompletionDialog extends AlertDialog {

    private XTask task;

    private Date tempCompletionDate;

    private EditText completionDateEdit;
    private EditText completionTimeEdit;

    private RegisterCompletionDialogInterface mCallback;

    interface RegisterCompletionDialogInterface{
        void onCompletionRegistered(Date completionDate);
    }

    RegisterCompletionDialog(Context context, XTask task, RegisterCompletionDialogInterface callback) {
        super(context, R.style.AlertDialogTheme);

        this.task = task;
        this.mCallback = callback;
        this.tempCompletionDate = new Date();

        initDialog();

    }

    private void initDialog(){

        ScrollView scrollView = new ScrollView(getContext());
        ConstraintLayout dialogContainer = (ConstraintLayout) getLayoutInflater().inflate(R.layout.module_register_completion_dialog, scrollView, false);

        ColorUtilities.setViewBackgroundColor(dialogContainer,task.getTaskIdentity().getColor());

        int darker = ColorUtilities.getDarkerColor(task.getTaskIdentity().getColor());
        ColorUtilities.setViewBackgroundColor(dialogContainer.findViewById(R.id.register_completion_title),darker);
        ColorUtilities.setViewBackgroundColor(dialogContainer.findViewById(R.id.register_completion_button),darker);

        final Date now = new Date();

        // Edit completion date
        completionDateEdit = dialogContainer.findViewById(R.id.register_completion_date);
        final String completionDateString = DateFormatter.format(now.getTime(),"EEEE d MMM yyyy");
        completionDateEdit.setText(completionDateString);

        // Edit date listener
        completionDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDatePicker(tempCompletionDate);
            }
        });


        // Completion time string
        completionTimeEdit = dialogContainer.findViewById(R.id.register_completion_time);
        final String completionTimeString = DateFormatter.format(now.getTime(),"HH:mm");
        completionTimeEdit.setText(completionTimeString);

        // Edit time listener
        completionTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTimePicker(tempCompletionDate);
            }
        });

        scrollView.addView(dialogContainer);

        dialogContainer.findViewById(R.id.register_completion_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onCompletionRegistered(tempCompletionDate);
            }
        });

        this.setView(scrollView);

    }

    private void newDatePicker(Date date){

        // Use calendar object to get date info
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        // Get current completion date
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        final int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);

        // Dialog to change completion date
        new DatePickerDialog(getContext(),android.R.style.Holo_Light_ButtonBar_AlertDialog,new DatePickerDialog.OnDateSetListener() {
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

    private void newTimePicker(final Date date){

        // Get current completion date
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

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

    private void onDatePicked(Date newDate) {

        tempCompletionDate = newDate;

        // Completion date text
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMM yyyy", Locale.getDefault());
        completionDateEdit.setText(dateFormat.format(newDate));

        // Completion time text
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        completionTimeEdit.setText(timeFormat.format(newDate));
    }
}
