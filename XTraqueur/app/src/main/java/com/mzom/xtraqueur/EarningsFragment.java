package com.mzom.xtraqueur;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class EarningsFragment extends XFragment {

    private static final String TAG = "Xtraqueur-EarningsFrag";

    private View view;

    private ArrayList<XTask> tasks;

    private ArrayList<XPayment> payments;

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


    public static EarningsFragment newInstance(ArrayList<XTask> tasks, ArrayList<XPayment> payments) {

        EarningsFragment fragment = new EarningsFragment();
        fragment.tasks = tasks;
        fragment.payments = payments;
        return fragment;
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
                FragmentLoader.reverseLoading(getContext());
            }
        });
    }

    private void initListeners() {

        // New Payment
        Button button_new_payment = view.findViewById(R.id.button_total_earnings_new_payment);
        button_new_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoader.loadFragment(NewPaymentFragment.newInstance(tasks,payments),getContext(), R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top, true);
            }
        });


        // Payment timeline
        Button button_payments_timeline = view.findViewById(R.id.button_total_earnings_payments_timeline);
        button_payments_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoader.loadFragment(PaymentsFragment.newInstance(payments),getContext(),R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right,true);
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
