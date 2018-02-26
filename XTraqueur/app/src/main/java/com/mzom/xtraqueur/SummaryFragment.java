package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class SummaryFragment extends Fragment {

    private View view;
    private SummaryFragmentListener summaryFragmentListener;

    private ArrayList<XTask> tasks;

    private final static String TAG = "Xtraqueur-Summary";

    public static SummaryFragment newInstance(ArrayList<XTask> tasks) {
        SummaryFragment fragment = new SummaryFragment();
        fragment.setTasks(tasks);
        return fragment;
    }

    private void setTasks(ArrayList<XTask> tasks) {
        this.tasks = tasks;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        this.view = inflater.inflate(R.layout.fragment_summary, container, false);
        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            summaryFragmentListener = (SummaryFragmentListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SummaryFragmentListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //loadTasks();
        loadSummary();
        super.onActivityCreated(savedInstanceState);
    }

    private void loadSummary() {

        view.findViewById(R.id.summary_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                summaryFragmentListener.onFragmentBackPressed();
            }
        });

        ((LinearLayout) view.findViewById(R.id.summary_task_container)).removeAllViews();

        int total = 0;

        if(tasks == null){
            Log.e(TAG,"Task array is null");
            return;
        }

        if(getContext() == null){
            Log.e(TAG,"Context is null");
            return;
        }

        for (final XTask task : tasks) {
            total += task.getValue();

            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ConstraintLayout task_layout;
            if (layoutInflater != null) {
                task_layout = (ConstraintLayout) layoutInflater.inflate(R.layout.template_summary_task, (ViewGroup) view, false);
            } else {
                return;
            }

            TextView tv_name = task_layout.findViewById(R.id.summary_task_name);
            tv_name.setText(task.getName());

            TextView tv_value = task_layout.findViewById(R.id.summary_task_value);
            String valueString = String.valueOf(task.getValue()) + " " + String.valueOf(Currency.getInstance(Locale.getDefault()).getSymbol());
            tv_value.setText(valueString);

            task_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    summaryFragmentListener.loadEditTaskFragment(task, tasks.indexOf(task));
                }
            });

            ((LinearLayout) view.findViewById(R.id.summary_task_container)).addView(task_layout);
        }

        // Remove all completions button listener
        view.findViewById(R.id.remove_all_completions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllCompletions();
            }
        });

        final String total_value = String.valueOf(total) + " " + String.valueOf(Currency.getInstance(Locale.getDefault()).getSymbol());

        ((TextView) view.findViewById(R.id.summary_total_value)).setText(total_value);
    }

    private void removeAllCompletions() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (XTask task : tasks) {
                            task.removeCompletions();
                        }

                        // GOOGLE DRIVE
                        summaryFragmentListener.updateTasksDataOnDrive(tasks);

                        loadSummary();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        alertDialog.setTitle(getString(R.string.delete_all_completions_title));
        alertDialog.setMessage(getString(R.string.delete_all_completions_message));
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#b71c1c"));
            }
        });
        alertDialog.show();
    }

    interface SummaryFragmentListener {

        void onFragmentBackPressed();

        void loadEditTaskFragment(XTask task, int index);

        void loadTasksFragment();

        void updateTasksDataOnDrive(ArrayList<XTask> tasks);
    }
}
