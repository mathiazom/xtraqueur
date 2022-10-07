package com.mzom.xtraqueur;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

class MaterialColorDialog extends AlertDialog {

    private LinearLayout mColorContainer;

    private final Context context;
    private LayoutInflater mLayoutInflater;

    private final int selectedColor;

    private Integer[] mTypedColors;
    private String[] mTypedColorNames;

    private final MaterialColorDialogListener mMaterialColorDialogListener;
    interface MaterialColorDialogListener{
        void onColorPicked(int color);
    }

    MaterialColorDialog(Context context, int selectedColor, MaterialColorDialogListener materialColorDialogListener) {
        super(context);

        this.context = context;
        this.selectedColor = selectedColor;
        this.mMaterialColorDialogListener = materialColorDialogListener;

        getMaterialColors(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mLayoutInflater == null) return;
        ScrollView scrollView = new ScrollView(context);
        setContentView(scrollView);

        mColorContainer = (LinearLayout) mLayoutInflater.inflate(R.layout.module_colors_dialog,scrollView,false);
        scrollView.addView(mColorContainer);

        loadDialog();
    }

    private void loadDialog(){

        for(int c = 0;c<mTypedColors.length;c++){

            final int color = mTypedColors[c];

            ConstraintLayout colorLayout = (ConstraintLayout) mLayoutInflater.inflate(R.layout.template_dialog_color,mColorContainer,false);

            TextView titleView = colorLayout.findViewById(R.id.dialog_color_title);
            titleView.setText(mTypedColorNames[c]);

            LinearLayout marker = colorLayout.findViewById(R.id.dialog_color_marker);
            Drawable background = marker.getBackground();
            background.setColorFilter(mTypedColors[c], PorterDuff.Mode.SRC_ATOP);
            marker.setBackground(background);

            ImageButton selected = colorLayout.findViewById(R.id.dialog_color_selected);
            selected.setColorFilter(getContext().getResources().getColor(R.color.colorAccent));

            if(color == selectedColor){
                titleView.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
                selected.setVisibility(View.VISIBLE);
            }else{
                titleView.setTextColor(Color.BLACK);
                selected.setVisibility(View.GONE);
            }

            final MaterialColorDialog dialog = this;
            colorLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMaterialColorDialogListener.onColorPicked(color);
                    dialog.dismiss();
                }
            });

            mColorContainer.addView(colorLayout);
        }

    }

    private void getMaterialColors(Context context){
        int arrayId = context.getResources().getIdentifier("mdcolor_900_light_text", "array", getContext().getPackageName());
        if (arrayId == 0) return;

        TypedArray typedColors = context.getResources().obtainTypedArray(arrayId);

        mTypedColors = new Integer[typedColors.length()];
        mTypedColorNames = new String[typedColors.length()];

        for(int c = 0;c<typedColors.length();c++){
            int pColor = typedColors.getColor(c,0);
            mTypedColors[c] = pColor;
            String color = String.format("#%06X", (0xFFFFFF & pColor));
            mTypedColorNames[c] = color;
        }

        typedColors.recycle();
    }

}
