package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PieView extends View {

    private static final String TAG = "XTQ-PieView";

    private ArrayList<XTask> tasks;

    private OnPieTaskClickedListener pieTaskClickedListener;

    int total = 0;

    interface OnPieTaskClickedListener{
        void onTaskClicked(XTask clickedTask);
    }

    public PieView(Context context) {
        super(context);
    }

    public PieView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setOnTaskClickListener(@NonNull OnPieTaskClickedListener pieTaskClickedListener){
        this.pieTaskClickedListener = pieTaskClickedListener;
    }

    void setTasks(ArrayList<XTask> tasks){

        final ArrayList<XTask> tasksCopy = (ArrayList<XTask>) tasks.clone();

        Collections.sort(tasksCopy, new Comparator<XTask>() {
            @Override
            public int compare(XTask o1, XTask o2) {
                return Integer.compare(o2.getCompletionsCount(),o1.getCompletionsCount());
            }
        });

        this.tasks = tasksCopy;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        total = getTotal();

        if(total == 0){
            setMeasuredDimension(0,0);
            return;
        }

        int desiredWidth = 1000 + getPaddingLeft() + getPaddingRight();
        int desiredHeight = 1000 + getPaddingTop() + getPaddingBottom();

        int width = measureDimension(desiredWidth,widthMeasureSpec);
        int height = measureDimension(desiredHeight,heightMeasureSpec);

        int dim = Math.min(width,height);

        setMeasuredDimension(dim,dim);

    }

    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        if (result < desiredSize){
            Log.e(TAG, "The view is too small, the content might get cut");
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (total > 0) {
            drawPieChart(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        performClick();

        // Get touch coordinates
        float x = event.getX();
        float y = event.getY();

        // Check if touch is inside pie
        float centerX = pieOval.centerX();
        float centerY = pieOval.centerY();
        double dist = Math.hypot(x-centerX,y-centerY);
        if(dist > pieOval.width()/2){
            return super.onTouchEvent(event);
        }

        // Get touched task
        XTask touchTask = getTouchPieTask(x,y);
        if(touchTask == null){
            return super.onTouchEvent(event);
        }

        // Notify listener
        if(pieTaskClickedListener != null){
            pieTaskClickedListener.onTaskClicked(touchTask);
        }

        return super.onTouchEvent(event);
    }

    private XTask getTouchPieTask(float x, float y){

        double angle = getTouchPieAngle(x,y);

        double compEq = total * (angle/360);

        int t = 0;

        for(XTask task : tasks){
            t += task.getCompletionsCount();
            if(t > compEq){
                return task;
            }
        }

        return null;
    }

    private double getTouchPieAngle(float x, float y){

        float centerX = pieOval.centerX();
        float centerY = pieOval.centerY();

        float distX = x-centerX;
        float distY = y-centerY;

        double angle = Math.toDegrees(Math.atan2(distY,distX));

        if(angle <= 0){
            angle += 360;
        }

        return angle;

    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private RectF pieOval;

    private void drawPieChart(Canvas canvas){

        pieOval = new RectF(getPaddingLeft(), getPaddingTop(),getWidth()-getPaddingRight(),getHeight()-getPaddingBottom());

        float startAngle = 0;

        for(XTask task : tasks){
            float sweepAngle = (360*getTaskProportion(task));

            if(startAngle + sweepAngle > 360){
                sweepAngle = 360-startAngle;
            }

            Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            colorPaint.setColor(task.getColor());

            canvas.drawArc(pieOval,startAngle,sweepAngle,true, colorPaint);

            startAngle += sweepAngle;
        }

    }

    private int getTotal(){

        if(tasks == null) return 0;

        int t = 0;

        for(XTask task : tasks){
            t += task.getCompletionsCount();
        }

        return t;

    }

    private float getTaskProportion(XTask task){

        if(total == 0) return 0;

        return (float)task.getCompletionsCount()/total;

    }
}
