package roca.bajet.com.vistanews;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roca.bajet.com.vistanews.data.ApiUtils;
import roca.bajet.com.vistanews.data.GetSourceResponse;
import roca.bajet.com.vistanews.data.NewsService;
import roca.bajet.com.vistanews.data.RealmSource;
import roca.bajet.com.vistanews.data.RealmString;
import roca.bajet.com.vistanews.data.Source;

public class MainActivity extends AppCompatActivity {

    private SourceAdapter mSourceAdapter;
    private RecyclerView mSourcesRecyclerView;
    private TabLayout mCategoryTabLayout;
    private TextView mCategoryTitleTextView;

    private NewsService mNewsService;
    private AppBarLayout mMainAppBarLayout;
    private static final String LOG_TAG = "MainActivity";
    private Realm mRealm;
    private String mCurrentCategoryTab;
    private int mCurrentCategoryTabPosition;

    private static final String STATE_CURRENT_CATEGORY_TAB = "STATE_CURRENT_CATEGORY_TAB";
    private static final String STATE_CURRENT_CATEGORY_TAB_POSITION = "STATE_CURRENT_CATEGORY_TAB_POSITION";
    private static final String STATE_SCROLL = "STATE_SCROLL";

    private SourceSharedElementCallback mSharedElementCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);
        mMainAppBarLayout = (AppBarLayout)findViewById(R.id.main_appbarlayout);
        mCategoryTabLayout = (TabLayout)findViewById(R.id.category_tablayout);
        mCategoryTitleTextView = (TextView)findViewById(R.id.category_title_textview);
        mSourcesRecyclerView = (RecyclerView) findViewById(R.id.source_recyclerview);

        Log.d(LOG_TAG, "onCreate");

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        mRealm = Realm.getInstance(config);

        mNewsService = ApiUtils.getNewsService();
        setupTabLayout();
        final GridLayoutManager gm = new GridLayoutManager(this, 4);

        //Update animation item count to avoid glitchy scroll animation.
        mSourcesRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSourcesRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int lastItemPosition = gm.findLastVisibleItemPosition();

                if ( lastItemPosition > 0)
                {
                    mSourceAdapter.countLimit = ++lastItemPosition;
                    Log.d(LOG_TAG, "OnGlobalLayoutListener sources recyclerview, count limit: " + mSourceAdapter.countLimit);
                }

            }
        });

        mSourcesRecyclerView.setHasFixedSize(true);
        mSourcesRecyclerView.setLayoutManager(gm);

        mCategoryTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        if (savedInstanceState != null)
        {
            mCurrentCategoryTab = savedInstanceState.getString(STATE_CURRENT_CATEGORY_TAB);
            mCurrentCategoryTabPosition = savedInstanceState.getInt(STATE_CURRENT_CATEGORY_TAB_POSITION);
            mCategoryTabLayout.getTabAt(mCurrentCategoryTabPosition).select();
            mSourcesRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(STATE_SCROLL));

            RealmResults<RealmSource> results = ApiUtils.getRealmResultsFromTab(mRealm, mCurrentCategoryTab);
            mSourceAdapter = new SourceAdapter(MainActivity.this, results, true);
        }
        else{
            RealmResults<RealmSource> results = mRealm.where(RealmSource.class).findAllAsync();
            mSourceAdapter = new SourceAdapter(MainActivity.this, results, true);
        }



        int offset = getResources().getDimensionPixelOffset(R.dimen.item_offset);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(offset);
        mSourcesRecyclerView.addItemDecoration(itemDecoration);



        mSourcesRecyclerView.setAdapter(mSourceAdapter);

        /*
        mSourcesRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Intent i = new Intent(MainActivity.this, ArticleListActivity.class);

                RealmSource newsSource = mSourceAdapter.getItem(position);
                i.putExtra(ArticleListActivity.EXTRA_INITIAL_SOURCE_ID, newsSource.id);
                i.putExtra(ArticleListActivity.EXTRA_SOURCE_CATEGORY, mCurrentCategoryTab);
                i.putExtra(ArticleListActivity.EXTRA_INITIAL_SELECTED_SOURCE_POSITION, position);



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {

                    View decorView = getWindow().getDecorView();
                    View statusBar = decorView.findViewById(android.R.id.statusBarBackground);
                    View navBar = decorView.findViewById(android.R.id.navigationBarBackground);

                    View sourceImageView = view.findViewById(R.id.source_item_imageview);

                    if (sourceImageView == null)
                    {
                        Log.d(LOG_TAG, "sourceImageView is null!");
                    }

                    Log.d(LOG_TAG, "sourceImageView transition name: " + sourceImageView.getTransitionName());


                    Pair<View, String> sourcePair = Pair.create(sourceImageView, newsSource.id);
                    Pair<View, String> statusPair = Pair.create(statusBar, statusBar.getTransitionName());
                    Pair<View, String> navPair = Pair.create(navBar, navBar.getTransitionName());

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, sourcePair, statusPair,navPair);
                    startActivityForResult(i, ArticleListActivity.REQUEST_CODE, options.toBundle());

                }else{
                    //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, sourcePair);
                    //startActivityForResult(i, ArticleListActivity.REQUEST_CODE, options.toBundle());
                    startActivity(i);
                }

            }
        }));

        */


        mSourceAdapter.setOnSourceItemClickListener(new SourceAdapter.OnSourceItemClickListener() {
            @Override
            public void onClick(ImageView v, int position) {
                final Intent i = new Intent(MainActivity.this, ArticleListActivity.class);

                RealmSource newsSource = mSourceAdapter.getItem(position);
                i.putExtra(ArticleListActivity.EXTRA_INITIAL_SOURCE_ID, newsSource.id);
                i.putExtra(ArticleListActivity.EXTRA_SOURCE_CATEGORY, mCurrentCategoryTab);
                i.putExtra(ArticleListActivity.EXTRA_INITIAL_SELECTED_SOURCE_POSITION, position);

                Log.d(LOG_TAG, "OnSourceItemClickListener");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {

                    View decorView = getWindow().getDecorView();
                    View statusBar = decorView.findViewById(android.R.id.statusBarBackground);
                    View navBar = decorView.findViewById(android.R.id.navigationBarBackground);



                    if (v == null)
                    {
                        Log.d(LOG_TAG, "v is null!");
                    }

                    Log.d(LOG_TAG, "v transition name: " + v.getTransitionName() + ", position: " + position);

                    if (mSharedElementCallback != null)
                    {
                        mSharedElementCallback.setImageView(v);
                    }

                    Pair<View, String> sourcePair = Pair.create((View)v, v.getTransitionName());
                    Pair<View, String> statusPair = Pair.create(statusBar, statusBar.getTransitionName());
                    ActivityOptions options;
                    if (navBar == null)
                    {
                        options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, statusPair, sourcePair);
                    }
                    else{
                        Pair<View, String> navPair = Pair.create(navBar, navBar.getTransitionName());
                        options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, statusPair,navPair, sourcePair);
                    }

                    startActivityForResult(i, ArticleListActivity.REQUEST_CODE, options.toBundle());

                }else{
                    //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, sourcePair);
                    //startActivityForResult(i, ArticleListActivity.REQUEST_CODE, options.toBundle());
                    startActivity(i);
                }
            }
        });



        mNewsService.getSource("en", null, null).enqueue(new Callback<GetSourceResponse>() {
            @Override
            public void onResponse(Call<GetSourceResponse> call, Response<GetSourceResponse> response) {

                final Response<GetSourceResponse> finalResponse = response;


                if (finalResponse.isSuccessful())
                {
                    Log.d(LOG_TAG, "getSource onResponse, Successful HTTP response!");

                    try {

                        mRealm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {

                                for (final Source source: finalResponse.body().sources) {

                                    RealmSource realmSource = new RealmSource();
                                    //realm.copyToRealmOrUpdate(realmSource);
                                    realmSource.category = source.category;
                                    realmSource.country = source.country;
                                    realmSource.description = source.description;
                                    realmSource.id = source.id;
                                    realmSource.language = source.language;
                                    realmSource.name = source.name;
                                    realmSource.url = source.url;

                                    Log.d(LOG_TAG, "getSource onResponse, " + source.name);

                                    if (source.sortBysAvailable.size() > 0)
                                    {
                                        RealmList<RealmString> newRealmList = new RealmList<>();
                                        for ( final String sortBy : source.sortBysAvailable)
                                        {
                                            newRealmList.add(new RealmString(sortBy));
                                        }
                                        realmSource.sortBysAvailable =  newRealmList;
                                    }

                                    realm.insertOrUpdate(realmSource);
                                }
                            }
                        });


                    }
                    catch (Exception e)
                    {
                        Log.d(LOG_TAG, e.toString());
                    }

                }

                else{
                    Log.d(LOG_TAG, "getSource onResponse, Failed HTTP response, code: " + finalResponse.code());

                }



            }

            @Override
            public void onFailure(Call<GetSourceResponse> call, Throwable t) {
                Log.d(LOG_TAG, "getSource onFailure ");

            }
        });

    }
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityReenter");

        int exitCurrentPosition = data.getExtras().getInt(ArticleListActivity.EXTRA_EXIT_SELECTED_SOURCE_POSITION);
        mSourcesRecyclerView.scrollToPosition(exitCurrentPosition);

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
        {
            postponeEnterTransition();

            SourceAdapter.SourceViewHolder vh = (SourceAdapter.SourceViewHolder)mSourcesRecyclerView.findViewHolderForAdapterPosition(exitCurrentPosition);


            if (vh == null || data == null)
            {
                Log.w(LOG_TAG, "onActivityReenter: Holder is null, remapping cancelled.");
                startPostponedEnterTransition();
                mSharedElementCallback = null;
                setExitSharedElementCallback(mSharedElementCallback);
                return;
            }

            Log.d(LOG_TAG, "onActivityReenter, vh transitionname: " + vh.mSourceImageView.getTransitionName());
            mSharedElementCallback = new SourceSharedElementCallback();
            mSharedElementCallback.LOG_TAG = LOG_TAG;
            mSharedElementCallback.setImageView(vh.mSourceImageView);
            setExitSharedElementCallback(mSharedElementCallback);

            mSourcesRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mSourcesRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                    mSourcesRecyclerView.requestLayout();

                    startPostponedEnterTransition();
                    return false;
                }
            });
        }





    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");

        if (data != null)
        {
            int exitCurrentPosition = data.getExtras().getInt(ArticleListActivity.EXTRA_EXIT_SELECTED_SOURCE_POSITION);
            mSourcesRecyclerView.scrollToPosition(exitCurrentPosition);
        }

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
    public void onResume()
    {
        super.onResume();


    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        Log.d(LOG_TAG, "onSaveInstanceState");

        outState.putString(STATE_CURRENT_CATEGORY_TAB, mCurrentCategoryTab);
        outState.putParcelable(STATE_SCROLL, mSourcesRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putInt(STATE_CURRENT_CATEGORY_TAB_POSITION, mCurrentCategoryTabPosition);
    }

    private void setupTabLayout()
    {
        TabLayout.Tab allTab = mCategoryTabLayout.newTab().setIcon(R.drawable.clear_tab_all).setTag("All");

        mCategoryTabLayout.addTab(allTab);
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.clear_tab_general).setTag("General"));
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.clear_tab_media).setTag("Media"));
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.clear_tab_tech).setTag("Technology & Science"));
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.clear_tab_sports).setTag("Sports"));

        mCurrentCategoryTab = getString(R.string.cat_tab_all);


        mCategoryTitleTextView.setText(getString(R.string.cat_tab_all));

        TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                RealmResults<RealmSource> tabRealmResults = null;

                mCurrentCategoryTab = String.valueOf(tab.getTag());
                mCurrentCategoryTabPosition = tab.getPosition();



                if (tab.getTag().equals(getString(R.string.cat_tab_all)))
                {
                    tabRealmResults = mRealm.where(RealmSource.class).findAll();
                    mCategoryTitleTextView.setText(getString(R.string.cat_tab_all));
                }
                else if (tab.getTag().equals(getString(R.string.cat_tab_general)))
                {
                    mCategoryTitleTextView.setText(getString(R.string.cat_tab_general));
                    tabRealmResults = mRealm.where(RealmSource.class)
                            .contains("category", "general")
                            .or()
                            .contains("category", "politics")
                            .or()
                            .contains("category", "business")
                            .findAll();

                }

                else if (tab.getTag().equals(getString(R.string.cat_tab_media)))
                {
                    mCategoryTitleTextView.setText(getString(R.string.cat_tab_media));
                    tabRealmResults = mRealm.where(RealmSource.class)
                            .contains("category", "entertainment")
                            .or()
                            .contains("category", "gaming")
                            .or()
                            .contains("category", "music")
                            .findAll();
                }
                else if (tab.getTag().equals(getString(R.string.cat_tab_tech)))
                {
                    mCategoryTitleTextView.setText(getString(R.string.cat_tab_tech));
                    tabRealmResults = mRealm.where(RealmSource.class)
                            .contains("category", "technology")
                            .or()
                            .contains("category", "science-and-nature")
                            .findAll();
                }
                else if (tab.getTag().equals(getString(R.string.cat_tab_sports)))
                {
                    mCategoryTitleTextView.setText(getString(R.string.cat_tab_sports));
                    tabRealmResults = mRealm.where(RealmSource.class)
                            .contains("category", "sport")
                            .findAll();
                }

                if (mSourceAdapter != null)
                {
                    Log.d(LOG_TAG, "onTabSelected, update source adapter");
                    mSourceAdapter.resetAnimation();
                    mSourceAdapter.updateData(tabRealmResults);
                }


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
        mCategoryTabLayout.addOnTabSelectedListener(tabSelectedListener);

        mCurrentCategoryTabPosition = 0;
        allTab.select();


    }


}
