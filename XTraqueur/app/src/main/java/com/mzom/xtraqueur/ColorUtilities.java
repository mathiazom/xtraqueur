package com.mzom.xtraqueur;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.View;

class ColorUtilities {

    // Get a darker shade of the original color
    @ColorInt
    static int getDarkerColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    static void setViewBackgroundColor(@NonNull View view, int color) {
        Drawable background = view.getBackground();
        background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        view.setBackground(background);
    }

}
