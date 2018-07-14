package com.mzom.xtraqueur;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

class WelcomePagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    
    private int[] pageDrawablesResId;
    private String[] pageTitles;
    private String[] pageTexts;

    WelcomePagerAdapter(@NonNull Context context,@NonNull LayoutInflater layoutInflater){

        this.context = context;
        this.layoutInflater = layoutInflater;


        pageDrawablesResId = new int[]{

                R.drawable.welcome_page_intro,
                R.drawable.welcome_page_custom_tasks_accent,
                R.drawable.welcome_page_instant_completions,
                R.drawable.welcome_page_payments,
                R.drawable.welcome_page_google_account
        };

        pageTitles = new String[]{

                context.getString(R.string.welcome_page_title_intro),
                context.getString(R.string.welcome_page_title_custom_tasks),
                context.getString(R.string.welcome_page_title_instant_completions),
                context.getString(R.string.welcome_page_title_payments),
                context.getString(R.string.welcome_page_title_google_account)
        };

        pageTexts = new String[]{

                context.getString(R.string.welcome_page_text_intro),
                context.getString(R.string.welcome_page_text_custom_tasks),
                context.getString(R.string.welcome_page_text_instant_completions),
                context.getString(R.string.welcome_page_text_payments),
                context.getString(R.string.welcome_page_text_google_account)
        };
    }
    @Override
    public int getCount() {
        return pageTitles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        final View view = layoutInflater.inflate(R.layout.template_welcome_page,container,false);

        final ImageView pageImageView = view.findViewById(R.id.welcome_page_image);
        pageImageView.setImageDrawable(context.getResources().getDrawable(pageDrawablesResId[position]));

        final TextView pageTitleView = view.findViewById(R.id.welcome_page_title);
        pageTitleView.setText(pageTitles[position]);

        final TextView pageTextView = view.findViewById(R.id.welcome_page_text);
        pageTextView.setText(Html.fromHtml(pageTexts[position]));

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((ConstraintLayout) object);

    }
}
