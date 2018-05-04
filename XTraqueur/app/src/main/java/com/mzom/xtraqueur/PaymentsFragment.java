package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class PaymentsFragment extends XFragment {

    private static final String TAG = "XTQ-PaymentsFragment";

    private ArrayList<XTaskPayment> payments;

    private ArrayList<Boolean> selectionArray;

    private View view;

    private Toolbar mToolbar;

    private Toolbar mSelectionModeToolbar;

    private TimelineAdapter mAdapter;

    private PaymentsFragmentListener mPaymentsFragmentListener;

    interface PaymentsFragmentListener {
        void onBackPressed();

        void loadEditPaymentFragment(XTaskPayment payment);

        void updatePaymentsDataOnDrive(ArrayList<XTaskPayment> payments);
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

    static class PaymentViewHolder extends TimelineAdapter.ViewHolder {

        final TextView paymentValueText;
        final String paymentValueString = "";

        PaymentViewHolder(ConstraintLayout itemBase) {
            super(itemBase,
                    (ConstraintLayout) itemBase.findViewById(R.id.item_custom_view),
                    (TextView) itemBase.findViewById(R.id.timeline_item_title),
                    (TextView) itemBase.findViewById(R.id.timeline_item_date),
                    (ImageView) itemBase.findViewById(R.id.timeline_item_selected_mark));

            this.paymentValueText = itemBase.findViewById(R.id.payment_value_text);
        }
    }

    private void loadPayments() {

        // Load recycler view
        TimelineRecycler mRecyclerView = view.findViewById(R.id.timeline_recycler);

        // Sort completions based on recency
        Collections.sort(payments, new Comparator<XTaskPayment>() {
            @Override
            public int compare(XTaskPayment p1, XTaskPayment p2) {
                return Long.compare(p2.getPaymentDate(), p1.getPaymentDate());
            }
        });

        Log.i(TAG,"Regular timelineAdapter");
        mAdapter = new TimelineAdapter(false, new TimelineAdapter.TimelineAdapterListener() {

            @Override
            public void onItemClick(int pos, float y) {
                editPayment(pos, y, getTimelineItem(pos));
            }

            @Override
            public void deleteItemData(int index) {
                payments.remove(index);
            }

            @Override
            public void onDatasetChanged() {
                mPaymentsFragmentListener.updatePaymentsDataOnDrive(payments);
            }

            @Override
            public void onSelectionModeToggled(boolean isSelecting) {
                updateSelectionUI(isSelecting);
            }

            @Override
            public int getItemCount() {
                return getTotalPayments();
            }

            @Override
            public void onSelectionChanged(ArrayList<Boolean> updatedSelectionArray) {

                // Update selection array
                selectionArray = updatedSelectionArray;
            }

            @Override
            public TimelineItem getTimelineItem(int pos) {

                String title = "";
                if (payments != null && payments.get(pos) != null && payments.get(pos).getCompletions() != null) {
                    //title = String.valueOf(payments.get(pos).getCompletions().size()) + " completions";
                    // Get currency format
                    title =  payments.get(pos).getCompletions().size() + " " + getString(R.string.completions);

                }

                /*int redTotal = 0;
                int greenTotal = 0;
                int blueTotal = 0;

                ArrayList<XTaskCompletion> completions = payments.get(pos).getCompletions();

                for (XTaskCompletion completion : completions) {

                    int color = completion.getTask().getColor();

                    redTotal += Color.red(color);
                    greenTotal += Color.green(color);
                    blueTotal += Color.blue(color);
                }

                int redMean = redTotal / completions.size();
                int greenMean = greenTotal / completions.size();
                int blueMean = blueTotal / completions.size();

                int color = Color.rgb(redMean, greenMean, blueMean);*/

                long date = payments.get(pos).getPaymentDate();

                int color = getResources().getColor(R.color.colorGrey);

                return new TimelineItem(title, color, date);
            }
        });

        mAdapter.setCustomViewHolder(new TimelineAdapter.TimelineCustomViewListener() {

            @Override
            public TimelineAdapter.ViewHolder getCustomViewHolder(ConstraintLayout itemBase) {

                // Base item layout (both for custom and default layout)
                final ConstraintLayout layoutContainer = itemBase.findViewById(R.id.item_container);

                // Use custom layout
                final ConstraintLayout customLayout = (ConstraintLayout) LayoutInflater.from(itemBase.getContext()).inflate(R.layout.template_timeline_payment_item, itemBase, false);
                layoutContainer.addView(customLayout);

                return new PaymentViewHolder(itemBase);
            }

            @Override
            public void displayItemData(@NonNull TimelineAdapter.ViewHolder holder, TimelineItem timelineItem) {

                ArrayList<XTaskCompletion> completions = payments.get(holder.getAdapterPosition()).getCompletions();

                SparseIntArray colorsCount = new SparseIntArray();

                for (int c = 0; c < completions.size(); c++) {

                    int color = completions.get(c).getTask().getColor();

                    if (colorsCount.indexOfKey(color) == -1) {
                        colorsCount.put(color, 1);
                    } else {
                        colorsCount.put(color, colorsCount.get(color) + 1);
                    }

                }

                float paymentLayoutWidth = holder.itemBaseLayout.getWidth();

                int topColor = 0;

                for (int i = 0; i < colorsCount.size(); i++) {

                    if(colorsCount.valueAt(i) > colorsCount.get(topColor)){
                        topColor = colorsCount.keyAt(i);
                    }

                    /*LinearLayout taskBar = new LinearLayout(getContext());

                    float percent = (float) colorsCount.valueAt(i) / colorsCount.size();

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (paymentLayoutWidth * percent),
                            100);

                    taskBar.setLayoutParams(lp);

                    taskBar.setBackground(new ColorDrawable(colorsCount.keyAt(i)));

                    ((PaymentViewHolder) holder).tasksDiagramLayout.addView(taskBar);*/
                }

                //((PaymentViewHolder) holder).tasksDiagramLayout.setBackground(new ColorDrawable(topColor));

                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
                ((PaymentViewHolder)holder).paymentValueText.setText(nf.format(payments.get(holder.getAdapterPosition()).getPaymentValue()));
            }

            @Override
            public void markSelection(TimelineAdapter.ViewHolder holder, TimelineItem timelineItem, int position, boolean isSelected) {

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

    private void updateSelectionUI(boolean isSelecting) {

        // Update toolbar title based on number of selected
        int total_selected = mAdapter.getTotalSelected();
        String toolbar_title;
        if (!isSelecting) {
            toolbar_title = getString(R.string.select_completions);
        } else {
            toolbar_title = String.valueOf(total_selected) + " " + getString(R.string.selected);
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

        // Selection array with none selected
        selectionArray = new ArrayList<>(Arrays.asList(new Boolean[payments.size()]));
        Collections.fill(selectionArray, false);

        // Hide selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Make regular toolbar visible
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);
    }

    private int getTotalPayments() {
        return this.payments.size();
    }

    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
