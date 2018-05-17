package com.mzom.xtraqueur;

import android.content.Context;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class NewTaskFragment extends XFragment {

    // Fragment root view
    private View view;

    // Fragment toolbar
    private Toolbar mToolbar;

    // Tasks data set
    private ArrayList<XTask> tasks;

    // Task name input field
    private TextInputEditText mNameEditText;
    private TextInputLayout mNameLayout;

    // Task fee input field
    private TextInputEditText mFeeEditText;
    private TextInputLayout mFeeLayout;

    // Current unsaved task color value
    private int temp_color;


    private boolean instantCompletion = false;



    // Log tag for debugging
    private final static String TAG = "Xtraqueur-NewTask";

    // Interface instance to communicate with MainActivity
    private NewTaskFragmentListener newTaskFragmentListener;

    // Interface class to communicate with MainActivity
    interface NewTaskFragmentListener {

        void onBackPressed();

        void loadTasksFragment();

        void updateTasksDataOnDrive(ArrayList<XTask> tasks);
    }

    // Initialize listener when activity is available
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            newTaskFragmentListener = (NewTaskFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NewTaskFragmentListener");
        }
    }

    // Custom constructor to pass required fragment variables
    public static NewTaskFragment newInstance(ArrayList<XTask> tasks, int temp_color) {
        return newInstance(tasks,temp_color,false);
    }

    public static NewTaskFragment newInstance(ArrayList<XTask> tasks, int temp_color, boolean instantCompletion) {
        NewTaskFragment fragment = new NewTaskFragment();
        fragment.instantCompletion = instantCompletion;
        fragment.tasks = tasks;
        fragment.temp_color = temp_color;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Save the fragment state on configuration changes (mainly screen rotations)
        setRetainInstance(true);

        // Fragment root view
        this.view = inflater.inflate(R.layout.fragment_newtask, container, false);

        // Fragment view fields
        mNameLayout = view.findViewById(R.id.newtask_layout_name);
        mFeeLayout = view.findViewById(R.id.newtask_layout_fee);

        mNameEditText = view.findViewById(R.id.newtask_edit_name);
        mFeeEditText = view.findViewById(R.id.newtask_edit_fee);

        // Initialize toolbar field variable and add action buttons with listeners
        initToolbar();

        // Color list
        initMaterialColorList();

        // Paint fragment views with task color
        initColorFilters();

        // Open keyboard to let user edit task name immediately
        focusTaskNameEditText();

        return view;
    }

    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Initialize toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_new_task_fragment);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                newTaskFragmentListener.onBackPressed();
            }
        });

        // Add listeners to the other toolbar action buttons
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.newtaks_new_task_icon:
                        createTask();
                }
                return false;
            }
        });
    }

    private void initMaterialColorList(){

        if(getContext() == null) return;

        ConstraintLayout colorButton = view.findViewById(R.id.newtask_color_button);
        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialColorDialog materialColorDialog = new MaterialColorDialog(getContext(),temp_color, new MaterialColorDialog.MaterialColorDialogListener() {
                    @Override
                    public void onColorPicked(int color) {
                        onColorChanged(color);
                    }
                });
                materialColorDialog.show();
            }
        });

        LinearLayout marker = view.findViewById(R.id.newtask_color_button_marker);
        Drawable background = marker.getBackground();
        background.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        marker.setBackground(background);

        TextView colorTitle = view.findViewById(R.id.newtask_color_button_title);
        String sColor = String.format("#%06X", (0xFFFFFF & temp_color));
        colorTitle.setText(sColor);


    }

    // Apply task color filters
    private void initColorFilters() {

        //Top toolbar
        mToolbar.setBackground(new ColorDrawable(temp_color));

        // ScrollView background
        ScrollView scrollView = view.findViewById(R.id.newtask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(temp_color));
        scrollView.setBackground(scrollDrawable);
    }

    // Focus task name EditText and show Soft Keyboard to let user quickly create the task
    private void focusTaskNameEditText() {
        view.findViewById(R.id.newtask_edit_name).requestFocus();

        if(getContext() == null) return;

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    // If Soft Keyboard is still visible on back button click or "Save" button, the keyboard is hidden to avoid transferring keyboard to other fragments
    private void hideSoftKeyboard() {
        if (getContext() == null) return;

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Chcek if name string is in use by another task
    private boolean isNameUsed(String name) {
        for (XTask t : tasks) {
            if (t.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // Check if name string is a valid task name
    private boolean invalidName(String name) {
        return String.valueOf(name).trim().isEmpty();
    }

    // Create task object according to input and add it to the tasks ArrayList
    // Tell MainActivity to update Google Drive and SharedPreferences
    private void createTask() {

        // Get name from TextInputEditText (with leading and trailing white space removed)
        String name = String.valueOf(mNameEditText.getEditableText()).trim();

        // Notify user if the inputted name is invalid
        if (invalidName(name)) mNameLayout.setError(getString(R.string.invalid_name));

        // Notify user if the inputted name is already in use by another task
        if (isNameUsed(name)) mNameLayout.setError(getString(R.string.already_in_use));

        // Check if inputted fee is valid (not too big and integer format)
        boolean validFee;
        double fee = 0;
        try {
            fee = Double.parseDouble(String.valueOf(mFeeEditText.getEditableText()));
            validFee = true;
        } catch (NumberFormatException e) {
            mFeeLayout.setError(getString(R.string.invalid_fee));
            validFee = false;
        }

        // Cancel task creation if any of the inputted values are invalid
        if (invalidName(name) || isNameUsed(name) || !validFee) {
            return;
        }

        // Create new task according to inputs
        XTask newTask = new XTask(name, fee, temp_color, instantCompletion);

        if(instantCompletion){
            newTask.addToCompletions();
        }

        // Add new task to tasks ArrayList
        tasks.add(newTask);

        // Update tasks_data.txt on Google Drive with updated ArrayList
        newTaskFragmentListener.updateTasksDataOnDrive(tasks);

        // Return to TasksFragment
        newTaskFragmentListener.loadTasksFragment();

        hideKeyboard();
    }

    private void hideKeyboard(){
        if(getContext() == null) return;

        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if(imm == null) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Triggered when user picks a color in the color picker
    private void onColorChanged(int color) {

        // Change temp_color value to the picked color
        temp_color = color;

        // Change toolbar background to picked color
        mToolbar.setBackground(new ColorDrawable(temp_color));

        // Change ScrollView background to picked color
        ScrollView scrollView = view.findViewById(R.id.newtask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(color));
        scrollView.setBackground(scrollDrawable);

        LinearLayout marker = view.findViewById(R.id.newtask_color_button_marker);
        Drawable background = marker.getBackground();
        background.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        marker.setBackground(background);

        TextView colorTitle = view.findViewById(R.id.newtask_color_button_title);
        String sColor = String.format("#%06X", (0xFFFFFF & temp_color));
        colorTitle.setText(sColor);

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
