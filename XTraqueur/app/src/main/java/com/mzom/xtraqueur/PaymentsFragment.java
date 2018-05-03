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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentsFragment extends XFragment {

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

    private void loadPayments(){

        // Load recycler view
        TimelineRecycler mRecyclerView = view.findViewById(R.id.timeline_recycler);

        // Sort completions based on recency
        Collections.sort(payments, new Comparator<XTaskPayment>() {
            @Override
            public int compare(XTaskPayment p1, XTaskPayment p2) {
                return Long.compare(p2.getPaymentDate(), p1.getPaymentDate());
            }
        });

        mAdapter = new TimelineAdapter(false, new TimelineAdapter.TimelineAdapterListener() {

            @Override
            public void onItemClick(int pos,float y) {
                editPayment(pos,y);
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
                if(payments != null && payments.get(pos) != null && payments.get(pos).getCompletions() != null){
                    title = String.valueOf(payments.get(pos).getCompletions().size()) + " completions";
                }

                /*int color = Color.BLACK;
                if(getContext() != null){
                    color = getResources().getColor(R.color.colorAccentDark);
                }*/

                int redTotal = 0;
                int greenTotal = 0;
                int blueTotal = 0;

                ArrayList<XTaskCompletion> completions = payments.get(pos).getCompletions();

                for(XTaskCompletion completion : completions){

                    int color = completion.getTask().getColor();

                    redTotal += Color.red(color);
                    greenTotal += Color.green(color);
                    blueTotal += Color.blue(color);
                }

                int redMean = redTotal/completions.size();
                int greenMean = greenTotal/completions.size();
                int blueMean = blueTotal/completions.size();

                int color = Color.rgb(redMean,greenMean,blueMean);

                long date = payments.get(pos).getPaymentDate();

                return new TimelineItem(title,color,date);
            }
        });

        mAdapter.setCustomItemView(R.layout.template_timeline_payment_item, R.id.timeline_item_title,R.id.timeline_item_date,R.id.timeline_item_selected_mark,new TimelineAdapter.TimelineCustomViewListener() {
            @Override
            public void displayItemData(@NonNull TimelineAdapter.ViewHolder holder, TimelineItem timelineItem) {

                Log.i(TAG, "displayCustomItemData: " + holder.getAdapterPosition());

                // Item title
                final TextView itemTitleView = holder.itemBaseLayout.findViewById(R.id.timeline_item_title);
                itemTitleView.setText(timelineItem.getTitle());

                // Item date
                final TextView itemDateView = holder.itemBaseLayout.findViewById(R.id.timeline_item_date);
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE d MMM yyyy HH:mm", Locale.getDefault());
                final Date itemDate = new Date(timelineItem.getDate());
                itemDateView.setText(simpleDateFormat.format(itemDate));

                // Item background
                Drawable itemBackground = holder.itemLayout.getBackground();
                itemBackground.setColorFilter(timelineItem.getColor(), PorterDuff.Mode.SRC_ATOP);
                holder.itemLayout.setBackground(itemBackground);


                /*ArrayList<XTaskCompletion> completions = payments.get(holder.getAdapterPosition()).getCompletions();

                float paymentLayoutWidth = holder.itemBaseLayout.getWidth();

                HashMap<XTask, Integer> hashMap = new HashMap<>();

                ArrayList<XTask> tasks = new ArrayList<>();

                for (int c = 0; c < completions.size(); c++) {

                    XTask task = completions.get(c).getTask();

                    for(Map.Entry<XTask, Integer> entry : hashMap.entrySet()){
                        if(task.getName().equals(entry.getKey().getName())){
                            hashMap.put(task, hashMap.get(entry.getKey()) + 1);
                            task = entry.getKey();
                            break;
                        }
                    }

                    if (hashMap.get(task) == null) {
                        hashMap.put(task, 1);
                    }

                }

                Log.i(TAG,"Tasks in payment: " + tasks);

                for (Map.Entry<XTask, Integer> entry : hashMap.entrySet()) {

                    LinearLayout taskBar = new LinearLayout(getContext());

                    float percent = (float) entry.getValue() / hashMap.size();

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (paymentLayoutWidth * percent),
                            100);

                    taskBar.setLayoutParams(lp);

                    taskBar.setBackground(new ColorDrawable(entry.getKey().getColor()));

                    ((LinearLayout) holder.itemLayout.findViewById(R.id.tasks_percents_container)).addView(taskBar);

                }*/

            }

            @Override
            public void markSelection(final TimelineAdapter.ViewHolder holder, final TimelineItem timelineItem, int position, final boolean isSelected) {

                ImageView itemSelectedMark = holder.itemBaseLayout.findViewById(R.id.timeline_item_selected_mark);

                // Selected
                if (isSelected) {
                    // Selected item background
                    Drawable itemBackground = holder.itemLayout.getBackground();
                    itemBackground.setColorFilter(darkenColor(timelineItem.getColor()), PorterDuff.Mode.SRC_ATOP);
                    holder.itemLayout.setBackground(itemBackground);

                    // Show selection check mark
                    itemSelectedMark.setVisibility(View.VISIBLE);
                }
                // Not selected
                else {
                    // Regular item background
                    Drawable itemBackground = holder.itemLayout.getBackground();
                    itemBackground.setColorFilter(timelineItem.getColor(), PorterDuff.Mode.SRC_ATOP);
                    holder.itemLayout.setBackground(itemBackground);

                    // Hide selection check mark
                    itemSelectedMark.setVisibility(View.GONE);
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.startLayoutAnimation();

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

    private void updateSelectionUI(boolean isSelecting){

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

    private void displaySelectionUI(){

        // Hide regular toolbar
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.GONE);

        // Make selection mode toolbar visible
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.VISIBLE);
    }

    private void hideSelectionUI(){

        // Selection array with none selected
        selectionArray = new ArrayList<>(Arrays.asList(new Boolean[payments.size()]));
        Collections.fill(selectionArray,false);

        // Hide selection mode toolbar
        mSelectionModeToolbar = view.findViewById(R.id.toolbar_selection_mode);
        mSelectionModeToolbar.setVisibility(View.GONE);

        // Make regular toolbar visible
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);
    }

    private int getTotalPayments(){
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
