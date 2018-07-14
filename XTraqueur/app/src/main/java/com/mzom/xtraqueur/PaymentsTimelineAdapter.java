package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

class PaymentsTimelineAdapter extends TimelineAdapter {

    private static final int paymentsViewResId = R.layout.template_payment;

    private final ArrayList<XPayment> payments;

    private final Context context;

    private static final String TAG = "XTQ-PaymentsTimeline";

    PaymentsTimelineAdapter(Context context, ArrayList<XPayment> payments, PaymentsTimelineAdapterListener paymentsTimelineAdapterListener) {
        this(context,payments, false, paymentsTimelineAdapterListener);
    }

    private PaymentsTimelineAdapter(Context context, ArrayList<XPayment> payments, boolean selectionMode, PaymentsTimelineAdapterListener paymentsTimelineAdapterListener) {
        super(paymentsViewResId, selectionMode);

        this.context = context;
        this.payments = payments;
        this.paymentsTimelineAdapterListener = paymentsTimelineAdapterListener;
    }

    private final PaymentsTimelineAdapterListener paymentsTimelineAdapterListener;

    interface PaymentsTimelineAdapterListener extends TimelineAdapterListener {

        void deletePaymentData(XPayment payment);
    }

    private class PaymentViewHolder extends ViewHolder {

        final TextView paymentTitle;
        final PaymentDiagram completionsDiagram;
        //final TextView paymentDate;
        final ImageView selectedMark;

        private PaymentViewHolder(ConstraintLayout itemBaseLayout) {
            super(itemBaseLayout);

            paymentTitle = itemBaseLayout.findViewById(R.id.payment_item_title);
            completionsDiagram = itemBaseLayout.findViewById(R.id.payment_horizontal_diagram_view);
            //paymentDate = itemBaseLayout.findViewById(R.id.payment_item_date);
            selectedMark = itemBaseLayout.findViewById(R.id.payment_item_selected_mark);
        }
    }


    @Override
    void onSelectionChanged(int totalSelected, final boolean isSelecting) {

        paymentsTimelineAdapterListener.onSelectionChanged(totalSelected, isSelecting);

    }

    @Override
    void displayItemData(@NonNull ViewHolder holder, int position) {

        final XPayment payment = payments.get(position);

        PaymentViewHolder paymentViewHolder = (PaymentViewHolder) holder;

        paymentViewHolder.paymentTitle.setText(CurrencyFormatter.formatValue(payment.getPaymentValue()));

        paymentViewHolder.completionsDiagram.setPayment(payment);

    }


    @Override
    void displaySelectionState(@NonNull ViewHolder holder, boolean selected) {

        final PaymentViewHolder paymentViewHolder = (PaymentViewHolder) holder;

        // Selected
        if (selected) {
            // Selected item background
            Drawable itemBackground = holder.itemDataLayout.getBackground();
            itemBackground.setColorFilter(ColorUtilities.getDarkerColor(context.getResources().getColor(R.color.colorPayment)), PorterDuff.Mode.SRC_ATOP);
            holder.itemDataLayout.setBackground(itemBackground);

            // Show selection check mark
            paymentViewHolder.selectedMark.setVisibility(View.VISIBLE);
        }
        // Not selected
        else {
            // Regular item background
            Drawable itemBackground = holder.itemDataLayout.getBackground();
            itemBackground.setColorFilter(context.getResources().getColor(R.color.colorPayment), PorterDuff.Mode.SRC_ATOP);
            holder.itemDataLayout.setBackground(itemBackground);

            // Hide selection check mark
            paymentViewHolder.selectedMark.setVisibility(View.GONE);
        }

    }

    @Override
    void deleteItemData(int position) {

        final XPayment payment = payments.get(position);

        payments.remove(payment);

        paymentsTimelineAdapterListener.deletePaymentData(payment);

    }

    @Override
    void onItemsDataChanged() {

        paymentsTimelineAdapterListener.onItemsDataChanged();

    }

    @Override
    void onItemClicked(int position, int yPos) {

        paymentsTimelineAdapterListener.onItemClicked(position, yPos);

    }

    @Override
    Date getItemDate(int position) {
        return new Date(payments.get(position).getDate());
    }

    @Override
    int getItemColor(int position) {

        return context.getResources().getColor(R.color.colorPayment);
    }

    @Override
    public int getItemCount() {
        if(payments == null) return 0;
        return payments.size();
    }

    @Override
    ViewHolder onCreateViewHolder(ConstraintLayout itemBaseLayout) {
        return new PaymentViewHolder(itemBaseLayout);
    }
}
