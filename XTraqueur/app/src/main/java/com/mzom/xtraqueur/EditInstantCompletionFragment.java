package com.mzom.xtraqueur;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class EditInstantCompletionFragment extends BaseEditCompletionFragment {

    private ArrayList<XTaskCompletion> instantCompletions;

    public static EditInstantCompletionFragment newInstance(XTaskCompletion completion, ArrayList<XTaskCompletion> instantCompletions) {

        EditInstantCompletionFragment fragment = new EditInstantCompletionFragment();
        fragment.completion = completion;
        fragment.completionColor = fragment.completion.getTaskIdentity().getColor();
        fragment.tempCompletionDate = completion.getDate();
        fragment.instantCompletions = instantCompletions;
        return fragment;
    }

    @Override
    void editCompletionDate(XTaskCompletion completion, long editedCompletionDate) {

        if (!completion.isInstantCompletion()) return;


        int index = instantCompletions.indexOf(completion);
        completion.setDate(editedCompletionDate);
        instantCompletions.set(index, completion);

        // Upload changes to Google Drive
        XDataUploader.uploadData(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME, instantCompletions, getContext(), new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                FragmentLoader.reverseLoading(getContext());
            }
        });

    }

    @Override
    void deleteCompletion(XTaskCompletion completion) {

        if (!completion.isInstantCompletion()) return;


        int index = instantCompletions.indexOf(completion);

        if (index == -1) return;

        instantCompletions.remove(completion);

        // Upload changes to Google Drive
        XDataUploader.uploadData(XDataConstants.INSTANT_COMPLETIONS_DATA_FILE_NAME, instantCompletions, getContext(), new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                FragmentLoader.reverseLoading(getContext());
            }
        });


    }
}
