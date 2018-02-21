package com.mzom.xtraqueur;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class XTaskRecyclerAdapter extends RecyclerView.Adapter<XTaskRecyclerAdapter.ViewHolder> {

    private final ArrayList<XTask> mDataset;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout mConstraintLayout;

        ViewHolder(ConstraintLayout v) {
            super(v);
            mConstraintLayout = v;
        }
    }

    XTaskRecyclerAdapter(ArrayList<XTask> dataset) {
        mDataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.template_xtask, parent, false);

        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // TASK OBJECT FROM ARRAYLIST
        XTask mTask = mDataset.get(position);

        // TASK NAME
        TextView xtask_name = holder.mConstraintLayout.findViewById(R.id.xtask_name);
        xtask_name.setText(mTask.getName());

        // TASK COMPLETIONS
        TextView xtask_completions = holder.mConstraintLayout.findViewById(R.id.xtask_completions);
        xtask_completions.setText(String.valueOf(mTask.getCompletions()));

        // TASK COLOR
        ConstraintLayout xtask = holder.mConstraintLayout.findViewById(R.id.xtask);
        Drawable xtask_drawable = xtask.getBackground();
        xtask_drawable.setColorFilter(mTask.getColor(), PorterDuff.Mode.SRC_ATOP);
        xtask.setBackground(xtask_drawable);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
