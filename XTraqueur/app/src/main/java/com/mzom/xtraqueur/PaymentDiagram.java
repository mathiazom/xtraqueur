package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PaymentDiagram extends View {

    private int width;
    private int height;

    private XPayment payment;

    private static final String TAG = "XTQ-PaymentDiagram";


    public PaymentDiagram(Context context) {
        super(context);
    }

    public PaymentDiagram(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PaymentDiagram(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // If using "wrap_content", calculate standard dimensions based on screen dimensions (80% to add some padding)
        width = (MeasureSpec.getSize(widthMeasureSpec) + getPaddingRight() + getPaddingLeft());
        height = (MeasureSpec.getSize(heightMeasureSpec) + getPaddingTop() + getPaddingBottom());

        // Apply measurements according to onMeasure() contract
        setMeasuredDimension(width,height);

    }

    void setPayment(XPayment payment){
        this.payment = payment;

        this.completions = payment.getCompletions();
        this.taskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(completions);
    }

    private ArrayList<XTaskCompletion> completions;

    private ArrayList<XTaskIdentity> taskIdentities;


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(payment != null){
            drawDiagramBars(canvas);
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private void drawDiagramBars(Canvas canvas){

        float leftOffset = getPaddingLeft();

        // Sort taskIdentities based on completion counts
        Collections.sort(taskIdentities, new Comparator<XTaskIdentity>() {

            @Override
            public int compare(XTaskIdentity o1, XTaskIdentity o2) {

                int count1 = XTaskUtilities.getCompletionCountOfTask(completions,o1);
                int count2 = XTaskUtilities.getCompletionCountOfTask(completions,o2);

                return Long.compare(count2, count1);
            }
        });

        for(XTaskIdentity taskIdentity : taskIdentities){

            int completionsCount = XTaskUtilities.getCompletionCountOfTask(completions,taskIdentity);

            float portion = completionsCount/(float)completions.size();

            float w = width * portion;

            paint.setColor(taskIdentity.getColor());

            canvas.drawRect(leftOffset,getPaddingTop(),leftOffset + w,height,paint);

            leftOffset += w;


        }

    }


}
