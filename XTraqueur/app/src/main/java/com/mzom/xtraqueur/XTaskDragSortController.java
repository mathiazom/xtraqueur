package com.mzom.xtraqueur;

import android.view.MotionEvent;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

// Class that gives control to item clicks of a DragSortListView
class XTaskDragSortController extends DragSortController {

    private DragSortListView mDragSortListView;
    private XTaskDragSortControllerListener mXTaskDragSortControllerListener;

    interface XTaskDragSortControllerListener {
        void onEditTask(final int index, float y);
    }

    XTaskDragSortController(DragSortListView dslv, XTaskDragSortControllerListener xTaskDragSortControllerListener) {
        super(dslv);

        // Get DragSortListView instance
        mDragSortListView = dslv;

        // Get listener to communicate with TasksFragment
        mXTaskDragSortControllerListener = xTaskDragSortControllerListener;
    }

    // Handle ListView item click when not dragging
    @Override
    public boolean onSingleTapUp(MotionEvent ev) {
        // Get coordinates of click
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        // Get position of the clicked ListView item
        int pos = mDragSortListView.pointToPosition(x, y);

        // Launch EdiTaskFragment with the task that the item represents
        mXTaskDragSortControllerListener.onEditTask(pos, y);

        return super.onSingleTapUp(ev);
    }
}