package com.xtraqueur.mzom.xtraqueur;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Created by Mathias on 06.06.2017.
 */

public class Task {
    String name;
    String col;
    int count;
    int fee;

    public static int randInt(int min, int max) {
        Random random = new Random();
        int randomNum = random.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static void newTask(List<Object> list, String name, String col, int count, int fee){
        Task newtask = new Task();
        newtask.name = name;
        newtask.count = count;
        newtask.col = col;
        newtask.fee = fee;
        list.add(newtask);
    }

    @Override
    public String toString(){
        return this.name;
    }

}
