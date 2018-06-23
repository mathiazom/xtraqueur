package com.mzom.xtraqueur;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PaymentsFragment extends XFragment {

    private static final String TAG = "XTQ-PaymentsFragment";

    private ArrayList<XPayment> payments;

    private View view;

    private Toolbar mToolbar;

    private Toolbar mSelectionModeToolbar;

    private TimelineAdapter mAdapter;

    public static PaymentsFragment newInstance(ArrayList<XPayment> payments) {

        PaymentsFragment fragment = new PaymentsFragment();
        fragment.payments = payments;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_payments_timeline, container, false);

        initToolbar();

        loadPayments();

        return view;
    }

    private void initToolbar() {
        mToolbar = view.findViewById(R.id.toolbar);

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoader.reverseLoading(getContext());
            }
        });


        // Selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);

        // Hide when not using selection mode
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Add action buttons to toolbar from menu resource
        mSelectionModeToolbar.inflateMenu(R.menu.menu_payments_fragment_selection_mode);

        // Selection icons listener
        mSelectionModeToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.payments_selection_mode_icon_delete:
                        mAdapter.deleteSelectedItems();
                        break;
                }
                return false;
            }
        });

        // Cancel selection icon listener
        mSelectionModeToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.setUniversalItemSelection(false);
            }
        });

    }

    private void loadPayments() {

        // Load recycler view
        TimelineRecycler mRecyclerView = view.findViewById(R.id.timeline_recycler);

        // Sort payments based on recency
        Collections.sort(payments, new Comparator<XPayment>() {
            @Override
            public int compare(XPayment p1, XPayment p2) {
                return Long.compare(p2.getDate(), p1.getDate());
            }
        });

        mAdapter = new PaymentsTimelineAdapter(getContext(), payments,new PaymentsTimelineAdapter.PaymentsTimelineAdapterListener() {
            @Override
            public void onSelectionChanged(int totalSelected, final boolean isSelecting) {

                updateSelectionUI(totalSelected, isSelecting);

            }

            @Override
            public void deletePaymentData(XPayment payment) {

                payments.remove(payment);

            }

            @Override
            public void onItemsDataChanged() {
                XDataUploader.uploadData(XDataConstants.PAYMENTS_DATA_FILE_NAME, payments, getContext());

            }

            @Override
            public void onItemClicked(final int position, int yPos) {

                // Display these payment completions with the PaymentCompletionsFragment
                FragmentLoader.loadFragment(PaymentCompletionsFragment.newInstance(position,payments),getContext());

            }
        });

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.startLayoutAnimation();

    }

    private void updateSelectionUI(final int totalSelected, final boolean isSelecting) {

        // Update toolbar title based on number of selected
        String toolbar_title;
        if (totalSelected == 0 && isSelecting) {
            toolbar_title = getString(R.string.select_payments);
        } else {
            toolbar_title = String.valueOf(totalSelected) + " " + getString(R.string.selected);
        }

        mSelectionModeToolbar.setTitle(toolbar_title);

        if (isSelecting) {
            displaySelectionUI();
            return;
        }

        hideSelectionUI();
    }

    private void displaySelectionUI() {

        // Hide regular toolbar
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.GONE);

        // Make selection mode toolbar visible
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.VISIBLE);
    }

    private void hideSelectionUI() {

        // Hide selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Make regular toolbar visible
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);
    }
}
