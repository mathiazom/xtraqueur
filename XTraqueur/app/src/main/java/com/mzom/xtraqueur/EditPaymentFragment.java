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

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EditPaymentFragment extends BaseEditFragment {

    private ArrayList<XTaskPayment> payments;

    private TextInputEditText paymentDateEdit;
    private TextInputEditText paymentTimeEdit;

    private long tempPaymentDate;

    private XTaskPayment payment;

    public static EditPaymentFragment newInstance(XTaskPayment payment, ArrayList<XTaskPayment> payments) {
        EditPaymentFragment fragment = new EditPaymentFragment();
        fragment.payment = payment;
        fragment.tempPaymentDate = payment.getPaymentDate();
        fragment.payments = payments;
        return fragment;
    }

    private int totalCompletionsFromTask(XTaskPayment payment, XTask task){

        int total = 0;

        for(XTaskCompletion completion : payment.getCompletions()){

            if(completion.getTask().getName().equals(task.getName())){
                total += 1;
            }

        }

        return total;

    }

    private ArrayList<XTask> tasksFromPayment(XTaskPayment payment){

        ArrayList<XTask> tasks = new ArrayList<>();

        for(XTaskCompletion completion : payment.getCompletions()){

            boolean notAdded = true;

            for(XTask task : tasks){
                if(task.getName().equals(completion.getTask().getName())){
                    notAdded = false;
                    break;
                }
            }

            if(notAdded){
                tasks.add(completion.getTask());
            }

        }

        return tasks;

    }

    @NonNull
    @Override
    ConstraintLayout getEditLayout(ConstraintLayout baseEditContainer) {

        // Fragment view
        final ConstraintLayout fragmentView = (ConstraintLayout) getLayoutInflater().inflate(R.layout.fragment_edit_payment,baseEditContainer,false);

        final LinearLayout paymentCompletionsContainer = fragmentView.findViewById(R.id.payment_completions_container);

        for(XTask task : tasksFromPayment(payment)){

            final ConstraintLayout taskLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_payment_task_item,fragmentView,false);
            Drawable layoutBackround = taskLayout.getBackground();
            layoutBackround.setColorFilter(task.getColor(), PorterDuff.Mode.SRC_ATOP);
            layoutBackround.setAlpha(150);
            taskLayout.setBackground(layoutBackround);

            TextView completionTitle = taskLayout.findViewById(R.id.payment_task_title);
            completionTitle.setText(task.getName());

            ConstraintLayout colorMarker = taskLayout.findViewById(R.id.payment_task_color_marker);
            Drawable markerBackground = colorMarker.getBackground();
            markerBackground.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
            colorMarker.setBackground(markerBackground);

            TextView taskCount = taskLayout.findViewById(R.id.payment_task_completions_total);
            taskCount.setText(String.valueOf(totalCompletionsFromTask(payment,task)));
            taskCount.setTextColor(task.getColor());
            taskCount.setAlpha(0.80f);

            paymentCompletionsContainer.addView(taskLayout);

        }



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

        // Tell base fragment that this fragment uses a item delete button
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

        // Check if payment date long has been changed
        return tempPaymentDate != payment.getPaymentDate();

    }

    @Override
    void saveChanges() {

        // Apply changes to payment
        payment.setPaymentDate(tempPaymentDate);

        // Add to payments array
        payments.set(payments.indexOf(payment),payment);

        // Update payments data on drive
        getBaseEditListener().updatePaymentsDataOnDrive(payments);

    }

    @Override
    void deleteItem() {

        // Remove payment from payments array
        payments.remove(payment);

        // Update payments data on drive
        getBaseEditListener().updatePaymentsDataOnDrive(payments);

    }

    @Override
    void onDatePicked(Date newDate) {
        tempPaymentDate = newDate.getTime();

        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMM yyyy", Locale.getDefault());
        paymentDateEdit.setText(dateFormat.format(newDate));

        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        paymentTimeEdit.setText(timeFormat.format(newDate));
    }
}
