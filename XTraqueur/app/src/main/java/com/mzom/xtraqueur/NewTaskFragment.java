package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;

public class NewTaskFragment extends XFragment {

    // Fragment root view
    private View view;

    // Fragment toolbar
    private Toolbar mToolbar;

    // Tasks data set
    private ArrayList<XTask> tasks;

    private ArrayList<XPayment> payments;

    private ArrayList<XTaskCompletion> instantCompletions;

    // Task name input field
    private TextInputEditText mNameEditText;
    private TextInputLayout mNameLayout;

    // Task fee input field
    private TextInputEditText mFeeEditText;
    private TextInputLayout mFeeLayout;

    private CheckBox checkBox;
    private boolean isInstantCompletion;

    // Current unsaved task color value
    private int temp_color;

    // Log tag for debugging
    private final static String TAG = "Xtraqueur-NewTask";

    public static NewTaskFragment newInstance(ArrayList<XTask> tasks, ArrayList<XPayment> payments,ArrayList<XTaskCompletion> instantCompletions, int temp_color) {
        return newInstance(tasks,payments,instantCompletions,false,temp_color);
    }

    // Custom constructor to pass required fragment variables
    public static NewTaskFragment newInstance(ArrayList<XTask> tasks, ArrayList<XPayment> payments,ArrayList<XTaskCompletion> instantCompletions, boolean isInstantCompletion, int temp_color) {
        NewTaskFragment fragment = new NewTaskFragment();
        fragment.tasks = tasks;
        fragment.payments = payments;
        fragment.instantCompletions = instantCompletions;
        fragment.isInstantCompletion = isInstantCompletion;
        fragment.temp_color = temp_color;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Save the fragment state on configuration changes (mainly screen rotations)
        setRetainInstance(true);

        // Fragment root view
        this.view = inflater.inflate(R.layout.fragment_new_task, container, false);

        // Fragment view fields
        mNameLayout = view.findViewById(R.id.newtask_layout_name);
        mFeeLayout = view.findViewById(R.id.newtask_layout_fee);

        mNameEditText = view.findViewById(R.id.newtask_edit_name);
        mFeeEditText = view.findViewById(R.id.newtask_edit_fee);


        // Initialize toolbar field variable and add action buttons with listeners
        initToolbar();

        initInstantCompletionCheckBox();

        // Color list
        initMaterialColorList();

        // Paint fragment views with task color
        //initColorFilters();
        onColorChanged(temp_color);

        // Open keyboard to let user edit task name immediately
        focusTaskNameEditText();

        return view;
    }

