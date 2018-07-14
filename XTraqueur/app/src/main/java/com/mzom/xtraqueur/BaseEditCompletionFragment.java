package com.mzom.xtraqueur;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

abstract class BaseEditCompletionFragment extends BaseEditFragment {


    abstract void editCompletionDate(XTaskCompletion completion, long editedCompletionDate);

    abstract void deleteCompletion(XTaskCompletion completion);


    XTaskCompletion completion;

    int completionColor;

    long tempCompletionDate;

    private EditText completionDateEdit;
    private EditText completionTimeEdit;


    @NonNull
    @Override
    ConstraintLayout getEditLayout(ConstraintLayout baseEditContainer) {

        // Specific edit layout
        final ConstraintLayout editLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.fragment_edit_completion, baseEditContainer, false);

        // Tell base fragment that this layout contains an item delete button
        Button mDeleteButton = editLayout.findViewById(R.id.button_delete_completion);
        setItemDeleteButton(mDeleteButton,getString(R.string.delete_completion_confirmation_title),getString(R.string.delete_completion_confirmation_message));


        // Completion date
        final Date date = new Date(tempCompletionDate);

        //Display fragment title
        setToolbarTitle(getString(R.string.completion_of) + completion.getTaskIdentity().getName());


        // Edit completion date
        completionDateEdit = editLayout.findViewById(R.id.edit_completion_date);
        final String completionDateString = DateFormatter.format(date.getTime(),"EEEE d MMM yyyy");
        completionDateEdit.setText(completionDateString);

        // Edit date listener
        completionDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDatePicker(new Date(tempCompletionDate));
            }
        });


        // Completion time string
        completionTimeEdit = editLayout.findViewById(R.id.edit_completion_time);
        final String completionTimeString = DateFormatter.format(date.getTime(),"HH:mm");
        completionTimeEdit.setText(completionTimeString);

        // Edit time listener
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

    // Check if any changes have been made to the completion
    @Override
    boolean itemDataIsChanged() {
        return completion.getDate() != tempCompletionDate;
    }

    // Save any changes that have been made to the completion
    @Override
    void saveChanges() {

        // Check if any changes have been made
        if(!itemDataIsChanged()) return;

        editCompletionDate(completion, tempCompletionDate);


    }

    @Override
    void deleteItem() {
        deleteCompletion(completion);
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
