package com.mzom.xtraqueur;

import android.support.v4.app.Fragment;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class XFragment extends Fragment {

    private boolean viewsHaveBeenDestroyed;

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        boolean shouldNotAnimate = enter && viewsHaveBeenDestroyed;
        viewsHaveBeenDestroyed = false;
        return shouldNotAnimate ? AnimationUtils.loadAnimation(getContext(), R.anim.none) : super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewsHaveBeenDestroyed = true;
    }

}
