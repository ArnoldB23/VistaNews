package roca.bajet.com.vistanews;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.Realm;
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
    private RecyclerView mCategoryRecyclerView;
    private RecyclerView mSourcesRecyclerView;
    private NewsService mNewsService;
    private static final String LOG_TAG = "MainActivity";
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get array resources for category titles and drawables
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
        mCategoryRecyclerView.setLayoutManager(lm);


        mCategoryRecyclerView.setAdapter(mCategoryAdapter);

        SnapHelper snapHelper = new StartSnapHelper();
        snapHelper.attachToRecyclerView(mCategoryRecyclerView);

        Realm.init(this);

        mNewsService = ApiUtils.getNewsService();


        mSourcesRecyclerView = (RecyclerView) findViewById(R.id.source_recyclerview);
        GridLayoutManager gm = new GridLayoutManager(this, 4);
        mSourcesRecyclerView.setLayoutManager(gm);



        mRealm = Realm.getDefaultInstance();

        RealmResults<RealmSource> results = mRealm.where(RealmSource.class).findAllAsync();
        mSourceAdapter = new SourceAdapter(MainActivity.this, results, true);

        mSourcesRecyclerView.setAdapter(mSourceAdapter);

        //mSourceAdapter.updateData(realm.where(RealmSource.class).findAllAsync().createSnapshot());




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

    public class CategoryAdapter extends RecyclerView.Adapter<CatViewHolder> {
        private String[] mCatTitles;
        private Drawable[] mCatDrawables;


        public CategoryAdapter(String[] titles, Drawable[] resourceIds) {
            mCatTitles = titles;
            mCatDrawables = resourceIds;
        }

        @Override
        public CatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.category_item, parent, false);
            CatViewHolder vh = new CatViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(CatViewHolder holder, int position) {
            holder.mCatTextView.setText(mCatTitles[position]);

            holder.mCatImageView.setImageDrawable(mCatDrawables[position]);

        }

        @Override
        public int getItemCount() {
            return mCatTitles.length;
        }
    }

    public static class CatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView mCatTextView;
        public ImageView mCatImageView;
        public CatViewHolder(View v) {
            super(v);
            mCatTextView = (TextView) v.findViewById(R.id.category_title_textview);
            mCatImageView = (ImageView) v.findViewById(R.id.category_imageview);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
