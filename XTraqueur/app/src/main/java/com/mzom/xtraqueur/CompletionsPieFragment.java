package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CompletionsPieFragment extends XFragment {

    // Fragment main views
    private View view;

    private Toolbar mToolbar;

    private ArrayList<XTask> tasks;

    private CompletionsPieFragmentListener mCompletionsFragmentListener;

    interface CompletionsPieFragmentListener{

        void onBackPressed();

        void loadCompletionsFragment(ArrayList<XTask> tasks, XTask filterTask);

    }

    public static CompletionsPieFragment newInstance(ArrayList<XTask> tasks) {

        CompletionsPieFragment fragment = new CompletionsPieFragment();
        fragment.tasks = tasks;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_completions_pie, container, false);

        initToolbar();

        initCompletionsPie();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCompletionsFragmentListener = (CompletionsPieFragmentListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CompletionsPieFragmentListener");
        }
    }

    // Initialize toolbar field variable and add action buttons with listeners
    private void initToolbar() {

        // Initialize regular toolbar field variable
        mToolbar = view.findViewById(R.id.toolbar);

        // Add action buttons to toolbar from menu resource
        mToolbar.inflateMenu(R.menu.menu_completions_pie_fragment);

        // Menu items
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {


                }
                return false;
            }
        });

        // Set back button as toolbar navigation icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCompletionsFragmentListener.onBackPressed();
            }
        });

    }


    private void initCompletionsPie(){

        // Pie view init
        PieView pieView = view.findViewById(R.id.pieView);
        pieView.setTasks(tasks);
        pieView.setOnTaskClickListener(new PieView.OnPieTaskClickedListener() {
            @Override
            public void onTaskClicked(XTask clickedTask) {

                // Pie legend init
                LinearLayout pieLegend = view.findViewById(R.id.pieViewLegend);
                pieLegend.removeAllViews();

                for(XTask task : tasks){

                    final ConstraintLayout legendItem = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_pie_view_legend,pieLegend,false);

                    if(task == clickedTask){
                        Drawable legendItemBackground = legendItem.getBackground();
                        legendItemBackground.setColorFilter(darkenColor(task.getColor()), PorterDuff.Mode.SRC_ATOP);
                        legendItem.setBackground(legendItemBackground);
                    }

                    final LinearLayout circle = legendItem.findViewById(R.id.pie_view_legend_circle);
                    final Drawable circleDrawable = circle.getBackground();
                    circleDrawable.setColorFilter(task.getColor(), PorterDuff.Mode.SRC_ATOP);
                    circle.setBackground(circleDrawable);

                    final TextView title = legendItem.findViewById(R.id.pie_view_legend_title);
                    title.setText(task.getName());

                    final TextView value = legendItem.findViewById(R.id.pie_view_legend_value);
                    NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
                    String valueString = nf.format(task.getValue());
                    value.setText(valueString);

                    pieLegend.addView(legendItem);
                }

                // Update scrollView to account for dynamically added views
                ScrollView scroll = view.findViewById(R.id.completions_pie_scroll);
                scroll.computeScroll();
            }
        });

        // Pie legend init
        LinearLayout pieLegend = view.findViewById(R.id.pieViewLegend);
        for(XTask task : tasks){

            final ConstraintLayout legendItem = (ConstraintLayout) getLayoutInflater().inflate(R.layout.template_pie_view_legend,pieLegend,false);

            final LinearLayout circle = legendItem.findViewById(R.id.pie_view_legend_circle);
            final Drawable circleDrawable = circle.getBackground();
            circleDrawable.setColorFilter(task.getColor(), PorterDuff.Mode.SRC_ATOP);
            circle.setBackground(circleDrawable);

            final TextView title = legendItem.findViewById(R.id.pie_view_legend_title);
            title.setText(task.getName());

            final TextView value = legendItem.findViewById(R.id.pie_view_legend_value);
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
            String valueString = nf.format(task.getValue());
            value.setText(valueString);

            pieLegend.addView(legendItem);
        }

        // Update scrollView to account for dynamically added views
        ScrollView scroll = view.findViewById(R.id.completions_pie_scroll);
        scroll.computeScroll();
    }

    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }


}
