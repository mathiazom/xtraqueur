package com.mzom.xtraqueur;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.common.SignInButton;
import com.shobhitpuri.custombuttons.GoogleSignInButton;


public class SignInFragment extends XFragment {

    private View view;

    private SignInFragmentListener mSignInFragmentListener;

    interface SignInFragmentListener {
        void signIn();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_signin,container,false);

        initListeners();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mSignInFragmentListener = (SignInFragmentListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SignInFragmentListener");
        }
    }

    private void initListeners(){

        final LinearLayout googleSignInButton = view.findViewById(R.id.signin_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignInFragmentListener.signIn();
            }
        });
    }
}
