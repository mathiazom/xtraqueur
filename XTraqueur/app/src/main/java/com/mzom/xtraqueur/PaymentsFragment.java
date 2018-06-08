package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

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

    private PaymentsFragmentListener mPaymentsFragmentListener;

    interface PaymentsFragmentListener {
        void onBackPressed();

        void loadEditPaymentFragment(XPayment payment);

        void loadCompletionsFragment(ArrayList<XTaskCompletion> completions);

        void updatePaymentsDataOnDrive(ArrayList<XPayment> payments);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mPaymentsFragmentListener = (PaymentsFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PaymentsFragmentListener");
        }
    }

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
                mPaymentsFragmentListener.onBackPressed();
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

                mPaymentsFragmentListener.updatePaymentsDataOnDrive(payments);

            }

            @Override
            public void onItemClicked(int position, int yPos) {

                ArrayList<XTaskCompletion> completions = payments.get(position).getCompletions();

                mPaymentsFragmentListener.loadCompletionsFragment(completions);

            }
        });

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.startLayoutAnimation();

    }

    // Animate launch of EditTaskFragment with the selected task
    private void editPayment(final int pos, float y, TimelineItem timelineItem) {

        // View that acts as a drawable with task color expanding and covering the whole screen
        final View scaleView = new View(getContext());

        // Add elevation to make scaleView appear over all other views and cover the whole screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scaleView.setElevation(100);
        }

        // Set drawable color to task color
        scaleView.setBackground(new ColorDrawable(timelineItem.getColor()));

        // Add view to the fragment root view (get access to ViewGroup method addView() by casting to ConstraintLayout)
        ((ConstraintLayout) view).addView(scaleView);

        // Expand animation to fill the whole screen with task color
        final ScaleAnimation expand_animation = new ScaleAnimation(1f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.ABSOLUTE, y);

        expand_animation.setDuration(200);

        // Keep the transformation after animation has finished
        expand_animation.setFillAfter(true);

        expand_animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Once the expand animation has finished, tell MainActivity to switch to an EditTaskFragment in the FrameLayout
                mPaymentsFragmentListener.loadEditPaymentFragment(payments.get(pos));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        scaleView.startAnimation(expand_animation);
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
