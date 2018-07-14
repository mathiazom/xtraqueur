package com.mzom.xtraqueur;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class EditTaskCompletionFragment extends BaseEditCompletionFragment {

    private ArrayList<XTask> tasks;

    public static EditTaskCompletionFragment newInstance(XTaskCompletion completion,ArrayList<XTask> tasks) {

        EditTaskCompletionFragment fragment = new EditTaskCompletionFragment();
        fragment.completion = completion;
        fragment.completionColor = fragment.completion.getTaskIdentity().getColor();
        fragment.tempCompletionDate = completion.getDate();
        fragment.tasks = tasks;
        return fragment;
    }

    @Override
    void editCompletionDate(XTaskCompletion completion, long editedCompletionDate) {

        final XTask task = completion.findTask(tasks);

        if(task == null) return;

        final ArrayList<XTaskCompletion> completions = task.getCompletions();

        int index = completions.indexOf(completion);

        completion.setDate(editedCompletionDate);

        completions.set(index,completion);

        task.setCompletions(completions);

        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME, tasks, getContext(), new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                FragmentLoader.reverseLoading(getContext());
            }
        });

    }

    @Override
    void deleteCompletion(XTaskCompletion completion) {

        // Find task storing completion
        final XTask task = completion.findTask(tasks);

        // Make sure task search was successful
        if(task == null) return;

        // Remove completion from this task
        task.removeCompletion(completion);

        // Upload changes to Google Drive
        XDataUploader.uploadData(XDataConstants.TASKS_DATA_FILE_NAME, tasks, getContext(), new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                FragmentLoader.reverseLoading(getContext());
            }
        });

    }
}
