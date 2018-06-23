package com.mzom.xtraqueur;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditCompletionFragment extends BaseEditFragment {

    private XTaskCompletion completion;

    private int completionColor;

    private long tempCompletionDate;

    private EditText completionDateEdit;
    private EditText completionTimeEdit;

    private EditCompletionFragmentListener editCompletionFragmentListener;

    interface EditCompletionFragmentListener{

        void onEditCompletionDate(XTaskCompletion completion, long editedCompletionDate, final OnFinishedListener onFinishedListener);

        void onDeleteCompletion(XTaskCompletion completion, final OnFinishedListener onFinishedListener);
    }

    interface OnFinishedListener{
        void onFinished();
    }

    public static EditCompletionFragment newInstance(XTaskCompletion completion, EditCompletionFragmentListener editCompletionFragmentListener) {

        EditCompletionFragment fragment = new EditCompletionFragment();
        fragment.completion = completion;
        fragment.completionColor = fragment.completion.getTaskIdentity().getColor();
        fragment.tempCompletionDate = completion.getDate();
        fragment.editCompletionFragmentListener = editCompletionFragmentListener;
        return fragment;
    }

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

        editCompletionFragmentListener.onEditCompletionDate(completion, tempCompletionDate, new OnFinishedListener() {
            @Override
            public void onFinished() {
                returnToItemsList();
            }
        });


    }

    @Override
    void deleteItem() {

        editCompletionFragmentListener.onDeleteCompletion(completion, new OnFinishedListener() {
            @Override
            public void onFinished() {
                Log.i("XTQ-EditCompletion","Completion deleted");
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
