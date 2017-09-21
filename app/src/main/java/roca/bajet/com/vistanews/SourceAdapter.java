package roca.bajet.com.vistanews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import roca.bajet.com.vistanews.data.ApiUtils;
import roca.bajet.com.vistanews.data.RealmSource;

/**
 * Created by Arnold on 8/22/2017.
 */

public class SourceAdapter extends RealmRecyclerViewAdapter<RealmSource, SourceAdapter.SourceViewHolder> {

    private Context mContext;
    private final String LOG_TAG = "SourceAdapter";
    private int lastPosition = -1;
    private float offset = 300f;
    public int countLimit = 20;
    private int count = 0;

    public OnSourceViewHolderClick mOnSourceViewHolderClick;


    public interface OnSourceViewHolderClick{
        void onClick(SourceViewHolder viewHolder);
    }


    public SourceAdapter(Context c, @Nullable OrderedRealmCollection<RealmSource> data, boolean autoUpdate)
    {
        super(data,autoUpdate);

        mContext = c;
    }



    @Override
    public SourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(mContext).inflate(R.layout.source_item, parent, false);
        SourceViewHolder vh = new SourceViewHolder(itemView);
        //Log.d(LOG_TAG, "onCreateViewHolder ");
        return vh;
    }

    @Override
    public void onBindViewHolder(final SourceViewHolder holder, int position) {

        final RealmSource realmSource = getItem(position);
        final Drawable placeholder = ApiUtils.getCategoryDrawableFromRealSource(mContext, realmSource);

        String sourceUrl = ApiUtils.getLogosUrl(realmSource.url);

        //Log.d(LOG_TAG, "onBindViewHolder, drawable : " + placeholder + ", url : " + sourceUrl);
        Glide.with(mContext).load(sourceUrl).into(holder.mSourceImageView).onLoadFailed(placeholder);

        /*
        GlideApp.with(mContext).load(sourceUrl).error(placeholder).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {

                //Log.d(LOG_TAG, "w : " + resource.getIntrinsicWidth() + ", h : " + resource.getIntrinsicHeight());

                //ViewCompat.setTransitionName(holder.mSourceImageView, realmSource.id);

                if (resource.getIntrinsicWidth() >= 288 && resource.getIntrinsicHeight() >= 288)
                {
                    holder.mSourceImageView.setImageDrawable(resource);
                }
                else{
                    holder.mSourceImageView.setImageDrawable(placeholder);
                }
            }
        });
        */

        ViewCompat.setTransitionName(holder.mSourceImageView, realmSource.id);
        holder.mSourceTitleTextView.setText(realmSource.name);

        setAnimation(holder.itemView, position);
    }

    public void resetAnimation()
    {
        lastPosition = -1;
        offset = 300f;
        count = 0;
    }

    private void setAnimation(View view, int position)
    {
        if (position > lastPosition && count < countLimit)
        {
            //Log.d(LOG_TAG, "animate offeset: " + offset + ", pos: " + position);
            Interpolator interpolator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                interpolator = AnimationUtils.loadInterpolator(mContext, android.R.interpolator.linear_out_slow_in);
            }else{
                interpolator = AnimationUtils.loadInterpolator(mContext, android.R.interpolator.linear);
            }

            view.setVisibility(View.VISIBLE);
            view.setTranslationY(offset);
            view.setAlpha(0.85f);

            view.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setInterpolator(interpolator)
                    .setDuration(500L)
                    .start();

            lastPosition = position;
            count++;

            if ((position + 1) % 4 == 0)
            {
                offset = 1.5f * offset;

            }


        }
    }


    public class SourceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mSourceTitleTextView;
        public ImageView mSourceImageView;
        public CardView mSourceCardView;


        public SourceViewHolder(View v) {
            super(v);
            mSourceTitleTextView = (TextView) v.findViewById(R.id.source_item_textview);
            mSourceImageView = (ImageView) v.findViewById(R.id.source_item_imageview);
            mSourceCardView = (CardView) v.findViewById(R.id.source_item_cardview);

            v.setClickable(true);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            /*
            Intent i = new Intent(mContext, ArticleListActivity.class);

            RealmSource newsSource = getItem(getAdapterPosition());
            i.putExtra(ArticleListActivity.EXTRA_SOURCE_ID, newsSource.id);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)mContext, mSourceImageView, newsSource.id);
            mContext.startActivity(i, options.toBundle());

            */

            if (mOnSourceViewHolderClick != null)
            {
                mOnSourceViewHolderClick.onClick(this);
            }
        }
    }
}
