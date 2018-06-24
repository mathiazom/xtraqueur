package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class TasksCompletionsFragment extends BaseCompletionsFragment {

    private ArrayList<XTask> tasks;

    private ArrayList<XPayment> payments;

    public static TasksCompletionsFragment newInstance(ArrayList<XTask> tasks,ArrayList<XPayment> payments) {
        return newInstance(tasks,payments,null);
    }

    public static TasksCompletionsFragment newInstance(ArrayList<XTask> tasks, ArrayList<XPayment> payments,XTaskIdentity taskIdentity) {

        TasksCompletionsFragment fragment = new TasksCompletionsFragment();

        fragment.tasks = tasks;
        fragment.payments = payments;

        ArrayList<XTaskCompletion> completions = XTaskUtilities.getCompletionsFromTasks(tasks);
        fragment.setAllCompletions(completions);

        ArrayList<XTaskIdentity> taskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(completions);
        fragment.setAllTaskIdentities(taskIdentities);

        fragment.setFilterTaskIdentity(taskIdentity);

        return fragment;
    }

    @Override
    void deleteCompletion(XTaskCompletion completion, final OnSuccessListener<DriveFile> onSuccessListener) {

        // Find task storing completion
        final XTask task = completion.findTask(tasks);

        // Make sure task search was successful
        if(task == null) return;

        // Remove completion from this task
        task.removeCompletion(completion);

        final ArrayList<XTaskCompletion> completions = XTaskUtilities.getCompletionsFromTasks(tasks);
        setAllCompletions(completions);

        final ArrayList<XTaskIdentity> taskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(completions);
        setAllTaskIdentities(taskIdentities);

        loadCompletions();

        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME, tasks, getContext(), onSuccessListener, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        
    }

    @Override
    void editCompletionDate(XTaskCompletion completion, long completionDate, OnSuccessListener<DriveFile> onSuccessListener) {

        final XTask task = completion.findTask(tasks);

        if(task == null) return;

        ArrayList<XTaskCompletion> completions = task.getCompletions();

        int index = completions.indexOf(completion);

        completion.setDate(completionDate);

        completions.set(index,completion);

        task.setCompletions(completions);

        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME, tasks, getContext(), onSuccessListener, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    @Override
    int getSelectionMenuResId() {
        return R.menu.menu_tasks_completions_fragment_selection_mode;
    }

    @Override
    Toolbar.OnMenuItemClickListener getSelectionMenuItemClickListener() {
        return new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.timeline_selection_mode_icon_register_payment:

                        FragmentLoader.loadFragment(NewPaymentFragment.newInstance(tasks,payments,getAdapter().getSelectionArray()),getContext(),R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom, true);

                        hideSelectionUI();
                        break;
                    case R.id.timeline_selection_mode_icon_delete:

                        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                                .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getAdapter().deleteSelectedItems();
                                        hideSelectionUI();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .create();

                        alertDialog.setTitle(getString(R.string.delete_completions_confirmation_title));
                        alertDialog.setMessage(getString(R.string.delete_completions_confirmation_message));
                        alertDialog.show();

                        break;
                }

                return false;
            }
        };
    }

    @Override
    void onDataSetChanged(OnSuccessListener<DriveFile> onSuccessListener) {

        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME, tasks, getContext(), onSuccessListener, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
}
