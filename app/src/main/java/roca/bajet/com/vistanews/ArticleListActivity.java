package roca.bajet.com.vistanews;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roca.bajet.com.vistanews.data.ApiUtils;
import roca.bajet.com.vistanews.data.Article;
import roca.bajet.com.vistanews.data.GetArticleResponse;
import roca.bajet.com.vistanews.data.NewsService;
import roca.bajet.com.vistanews.data.RealmArticle;
import roca.bajet.com.vistanews.data.RealmSource;

/**
 * Created by Arnold on 8/29/2017.
 */

public class ArticleListActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    public static final String EXTRA_SOURCE_ID = "news source id";
    private static final String LOG_TAG = "ArticleListActivity";

    private String mSourceId;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mArticleListImageView;
    private Toolbar mToolbar;
    private RecyclerView mArticleListRecyclerView;
    private ImageView mSourceIconImageView;
    private Realm mRealm;
    private NewsService mNewsService;
    private Context mContext;
    private ArticleAdapter mArticleAdapter;
    private CardView mSourceIconCardView;
    private CoordinatorLayout mRootView;

    private static final int PERCENTAGE_TO_SHOW_IMAGE = 60;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_list);


        mContext = getApplicationContext();

        mRootView = (CoordinatorLayout)findViewById(R.id.root_coordinatorlayout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.article_list_appbarlayout);
        mArticleListImageView = (ImageView) findViewById(R.id.article_list_imageview);
        mArticleListRecyclerView = (RecyclerView) findViewById(R.id.article_list_recyclerview);
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


        mSourceId = getIntent().getExtras().getString(EXTRA_SOURCE_ID);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        mRealm = Realm.getInstance(config);

        Log.d(LOG_TAG, "source_id : " + mSourceId);
        RealmResults<RealmSource> realmSourceResult= mRealm.where(RealmSource.class).contains("id",mSourceId).findAll();
        final RealmSource source = realmSourceResult.first();

        mCollapsingToolbarLayout.setTitle(source.name);
        final String sourceUrl = ApiUtils.getLogosUrl(source.url);

        //mArticleListImageView.setAlpha(0f);
        //mArticleListRecyclerView.setAlpha(0f);


        final Drawable placeholder = ApiUtils.getCategoryDrawableFromRealSource(mContext, source);

        mSourceIconImageView.setTransitionName(mSourceId);

        SimpleTarget<Drawable> target = new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> t) {

                //Log.d(LOG_TAG, "w : " + resource.getIntrinsicWidth() + ", h : " + resource.getIntrinsicHeight());

                ViewCompat.setTransitionName(mSourceIconImageView, source.id);

                if (resource.getIntrinsicWidth() >= 288 && resource.getIntrinsicHeight() >= 288)
                {
                    mSourceIconImageView.setImageDrawable(resource);
                }
                else{
                    mSourceIconImageView.setImageDrawable(placeholder);
                }



                Slide slideTransitionDown = new Slide();
                slideTransitionDown.setDuration(500);
                slideTransitionDown.setInterpolator(AnimationUtils.loadInterpolator(ArticleListActivity.this, android.R.interpolator.linear_out_slow_in));

                final com.transitionseverywhere.Slide slideTransitionUp = new com.transitionseverywhere.Slide(Gravity.BOTTOM);
                slideTransitionUp.setDuration(1000);
                slideTransitionUp.setInterpolator(AnimationUtils.loadInterpolator(ArticleListActivity.this, android.R.interpolator.linear_out_slow_in));
                slideTransitionUp.addTarget(mSourceIconImageView);

                final Fade fadeTransition = new Fade(Fade.IN);
                fadeTransition.setDuration(1000);
                fadeTransition.setInterpolator(new FastOutSlowInInterpolator());
                fadeTransition.addTarget(mArticleListRecyclerView);


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
                        mArticleListRecyclerView.setVisibility(View.VISIBLE);
                        mCollapsingToolbarLayout.setVisibility(View.VISIBLE);
                        mArticleListImageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                });


                mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        Log.d(LOG_TAG, "OnGlobalLayoutListener " );
                        //supportStartPostponedEnterTransition();
                        //TransitionManager.beginDelayedTransition(mRootView, transitionSet);

                        //TransitionManager.beginDelayedTransition(mRootView, slideTransition);
                        //TransitionManager.beginDelayedTransition(mRootView, fadeTransition);
                        //mAppBarLayout.setVisibility(View.VISIBLE);
                        //mArticleListRecyclerView.setVisibility(View.VISIBLE);
                        //mArticleListRecyclerView.animate().setDuration(1000).alpha(1f);

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



        RealmResults<RealmArticle> realmArticles = mRealm.where(RealmArticle.class).contains("sourceId",mSourceId).findAll();
        mArticleAdapter = new ArticleAdapter(realmArticles, true);
        mArticleListRecyclerView.setAdapter(mArticleAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mArticleListRecyclerView.setLayoutManager(lm);

        mArticleListRecyclerView.addItemDecoration(new DividerItemDecoration(this, lm.getOrientation()));

        mNewsService = ApiUtils.getNewsService();
        mNewsService.getArticle(mSourceId,BuildConfig.NEWS_API_KEY,"").enqueue(new Callback<GetArticleResponse>() {
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {

                if (response.isSuccessful())
                {
                    final Response<GetArticleResponse> finalResponse = response;
                    final RealmResults<RealmArticle> currentArticles = mRealm.where(RealmArticle.class).contains("sourceId", mSourceId).findAll();

                    try {
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                currentArticles.deleteAllFromRealm();
                                int count = 1;

                                for (Article article : finalResponse.body().articles)
                                {
                                    RealmArticle realmArticle = new RealmArticle();
                                    realmArticle.author = article.author;
                                    realmArticle.title = article.title;
                                    realmArticle.description = article.description;
                                    realmArticle.url = article.url;
                                    realmArticle.urlToImage = article.urlToImage;
                                    realmArticle.publishedAt = article.publishedAt;
                                    realmArticle.sourceId = mSourceId;

                                    Log.d(LOG_TAG, "response: " + article.title);
                                    //switch ()
                                    realmArticle.topRank = count;
                                    count++;

                                    realm.insert(realmArticle);

                                }
                            }
                        });


                    }
                    catch( Exception e )
                    {
                        Log.e(LOG_TAG, e.toString());
                    }

                    RealmResults<RealmArticle> articles = mRealm.where(RealmArticle.class).contains("sourceId", mSourceId).findAll();
                    Log.d(LOG_TAG, "articles: " + articles.size());
                    mArticleAdapter.updateData(articles);

                }
                else{
                    Log.d(LOG_TAG, "Failed to fetch articles. " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {

            }
        });

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

    private String formatDateString(String publishedAt)
    {
        Date now = new Date();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a zzz");
        try {
            Date date = df.parse(publishedAt.replaceAll("[tTzZ]", ""));

            long duration = now.getTime() - date.getTime();

            long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);

            Log.d(LOG_TAG, "formatDateString, diffInHours " + diffInHours);

            String elapsedTime = publishedAt;

            if (diffInHours == 0)
            {
                elapsedTime = "Less than 1 hour ago";
            }
            else if (diffInHours == 1)
            {
                elapsedTime = "1 hour ago";
            }
            else if (diffInHours > 1 && diffInHours < 24)
            {
                elapsedTime = String.valueOf(diffInHours) + " hours ago";
            }

            else if (diffInHours >= 24 && diffInHours < 48)
            {
                elapsedTime = "1 day ago";
            }

            else if (diffInHours >= 48)
            {
                elapsedTime = String.valueOf(diffInHours/24) + " days ago";
            }

            else{
                elapsedTime = df2.format(date).toString();
            }

            return elapsedTime;
        } catch (Exception e) {
            e.printStackTrace();
            return publishedAt;
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                supportFinishAfterTransition();


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


    public class ArticleAdapter extends RealmRecyclerViewAdapter<RealmArticle, ArticleViewHolder>
    {

        public ArticleAdapter(@Nullable OrderedRealmCollection<RealmArticle> data, boolean autoUpdate) {
            super(data, autoUpdate);
        }

        @Override
        public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(mContext).inflate(R.layout.article_item, parent, false);
            ArticleViewHolder vh = new ArticleViewHolder(itemView);

            return vh;
        }

        @Override
        public void onBindViewHolder(ArticleViewHolder holder, int position) {

            RealmArticle realmArticle = getItem(position);
            holder.mTitleTextView.setText(realmArticle.title);
            holder.mDescriptionTextView.setText(realmArticle.description);
            holder.mDateTextView.setText(formatDateString(realmArticle.publishedAt));
            holder.mArticleUrl = realmArticle.url;

            GlideApp.with(mContext).load(realmArticle.urlToImage).into(holder.mArticleImageView);
            Log.d(LOG_TAG, "onBindViewHolder" + realmArticle.title);

        }
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView mArticleImageView;
        public TextView mTitleTextView;
        public TextView mDescriptionTextView;
        public TextView mDateTextView;
        public String mArticleUrl;


        public ArticleViewHolder (View v)
        {
            super(v);
            v.setOnClickListener(this);
            v.setClickable(true);

            mArticleImageView = (ImageView) v.findViewById(R.id.article_item_imageview);
            mTitleTextView = (TextView) v.findViewById(R.id.article_item_title);
            mDescriptionTextView = (TextView) v.findViewById(R.id.article_item_description);
            mDateTextView = (TextView) v.findViewById(R.id.article_item_date);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(ArticleListActivity.this, ArticleDetailActivity.class);
            i.putExtra(ArticleDetailActivity.EXTRA_DETAIL_URL, mArticleUrl);

            startActivity(i);
        }
    }



}
