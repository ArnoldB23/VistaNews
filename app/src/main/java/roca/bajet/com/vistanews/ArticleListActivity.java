package roca.bajet.com.vistanews;

import android.content.Context;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import roca.bajet.com.vistanews.data.ApiUtils;
import roca.bajet.com.vistanews.data.NewsService;
import roca.bajet.com.vistanews.data.RealmSource;

/**
 * Created by Arnold on 8/29/2017.
 */

public class ArticleListActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    public static final String EXTRA_INITIAL_SOURCE_ID = "news source id";
    public static final String EXTRA_SOURCE_CATEGORY = "EXTRA_SOURCE_CATEGORY";
    private static final String LOG_TAG = "ArticleListActivity";

    private String mSourceId;
    private String mCurrentTab;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mArticleListImageView;
    private Toolbar mToolbar;
    //private RecyclerView mArticleListRecyclerView;
    private ImageView mSourceIconImageView;
    private Realm mRealm;
    private NewsService mNewsService;
    private Context mContext;

    private CardView mSourceIconCardView;
    private CoordinatorLayout mRootView;
    private ViewPager mViewPager;
    private ArticleListPageAdapter mPageAdapter;

    private static final int PERCENTAGE_TO_SHOW_IMAGE = 60;
    public static final int RESULT_CODE = 13;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_list_pager);


        mContext = getApplicationContext();

        mRootView = (CoordinatorLayout)findViewById(R.id.root_coordinatorlayout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.article_list_appbarlayout);
        mArticleListImageView = (ImageView) findViewById(R.id.article_list_imageview);
        //mArticleListRecyclerView = (RecyclerView) findViewById(R.id.article_list_recyclerview);
        mViewPager = (ViewPager) findViewById(R.id.article_list_viewpager);
        mSourceIconImageView = (ImageView) findViewById(R.id.source_icon_imageview);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.article_list_collapsingtoolbarlayout);
        mToolbar = (Toolbar) findViewById(R.id.article_list_toolbar);
        mSourceIconCardView = (CardView) findViewById(R.id.source_icon_cardview);



        final com.transitionseverywhere.Slide slideTransition = new com.transitionseverywhere.Slide(Gravity.TOP);
        slideTransition.setDuration(1000);
        slideTransition.setInterpolator(new FastOutSlowInInterpolator());
        slideTransition.addTarget(mAppBarLayout);
        slideTransition.addListener(new com.transitionseverywhere.Transition.TransitionListener() {
            @Override
            public void onTransitionStart(com.transitionseverywhere.Transition transition) {

            }

            @Override
            public void onTransitionEnd(com.transitionseverywhere.Transition transition) {
                //mArticleListRecyclerView.animate().setDuration(1000).alpha(1f);
                slideTransition.removeListener(this);
            }

            @Override
            public void onTransitionCancel(com.transitionseverywhere.Transition transition) {

            }

            @Override
            public void onTransitionPause(com.transitionseverywhere.Transition transition) {

            }

            @Override
            public void onTransitionResume(com.transitionseverywhere.Transition transition) {

            }
        });


        //android.transition.TransitionManager.beginDelayedTransition(mAppBarLayout, slideTransitionDown);
        //android.transition.TransitionManager.beginDelayedTransition(mArticleListRecyclerView, slideTransitionUp);
        //mAppBarLayout.setVisibility(View.VISIBLE);
        //mArticleListRecyclerView.setVisibility(View.VISIBLE);


        supportPostponeEnterTransition();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAppBarLayout.addOnOffsetChangedListener(this);


        mSourceId = getIntent().getExtras().getString(EXTRA_INITIAL_SOURCE_ID);
        mCurrentTab = getIntent().getExtras().getString(EXTRA_SOURCE_CATEGORY);


        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        mRealm = Realm.getInstance(config);


        Log.d(LOG_TAG, "source_id : " + mSourceId);
        final RealmResults<RealmSource> realmList = ApiUtils.getRealmResultsFromTab(mRealm, mCurrentTab);
        final RealmSource source = realmList.where().equalTo("id", mSourceId).findFirst();

        mPageAdapter = new ArticleListPageAdapter(getSupportFragmentManager(), realmList);
        mViewPager.setAdapter(mPageAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mCollapsingToolbarLayout.setTitle(source.name);
        final String sourceUrl = ApiUtils.getLogosUrl(source.url);

        //mArticleListImageView.setAlpha(0f);
        //mArticleListRecyclerView.setAlpha(0f);


        final Drawable placeholder = ApiUtils.getCategoryDrawableFromRealSource(mContext, source);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mSourceIconImageView.setTransitionName(mSourceId);

        }



        SimpleTarget<Drawable> target = new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> t) {

                //Log.d(LOG_TAG, "w : " + resource.getIntrinsicWidth() + ", h : " + resource.getIntrinsicHeight());

                ViewCompat.setTransitionName(mSourceIconImageView, source.id);

                mSourceIconImageView.setImageDrawable(resource);

                /*
                if (resource.getIntrinsicWidth() >= 288 && resource.getIntrinsicHeight() >= 288)
                {
                    mSourceIconImageView.setImageDrawable(resource);
                }
                else{
                    mSourceIconImageView.setImageDrawable(placeholder);
                }

                */





                final com.transitionseverywhere.Slide slideTransitionUp = new com.transitionseverywhere.Slide(Gravity.BOTTOM);
                slideTransitionUp.setDuration(1000);
                slideTransitionUp.setInterpolator(new FastOutSlowInInterpolator());
                slideTransitionUp.addTarget(mSourceIconImageView);

                final Fade fadeTransition = new Fade(Fade.IN);
                fadeTransition.setDuration(1000);
                fadeTransition.setInterpolator(new FastOutSlowInInterpolator());
                //fadeTransition.addTarget(mArticleListRecyclerView);


                //mArticleListImageView.animate().setDuration(1000).alpha(1f);
                //transition.removeListener(com.transitionseverywhere.Transition.TransitionListener.this);
                //getWindow().setEnterTransition(slideTransitionUp);

                //mRootView.getViewTreeObserver().add
                //supportStartPostponedEnterTransition();
                final TransitionSet transitionSet = new TransitionSet();
                transitionSet.addTransition(fadeTransition);
                transitionSet.addTransition(slideTransitionUp);
                transitionSet.addTransition(slideTransition);
                transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
                //supportStartPostponedEnterTransition();

                //TransitionManager.beginDelayedTransition(mRootView, transitionSet);

                mRootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mRootView.getViewTreeObserver().removeOnPreDrawListener(this);

                        Log.d(LOG_TAG, "OnPreDrawListener " );
                        supportStartPostponedEnterTransition();
                        TransitionManager.beginDelayedTransition(mRootView, transitionSet);

                        //TransitionManager.beginDelayedTransition(mRootView, slideTransition);
                        //TransitionManager.beginDelayedTransition(mRootView, fadeTransition);
                        mAppBarLayout.setVisibility(View.VISIBLE);
                        //mArticleListRecyclerView.setVisibility(View.VISIBLE);
                        mCollapsingToolbarLayout.setVisibility(View.VISIBLE);
                        mArticleListImageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                });


            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                supportStartPostponedEnterTransition();
            }


        };


        GlideApp.with(mContext).load(sourceUrl).error(placeholder).into(target);


        //com.transitionseverywhere.TransitionManager.beginDelayedTransition(mAppBarLayout, slideTransition);
        //mAppBarLayout.setVisibility(View.VISIBLE);

        //final com.transitionseverywhere.Slide slideTransition = new com.transitionseverywhere.Slide(Gravity.TOP);




        //mArticleListRecyclerView.setAdapter(mArticleAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        //mArticleListRecyclerView.setLayoutManager(lm);

        //mArticleListRecyclerView.addItemDecoration(new DividerItemDecoration(this, lm.getOrientation()));




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
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_article_list, menu);

        MenuItem menuItemSortBy = menu.findItem(R.id.action_sort_by);
        Spinner sortBySpinner = (Spinner)menuItemSortBy.getActionView();

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(mContext, android.R.layout.simple_spinner_item);


        return true;
    }

    @Override
    public void onBackPressed() {


        Log.d(LOG_TAG, "onBackPressed() Exiting ");
        //super.onBackPressed();


        if (mIsImageHidden && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {


            getWindow().setSharedElementReturnTransition(null);
            //getWindow().setSharedElementReenterTransition(null);
            //getWindow().setSharedElementExitTransition(null);

            ViewCompat.setTransitionName(mSourceIconImageView, null);
            finish();
        }
        else{
            supportFinishAfterTransition();
        }


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected() Exiting ");
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                //Log.d(LOG_TAG, "onOptionsItemSelected() Exiting ");

                if (mIsImageHidden && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    getWindow().setSharedElementReturnTransition(null);

                    ViewCompat.setTransitionName(mSourceIconImageView, null);
                    finish();
                }
                else{
                    supportFinishAfterTransition();
                }

                //overridePendingTransition(0,0);
                //finish();

                return true;

            case R.id.action_sort_by:

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


}
