package com.mzom.xtraqueur;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class EditPaymentCompletionFragment extends BaseEditCompletionFragment {

    private XPayment payment;

    private ArrayList<XPayment> payments;

    public static EditPaymentCompletionFragment newInstance(XTaskCompletion completion,XPayment payment, ArrayList<XPayment> payments) {

        EditPaymentCompletionFragment fragment = new EditPaymentCompletionFragment();
        fragment.completion = completion;
        fragment.completionColor = fragment.completion.getTaskIdentity().getColor();
        fragment.tempCompletionDate = completion.getDate();
        fragment.payment = payment;
        fragment.payments = payments;
        return fragment;
    }

    @Override
    void editCompletionDate(XTaskCompletion completion, long editedCompletionDate) {

        int index = payment.getCompletions().indexOf(completion);
        if(index == -1) return;

        completion.setDate(editedCompletionDate);

        payment.getCompletions().set(index,completion);

        XDataUploader.uploadData(XDataConstants.PAYMENTS_DATA_FILE_NAME, payments, getContext(), new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                FragmentLoader.reverseLoading(getContext());
            }
        });

    }

    @Override
    void deleteCompletion(XTaskCompletion completion) {

        int paymentIndex = payments.indexOf(payment);

        payment.deleteCompletion(completion);

        payments.set(paymentIndex, payment);

        XDataUploader.uploadData(XDataConstants.PAYMENTS_DATA_FILE_NAME, payments, getContext(), new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                FragmentLoader.reverseLoading(getContext());
            }
        });

    }
}
