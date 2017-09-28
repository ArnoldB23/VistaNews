package roca.bajet.com.vistanews;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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

/**
 * Created by Arnold on 9/18/2017.
 */

public class ArticleListFragment extends Fragment {

    public static final String ARG_SOURCE_ID = "ARG_SOURCE_ID";
    private static final String LOG_TAG = "ArticleListFragment";
    private static final String STATE_SCROLL = "STATE_SCROLL";

    private ArticleAdapter mArticleAdapter;
    private Realm mRealm;
    private NewsService mNewsService;
    private Context mContext;
    private String mSourceId;
    private RecyclerView mArticleListRecyclerView;


    public static ArticleListFragment newInstance(String sourceId) {
        Bundle args = new Bundle();
        args.putString(ARG_SOURCE_ID, sourceId);
        ArticleListFragment fragment = new ArticleListFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState){



        View rootView = inflater.inflate(R.layout.fragment_article_list_pager, container, false);

        mContext = getActivity().getApplicationContext();
        mSourceId = getArguments().getString(ARG_SOURCE_ID);

        Log.d(LOG_TAG, "onCreateView, source: " + mSourceId);

        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        mRealm = Realm.getInstance(config);

        final RealmResults<RealmArticle> realmArticles = mRealm.where(RealmArticle.class).equalTo("sourceId",mSourceId).findAll();
        mArticleAdapter = new ArticleAdapter(realmArticles, true);

        mArticleListRecyclerView = (RecyclerView) rootView.findViewById(R.id.page_recyclerview);


        LinearLayoutManager lm = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mArticleListRecyclerView.setLayoutManager(lm);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(mContext, lm.getOrientation());

        mArticleListRecyclerView.addItemDecoration(itemDecoration);

        mNewsService = ApiUtils.getNewsService();
        mNewsService.getArticle(mSourceId,BuildConfig.NEWS_API_KEY,"").enqueue(new Callback<GetArticleResponse>() {
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {

                if (response.isSuccessful())
                {
                    final Response<GetArticleResponse> finalResponse = response;

                    try {
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realmArticles.deleteAllFromRealm();
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

                    RealmResults<RealmArticle> articles = mRealm.where(RealmArticle.class).equalTo("sourceId", mSourceId).findAll();
                    mArticleAdapter.updateData(articles);
                }
                else{
                    Log.d(LOG_TAG, "Failed to fetch articles. " + response.message());
                }

                mArticleListRecyclerView.setAdapter(mArticleAdapter);

                if (savedInstanceState != null)
                {
                    mArticleListRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(STATE_SCROLL));
                }
            }

            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {
                mArticleListRecyclerView.setAdapter(mArticleAdapter);

                if (savedInstanceState != null)
                {
                    mArticleListRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(STATE_SCROLL));
                }
            }
        });



        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(STATE_SCROLL, mArticleListRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();


        /*
        GlideApp.get(mContext).clearMemory();
        new Thread(new Runnable() {
            @Override
            public void run() {

                GlideApp.get(mContext).clearDiskCache();
            }
        }).start();

        Log.d(LOG_TAG, "onDestroy sourceId: " + mSourceId);

        mArticleListRecyclerView.setAdapter(null);
        mArticleListRecyclerView = null;
        mArticleAdapter = null;
        mNewsService = null;
        mContext = null;
        mRealm = null;
        mSourceId = null;
        */


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
        public void onBindViewHolder(final ArticleViewHolder holder, int position) {

            RealmArticle realmArticle = getItem(position);
            holder.mTitleTextView.setText(realmArticle.title);
            holder.mDateTextView.setText(formatDateString(realmArticle.publishedAt));
            holder.mArticleUrl = realmArticle.url;

            GlideApp.with(mContext).load(realmArticle.urlToImage).error(R.drawable.loading_error).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.mProgressBar.setVisibility(View.INVISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    holder.mProgressBar.setVisibility(View.INVISIBLE);
                    return false;
                }
            }).into(holder.mArticleImageView);

            Log.d(LOG_TAG, "onBindViewHolder, " + realmArticle.urlToImage);

        }
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView mArticleImageView;
        public TextView mTitleTextView;
        public TextView mDateTextView;
        public String mArticleUrl;
        public ProgressBar mProgressBar;


        public ArticleViewHolder (View v)
        {
            super(v);
            v.setOnClickListener(this);
            v.setClickable(true);


            mArticleImageView = (ImageView) v.findViewById(R.id.article_item_imageview);
            mTitleTextView = (TextView) v.findViewById(R.id.article_item_title);
            mProgressBar = (ProgressBar) v.findViewById(R.id.article_item_progressbar);
            mDateTextView = (TextView) v.findViewById(R.id.article_item_date);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(mContext, ArticleDetailActivity.class);
            i.putExtra(ArticleDetailActivity.EXTRA_DETAIL_URL, mArticleUrl);

            startActivity(i);
        }
    }
}
