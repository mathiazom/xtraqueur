package com.mzom.xtraqueur;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;

/**
 * Created by elev on 24.02.2018.
 */

public class WelcomeFragment extends Fragment {

    private View view;

    private WelcomeFragmentListener mWelcomeFragmentListener;

    interface WelcomeFragmentListener{
        void signIn();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_welcome,container,false);

        initListeners();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mWelcomeFragmentListener = (WelcomeFragmentListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement WelcomeFragmentListener");
        }
    }

    private void initListeners(){
        SignInButton signInButton = view.findViewById(R.id.welcome_button_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWelcomeFragmentListener.signIn();
            }
        });
    }
}
