package com.mzom.xtraqueur;

import java.util.ArrayList;
import java.util.Objects;

class XTaskFieldsUtilities {

    static boolean areEqual(Object o1, Object o2){

        if (o1 == o2) return true;

        if (o1 == null || o2 == null) return false;

        if (o1.getClass() != o2.getClass()) return false;


        XTaskFields tf1 = (XTaskFields) o1;
        XTaskFields tf2 = (XTaskFields) o2;

        return Objects.equals(tf1.getName(), tf2.getName()) &&
                Objects.equals(tf1.getFee(), tf2.getFee()) &&
                Objects.equals(tf1.getColor(), tf2.getColor());

    }

    static XTask getTaskFromCompletion(XTaskCompletion completion, ArrayList<XTask> tasks){

        for(XTask task : tasks){

            if(areEqual(completion.getTaskFields(),task.getTaskFields())){
                return task;
            }

        }

        return null;

    }

    static int getCompletionCountOfTask(final ArrayList<XTaskCompletion> completions, final XTaskFields taskFields){

        int total = 0;

        for(XTaskCompletion completion : completions){

            if(areEqual(completion.getTaskFields(),taskFields)){
                total += 1;
            }

        }

        return total;
    }

    static ArrayList<XTaskFields> getTasksFieldsFromCompletions(ArrayList<XTaskCompletion> completions){

        ArrayList<XTaskFields> tasksFields = new ArrayList<>();

        for(XTaskCompletion completion : completions){

            boolean notAdded = true;

            for(XTaskFields fields : tasksFields){
                if(XTaskFieldsUtilities.areEqual(fields,completion.getTaskFields())){
                    notAdded = false;
                    break;
                }
            }

            if(notAdded){
                tasksFields.add(completion.getTaskFields());
            }

        }

        return tasksFields;

    }

    static ArrayList<XTask> removeCompletion(XTaskCompletion completion, ArrayList<XTask> tasks){

        tasks.get(tasks.indexOf(getTaskFromCompletion(completion,tasks))).removeCompletion(completion);

        return tasks;

    }


}
