package com.mzom.xtraqueur;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Date;

public class EditTaskFragment extends BaseEditFragment {

    private ConstraintLayout fragmentView;

    private TextInputEditText mNameEditText;
    private TextInputEditText mFeeEditText;

    private TextInputLayout mNameLayout;
    private TextInputLayout mFeeLayout;

    private Button mManageButton;

    // Tasks data set
    private ArrayList<XTask> tasks;

    // Current task being edited
    private XTask task;

    // Index of task in the tasks data set
    private int index;

    // Color selected in color picker, but not yet saved to task
    private int temp_color;

    final static String TAG = "Xtraqueur-EditFrag";

    // Constructor that enables MainActivity to pass arguments to the fragments field variables on creation
    public static EditTaskFragment newInstance(ArrayList<XTask> tasks, XTask task, int index) {
        EditTaskFragment fragment = new EditTaskFragment();
        fragment.index = index;
        fragment.task = task;
        fragment.tasks = tasks;
        fragment.temp_color = task.getTaskFields().getColor();
        return fragment;
    }

    private void initMaterialColorList(){

        ConstraintLayout colorButton = fragmentView.findViewById(R.id.edittask_color_button);
        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialColorDialog materialColorDialog = new MaterialColorDialog(getContext(), temp_color,new MaterialColorDialog.MaterialColorDialogListener() {
                    @Override
                    public void onColorPicked(int color) {
                        onColorChanged(color);
                    }
                });
                materialColorDialog.show();
            }
        });

        LinearLayout marker = fragmentView.findViewById(R.id.edittask_color_button_marker);
        Drawable background = marker.getBackground();
        background.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        marker.setBackground(background);

        TextView colorTitle = fragmentView.findViewById(R.id.edittask_color_button_title);
        String sColor = String.format("#%06X", (0xFFFFFF & temp_color));
        colorTitle.setText(sColor);

    }

    // Triggered when user picks a color in the color picker
    private void onColorChanged(final int color) {

        temp_color = color;

        notifyItemColorChange();

        // Manage button
        Drawable manage_drawable = mManageButton.getBackground();
        manage_drawable.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        mManageButton.setBackground(manage_drawable);

        // Color marker
        LinearLayout marker = fragmentView.findViewById(R.id.edittask_color_button_marker);
        Drawable background = marker.getBackground();
        background.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        marker.setBackground(background);

        // Color title
        TextView colorTitle = fragmentView.findViewById(R.id.edittask_color_button_title);
        String sColor = String.format("#%06X", (0xFFFFFF & temp_color));
        colorTitle.setText(sColor);
    }

    @NonNull
    @Override
    ConstraintLayout getEditLayout(ConstraintLayout baseEditContainer) {

        // Root view for this fragment
        fragmentView = (ConstraintLayout) getLayoutInflater().inflate(R.layout.fragment_edit_task, baseEditContainer, false);

        // Init field variables for fragment views

        mNameLayout = fragmentView.findViewById(R.id.edittask_layout_name);
        mFeeLayout = fragmentView.findViewById(R.id.edittask_layout_fee);

        mNameEditText = fragmentView.findViewById(R.id.edittask_edit_name);
        mFeeEditText = fragmentView.findViewById(R.id.edittask_edit_fee);

        mManageButton = fragmentView.findViewById(R.id.button_manage_completions);

        // Load task values to EditTexts
        mNameEditText.setText(task.getTaskFields().getName());
        mFeeEditText.setText(String.valueOf(task.getTaskFields().getFee()));

        // Manage button background
        Drawable manage_drawable = mManageButton.getBackground();
        manage_drawable.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        mManageButton.setBackground(manage_drawable);

        // Visually disable completion manage button if there are no task completions
        if(task.getCompletions().size() == 0) mManageButton.setAlpha(0.30f);

        Button deleteButton = fragmentView.findViewById(R.id.delete_task);

        // Delete button background
        if (getContext() != null) {
            deleteButton.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            Drawable delete_drawable = deleteButton.getBackground();
            delete_drawable.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
            deleteButton.setBackground(delete_drawable);
        }
        setItemDeleteButton(deleteButton,getString(R.string.delete_task_confirmation),getString(R.string.delete_task_message));

        // Completions Manage button
        mManageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task.getCompletions() == null || task.getCompletions().size() == 0) {
                    return;
                }

                getBaseEditListener().loadCompletionsFragment(tasks,task.getTaskFields());
            }
        });


        initMaterialColorList();

        return fragmentView;
    }

    @Override
    int getItemColor() {
        return temp_color;
    }

    @Override
    boolean itemDataIsChanged() {
        // Get edited task fee
        double fee;
        try {
            // CHECK FOR NUMBERS THAT ARE TOO LARGE (> 2^31-1)
            fee = Double.parseDouble(String.valueOf(mFeeEditText.getEditableText()));
        } catch (NumberFormatException e) {
            return true;
        }

        // Get edited task name
        String name = String.valueOf(mNameEditText.getEditableText());

        return !(task.getTaskFields().getName().equals(name) && task.getTaskFields().getFee() == fee && temp_color == task.getTaskFields().getColor());
    }

    @Override
    void saveChanges() {

        // Collect input
        String name = (String.valueOf(mNameEditText.getEditableText())).trim();

        if (name.isEmpty()) {
            mNameLayout.setError(getString(R.string.invalid_name));
            return;
        }

        // Check if name is already in use
        for (XTask t : tasks) {
            if (t.getTaskFields().getName().equals(name) && tasks.indexOf(t) != index) {
                // Notify user that this name is already in use by another task
                mNameLayout.setError(getString(R.string.already_in_use));
                return;
            }
        }

        // Check if number format is invalid
        double fee;
        try {
            fee = Double.parseDouble(String.valueOf(mFeeEditText.getEditableText()));
        } catch (NumberFormatException e) {
            // Notify user that the inputted fee is invalid
            mFeeLayout.setError(getString(R.string.invalid_fee));
            return;
        }

        // Check if any changes have been made
        if (itemDataIsChanged()) {

            // Create duplicate of task and set properties according to input
            XTask edit = task;
            edit.getTaskFields().setName(name);
            edit.getTaskFields().setFee(fee);
            edit.getTaskFields().setColor(temp_color);

            // Commit changes
            tasks.set(index, edit);

            // Update tasks_data.txt on Google Drive
            getBaseEditListener().updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
                @Override
                public void onSuccess(DriveFile driveFile) {
                    returnToItemsList();
                }
            });


        }
    }

    @Override
    void deleteItem() {
        // Delete task from ArrayList
        tasks.remove(task);

        // Update tasks data on drive
        getBaseEditListener().updateTasksDataOnDrive(tasks, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                returnToItemsList();
            }
        });
    }

    @Override
    void onDatePicked(Date newDate) {

    }
}
