package com.mzom.xtraqueur;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

class XTaskUtilities {

    static boolean areEqual(Object o1, Object o2){

        if (o1 == o2) return true;

        if (o1 == null || o2 == null) return false;

        if (o1.getClass() != o2.getClass()) return false;


        XTaskIdentity tf1 = (XTaskIdentity) o1;
        XTaskIdentity tf2 = (XTaskIdentity) o2;

        return Objects.equals(tf1.getName(), tf2.getName()) &&
                Objects.equals(tf1.getFee(), tf2.getFee()) &&
                Objects.equals(tf1.getColor(), tf2.getColor());

    }

    static XTask getTaskFromCompletion(XTaskCompletion completion, ArrayList<XTask> tasks){

        for(XTask task : tasks){

            if(areEqual(completion.getTaskIdentity(),task.getTaskIdentity())){
                return task;
            }

        }

        return null;

    }

    static int getCompletionCountOfTask(final ArrayList<XTaskCompletion> completions, final XTaskIdentity taskIdentity){

        int total = 0;

        for(XTaskCompletion completion : completions){

            if(areEqual(completion.getTaskIdentity(),taskIdentity)){
                total += 1;
            }

        }

        return total;
    }

    static ArrayList<XTaskIdentity> getTaskIdentitiesFromCompletions(ArrayList<XTaskCompletion> completions){

        ArrayList<XTaskIdentity> taskIdentities = new ArrayList<>();

        for(XTaskCompletion completion : completions){

            boolean notAdded = true;

            for(XTaskIdentity identity : taskIdentities){
                if(XTaskUtilities.areEqual(identity,completion.getTaskIdentity())){
                    notAdded = false;
                    break;
                }
            }

            if(notAdded){
                taskIdentities.add(completion.getTaskIdentity());
            }

        }

        return taskIdentities;

    }

    static ArrayList<XTaskIdentity> getTaskIdentitiesFromTasks(ArrayList<XTask> tasks, boolean onlyWithCompletions){

        ArrayList<XTaskIdentity> taskIdentity = new ArrayList<>();

        for(XTask task : tasks){

            // If "only with completions"-filter is enabled, make sure task has completions
            if(!onlyWithCompletions || task.getCompletionsCount() > 0) taskIdentity.add(task.getTaskIdentity());
        }

        return taskIdentity;

    }

    static ArrayList<XTaskCompletion> getCompletionsFromTasks(ArrayList<XTask> tasks) {

        ArrayList<XTaskCompletion> retrievedCompletions = new ArrayList<>();

        for (XTask task : tasks) {
            retrievedCompletions.addAll(task.getCompletions());
        }

        // Sort filteredCompletions based on recency
        Collections.sort(retrievedCompletions, new Comparator<XTaskCompletion>() {
            @Override
            public int compare(XTaskCompletion c1, XTaskCompletion c2) {
                return Long.compare(c2.getDate(), c1.getDate());
            }
        });

        return retrievedCompletions;

    }

    static ArrayList<XTaskCompletion> getCompletionsFromTaskIdentity(@NonNull XTaskIdentity taskIdentity, ArrayList<XTaskCompletion> allCompletions){

        ArrayList<XTaskCompletion> filteredCompletions = new ArrayList<>();

        for(XTaskCompletion completion : allCompletions){

            if(XTaskUtilities.areEqual(completion.getTaskIdentity(),taskIdentity)){
                filteredCompletions.add(completion);
            }

        }

        return filteredCompletions;

    }

    static ArrayList<XTask> removeCompletionFromTasks(XTaskCompletion completion, ArrayList<XTask> tasks){

        tasks.get(tasks.indexOf(getTaskFromCompletion(completion,tasks))).removeCompletion(completion);

        return tasks;

    }

    static ArrayList<XTask> getNonSingleUseTasks(ArrayList<XTask> tasks){

        ArrayList<XTask> nonSingleUseTasks = new ArrayList<>();

        for(XTask task : tasks){

            if(!task.isSingleUse()) nonSingleUseTasks.add(task);

        }

        return nonSingleUseTasks;

    }

    static int getRandomMaterialColor(Context context){

        int arrayId = context.getResources().getIdentifier("mdcolor_700_light_text", "array", context.getPackageName());
        if (arrayId == 0) return Color.BLACK;

        TypedArray typedColors = context.getResources().obtainTypedArray(arrayId);

        int randIndex = (int) (Math.random() * typedColors.length());

        int randColor = typedColors.getColor(randIndex, 0);

        typedColors.recycle();

        return randColor;
    }


}
