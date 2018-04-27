package com.mzom.xtraqueur;

import android.app.AlertDialog;
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
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class EditTaskFragmentOld extends Fragment {

    // Fragment main views
    private View view;

    private Toolbar mToolbar;

    private TextInputEditText mNameEditText;
    private TextInputLayout mNameLayout;

    private TextInputEditText mFeeEditText;
    private TextInputLayout mFeeLayout;

    private Button mManageButton;
    private Button mDeleteButton;

    // Tasks data set
    private ArrayList<XTask> tasks;

    // Current task being edited
    private XTask task;

    // Index of task in the tasks data set
    private int index;

    // Color selected in color picker, but not yet saved to task
    private int temp_color;

    final static String TAG = "Xtraqueur-EditFrag";

    // Listener to communicate with MainActivity
    private EditTaskFragmentListener editTaskFragmentListener;

    // Interface to communicate with MainActivity
    interface EditTaskFragmentListener {

        void onBackPressed();

        // Tell MainActivity to display TasksFragment in the FrameLayout
        void loadTasksFragment();

        // Update tasks data on Google Drive
        void updateTasksDataOnDrive(ArrayList<XTask> tasks);

        // Show task completions in timeline
        void loadCompletionsFragment(ArrayList<XTask> tasks, XTask filterTask);
    }

    // Initialize listener field variable when fragment has been attached
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            editTaskFragmentListener = (EditTaskFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditTaskFragmentListener");
        }
    }

    // Constructor that enables MainActivity to pass arguments to the fragments field variables on creation
    public static EditTaskFragmentOld newInstance(ArrayList<XTask> tasks, XTask task, int index) {
        EditTaskFragmentOld fragment = new EditTaskFragmentOld();
        fragment.index = index;
        fragment.task = task;
        fragment.tasks = tasks;
        fragment.temp_color = task.getColor();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Retain fragment state to save its variables
        setRetainInstance(true);

        // Root view for this fragment
        this.view = inflater.inflate(R.layout.fragment_edit_task, container, false);

        // Init field variables for fragment views
        mNameLayout = view.findViewById(R.id.edittask_layout_name);
        mFeeLayout = view.findViewById(R.id.edittask_layout_fee);

        mNameEditText = view.findViewById(R.id.edittask_edit_name);
        mFeeEditText = view.findViewById(R.id.edittask_edit_fee);

        mManageButton = view.findViewById(R.id.button_manage_completions);
        mDeleteButton = view.findViewById(R.id.delete_task);

        // Init fragment toolbar
        initToolbar();


        initMaterialColorList();

        // Setup the listeners for the views in this fragment
        initListeners();

        return view;
    }

    // Execute methods that depend on an activity being created
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Fill UI with original task data
        // This is done in onActivityCreated to avoid getActivity() returning null
        loadTaskData();
    }


    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Initialize toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_edit_task_fragment);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDiscardChanges();
            }
        });

        // Add listeners to the other toolbar action buttons
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edittask_save_changes_text:
                        saveChanges();
                }
                return false;
            }
        });
    }

    private void initMaterialColorList(){

        if(getContext() == null) return;

        ConstraintLayout colorButton = view.findViewById(R.id.edittask_color_button);
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

        LinearLayout marker = view.findViewById(R.id.edittask_color_button_marker);
        Drawable background = marker.getBackground();
        background.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        marker.setBackground(background);

        TextView colorTitle = view.findViewById(R.id.edittask_color_button_title);
        String sColor = String.format("#%06X", (0xFFFFFF & temp_color));
        colorTitle.setText(sColor);


    }

    // Setup the listeners for the views in this fragment
    private void initListeners() {

        // Delete task button
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTask();
            }
        });

        // Completions Manage button
        mManageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task.getCompletionsList() == null || task.getCompletionsList().size() == 0) {
                    return;
                }

                editTaskFragmentListener.loadCompletionsFragment(tasks,task);
            }
        });
    }

    // Triggered when user picks a color in the color picker
    private void onColorChanged(final int color) {

        temp_color = color;

        Log.i("TAG",String.valueOf(task.getColor()) + " vs " + String.valueOf(temp_color));

        // TOOLBAR COLOR
        mToolbar.setBackground(new ColorDrawable(temp_color));

        //MANAGE BUTTON
        Drawable manage_drawable = mManageButton.getBackground();
        manage_drawable.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        mManageButton.setBackground(manage_drawable);

        // SCROLL CONTAINER BACKGROUND
        ScrollView scrollView = view.findViewById(R.id.edittask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(temp_color));
        scrollView.setBackground(scrollDrawable);

        LinearLayout marker = view.findViewById(R.id.edittask_color_button_marker);
        Drawable background = marker.getBackground();
        background.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        marker.setBackground(background);

        TextView colorTitle = view.findViewById(R.id.edittask_color_button_title);
        String sColor = String.format("#%06X", (0xFFFFFF & temp_color));
        colorTitle.setText(sColor);
    }

    // Fill input and color field with current task values
    private void loadTaskData() {

        // Load task values to EditTexts
        mNameEditText.setText(task.getName());
        mFeeEditText.setText(String.valueOf(task.getFee()));

        // Toolbar background
        mToolbar.setBackground(new ColorDrawable(temp_color));

        // Manage button background
        Drawable manage_drawable = mManageButton.getBackground();
        manage_drawable.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        mManageButton.setBackground(manage_drawable);

        // Visually disable completion manage button if there are no task completions
        if(task.getCompletionsList().size() == 0) mManageButton.setAlpha(0.30f);

        // Delete button background
        if (getContext() != null) {
            mDeleteButton.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            Drawable delete_drawable = mDeleteButton.getBackground();
            delete_drawable.setColorFilter(Color.parseColor("#eeeeee"), PorterDuff.Mode.SRC_ATOP);
            mDeleteButton.setBackground(delete_drawable);
        }

        // ScrollView background
        ScrollView scrollView = view.findViewById(R.id.edittask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(temp_color));
        scrollView.setBackground(scrollDrawable);
    }

    // Change task values according to inputs and update to Google drive
    private void saveChanges() {

        // Collect input
        String name = (String.valueOf(mNameEditText.getEditableText())).trim();

        if (name.isEmpty()) {
            mNameLayout.setError(getString(R.string.invalid_name));
            return;
        }

        // Check if name is already in use
        for (XTask t : tasks) {
            if (t.getName().equals(name) && tasks.indexOf(t) != index) {
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

        Log.i("Colortesting",String.valueOf(task.getColor()) + " vs " + String.valueOf(temp_color));

        // Check if any changes have been made
        if (taskDataIsChanged()) {

            // Create duplicate of task and set properties according to input
            XTask edit = task;
            edit.setName(name);
            edit.setFee(fee);
            edit.setColor(temp_color);


            Log.i("Colortesting", "setColor: " + String.valueOf(temp_color));

            // Commit changes
            tasks.set(index, edit);

            // Update tasks_data.txt on Google Drive
            editTaskFragmentListener.updateTasksDataOnDrive(tasks);


        }

        // Return to TasksFragments
        editTaskFragmentListener.loadTasksFragment();

    }

    // Permanently delete the task that is being edited
    private void deleteTask() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext(),R.style.AlertDialogTheme)
                .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Delete task from ArrayList
                        for (XTask t : tasks) {
                            if (t.getName().equals(task.getName())) {
                                tasks.remove(t);
                                break;
                            }
                        }

                        // Google Drive
                        editTaskFragmentListener.updateTasksDataOnDrive(tasks);

                        // Return to TasksFragment
                        editTaskFragmentListener.loadTasksFragment();


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        alertDialog.setTitle(getString(R.string.delete_task_confirmation));
        alertDialog.setMessage(getString(R.string.delete_task_message));
        alertDialog.show();
    }

    // Check if any properties of the task has been changed
    boolean taskDataIsChanged() {

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

        return !(task.getName().equals(name) && task.getFee() == fee && temp_color == task.getColor());
    }

    // Notify user if any unsaved changes have been made
    private void confirmDiscardChanges(){
        // Check if changes have been made and ask user to stay in fragment or discard changes
        if (taskDataIsChanged()) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.discard_changes_dialog_title)
                    .setMessage(R.string.discard_changes_dialog_message)
                    .setPositiveButton(R.string.discard_option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editTaskFragmentListener.onBackPressed();
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
            editTaskFragmentListener.onBackPressed();
        }
    }

    // Get a darker shade of the original task color
    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
