package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
import android.widget.DatePicker;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class NewPaymentFragment extends XFragment {

    private static final String TAG = "NewPaymentFragment";

    private Date paymentDate;

    private View view;

    private Toolbar mSelectionModeToolbar;

    private TimelineAdapter mAdapter;

    private ArrayList<Boolean> selectionArray;

    private ArrayList<XTask> tasks;

    private ArrayList<XPayment> payments;

    private ArrayList<XTaskCompletion> completions;

    private ArrayList<XTaskCompletion> instantCompletions;

    public static NewPaymentFragment newInstance(ArrayList<XTask> tasks, ArrayList<XPayment> payments,ArrayList<XTaskCompletion> instantCompletions) {

        NewPaymentFragment fragment = new NewPaymentFragment();
        fragment.tasks = tasks;
        fragment.payments = payments;
        fragment.completions = fragment.getCompletionsFromTasks(tasks);
        fragment.completions.addAll(instantCompletions);
        fragment.instantCompletions = instantCompletions;
        fragment.initSelectionArray();
        return fragment;
    }

    public static NewPaymentFragment newInstance(ArrayList<XTask> tasks, ArrayList<XPayment> payments,ArrayList<XTaskCompletion> instantCompletions, ArrayList<Boolean> selectionArray) {

        NewPaymentFragment fragment = new NewPaymentFragment();
        fragment.tasks = tasks;
        fragment.payments = payments;
        fragment.completions = fragment.getCompletionsFromTasks(tasks);
        fragment.completions.addAll(instantCompletions);
        fragment.instantCompletions = instantCompletions;
        fragment.selectionArray = selectionArray;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_new_payment, container, false);

        initToolbar();

        initListeners();

        loadCompletions();

        setPaymentDate(Calendar.getInstance().getTime());

        return view;
    }

    private void initToolbar() {

        // Regular toolbar

        Toolbar mToolbar = view.findViewById(R.id.toolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoader.reverseLoading(getContext());
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

                switch (item.getItemId()) {
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
                FragmentLoader.reverseLoading(getContext());
            }
        });

    }

    private void initListeners() {

        view.findViewById(R.id.newpayment_button_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
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
                calendar.set(year, month, dayOfMonth);
                final Date newDate = calendar.getTime();

                setPaymentDate(newDate);

            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void setPaymentDate(final Date newDate) {

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


    private ArrayList<XTaskCompletion> getCompletionsFromTasks(ArrayList<XTask> tasks) {
        ArrayList<XTaskCompletion> completions = new ArrayList<>();

        for (XTask t : tasks) {
            if (t.getCompletions() != null){
                completions.addAll(t.getCompletions());
            }
        }

        // Sort payments based on recency
        Collections.sort(completions, new Comparator<XTaskCompletion>() {
            @Override
            public int compare(XTaskCompletion xTaskCompletion, XTaskCompletion t1) {
                return Long.compare(t1.getDate(), xTaskCompletion.getDate());
            }
        });

        return completions;
    }

    private void loadCompletions() {

        TimelineRecycler mRecyclerView = view.findViewById(R.id.register_payment_completions);

        mAdapter = new CompletionsTimelineAdapter(completions, TimelineAdapter.ALWAYS_SELECTING, new TimelineAdapter.TimelineAdapterListener() {
            @Override
            public void onItemsDataChanged() {

            }

            @Override
            public void onItemClicked(int position, int yPos) {

            }

            @Override
            public void onSelectionChanged(int totalSelected, boolean isSelecting) {

                // Check if all completions have been selected for registering
                boolean allSelected = totalSelected == completions.size();

                // Make "SELECT ALL" action visible if not all payments have been selected
                MenuItem selectAll = mSelectionModeToolbar.getMenu().findItem(R.id.payment_selection_mode_icon_select_all);
                selectAll.setVisible(!allSelected);

                // Make "DESELECT ALL" action visible if all payments have been selected
                MenuItem deselectAll = mSelectionModeToolbar.getMenu().findItem(R.id.payment_selection_mode_icon_deselect_all);
                deselectAll.setVisible(allSelected);

                // Use toolbar to display number of payments selected and their total value
                updateSelectionToolbar(totalSelected);

                // Disable registering if no payments are selected, enable otherwise
                view.findViewById(R.id.newpayment_button_register).setEnabled(totalSelected != 0);

            }
        });

        mAdapter.setSelectionArray(selectionArray);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.startLayoutAnimation();


    }

    private void initSelectionArray() {
        selectionArray = new ArrayList<>(Arrays.asList(new Boolean[completions.size()]));
        Collections.fill(selectionArray, false);
    }

    // Update toolbar title based on number of selected
    private void updateSelectionToolbar(final int totalSelected) {

        String toolbar_title;
        if (totalSelected > 0) {

            // Total value of all tasks
            double total = 0;

            // Loop trough data set and add task value to total value
            for (XTaskCompletion completion : getSelectedCompletions()) {
                total += completion.getTaskIdentity().getFee();
            }

            // Get currency format
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
            String totalString = nf.format(total);

            // Display number of selected payments and their total value
            toolbar_title = String.valueOf(totalSelected) + " " + getString(R.string.selected) + " (" + totalString + ")";
        } else {
            toolbar_title = getString(R.string.select_completions);
        }

        mSelectionModeToolbar.setTitle(toolbar_title);
    }

    // Register new payment for selected payments
    private void registerPayment() {

        // Completions will be archived, so remove them from regular completion list
        for (XTaskCompletion completion : getSelectedCompletions()) {

            // Find task storing completion
            final XTask task = completion.findTask(tasks);

            // Remove completion from this task
            if(task != null){
                task.removeCompletion(completion);
            }else{
                int index = instantCompletions.indexOf(completion);
                instantCompletions.remove(index);
            }


        }

        // Save changes
        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME, tasks,getContext());

        XDataUploader.uploadData(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME,instantCompletions,getContext());

        // Create payment with selected completions
        XPayment newPayment = new XPayment(getSelectedCompletions(), paymentDate.getTime());

        // Add new payment to payments data set
        payments.add(newPayment);

        // Save payments data set changes
        XDataUploader.uploadData(XDataConstants.PAYMENTS_DATA_FILE_NAME, payments, getContext(), new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                // Show the new payment on the timeline
                FragmentLoader.loadFragment(PaymentsFragment.newInstance(payments),getContext(),R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right,false);
            }
        });

    }

    // Use selectionArray to get RecyclerView's currently selected completions
    private ArrayList<XTaskCompletion> getSelectedCompletions() {
        ArrayList<XTaskCompletion> selectedCompletions = new ArrayList<>();

        for (int c = 0; c < completions.size(); c++) {
            if (selectionArray.get(c)) {
                selectedCompletions.add(completions.get(c));
            }
        }
        return selectedCompletions;
    }


}
