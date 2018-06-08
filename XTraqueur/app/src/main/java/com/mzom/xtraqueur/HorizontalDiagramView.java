package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class HorizontalDiagramView extends LinearLayout {

    private int width;

    private XPayment payment;

    public HorizontalDiagramView(Context context) {
        super(context);
    }

    public HorizontalDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalDiagramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;

        if(payment != null) addDiagramBars();
    }

    void setPayment(XPayment payment){
        this.payment = payment;
        if(width > 0) addDiagramBars();
    }

    void addDiagramBars(){

        this.removeAllViews();

        final ArrayList<XTaskCompletion> completions = payment.getCompletions();
        final ArrayList<XTaskFields> tasksFields = XTaskFieldsUtilities.getTasksFieldsFromCompletions(completions);

        for(XTaskFields taskFields : tasksFields){
            LinearLayout diagramBar = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.template_payment_completions_diagram_bar,this,false);

            int completionsCount = XTaskFieldsUtilities.getCompletionCountOfTask(completions,taskFields);
            Log.i("PaymentDiagram","Count: " + String.valueOf(completionsCount));

            float portion = completionsCount/(float)completions.size();
            Log.i("PaymentDiagram","Portion: " + String.valueOf(portion));

            int width = (int)(this.width*portion);
            Log.i("PaymentDiagram","Width: " + String.valueOf(width));

            diagramBar.setLayoutParams(new LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));

            diagramBar.setBackground(new ColorDrawable(taskFields.getColor()));
            this.addView(diagramBar);
        }

    }
}
