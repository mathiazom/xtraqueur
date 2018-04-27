package com.mzom.xtraqueur;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class EarningsFragment extends Fragment {

    private static final String TAG = "Xtraqueur-EarningsFrag";

    private View view;

    private ArrayList<XTask> tasks;

    private ArrayList<XTaskPayment> payments;

    private EarningsFragmentListener mEarningsFragmentListener;

    interface EarningsFragmentListener {
        void onBackPressed();

        void loadNewPaymentFragment();

        void loadPaymentsFragment();

        void loadTasksFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_earnings, container, false);

        initToolbar();

        initListeners();

        loadEarnings();

        return view;
    }

    public static EarningsFragment newInstance(ArrayList<XTask> tasks,ArrayList<XTaskPayment> payments) {

        EarningsFragment fragment = new EarningsFragment();
        fragment.tasks = tasks;
        fragment.payments = payments;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mEarningsFragmentListener = (EarningsFragmentListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EarningsFragmentListener");
        }
    }

    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Initialize toolbar field variable
        Toolbar mToolbar = view.findViewById(R.id.toolbar);

        mToolbar.setVisibility(View.VISIBLE);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_earnings_fragment);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEarningsFragmentListener.onBackPressed();
            }
        });
    }

    private void initListeners() {

        // New Payment
        Button button_new_payment = view.findViewById(R.id.button_total_earnings_new_payment);
        button_new_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEarningsFragmentListener.loadNewPaymentFragment();
            }
        });


        // Payment timeline
        Button button_payments_timeline = view.findViewById(R.id.button_total_earnings_payments_timeline);
        button_payments_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mEarningsFragmentListener.loadPaymentsFragment();
            }
        });

    }

    private void loadEarnings() {

        // Show earnings layout
        ConstraintLayout totalEarningsContainer = view.findViewById(R.id.total_earnings_container);
        totalEarningsContainer.setVisibility(View.VISIBLE);

        if (tasks == null) {
            return;
        }

        // Total value of all tasks
        double total = 0;

        // Loop trough data set and add task value to total value
        for (XTask task : tasks) {
            total += task.getValue();
        }

        // Get currency format
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String totalString = nf.format(total);

        // Display total
        TextView total_earning_text = view.findViewById(R.id.total_earnings_value);
        total_earning_text.setText(totalString);

        Button button_new_payment = view.findViewById(R.id.button_total_earnings_new_payment);
        button_new_payment.setEnabled(total != 0);

        Button button_payments_timeline = view.findViewById(R.id.button_total_earnings_payments_timeline);
        button_payments_timeline.setEnabled(payments.size() > 0);

    }
}
