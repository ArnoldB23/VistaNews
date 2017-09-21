package roca.bajet.com.vistanews;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

    private CategoryAdapter mCategoryAdapter;
    private SourceAdapter mSourceAdapter;
    private RecyclerItemClickListener mCategoryItemClickListener;
    private RecyclerView mCategoryRecyclerView;
    private RecyclerView mSourcesRecyclerView;
    private TabLayout mCategoryTabLayout;
    private TextView mCategoryTitleTextView;

    private NewsService mNewsService;
    private AppBarLayout mMainAppBarLayout;
    private static final String LOG_TAG = "MainActivity";
    private Realm mRealm;
    private String mCurrentCategoryTab;

    private String [] categoryParamNames = {
            "",
            "business",
            "entertainment",
            "general",
            "gaming",
            "music",
            "politics",
            "science-and-nature",
            "sport",
            "technology"
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);

        mMainAppBarLayout = (AppBarLayout)findViewById(R.id.main_appbarlayout);

        mCategoryTabLayout = (TabLayout)findViewById(R.id.category_tablayout);
        mCategoryTitleTextView = (TextView)findViewById(R.id.category_title_textview);
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.cat_tab_all).setTag("All"));
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.cat_tab_general).setTag("General"));
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.cat_tab_media).setTag("Media"));
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.cat_tab_tech).setTag("Technology & Science"));
        mCategoryTabLayout.addTab(mCategoryTabLayout.newTab().setIcon(R.drawable.cat_tab_sports).setTag("Sports"));


        mCategoryTitleTextView.setText("All");

        mCategoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                RealmResults<RealmSource> tabRealmResults = null;

                mCurrentCategoryTab = String.valueOf(tab.getTag());

                if (tab.getTag().equals("All"))
                {
                    tabRealmResults = mRealm.where(RealmSource.class).findAll();
                    mCategoryTitleTextView.setText("All");
                }
                else if (tab.getTag().equals("General"))
                {
                    mCategoryTitleTextView.setText("General");
                    tabRealmResults = mRealm.where(RealmSource.class)
                            .contains("category", "general")
                            .or()
                            .contains("category", "politics")
                            .or()
                            .contains("category", "business")
                            .findAll();

                }

                else if (tab.getTag().equals("Media"))
                {
                    mCategoryTitleTextView.setText("Media");
                    tabRealmResults = mRealm.where(RealmSource.class)
                            .contains("category", "entertainment")
                            .or()
                            .contains("category", "gaming")
                            .or()
                            .contains("category", "music")
                            .findAll();
                }
                else if (tab.getTag().equals("Technology & Science"))
                {
                    mCategoryTitleTextView.setText("Technology & Science");
                    tabRealmResults = mRealm.where(RealmSource.class)
                            .contains("category", "technology")
                            .or()
                            .contains("category", "science-and-nature")
                            .findAll();
                }
                else if (tab.getTag().equals("Sports"))
                {
                    mCategoryTitleTextView.setText("Sports");
                    tabRealmResults = mRealm.where(RealmSource.class)
                            .contains("category", "sport")
                            .findAll();
                }

                mSourceAdapter.resetAnimation();
                mSourceAdapter.updateData(tabRealmResults);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        //Get array resources for category titles and drawables
        /*


        TypedArray typedTitleArray = getResources().obtainTypedArray(R.array.category_title_array);
        String [] catTitles = new String[typedTitleArray.length()];
        for (int i = 0; i < typedTitleArray.length(); i++)
        {
            catTitles[i] = typedTitleArray.getString(i);
        }
        typedTitleArray.recycle();
        TypedArray typedDrawableArray = getResources().obtainTypedArray(R.array.category_drawable_array);
        Drawable [] catDrawables = new Drawable[typedDrawableArray.length()];
        for (int i = 0; i < typedDrawableArray.length(); i++)
        {
            catDrawables[i] = typedDrawableArray.getDrawable(i);
        }
        typedDrawableArray.recycle();
        mCategoryAdapter = new CategoryAdapter(catTitles, catDrawables);
        mCategoryRecyclerView = (RecyclerView) findViewById(R.id.category_recyclerview);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        mCategoryRecyclerView.setHasFixedSize(true);
        mCategoryRecyclerView.setLayoutManager(lm);
        mCategoryItemClickListener = new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {

            private int mSelectedPosition = -1;

            @Override
            public void onItemClick(View view, int position) {

                if (mSelectedPosition > -1 || mSelectedPosition != position)
                {

                    CatViewHolder previousVH = (CatViewHolder)mCategoryRecyclerView.findViewHolderForAdapterPosition(mSelectedPosition);

                    if (previousVH != null)
                    {
                        previousVH.mCatImageView.setSelected(false);
                    }

                }

                mSelectedPosition = position;
                RealmResults<RealmSource> results = mRealm.where(RealmSource.class).contains("category", categoryParamNames[position]).findAll();
                mSourceAdapter.resetAnimation();
                mSourceAdapter.updateData(results);


            }
        });
        mCategoryRecyclerView.addOnItemTouchListener(mCategoryItemClickListener);
        mCategoryRecyclerView.setAdapter(mCategoryAdapter);
        SnapHelper snapHelper = new StartSnapHelper();
        snapHelper.attachToRecyclerView(mCategoryRecyclerView);
        */




        Log.d(LOG_TAG, "onCreate");

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();


        mNewsService = ApiUtils.getNewsService();

        mCurrentCategoryTab = "All";
        mSourcesRecyclerView = (RecyclerView) findViewById(R.id.source_recyclerview);
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
                }

            }
        });



        mSourcesRecyclerView.setHasFixedSize(true);
        mSourcesRecyclerView.setLayoutManager(gm);



        mRealm = Realm.getInstance(config);



        RealmResults<RealmSource> results = mRealm.where(RealmSource.class).findAllAsync();
        mSourceAdapter = new SourceAdapter(MainActivity.this, results, true);
        int offset = getResources().getDimensionPixelOffset(R.dimen.item_offset);




        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(offset);
        mSourcesRecyclerView.addItemDecoration(itemDecoration);

        mSourcesRecyclerView.setAdapter(mSourceAdapter);




        mSourcesRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Intent i = new Intent(MainActivity.this, ArticleListActivity.class);

                //ImageView mSourceImageView = (ImageView) view.findViewById(R.id.source_item_imageview);



                RealmSource newsSource = mSourceAdapter.getItem(position);
                i.putExtra(ArticleListActivity.EXTRA_INITIAL_SOURCE_ID, newsSource.id);
                i.putExtra(ArticleListActivity.EXTRA_SOURCE_CATEGORY, mCurrentCategoryTab);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    /*
                    int cx = mSourcesRecyclerView.getWidth() / 2;
                    int cy = mSourcesRecyclerView.getHeight() / 2;
                    int vx = (int)view.getX() + (view.getWidth()/2);
                    int vy = (int)view.getY() + (view.getWidth()/2);

                    float initialRadius = (float) Math.hypot(cx, cy);
                    Animator animCircle = ViewAnimationUtils.createCircularReveal(mSourcesRecyclerView, vx, vy, initialRadius, 0);
                    animCircle.setDuration(300);


                    AnimationSet animSet = new AnimationSet(false);

                    animCircle.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mSourcesRecyclerView.setVisibility(View.INVISIBLE);
                            startActivity(i);
                        }
                    });

                    animCircle.start();
                    */

                    ImageView sourceImageView = (ImageView)view.findViewById(R.id.source_item_imageview);
                    //CardView sourceCardView = (CardView)view.findViewById(R.id.source_item_cardview);

                    //Pair<View, String> p1 = Pair.create((View)sourceImageView, newsSource.id);
                    //Pair<View, String> p2 = Pair.create((View)sourceCardView, newsSource.name);


                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, sourceImageView, newsSource.id);
                    //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, p1, p2);
                    startActivityForResult(i, ArticleListActivity.RESULT_CODE, options.toBundle());

                }else{
                    startActivity(i);
                }

            }
        }));



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

                                    //RealmSource managedRealmSource = realm.copyToRealmOrUpdate(realmSource);
                                    realm.insertOrUpdate(realmSource);
                                    //realm.copyToRealmOrUpdate(realmSource);
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


        //CardView cv = (CardView) findViewById(R.id.source_item_cardview);
        //TextView title = (TextView) findViewById(R.id.source_item_textview);
        //cv.setCardBackgroundColor(getResources().getColor(R.color.colorTitleA));
        //title.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf"));

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



    public class CategoryAdapter extends RecyclerView.Adapter<CatViewHolder> {
        private String[] mCatTitles;
        private Drawable[] mCatDrawables;
        public int mSelectedPosition = 0;


        public CategoryAdapter(String[] titles, Drawable[] resourceIds) {
            mCatTitles = titles;
            mCatDrawables = resourceIds;

        }

        @Override
        public CatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.category_item, parent, false);
            CatViewHolder vh = new CatViewHolder(view);



            vh.setOnCatViewHolderClick(new OnCatViewHolderClick() {
                @Override
                public void onClick(int pos) {
                    mSelectedPosition = pos;
                    notifyDataSetChanged();
                }
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(CatViewHolder holder, int position) {
            holder.mCatTextView.setText(mCatTitles[position]);
            holder.mCatImageView.setImageDrawable(mCatDrawables[position]);


            if (mSelectedPosition == position)
            {
                holder.mCatImageView.setSelected(true);
                Log.d(LOG_TAG, "onBindView selected " + position);

            }
            else{
                holder.mCatImageView.setSelected(false);
            }
        }

        @Override
        public int getItemCount() {
            return mCatTitles.length;
        }
    }

    public interface OnCatViewHolderClick
    {
        void onClick(int pos);
    }


    public class CatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView mCatTextView;
        public ImageView mCatImageView;
        public OnCatViewHolderClick mOnCatViewHolderClick;

        public CatViewHolder(View v) {

            super(v);

            v.setClickable(true);
            v.setOnClickListener(this);
            v.setSelected(false);

            mCatTextView = (TextView) v.findViewById(R.id.category_title_textview);
            mCatImageView = (ImageView) v.findViewById(R.id.category_imageview);



        }

        public void setOnCatViewHolderClick(OnCatViewHolderClick cb){
            mOnCatViewHolderClick = cb;
        }

        @Override
        public void onClick(View v) {

            Log.d(LOG_TAG, "onClick " + getLayoutPosition());
            mCatImageView.setSelected(true);

            if (mOnCatViewHolderClick != null)
            {
                mOnCatViewHolderClick.onClick(getLayoutPosition());
            }

        }
    }
}
