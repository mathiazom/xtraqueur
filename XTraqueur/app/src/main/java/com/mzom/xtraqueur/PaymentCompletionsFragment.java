package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class PaymentCompletionsFragment extends BaseCompletionsFragment {

    private XPayment payment;

    private int paymentIndex;

    private ArrayList<XPayment> payments;

    static PaymentCompletionsFragment newInstance(int paymentIndex, ArrayList<XPayment> payments) {

        final PaymentCompletionsFragment fragment = new PaymentCompletionsFragment();

        fragment.paymentIndex = paymentIndex;
        fragment.payment = payments.get(paymentIndex);
        fragment.payments = payments;

        final ArrayList<XTaskCompletion> allCompletions = payments.get(paymentIndex).getCompletions();
        fragment.setAllCompletions(allCompletions);

        final ArrayList<XTaskIdentity> allTaskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(allCompletions);
        fragment.setAllTaskIdentities(allTaskIdentities);

        return fragment;
    }

    @Override
    void deleteCompletion(XTaskCompletion completion, final OnSuccessListener<DriveFile> onSuccessListener){

        // Delete completion from payment completions
        if (!payment.deleteCompletion(completion)) return;

        // Update changes in payments data set if deletion was successful
        payments.set(paymentIndex, payment);

        final ArrayList<XTaskCompletion> allCompletions = payments.get(paymentIndex).getCompletions();
        setAllCompletions(allCompletions);

        final ArrayList<XTaskIdentity> allTaskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(allCompletions);
        setAllTaskIdentities(allTaskIdentities);

        loadCompletions();

        XDataUploader.uploadData(XDataConstants.PAYMENTS_DATA_FILE_NAME, payments, getContext(), onSuccessListener, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    @Override
    void editCompletionDate(XTaskCompletion completion, long editedCompletionDate, OnSuccessListener<DriveFile> onSuccessListener){

        int index = payment.getCompletions().indexOf(completion);
        if(index == -1) return;

        completion.setDate(editedCompletionDate);

        payment.getCompletions().set(index,completion);

        XDataUploader.uploadData(XDataConstants.PAYMENTS_DATA_FILE_NAME, payments, getContext(), onSuccessListener, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    int getSelectionMenuResId() {
        return R.menu.menu_payment_completions_fragment_selection_mode;
    }

    @Override
    Toolbar.OnMenuItemClickListener getSelectionMenuItemClickListener() {
        return new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.timeline_selection_mode_icon_delete:

                        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                                .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getAdapter().deleteSelectedItems();
                                        hideSelectionUI();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .create();

                        alertDialog.setTitle(getString(R.string.delete_completions_confirmation_title));
                        alertDialog.setMessage(getString(R.string.delete_completions_confirmation_message));
                        alertDialog.show();

                        break;
                }

                return false;
            }
        };
    }

    @Override
    void onDataSetChanged(OnSuccessListener<DriveFile> onSuccessListener) {

        XDataUploader.uploadData(XDataConstants.PAYMENTS_DATA_FILE_NAME, payments, getContext(), onSuccessListener, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
}
