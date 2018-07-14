package com.mzom.xtraqueur;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "XTQ-WelcomeActivity";

    private WelcomePagerAdapter mPagerAdapter;

    private ViewPager mViewPager;

    private Button nextBtn;

    private Button backBtn;

    private Button finishBtn;

    private int mCurrentPageNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        initWelcomePages();

        initPageIndicator();

        initButtonListeners();
    }

    private void initWelcomePages(){

        mPagerAdapter = new WelcomePagerAdapter(this,getLayoutInflater());

        mViewPager = findViewById(R.id.welcome_view_pager);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.addOnPageChangeListener(onPageChangeListener);

        mViewPager.setCurrentItem(mCurrentPageNum);

    }

    private void initPageIndicator(){

        final LinearLayout mPageIndicatorLayout = findViewById(R.id.welcome_page_indicator);
        mPageIndicatorLayout.removeAllViews();

        for(int i = 0;i<mPagerAdapter.getCount();i++){

            final LinearLayout dot = new LinearLayout(this);
            dot.setBackground(getResources().getDrawable(R.drawable.circle));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(30,30);
            layoutParams.setMargins(10,0,10,0);
            dot.setLayoutParams(layoutParams);
            if(i == mCurrentPageNum){
                ColorUtilities.setViewBackgroundColor(dot,getResources().getColor(R.color.colorAccent));
            }else{
                ColorUtilities.setViewBackgroundColor(dot,getResources().getColor(R.color.colorAccentDark));
            }

            mPageIndicatorLayout.addView(dot);

        }

    }

    private void initButtonListeners(){

        nextBtn = findViewById(R.id.welcome_page_button_next);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mCurrentPageNum+1);
            }
        });

        backBtn = findViewById(R.id.welcome_page_button_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mCurrentPageNum-1);
            }
        });

        finishBtn = findViewById(R.id.welcome_page_button_finish);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSignInActivity();
            }
        });


    }

    private void displayAppropriateButtons(int pagePos){

        boolean nextAble = pagePos != mPagerAdapter.getCount()-1;
        nextBtn.setEnabled(nextAble);
        nextBtn.setVisibility(nextAble ? View.VISIBLE : View.GONE);

        boolean backAble = pagePos > 0;
        backBtn.setEnabled(backAble);
        backBtn.setVisibility(backAble ? View.VISIBLE : View.GONE);

        boolean finishAble = pagePos == mPagerAdapter.getCount()-1;
        finishBtn.setEnabled(finishAble);
        finishBtn.setVisibility(finishAble ? View.VISIBLE : View.GONE);

    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            mCurrentPageNum = position;

            initPageIndicator();

            displayAppropriateButtons(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onBackPressed() {

        if(mCurrentPageNum > 0){
            mViewPager.setCurrentItem(mCurrentPageNum-1);

            // Back press has been handled
            return;
        }

        super.onBackPressed();
    }


    public void loadSignInActivity() {

        startActivity(new Intent(this,SignInActivity.class));
        finish();

    }
}
