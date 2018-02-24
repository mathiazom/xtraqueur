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
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;

public class NewTaskFragment extends Fragment {

    // Fragment root view
    private View view;

    // Fragment toolbar
    private Toolbar mToolbar;

    // Fragment launch triggered by TasksFragment EditText focus change
    private boolean fromPreEdit;

    // Tasks data set
    private ArrayList<XTask> tasks;

    // Task name input field
    private TextInputEditText mNameEditText;
    private TextInputLayout mNameLayout;

    // Task fee input field
    private TextInputEditText mFeeEditText;
    private TextInputLayout mFeeLayout;

    // Task color input field
    private LinearLayout mColorField;

    // Current unsaved task color value
    private int temp_color;

    // Log tag for debugging
    private final static String TAG = "Xtraqueur-NewTask";

    // Listener to communicate with MainActivity
    private NewTaskFragmentListener newTaskFragmentListener;

    interface NewTaskFragmentListener {
        void loadTasksFragment();

        void updateTasksDataOnDrive(ArrayList<XTask> tasks);
    }

    // Init listener when activity is available
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            newTaskFragmentListener = (NewTaskFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NewTaskFragmentListener");
        }
    }

    // Custom constructor
    public static NewTaskFragment newInstance(ArrayList<XTask> tasks, int temp_color, boolean fromPreEdit) {
        NewTaskFragment fragment = new NewTaskFragment();
        fragment.tasks = tasks;
        fragment.temp_color = temp_color;
        fragment.fromPreEdit = fromPreEdit;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        this.view = inflater.inflate(R.layout.fragment_newtask, container, false);

        mNameLayout = view.findViewById(R.id.newtask_layout_name);
        mFeeLayout = view.findViewById(R.id.newtask_layout_fee);

        mNameEditText = view.findViewById(R.id.newtask_edit_name);
        mFeeEditText = view.findViewById(R.id.newtask_edit_fee);

        mColorField = view.findViewById(R.id.newtask_field_color);

        initToolbar();

        initListeners();

        initColorFilters();

        // Open keyboard if from edittext
        if (fromPreEdit) preEdit();

        return view;
    }

    // Initialize toolbar field variable and add action buttons with listeners
    void initToolbar(){

        // Initialize toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_new_task_fragment);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                }
                newTaskFragmentListener.loadTasksFragment();
            }
        });

        // Add listeners to the other toolbar action buttons
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.newtaks_new_task_icon:
                        createTask();
                }
                return false;
            }
        });
    }

    // Show keyboard if fromPreEdit
    private void preEdit() {
        if(getContext() == null) return;

        view.findViewById(R.id.newtask_edit_name).requestFocus();
        TextInputEditText editText = view.findViewById(R.id.newtask_edit_name);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            Log.i(TAG,"Show Soft Input");
        }
    }

    // Init fragment listeners
    private void initListeners() {

        // COLOR PICKER
        view.findViewById(R.id.newtask_field_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPicker();
            }
        });
    }

    // Apply task color filters
    private void initColorFilters() {

        //Top toolbar
        mToolbar.setBackground(new ColorDrawable(temp_color));

        // ScrollView background
        ScrollView scrollView = view.findViewById(R.id.newtask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(temp_color));
        scrollView.setBackground(scrollDrawable);

        // Color field
        Drawable colorFieldBackground = mColorField.getBackground();
        colorFieldBackground.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        mColorField.setBackground(colorFieldBackground);
        mColorField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPicker();
            }
        });
    }

    // Chcek if name is in use by another task
    boolean nameInUse(String name){
        for (XTask t : tasks) {
            if (t.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // Check if name is a valid task name
    boolean validName(String name){
        return !String.valueOf(name).trim().isEmpty();
    }

    // Create task object according to input and add it to the tasks ArrayList
    // Tell MainActivity to update Google Drive and SharedPreferences
    private void createTask() {

        // Get name from TextInputEditText
        String name = String.valueOf(mNameEditText.getEditableText());

        if(!validName(name)) mNameLayout.setError(getString(R.string.invalid_name));

        if(nameInUse(name)) mNameLayout.setError(getString(R.string.already_in_use));

        int fee;
        try {
            fee = Integer.parseInt(String.valueOf(mFeeEditText.getEditableText()));
        } catch (NumberFormatException e) {
            mFeeLayout.setError(getString(R.string.invalid_fee));
            return;
        }

        if(!validName(name) || nameInUse(name)){
            return;
        }

        // All inputs are valid
        // Create new task according to inputs
        XTask newTask = new XTask(name, 0, fee, temp_color);

        // Add new task to tasks ArrayList
        tasks.add(newTask);

        // Update tasks_data.txt on Google Drive
        newTaskFragmentListener.updateTasksDataOnDrive(tasks);

        // Return to TasksFragment
        newTaskFragmentListener.loadTasksFragment();
    }

    // Triggered when user picks a color in the color picker
    private void onColorChanged(int color) {

        // Change temp_color value to the picked color
        temp_color = color;

        // Change color field background to picked color
        Drawable colorFieldBackground = mColorField.getBackground();
        colorFieldBackground.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        mColorField.setBackground(colorFieldBackground);

        // Change toolbar background to picked color
        mToolbar.setBackground(new ColorDrawable(temp_color));

        // Change ScrollView background to picked color
        ScrollView scrollView = view.findViewById(R.id.newtask_main_scroll);
        Drawable scrollDrawable = new ColorDrawable(darkenColor(color));
        scrollView.setBackground(scrollDrawable);

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

    // Get a darker shade of the original task color
    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    int getTempColor(){
        return this.temp_color;
    }

}
