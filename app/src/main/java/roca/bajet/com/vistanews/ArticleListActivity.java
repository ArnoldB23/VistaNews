package roca.bajet.com.vistanews;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import roca.bajet.com.vistanews.data.ApiUtils;
import roca.bajet.com.vistanews.data.Article;
import roca.bajet.com.vistanews.data.RealmSource;

/**
 * Created by Arnold on 8/29/2017.
 */

public class ArticleListActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    public static final String EXTRA_INITIAL_SOURCE_ID = "news source id";
    public static final String EXTRA_SOURCE_CATEGORY = "EXTRA_SOURCE_CATEGORY";
    private static final String LOG_TAG = "ArticleListActivity";

    private String mSourceId;
    private String mInitialSourceId;
    private String mCurrentTab;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mArticleListImageView;
    private Toolbar mToolbar;

    private ImageView mSourceIconImageView;
    private Realm mRealm;
    private Context mContext;

    private CardView mSourceIconCardView;
    private CoordinatorLayout mRootView;
    private ViewPager mViewPager;
    private ArticleListPageAdapter mPageAdapter;

    private static final int PERCENTAGE_TO_SHOW_IMAGE = 60;
    public static final int REQUEST_CODE = 13;

    public static final String EXTRA_EXIT_SELECTED_SOURCE_POSITION = "extra_selected_source_position";
    public static final String EXTRA_INITIAL_SELECTED_SOURCE_POSITION = "extra_initial_selected_source_position";
    private static final String STATE_CURRENT_PAGE = "STATE_CURRENT_PAGE";
    private int mMaxScrollSize;
    private boolean mIsImageHidden;
    private SourceSharedElementCallback mSourceSharedElementCallback;
    private int mInitialSelectedSourcePosition;

    private final Transition.TransitionListener sharedExitListener =
            new Transition.TransitionListener() {

                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {

                    SharedElementCallback sharedCb = null;
                    setExitSharedElementCallback(sharedCb);
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list_pager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            postponeEnterTransition();
        }else{
            supportPostponeEnterTransition();
        }

        mContext = getApplicationContext();
        mRootView = (CoordinatorLayout)findViewById(R.id.root_coordinatorlayout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.article_list_appbarlayout);
        mArticleListImageView = (ImageView) findViewById(R.id.article_list_imageview);
        mViewPager = (ViewPager) findViewById(R.id.article_list_viewpager);
        mSourceIconImageView = (ImageView) findViewById(R.id.source_icon_imageview);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.article_list_collapsingtoolbarlayout);
        mToolbar = (Toolbar) findViewById(R.id.article_list_toolbar);
        mSourceIconCardView = (CardView) findViewById(R.id.source_icon_cardview);

        mInitialSourceId = getIntent().getExtras().getString(EXTRA_INITIAL_SOURCE_ID);
        mCurrentTab = getIntent().getExtras().getString(EXTRA_SOURCE_CATEGORY);
        mInitialSelectedSourcePosition = getIntent().getExtras().getInt(EXTRA_INITIAL_SELECTED_SOURCE_POSITION);

        mArticleListImageView.setImageDrawable(ApiUtils.getArticleListBackground(mContext));

        final com.transitionseverywhere.Slide slideTransition = new com.transitionseverywhere.Slide(Gravity.TOP);
        slideTransition.setDuration(1000);
        slideTransition.setInterpolator(new FastOutSlowInInterpolator());
        slideTransition.addTarget(mAppBarLayout);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAppBarLayout.addOnOffsetChangedListener(this);

        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        mRealm = Realm.getInstance(config);


        Log.d(LOG_TAG, "source_id : " + mInitialSourceId);
        final RealmResults<RealmSource> realmList = ApiUtils.getRealmResultsFromTab(mRealm, mCurrentTab);



        mPageAdapter = new ArticleListPageAdapter(getSupportFragmentManager(), realmList);
        mViewPager.setAdapter(mPageAdapter);

        if (savedInstanceState != null)
        {

            mViewPager.setCurrentItem(savedInstanceState.getInt(STATE_CURRENT_PAGE));
        }else{
            mViewPager.setCurrentItem(mInitialSelectedSourcePosition);
        }


        mViewPager.setOffscreenPageLimit(1);

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                Log.d(LOG_TAG, "onPageSelected " + position);
                RealmSource source = mPageAdapter.getRealmSourceFromPosition(position);
                String logoUrl = ApiUtils.getLogosUrl(source.url);

                Drawable placeholder = ApiUtils.getCategoryDrawableFromRealSource(mContext, source);
                GlideApp.with(mContext).load(logoUrl).error(placeholder).into(mSourceIconImageView);


                mCollapsingToolbarLayout.setTitle(source.name);
                mSourceId = source.id;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mSourceIconImageView.setTransitionName(mSourceId);

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        mViewPager.addOnPageChangeListener(pageChangeListener);
        //pageChangeListener.onPageSelected(mInitialSelectedSourcePosition);

        RealmSource source = mPageAdapter.getRealmSourceFromPosition(mInitialSelectedSourcePosition);
        mCollapsingToolbarLayout.setTitle(source.name);
        mSourceId = source.id;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {


            mSourceIconImageView.setTransitionName(mSourceId);
            mSourceSharedElementCallback = new SourceSharedElementCallback();
            mSourceSharedElementCallback.LOG_TAG = LOG_TAG;
            mSourceSharedElementCallback.setImageView(mSourceIconImageView);

            android.transition.TransitionSet enterTransition = (android.transition.TransitionSet) TransitionInflater.from(mContext).inflateTransition(R.transition.article_list_activity_enter_transition);
            android.transition.TransitionSet returnTransition = (android.transition.TransitionSet) TransitionInflater.from(mContext).inflateTransition(R.transition.article_list_activity_return_transition);


            setEnterSharedElementCallback(mSourceSharedElementCallback);

            getWindow().setEnterTransition(enterTransition);
            getWindow().setReturnTransition(returnTransition);
            getWindow().getSharedElementExitTransition().addListener(sharedExitListener);

            /*
            mAppBarLayout.setVisibility(View.VISIBLE);
            mCollapsingToolbarLayout.setVisibility(View.VISIBLE);
            mArticleListImageView.setVisibility(View.VISIBLE);
            */
        }
        else{
            mAppBarLayout.setVisibility(View.INVISIBLE);
            mCollapsingToolbarLayout.setVisibility(View.INVISIBLE);
            mArticleListImageView.setVisibility(View.INVISIBLE);
        }


        String logoUrl = ApiUtils.getLogosUrl(source.url);

        Drawable placeholder = ApiUtils.getCategoryDrawableFromRealSource(mContext, source);
        GlideApp.with(mContext).load(logoUrl).error(placeholder).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    startPostponedEnterTransition();
                }else{
                    supportStartPostponedEnterTransition();
                }

                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    mViewPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            mViewPager.getViewTreeObserver().removeOnPreDrawListener(this);

                            startPostponedEnterTransition();
                            return false;
                        }
                    });

                }

                else{
                    final com.transitionseverywhere.Slide slideTransitionUp = new com.transitionseverywhere.Slide(Gravity.BOTTOM);
                    slideTransitionUp.setDuration(1000);
                    slideTransitionUp.setInterpolator(new FastOutSlowInInterpolator());
                    slideTransitionUp.addTarget(mSourceIconImageView);

                    final Fade fadeTransition = new Fade(Fade.IN);
                    fadeTransition.setDuration(1000);
                    fadeTransition.setInterpolator(new FastOutSlowInInterpolator());

                    final TransitionSet transitionSet = new TransitionSet();
                    transitionSet.addTransition(fadeTransition);
                    transitionSet.addTransition(slideTransitionUp);
                    transitionSet.addTransition(slideTransition);
                    transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);

                    mRootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            mRootView.getViewTreeObserver().removeOnPreDrawListener(this);

                            Log.d(LOG_TAG, "OnPreDrawListener " );
                            supportStartPostponedEnterTransition();
                            TransitionManager.beginDelayedTransition(mRootView, transitionSet);


                            mAppBarLayout.setVisibility(View.VISIBLE);
                            mCollapsingToolbarLayout.setVisibility(View.VISIBLE);
                            mArticleListImageView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    });
                }


                return false;
            }
        }).into(mSourceIconImageView);





    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        Log.d(LOG_TAG, "onSaveInstanceState");

        outState.putInt(STATE_CURRENT_PAGE, mViewPager.getCurrentItem());

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(mRealm != null)
        {
            mRealm.close();
            mRealm = null;
        }
    }



    @Override
    public void onPause()
    {
        super.onPause();
    }



    @Override
    public void onBackPressed() {

        Log.d(LOG_TAG, "onBackPressed()");



        if (mIsImageHidden)
        {
            Log.d(LOG_TAG, "onBackPressed() finish no transition ");
            setActivityResult();
            finish();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Log.d(LOG_TAG, "onBackPressed() finish with transition ");

            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setActivityResult();
            finishAfterTransition();
        }

        else{
            setActivityResult();
            supportFinishAfterTransition();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected()");
        switch (item.getItemId()) {

            case android.R.id.home:


                if (mIsImageHidden)
                {
                    Log.d(LOG_TAG, "home navigation, finish no transition ");
                    finish();
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    Log.d(LOG_TAG, "home navigation, finish with transition ");
                    setActivityResult();
                    finishAfterTransition();
                    getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                }
                else{
                    Log.d(LOG_TAG, "home navigation, finish with support transition");
                    setActivityResult();
                    supportFinishAfterTransition();
                }

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(verticalOffset)) * 100
                / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;

                ViewCompat.animate(mSourceIconCardView).scaleY(0).scaleX(0).start();
                ViewCompat.animate(mSourceIconImageView).scaleY(0).scaleX(0).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                ViewCompat.animate(mSourceIconCardView).scaleY(1).scaleX(1).start();
                ViewCompat.animate(mSourceIconImageView).scaleY(1).scaleX(1).start();
            }
        }
    }


    private void setActivityResult() {

        Intent intent = new Intent();
        intent.putExtra(EXTRA_EXIT_SELECTED_SOURCE_POSITION, mViewPager.getCurrentItem());
        setResult(RESULT_OK, intent);
    }
}
