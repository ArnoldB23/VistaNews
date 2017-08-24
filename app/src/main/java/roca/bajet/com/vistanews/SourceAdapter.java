package roca.bajet.com.vistanews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public SourceAdapter(Context c, @Nullable OrderedRealmCollection<RealmSource> data, boolean autoUpdate)
    {
        super(data,autoUpdate);

        mContext = c;
    }



    @Override
    public SourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(mContext).inflate(R.layout.source_item, parent, false);
        SourceViewHolder vh = new SourceViewHolder(itemView);
        Log.d(LOG_TAG, "onCreateViewHolder ");
        return vh;
    }

    @Override
    public void onBindViewHolder(SourceViewHolder holder, int position) {

        RealmSource realmSource = getItem(position);

        Drawable placeholder = ApiUtils.getCategoryDrawableFromRealSource(mContext, realmSource);

        String sourceUrl = ApiUtils.getLogosUrl(realmSource.url);

        Log.d(LOG_TAG, "onBindViewHolder, drawable : " + placeholder + ", url : " + sourceUrl);
        //Glide.with(mContext).load(sourceUrl).into(holder.mSourceImageView).onLoadFailed(placeholder);
        GlideApp.with(mContext).load(sourceUrl).error(placeholder).into(holder.mSourceImageView);

        holder.mSourceTitleTextView.setText(realmSource.name);
    }


    public static class SourceViewHolder extends RecyclerView.ViewHolder {

        public TextView mSourceTitleTextView;
        public ImageView mSourceImageView;
        public SourceViewHolder(View v) {
            super(v);
            mSourceTitleTextView = (TextView) v.findViewById(R.id.source_item_textview);
            mSourceImageView = (ImageView) v.findViewById(R.id.source_item_imageview);
        }
    }
}
