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
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;

public class EditTaskFragment extends Fragment {

    // Fragment main views
    private View view;

    private Toolbar mToolbar;

    private TextInputEditText mNameEditText;
    private TextInputLayout mNameLayout;

    private TextInputEditText mFeeEditText;
    private TextInputLayout mFeeLayout;

    private Button mManageButton;
    private Button mDeleteButton;

    private LinearLayout mColorField;

    private boolean isTaskNameChanged;
    private boolean isTaskFeeChanged;
    private boolean isTaskColorChanged;


    // Tasks data set
    private ArrayList<XTask> tasks;

    // Current task being edited
    private XTask task;

    // Index of task in the tasks data set
    private int index;

    // Color selected in color picker, but not yet saved to task
    private int temp_color;

    // Listener to communicate with MainActivity
    private EditTaskFragmentListener editTaskFragmentListener;

    // Constructor that enables MainActivity to pass arguments to the fragments field variables on creation
    public static EditTaskFragment newInstance(ArrayList<XTask> tasks, XTask task, int index) {
        EditTaskFragment fragment = new EditTaskFragment();
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

        LayoutInflater localInflater = inflater;

        if(editTaskFragmentListener.useDarkText(task.getColor())){
            final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(),R.style.ThemeOverlay_AppCompat_Light);
            localInflater = inflater.cloneInContext(contextThemeWrapper);
        }



        // Root view for this fragment
        this.view = localInflater.inflate(R.layout.fragment_edittask, container, false);

        // Init field variables for fragment views
        mNameLayout = view.findViewById(R.id.edittask_layout_name);
        mFeeLayout = view.findViewById(R.id.edittask_layout_fee);

        mNameEditText = view.findViewById(R.id.edittask_edit_name);
        mFeeEditText = view.findViewById(R.id.edittask_edit_fee);

        mManageButton = view.findViewById(R.id.button_manage_completions);
        mDeleteButton = view.findViewById(R.id.delete_task);

        mColorField = view.findViewById(R.id.edittask_field_color);

        // Init fragment toolbar
        initToolbar();

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

    // Interface to communicate with MainActivity
    interface EditTaskFragmentListener {
        // Tell MainActivity to display TasksFragment in the FrameLayout
        void loadTasksFragment();

        // Update tasks data on Google Drive
        void updateTasksDataOnDrive(ArrayList<XTask> tasks);

        // Access useDarkText method in MainActivity
        boolean useDarkText(int color);
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

    // Initialize toolbar field variable and add action buttons with listeners
    void initToolbar() {

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

        // Handle color contrast (dark text if needed)
        // Navigation icon
        Drawable toolbarNavIcon = mToolbar.getNavigationIcon();
        if(toolbarNavIcon == null || getContext() == null){
            return;
        }

        if(editTaskFragmentListener.useDarkText(task.getColor())){
            toolbarNavIcon.setColorFilter(getContext().getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }else{
            toolbarNavIcon.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
        }


    }

    void confirmDiscardChanges(){
        // Check if changes have been made and ask user to stay in fragment or discard changes
        if (taskDataIsChanged()) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.discard_changes_dialog_title)
                    .setMessage(R.string.discard_changes_dialog_message)
                    .setPositiveButton(R.string.discard_option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editTaskFragmentListener.loadTasksFragment();
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
            editTaskFragmentListener.loadTasksFragment();
        }
    }

