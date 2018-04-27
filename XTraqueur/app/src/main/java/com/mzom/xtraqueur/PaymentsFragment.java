package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import java.util.ArrayList;

public class PaymentsFragment extends Fragment {

    private static final String TAG = "XTQ-PaymentsFragment";

    private ArrayList<XTaskPayment> payments;

    private ArrayList<Boolean> selectionArray;

    private View view;

    private Toolbar mToolbar;

    private Toolbar mSelectionModeToolbar;

    private TimelineAdapter mAdapter;

    private PaymentsFragmentListener mPaymentsFragmentListener;

    interface PaymentsFragmentListener{
        void onBackPressed();

        void loadEditPaymentFragment(XTaskPayment payment);

        void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mPaymentsFragmentListener = (PaymentsFragmentListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement PaymentsFragmentListener");
        }
    }

    public static PaymentsFragment newInstance(ArrayList<XTaskPayment> payments) {

        PaymentsFragment fragment = new PaymentsFragment();
        fragment.payments = payments;
        fragment.selectionArray = new ArrayList<>(payments.size());
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_payments_timeline,container,false);

        initToolbar();

        loadPayments();

        return view;
    }

    private void initToolbar(){
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

                switch (item.getItemId()){
                    case R.id.payments_selection_mode_icon_delete:
                        deleteSelectedPayments();
                        break;
                }
                return false;
            }
        });

        // Cancel selection icon listener
        mSelectionModeToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableSelectionMode();
            }
        });

    }

    private void loadPayments(){

        Log.i(TAG,payments.toString());

        // Load recycler view
        RecyclerView mRecyclerView = view.findViewById(R.id.timeline_recycler);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TimelineAdapter(false, new TimelineAdapter.TimelineAdapterListener() {

            @Override
            public void onItemClick(int pos,float y) {
                editPayment(pos,y);
            }

            @Override
            public int getItemCount() {
                return getTotalPayments();
            }

            @Override
            public boolean onSelectionChanged(ArrayList<Boolean> updatedSelectionArray) {

                // Update selection array
                selectionArray = updatedSelectionArray;

                updateSelectionToolbar();

                // Enable/disable selection mode based on number of selections
                if(mAdapter.getTotalSelected() > 0){
                    enableSelectionMode();
                    return true;
                }else{
                    disableSelectionMode();
                    return false;
                }
            }

            @Override
            public TimelineItem getTimelineItem(int pos) {

                String title = "";
                if(payments != null && payments.get(pos) != null && payments.get(pos).getCompletions() != null){
                    title = String.valueOf(payments.get(pos).getCompletions().size()) + " completions";
                }

                int color = Color.BLACK;
                if(getContext() != null){
                    color = getResources().getColor(R.color.colorAccentDark);
                }

                long date = payments.get(pos).getPaymentDate();

                return new TimelineItem(title,color,date);
            }
        });

        mRecyclerView.setAdapter(mAdapter);

    }

    // Animate launch of EditTaskFragment with the selected task
    private void editPayment(final int pos, float y) {

        // View that acts as a drawable with task color expanding and covering the whole screen
        final View scaleView = new View(getContext());

        // Add elevation to make scaleView appear over all other views and cover the whole screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scaleView.setElevation(100);
        }

        // Set drawable color to task color
        scaleView.setBackground(new ColorDrawable(getResources().getColor(R.color.colorAccentDark)));

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

    private void deleteSelectedPayments(){

        ArrayList<XTaskPayment> toRemove = new ArrayList<>();

        for(int p = 0;p<payments.size();p++){
            if(selectionArray.get(p)){
                toRemove.add(payments.get(p));
                mAdapter.notifyItemRemoved(p);
            }
        }

        payments.removeAll(toRemove);

        mPaymentsFragmentListener.updatePaymentsDataOnDrive(payments);

        disableSelectionMode();

    }

    private void updateSelectionToolbar(){

        // Update toolbar title based on number of selected
        int total_selected = mAdapter.getTotalSelected();
        String toolbar_title;
        if(total_selected == 0){
            toolbar_title = getString(R.string.select_completions);
        }else{
            toolbar_title = String.valueOf(total_selected) + " " + getString(R.string.selected);
        }

        mSelectionModeToolbar.setTitle(toolbar_title);
    }

    private void enableSelectionMode(){

        // Hide regular toolbar
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.GONE);

        // Make selection mode toolbar visible
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.VISIBLE);

        updateSelectionToolbar();
    }

    private void disableSelectionMode(){
        selectionArray = new ArrayList<>(payments.size());

        // Hide selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Make regular toolbar visible
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);

        loadPayments();
    }

    private int getTotalPayments(){
        return this.payments.size();
    }
}
