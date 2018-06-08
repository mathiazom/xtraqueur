package com.mzom.xtraqueur;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EditCompletionFragment extends BaseEditFragment {

    private ArrayList<XTask> tasks;

    private XTask task;

    private XTaskCompletion completion;

    private int completionColor;

    private long tempCompletionDate;

    private EditText completionDateEdit;
    private EditText completionTimeEdit;

    public static EditCompletionFragment newInstance(ArrayList<XTask> tasks, XTaskCompletion completion) {

        EditCompletionFragment fragment = new EditCompletionFragment();
        fragment.task = XTaskFieldsUtilities.getTaskFromCompletion(completion,tasks);
        fragment.tasks = tasks;
        fragment.completion = completion;
        fragment.completionColor = fragment.task.getTaskFields().getColor();
        fragment.tempCompletionDate = completion.getDate();
        return fragment;
    }

    // Base edit methods

    @NonNull
    @Override
    ConstraintLayout getEditLayout(ConstraintLayout baseEditContainer) {

        final ConstraintLayout editLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.fragment_edit_completion, baseEditContainer, false);

        // Tell base fragment that this fragment uses a item delete button
        Button mDeleteButton = editLayout.findViewById(R.id.button_delete_completion);
        setItemDeleteButton(mDeleteButton,getString(R.string.delete_completion_confirmation_title),getString(R.string.delete_completion_confirmation_message));

        // Completion date
        final Date date = new Date(tempCompletionDate);

        // Get completion date string
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMM yyyy", Locale.getDefault());
        final String completionDate = dateFormat.format(date);

        // Get completion time string
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        final String completionTime = timeFormat.format(date);

        // Edit completion date
        completionDateEdit = editLayout.findViewById(R.id.edit_completion_date);
        completionDateEdit.setText(completionDate);
        completionDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDatePicker(new Date(tempCompletionDate));
            }
        });

        // Edit completion time (hour and minute)
        completionTimeEdit = editLayout.findViewById(R.id.edit_completion_time);
        completionTimeEdit.setText(completionTime);
        completionTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTimePicker(new Date(tempCompletionDate));
            }
        });

        return editLayout;

    }

    @Override
    int getItemColor() {
        return completionColor;
    }

    @Override
    boolean itemDataIsChanged() {
        return completion.getDate() != tempCompletionDate;
    }

    @Override
    void saveChanges() {

        if(!itemDataIsChanged()) return;

        XTaskCompletion oldCompletion = completion;

        completion.setDate(tempCompletionDate);

        // Update completionsList
        ArrayList<XTaskCompletion> completions = task.getCompletions();
        completions.set(completions.indexOf(oldCompletion),completion);

        // Update tasks data
        tasks.get(tasks.indexOf(task)).setCompletions(completions);

        // Update tasks data with change
        getBaseEditListener().updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                returnToItemsList();
            }
        });
    }

    @Override
    void deleteItem() {

        // Remove completion from task
        tasks = XTaskFieldsUtilities.removeCompletion(completion,tasks);

        // Update tasks data with change
        getBaseEditListener().updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                returnToItemsList();
            }
        });
    }

    @Override
    void onDatePicked(Date newDate) {
        tempCompletionDate = newDate.getTime();

        // Completion date text
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMM yyyy", Locale.getDefault());
        completionDateEdit.setText(dateFormat.format(newDate));

        // Completion time text
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        completionTimeEdit.setText(timeFormat.format(newDate));
    }
}