    // Setup the listeners for the views in this fragment
    private void initListeners() {
        // Color picker from color field
        mColorField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPicker();
            }
        });

        // Delete task button
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTask();
            }
        });

        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isTaskNameChanged = !mNameEditText.getEditableText().toString().equals(task.getName());
                updateNavigationIcon();
            }
        });

        mFeeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    isTaskFeeChanged = Integer.parseInt(mFeeEditText.getEditableText().toString()) != task.getFee();
                    updateNavigationIcon();
                }catch (NumberFormatException e){
                    mFeeLayout.setError(getString(R.string.invalid_fee));
                }
            }
        });
    }

    // Display a ColorPickerDialog to change the tasks color
    private void colorPicker() {

        // Declare new ColorPickerDialog with temp_color
        ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(temp_color).setShowAlphaSlider(true).create();

        // Add listener to the dialog
        colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
            @Override
            public void onColorSelected(int dialogId, int color) {
                // Change fragment UI to use the selected color
                onColorChanged(color);
            }

            @Override
            public void onDialogDismissed(int dialogId) {

            }
        });

        // Display color picker (as long as getActivity() doesn't return null)
        if(getActivity() != null) colorPickerDialog.show(getActivity().getFragmentManager(), "ColorPicker");
    }

    // Triggered when user picks a color in the color picker
    private void onColorChanged(final int color) {

        temp_color = color;

        isTaskColorChanged = temp_color != task.getColor();

        updateNavigationIcon();

        // COLOR FIELD
        Drawable colorFieldBackground = mColorField.getBackground();
        colorFieldBackground.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        mColorField.setBackground(colorFieldBackground);
        mColorField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPicker();
            }
        });

        // TOOLBAR COLOR
        mToolbar.setBackground(new ColorDrawable(color));

        //MANAGE BUTTON
        Drawable manage_drawable = mManageButton.getBackground();
        manage_drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        mManageButton.setBackground(manage_drawable);

        // SCROLL CONTAINER BACKGROUND
        ScrollView scrollView = view.findViewById(R.id.edittask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(color));
        scrollView.setBackground(scrollDrawable);
    }

    // Indicate to the user that there are unsaved changes made to the task
    private void updateNavigationIcon(){
        if(isTaskNameChanged || isTaskFeeChanged || isTaskColorChanged){
            mToolbar.setNavigationIcon(R.drawable.ic_clear);
        }
        else {
            mToolbar.setNavigationIcon(R.drawable.ic_back);
        }
    }

    boolean hasUnsavedChanges(){
        return isTaskNameChanged || isTaskFeeChanged || isTaskColorChanged;
    }

    // Fill input and color field with current task values
    private void loadTaskData() {

        // Load task values to EditTexts
        mNameEditText.setText(task.getName());
        mFeeEditText.setText(String.valueOf(task.getFee()));

        // Color field background
        Drawable colorFieldBackground = mColorField.getBackground();
        colorFieldBackground.setColorFilter(task.getColor(), PorterDuff.Mode.SRC_ATOP);
        mColorField.setBackground(colorFieldBackground);

        // Toolbar background
        mToolbar.setBackground(new ColorDrawable(task.getColor()));

        // Manage button background
        Drawable manage_drawable = mManageButton.getBackground();
        manage_drawable.setColorFilter(task.getColor(), PorterDuff.Mode.SRC_ATOP);
        mManageButton.setBackground(manage_drawable);

        // Manage button listener
        mManageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task.getCompletionsList() == null || task.getCompletionsList().size() == 0) {
                    return;
                }
                CompletionsDialog completionsDialog = new CompletionsDialog(getContext(), tasks, task, index);
                completionsDialog.setOnCompletionDeletedListener(new CompletionsDialog.OnCompletionDeletedListener() {
                    @Override
                    public void onCompletionDeleted(ArrayList<XTask> updated_tasks) {
                        editTaskFragmentListener.updateTasksDataOnDrive(updated_tasks);
                    }
                });
                completionsDialog.show();
            }
        });

        // Delete button background
        if (getContext() != null) {
            mDeleteButton.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            Drawable delete_drawable = mDeleteButton.getBackground();
            delete_drawable.setColorFilter(Color.parseColor("#eeeeee"), PorterDuff.Mode.SRC_ATOP);
            mDeleteButton.setBackground(delete_drawable);
        }

        // ScrollView background
        ScrollView scrollView = view.findViewById(R.id.edittask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(task.getColor()));
        scrollView.setBackground(scrollDrawable);
    }

    // Check if any properties of the task has been changed
    private boolean taskDataIsChanged() {

        // COLLECT INPUT
        String name = String.valueOf(mNameEditText.getEditableText());

        int fee;
        try {
            // CHECK FOR NUMBERS THAT ARE TOO LARGE (> 2^31-1)
            fee = Integer.parseInt(String.valueOf(mFeeEditText.getEditableText()));
        } catch (NumberFormatException e) {
            return true;
        }


        return !(task.getName().equals(name) && task.getFee() == fee && task.getColor() == temp_color);
    }

    // Change task values according to inputs and update to Google drive
    private void saveChanges() {

        // Collect input
        String name = String.valueOf(mNameEditText.getEditableText());

        if (name.trim().isEmpty()) {
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
        int fee;
        try {
            fee = Integer.parseInt(String.valueOf(mFeeEditText.getEditableText()));
        } catch (NumberFormatException e) {
            // Notify user that the inputted fee is invalid
            mFeeLayout.setError(getString(R.string.invalid_fee));
            return;
        }

        // Check if any changes have been made
        if (taskDataIsChanged()) {

            // Create duplicate of task and set properties according to input
            XTask edit = task;
            edit.setName(name);
            edit.setFee(fee);
            edit.setColor(temp_color);

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
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
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

    // Get a darker shade of the original task color
    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
