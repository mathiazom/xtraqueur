package com.mzom.xtraqueur;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

final class XTaskUtilities {

    // Prevent utility class from being instantiated
    private XTaskUtilities(){
        throw new UnsupportedOperationException();
    }


    // Return an array of all task identities represented in a given completions array
    static ArrayList<XTaskIdentity> getTaskIdentitiesFromCompletions(ArrayList<XTaskCompletion> completions){

        final ArrayList<XTaskIdentity> taskIdentities = new ArrayList<>();

        for(XTaskCompletion completion : completions){

            final XTaskIdentity taskIdentity = completion.getTaskIdentity();

            boolean notAdded = true;

            for(XTaskIdentity identity : taskIdentities){
                if(identity.equals(taskIdentity)){
                    notAdded = false;
                    break;
                }
            }

            // Make sure this XTaskIdentity has not already been retrieved
            if(notAdded) taskIdentities.add(taskIdentity);

        }

        return taskIdentities;

    }

    // Return an array of all completions represented in a given tasks array
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

}
