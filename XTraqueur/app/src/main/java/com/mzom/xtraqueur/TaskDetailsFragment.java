package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Mathias on 26.02.2018.
 */

public class TaskDetailsFragment extends Fragment {

    private View view;

    private XTask mTask;

    TaskDetailsFragmentListener mTaskDetailsFragmentListener;

    interface TaskDetailsFragmentListener{

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mTaskDetailsFragmentListener = (TaskDetailsFragmentListener) context;
        }catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TaskDetailsFragmentListener");
        }
    }

    public static TaskDetailsFragment newInstance(XTask task) {

        TaskDetailsFragment fragment = new TaskDetailsFragment();
        fragment.mTask = task;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_task_details,container,false);

        loadTaskDetails();

        return view;
    }

    private void loadTaskDetails(){

        /*// Task color
        ConstraintLayout container = (ConstraintLayout) view;
        container.setBackground(new ColorDrawable(mTask.getColor()));

        // Task name
        TextView task_name = view.findViewById(R.id.task_details_task_name);
        task_name.setText(mTask.getName());

        // Task completions count
        TextView task_completions_count = view.findViewById(R.id.task_details_task_completions_count);
        task_completions_count.setText(String.valueOf(mTask.getCompletions()));

        // Task value view
        TextView task_value = view.findViewById(R.id.task_details_task_value);
        String value = String.valueOf(mTask.getValue()) + " " + String.valueOf(Currency.getInstance(Locale.getDefault()).getSymbol());
        task_value.setText(value);

        // Latest task completion view
        TextView task_latest_completion = view.findViewById(R.id.task_details_task_latest_completion);
        ArrayList<Long> completions_list = mTask.getCompletionsList();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String latest = simpleDateFormat.format(new Date(completions_list.get(completions_list.size()-1)));

        task_latest_completion.setText(latest);*/
    }
}