    // Initialize toolbar field variable and add action buttons with listeners
    void initToolbar() {

        // Initialize toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_new_task_fragment);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                FragmentLoader.reverseLoading(getContext());
            }
        });

        // Add listeners to the other toolbar action buttons
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.newtaks_new_task_icon:

                        if(checkBox.isChecked()){

                            final XTaskCompletion instantCompletion = createInstantCompletionFromInputs();

                            if(instantCompletion == null) return false;

                            instantCompletions.add(instantCompletion);

                            // Update instant_completion_data.txt on Google Drive with updated ArrayList
                            XDataUploader.uploadData(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME,instantCompletions, getContext(), new OnSuccessListener<DriveFile>() {
                                @Override
                                public void onSuccess(DriveFile driveFile) {

                                    // Return to TasksFragment
                                    FragmentLoader.loadFragment(TasksFragment.newInstance(tasks,payments, instantCompletions),getContext());
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    if(getView() == null) return;

                                    Snackbar.make(getView(), R.string.error_could_not_create_new_task,Snackbar.LENGTH_LONG);
                                }
                            });

                        }else{

                            // Generate task object
                            final XTask newTask = createTaskFromInputs();

                            if(newTask == null) return false;

                            // Add new task to tasks ArrayList
                            tasks.add(newTask);

                            // Update tasks_data.txt on Google Drive with updated ArrayList
                            XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME,tasks, getContext(), new OnSuccessListener<DriveFile>() {
                                @Override
                                public void onSuccess(DriveFile driveFile) {

                                    // Return to TasksFragment
                                    FragmentLoader.loadFragment(TasksFragment.newInstance(tasks,payments, instantCompletions),getContext());
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    if(getView() == null) return;

                                    Snackbar.make(getView(), R.string.error_could_not_create_new_task,Snackbar.LENGTH_LONG);
                                }
                            });

                        }

                        hideKeyboard();

                }
                return false;
            }
        });
    }

    void initInstantCompletionCheckBox(){
        checkBox = view.findViewById(R.id.new_instant_completion_check_box);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onColorChanged(temp_color);
            }
        });
        checkBox.setChecked(isInstantCompletion);
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

    // Check if name string is in use by another task
    boolean isNameUsed(String name) {
        for (XTask t : tasks) {
            if (t.getTaskIdentity().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // Check if name string is a valid task name
    boolean invalidName(String name) {
        return String.valueOf(name).trim().isEmpty();
    }

    // Create task object according to input and add it to the tasks ArrayList
    // Tell MainActivity to update Google Drive and SharedPreferences
    private XTask createTaskFromInputs() {

        // Get name from TextInputEditText (with leading and trailing white space removed)
        String name = getNameInput();

        // Notify user if the inputted name is invalid
        boolean invalidName = displayNameError(name);

        Double fee = getFeeInput();

        // Cancel task creation if any of the inputted values are invalid
        if (invalidName || fee == null) {
            return null;
        }

        // Create new task according to inputs
        return new XTask(new XTaskIdentity(name, fee, temp_color));
    }

    // Create instant completion object according to input and add it to the instant completions ArrayList
    private XTaskCompletion createInstantCompletionFromInputs() {

        // Get name from TextInputEditText (with leading and trailing white space removed)
        String name = getNameInput();

        // Notify user if the inputted name is invalid
        boolean invalidName = displayNameError(name);

        Double fee = getFeeInput();

        // Cancel task creation if any of the inputted values are invalid
        if (invalidName || fee == null) {
            return null;
        }

        // Create new instant completion according to inputs
        return new XTaskCompletion(Calendar.getInstance().getTime().getTime(),new XTaskIdentity(name,fee,temp_color), true);
    }

    String getNameInput(){
        return String.valueOf(mNameEditText.getEditableText()).trim();
    }

    Double getFeeInput(){

        // Check if inputted fee is valid (not too big and integer format)

        try {
            return Double.parseDouble(String.valueOf(mFeeEditText.getEditableText()));
        } catch (NumberFormatException e) {
            mFeeLayout.setError(getString(R.string.invalid_fee));
            return null;
        }

    }

    boolean displayNameError(String name){

        boolean invalidName = false;

        // Notify user if the inputted name is invalid
        if (invalidName(name)){
            mNameLayout.setError(getString(R.string.invalid_name));
            invalidName = true;
        }

        // Notify user if the inputted name is already in use by another task
        if (isNameUsed(name)){
            mNameLayout.setError(getString(R.string.already_in_use));
            invalidName = true;
        }

        return invalidName;

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

        ScrollView scrollView = view.findViewById(R.id.newtask_main_scroll);

        Drawable scrollDrawable;

        if(checkBox.isChecked() && getContext() != null){
            scrollDrawable = new ColorDrawable(getContext().getResources().getColor(R.color.colorInstantCompletion));

        }else{
            scrollDrawable = new ColorDrawable(ColorUtilities.getDarkerColor(color));
        }

        scrollView.setBackground(scrollDrawable);

        LinearLayout marker = view.findViewById(R.id.newtask_color_button_marker);
        Drawable background = marker.getBackground();
        background.setColorFilter(temp_color, PorterDuff.Mode.SRC_ATOP);
        marker.setBackground(background);

        TextView colorTitle = view.findViewById(R.id.newtask_color_button_title);
        String sColor = String.format("#%06X", (0xFFFFFF & temp_color));
        colorTitle.setText(sColor);

    }

}
