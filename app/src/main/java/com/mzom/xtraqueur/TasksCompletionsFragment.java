package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class TasksCompletionsFragment extends BaseCompletionsFragment {

    private ArrayList<XTask> tasks;

    private ArrayList<XPayment> payments;

    private ArrayList<XTaskCompletion> instantCompletions;

    public static TasksCompletionsFragment newInstance(ArrayList<XTask> tasks,ArrayList<XPayment> payments,ArrayList<XTaskCompletion> instantCompletions) {
        return newInstance(tasks,payments,instantCompletions,null);
    }

    public static TasksCompletionsFragment newInstance(ArrayList<XTask> tasks, ArrayList<XPayment> payments,ArrayList<XTaskCompletion> instantCompletions, XTaskIdentity taskIdentity) {

        TasksCompletionsFragment fragment = new TasksCompletionsFragment();

        fragment.tasks = tasks;
        fragment.payments = payments;
        fragment.instantCompletions = instantCompletions;

        ArrayList<XTaskCompletion> completions = XTaskUtilities.getCompletionsFromTasks(tasks);
        completions.addAll(fragment.instantCompletions);
        fragment.setAllCompletions(completions);

        ArrayList<XTaskIdentity> taskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(completions);
        fragment.setAllTaskIdentities(taskIdentities);

        fragment.setFilterTaskIdentity(taskIdentity);

        return fragment;
    }

    @Override
    void onCompletionClicked(XTaskCompletion completion, int yPos) {

        if(completion.isInstantCompletion()){

            // Edit instant completion
            FragmentLoader.loadFragment(EditInstantCompletionFragment.newInstance(completion, instantCompletions),getContext());

        }else{

            // Edit task completion
            FragmentLoader.loadFragment(EditTaskCompletionFragment.newInstance(completion, tasks),getContext());
        }

    }

    @Override
    void deleteCompletion(XTaskCompletion completion) {

        // Find task storing completion
        final XTask task = completion.findTask(tasks);

        // Make sure task search was successful
        if(task != null){

            // Remove completion from this task
            task.removeCompletion(completion);

            XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME, tasks, getContext());

        }else{

            int index = instantCompletions.indexOf(completion);

            if(index != -1){

                instantCompletions.remove(completion);

                XDataUploader.uploadData(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME, instantCompletions, getContext());
            }


        }

        final ArrayList<XTaskCompletion> completions = XTaskUtilities.getCompletionsFromTasks(tasks);
        completions.addAll(instantCompletions);
        setAllCompletions(completions);

        final ArrayList<XTaskIdentity> taskIdentities = XTaskUtilities.getTaskIdentitiesFromCompletions(completions);
        taskIdentities.addAll(XTaskUtilities.getTaskIdentitiesFromCompletions(instantCompletions));
        setAllTaskIdentities(taskIdentities);

        loadCompletions();

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

                        FragmentLoader.loadFragment(NewPaymentFragment.newInstance(tasks,payments,instantCompletions,getCompleteSelectionArray()),getContext(),R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom, true);

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

    // Iterate selected completions to create a complete selection array
    private ArrayList<Boolean> getCompleteSelectionArray(){

        final ArrayList<XTaskCompletion> allCompletions = XTaskUtilities.getCompletionsFromTasks(tasks);

        final ArrayList<Boolean> completeSelectionArray = new ArrayList<>(allCompletions.size());

        final ArrayList<XTaskCompletion> selectedCompletions = getSelectedCompletions();

        for(int i = 0;i<allCompletions.size();i++){

            final XTaskCompletion completion = allCompletions.get(i);

            boolean selected = selectedCompletions.indexOf(completion) > -1;

            completeSelectionArray.add(i,selected);

        }

        return completeSelectionArray;

    }

    @Override
    void onDataSetChanged() {

        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME, tasks, getContext());

        XDataUploader.uploadData(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME, instantCompletions, getContext());

    }
}
