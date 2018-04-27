package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
import android.widget.DatePicker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class NewPaymentFragment extends Fragment {

    private static final String TAG = "NewPaymentFragment";

    private Date paymentDate;

    private View view;

    private Toolbar mSelectionModeToolbar;

    private TimelineAdapter mAdapter;

    private ArrayList<Boolean> selectionArray;

    private ArrayList<XTask> tasks;

    private ArrayList<XTaskCompletion> completions;

    private NewPaymentFragmentListener mNewPaymentFragmentListener;

    interface NewPaymentFragmentListener{
        void onBackPressed();

        void updatePaymentsDataOnDrive(XTaskPayment payment);

        void updateTasksDataOnDrive(ArrayList<XTask> tasks);

        void loadPaymentsFragment();
    }

    public static NewPaymentFragment newInstance(ArrayList<XTask> tasks) {

        NewPaymentFragment fragment = new NewPaymentFragment();
        fragment.tasks = tasks;
        fragment.completions = fragment.getCompletionsFromTasks(tasks);
        fragment.selectionArray = new ArrayList<>();
        fragment.initSelectionArray();
        return fragment;
    }

    public static NewPaymentFragment newInstance(ArrayList<XTask> tasks,ArrayList<Boolean> selectionArray) {

        NewPaymentFragment fragment = new NewPaymentFragment();
        fragment.tasks = tasks;
        fragment.completions = fragment.getCompletionsFromTasks(tasks);
        fragment.initSelectionArray();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_newpayment,container,false);

        initToolbar();

        initListeners();

        loadCompletions();

        setPaymentDate(Calendar.getInstance().getTime());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mNewPaymentFragmentListener = (NewPaymentFragmentListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(e.toString() + " must implement NewPaymentFragmentListener");
        }
    }

    private void initToolbar(){

        // Regular toolbar

        Toolbar mToolbar = view.findViewById(R.id.toolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewPaymentFragmentListener.onBackPressed();
            }
        });



        // Payment selection toolbar

        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);

        mSelectionModeToolbar.inflateMenu(R.menu.menu_new_payment_fragment_selection_mode);

        MenuItem selectAll = mSelectionModeToolbar.getMenu().findItem(R.id.payment_selection_mode_icon_select_all);
        selectAll.setVisible(true);
        MenuItem deselectAll = mSelectionModeToolbar.getMenu().findItem(R.id.payment_selection_mode_icon_deselect_all);
        deselectAll.setVisible(false);

        mSelectionModeToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.payment_selection_mode_icon_select_all:
                        // Select all payments
                        mAdapter.setUniversalItemSelection(true);
                        break;
                    case R.id.payment_selection_mode_icon_deselect_all:
                        mAdapter.setUniversalItemSelection(false);
                        break;
                }

                return false;
            }
        });

        mSelectionModeToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewPaymentFragmentListener.onBackPressed();
            }
        });

    }

    private void initListeners(){

        view.findViewById(R.id.newpayment_button_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(getContext(),R.style.AlertDialogTheme)
                        .setPositiveButton(R.string.confirmation_dialog_register_payment_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                registerPayment();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                alertDialog.setTitle(getString(R.string.confirmation_dialog_register_payment));
                alertDialog.setMessage(getString(R.string.confirmation_dialog_register_payment_msg));

                alertDialog.show();
            }
        });

    }


    private void datePicker(Date date) {

        // Get current completion date
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (getContext() == null) return;

        // Dialog to change completion date
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(year,month,dayOfMonth);
                final Date newDate = calendar.getTime();

                setPaymentDate(newDate);

            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void setPaymentDate(final Date newDate){

        // Date string format
        final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

        // Format to string
        String dateString = dateFormat.format(newDate);

        // Date EditText
        TextInputEditText editDate = view.findViewById(R.id.newpayment_edit_date);

        // Add date string to EditText
        editDate.setText(dateString);

        paymentDate = newDate;

        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker(newDate);
            }
        });
    }


    private ArrayList<XTaskCompletion> getCompletionsFromTasks(ArrayList<XTask> tasks){
        ArrayList<XTaskCompletion> completions = new ArrayList<>();

        for (XTask t : tasks) {
            if (t.getCompletionsList() != null)
                for (Long l : t.getCompletionsList()) {
                    completions.add(new XTaskCompletion(l, t));
                }
        }

        // Sort completions based on recency
        Collections.sort(completions, new Comparator<XTaskCompletion>() {
            @Override
            public int compare(XTaskCompletion xTaskCompletion, XTaskCompletion t1) {
                return Long.compare(t1.getDate(), xTaskCompletion.getDate());
            }
        });

        return completions;
    }

    private void loadCompletions() {

        RecyclerView mRecyclerView = view.findViewById(R.id.register_payment_completions);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Display completions in RecyclerView with TimelineAdapter
        mAdapter = new TimelineAdapter(true, new TimelineAdapter.TimelineAdapterListener() {

            @Override
            public void onItemClick(int pos,float y) {}

            @Override
            public int getItemCount() {
                return completions.size();
            }

            @Override
            public boolean onSelectionChanged(ArrayList<Boolean> updatedSelectionArray) {

                // Update selection array
                selectionArray = updatedSelectionArray;

                // Check if all completions have been selected for registering
                boolean allSelected = totalSelected() == completions.size();

                // Make "SELECT ALL" action visible if not all completions have been selected
                MenuItem selectAll = mSelectionModeToolbar.getMenu().findItem(R.id.payment_selection_mode_icon_select_all);
                selectAll.setVisible(!allSelected);

                // Make "DESELECT ALL" action visible if all completions have been selected
                MenuItem deselectAll = mSelectionModeToolbar.getMenu().findItem(R.id.payment_selection_mode_icon_deselect_all);
                deselectAll.setVisible(allSelected);

                // Use toolbar to display number of completions selected and their total value
                updateSelectionToolbar();

                // Disable registering if no completions are selected, enable otherwise
                setRegisterFieldEnabled(totalSelected() != 0);

                // Return true to always have selectionMode enabled
                return true;
            }

            @Override
            public TimelineItem getTimelineItem(int pos) {
                String title = completions.get(pos).getTask().getName();
                int color = completions.get(pos).getTask().getColor();
                long date = completions.get(pos).getDate();

                return new TimelineItem(title,color,date);
            }

        });

        mAdapter.setSelectionArray(selectionArray);

        mRecyclerView.setAdapter(mAdapter);


    }

    private void initSelectionArray(){
        selectionArray = new ArrayList<>(Arrays.asList(new Boolean[completions.size()]));
        Collections.fill(selectionArray,false);
    }

    // Calculate how many completions have been selected
    private int totalSelected(){
        return mAdapter.getTotalSelected();
    }

    private void setRegisterFieldEnabled(boolean enabled){

        // Register button
        view.findViewById(R.id.newpayment_button_register).setEnabled(enabled);

        /*// Register date field
        TextInputEditText editDate = view.findViewById(R.id.newpayment_edit_date);
        editDate.setEnabled(enabled);*/

    }

    // Update toolbar title based on number of selected
    private void updateSelectionToolbar(){

        int total_selected = totalSelected();
        String toolbar_title;
        if(total_selected == 0){
            toolbar_title = getString(R.string.select_completions);
        }else{

            // Total value of all tasks
            double total = 0;

            // Loop trough data set and add task value to total value
            for (XTaskCompletion completion : getSelectedCompletions()) {
                total += completion.getTask().getFee();
            }

            // Get currency format
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
            String totalString = nf.format(total);

            // Display number of selected completions and their total value
            toolbar_title = String.valueOf(total_selected) + " " + getString(R.string.selected) + " (" + totalString + ")";
        }

        mSelectionModeToolbar.setTitle(toolbar_title);
    }

    // Register new payment for selected completions
    private void registerPayment(){

        // Completions will be archived, so remove them from regular completion list
        for(XTaskCompletion completion : getSelectedCompletions()){
            tasks.get(tasks.indexOf(completion.getTask())).removeCompletion(completion.getDate());
        }

        // Save changes
        mNewPaymentFragmentListener.updateTasksDataOnDrive(tasks);

        // Add completions to payment object
        XTaskPayment newPayment = new XTaskPayment(getSelectedCompletions(),paymentDate.getTime());

        // Save new payment to drive
        mNewPaymentFragmentListener.updatePaymentsDataOnDrive(newPayment);

        // Show the new payment on the timeline
        mNewPaymentFragmentListener.onBackPressed();
        mNewPaymentFragmentListener.loadPaymentsFragment();

    }

    // Use SparseBooleanArray to get RecyclerView's currently selected completions
    private ArrayList<XTaskCompletion> getSelectedCompletions(){
        ArrayList<XTaskCompletion> selectedCompletions = new ArrayList<>();

        for(int c = 0;c<completions.size();c++){
            if(selectionArray.get(c)){
                selectedCompletions.add(completions.get(c));
            }
        }
        return selectedCompletions;
    }


}
