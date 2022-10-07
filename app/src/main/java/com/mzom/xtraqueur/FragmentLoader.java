package com.mzom.xtraqueur;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

class FragmentLoader{

    private static final String TAG = "XTQ-FragmentLoader";

    interface FragmentLoadable{

         int getFragmentFrameResId();

         FragmentManager getSupportFragmentManager();

         void onFragmentBackPressed();
    }

    static void loadFragment(Fragment fragment, Context context){
        loadFragment(fragment,context,true);
    }

    static void loadFragment(Fragment fragment,Context context,boolean addToBackStack){
        loadFragment(fragment,context,0,0,0,0,addToBackStack);
    }

    static void loadFragment(Fragment fragment,Context context,int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim, boolean addToBackStack){

        FragmentLoadable fragmentLoadable = getFragmentLoadableFromContext(context);

        FragmentTransaction transaction =  fragmentLoadable.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
                .replace(fragmentLoadable.getFragmentFrameResId(), fragment);

        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }

        //transaction.commit();

        transaction.commitAllowingStateLoss();

    }

    static void reverseLoading(Context context){

        final FragmentLoadable fragmentLoadable = getFragmentLoadableFromContext(context);

        if(fragmentLoadable != null){
            fragmentLoadable.onFragmentBackPressed();
        }

    }

    private static FragmentLoadable getFragmentLoadableFromContext(Context context){
        try{
            return (FragmentLoadable) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement FragmentLoadable");
        }
    }



}
