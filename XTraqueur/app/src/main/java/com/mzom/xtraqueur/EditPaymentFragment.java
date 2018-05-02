package com.mzom.xtraqueur;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Button;

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

    @NonNull
    @Override
    ConstraintLayout getEditLayout(ConstraintLayout baseEditContainer) {

        // Fragment view
        final ConstraintLayout fragmentView = (ConstraintLayout) getLayoutInflater().inflate(R.layout.fragment_edit_payment,baseEditContainer,false);

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
        return getResources().getColor(R.color.colorAccentDark);
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
