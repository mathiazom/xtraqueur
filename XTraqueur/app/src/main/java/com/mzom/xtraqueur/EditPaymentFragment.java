package com.mzom.xtraqueur;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/*

 * Fragment used to show tasks(with payments count) of a payment,
   as well as editing the payments data.

 * At the moment only date and time of payment can be changed

*/

public class EditPaymentFragment extends BaseEditFragment {

    private ArrayList<XPayment> payments;

    private XPayment payment;

    private long tempPaymentDate;

    private TextInputEditText paymentDateEdit;
    private TextInputEditText paymentTimeEdit;

    public static EditPaymentFragment newInstance(XPayment payment, ArrayList<XPayment> payments) {
        EditPaymentFragment fragment = new EditPaymentFragment();
        fragment.payment = payment;
        fragment.tempPaymentDate = payment.getDate();
        fragment.payments = payments;
        return fragment;
    }

    // Return layout to use inside edit fragment to make changes to payment
    @NonNull
    @Override
    ConstraintLayout getEditLayout(ConstraintLayout baseEditContainer) {

        // Fragment view
        final ConstraintLayout fragmentView = (ConstraintLayout) getLayoutInflater().inflate(R.layout.fragment_edit_payment,baseEditContainer,false);

        // Container view to hold all tasks that is part of this payment
        final LinearLayout paymentTasksContainer = fragmentView.findViewById(R.id.payment_tasks_container);

        // Represent each task from payment with task payments count, color and name
        for(XTaskFields taskFields : XTaskFieldsUtilities.getTasksFieldsFromCompletions(payment.getCompletions())){

            // Item layout for this task
            final ConstraintLayout taskLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_payment_task_item,fragmentView,false);
            Drawable layoutBackground = taskLayout.getBackground();
            layoutBackground.setColorFilter(taskFields.getColor(), PorterDuff.Mode.SRC_ATOP);
            layoutBackground.setAlpha(150);
            taskLayout.setBackground(layoutBackground);

            // Task name
            TextView taskTitle = taskLayout.findViewById(R.id.payment_task_title);
            taskTitle.setText(taskFields.getName());

            // Circle with task color as container for tasks payments count
            ConstraintLayout colorMarker = taskLayout.findViewById(R.id.payment_task_color_marker);
            Drawable markerBackground = colorMarker.getBackground();
            markerBackground.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
            colorMarker.setBackground(markerBackground);

            // Number of payments for this task
            TextView taskCount = taskLayout.findViewById(R.id.payment_task_completions_total);
            int completionCount = XTaskFieldsUtilities.getCompletionCountOfTask(payment.getCompletions(),taskFields);
            taskCount.setText(String.valueOf(completionCount));
            taskCount.setTextColor(taskFields.getColor());
            taskCount.setAlpha(0.80f);

            // Add to tasks container/overview
            paymentTasksContainer.addView(taskLayout);

        }

        /*for(XTaskCompletion completion : payment.getCompletions()){

            // Item layout for this task
            final ConstraintLayout taskLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_payment_task_item,fragmentView,false);
            Drawable layoutBackground = taskLayout.getBackground();
            layoutBackground.setColorFilter(completion.getTask().getColor(), PorterDuff.Mode.SRC_ATOP);
            layoutBackground.setAlpha(150);
            taskLayout.setBackground(layoutBackground);

            // Task name
            TextView paymentTitle = taskLayout.findViewById(R.id.payment_task_title);
            paymentTitle.setText(completion.getTask().getName());

            // Circle with task color as container for tasks payments count
            ConstraintLayout colorMarker = taskLayout.findViewById(R.id.payment_task_color_marker);
            Drawable markerBackground = colorMarker.getBackground();
            markerBackground.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
            colorMarker.setBackground(markerBackground);

           *//* // Number of payments for this task
            TextView taskCount = taskLayout.findViewById(R.id.payment_task_completions_total);
            taskCount.setText(String.valueOf(payment.completionsOfTask(task)));
            taskCount.setTextColor(task.getColor());
            taskCount.setAlpha(0.80f);*//*

            // Add to tasks container/overview
            paymentTasksContainer.addView(taskLayout);

        }*/

        // Current date and time of payment
        final Date paymentDate = new Date(tempPaymentDate);

        // Payment date editing
        paymentDateEdit = fragmentView.findViewById(R.id.edit_payment_date);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMM yyyy", Locale.getDefault());
        paymentDateEdit.setText(dateFormat.format(paymentDate));
        paymentDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDatePicker(paymentDate);
            }
        });

        // Payment time editing
        paymentTimeEdit = fragmentView.findViewById(R.id.edit_payment_time);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        paymentTimeEdit.setText(timeFormat.format(paymentDate));
        paymentTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTimePicker(paymentDate);
            }
        });


        Button showCompletions = fragmentView.findViewById(R.id.button_payment_completions);
        showCompletions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseEditListener().loadCompletionsFragment(payment.getCompletions());
            }
        });

        // Mark delete button for base edit fragment (automatically adds delete listeners etc.)
        Button deleteButton = fragmentView.findViewById(R.id.button_delete_payment);
        setItemDeleteButton(deleteButton,getString(R.string.delete_payment_confirmation_title),getString(R.string.delete_payment_confirmation_message));

        // Returning will attach fragment view to base view (BaseEditFragment)
        return fragmentView;
    }

    @Override
    int getItemColor() {
        return getResources().getColor(R.color.colorGrey);
    }

    @Override
    boolean itemDataIsChanged() {

        // Check if payment data has changed (only time of payment can be changed atm)
        return tempPaymentDate != payment.getDate();

    }

    @Override
    void saveChanges() {

        // Apply changes to payment (only time of payment atm)
        payment.setDate(tempPaymentDate);

        // Add to payments array
        payments.set(payments.indexOf(payment),payment);

        // Update payments data on drive
        getBaseEditListener().updatePaymentsDataOnDrive(payments, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                returnToItemsList();
            }
        });

    }

    @Override
    void deleteItem() {

        // Remove payment from payments array
        payments.remove(payment);

        // Update payments data on drive
        getBaseEditListener().updatePaymentsDataOnDrive(payments, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                returnToItemsList();
            }
        });

    }

    @Override
    void onDatePicked(Date newDate) {

        // Temporarily save selected date and time (not yet permanently saved)
        tempPaymentDate = newDate.getTime();


        // Update layout to display selected date

        // Display date
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMM yyyy", Locale.getDefault());
        paymentDateEdit.setText(dateFormat.format(newDate));

        // Display time (hour and minute)
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        paymentTimeEdit.setText(timeFormat.format(newDate));
    }
}
