package roca.bajet.com.vistanews;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import io.realm.RealmResults;
import roca.bajet.com.vistanews.data.RealmSource;

/**
 * Created by Arnold on 9/19/2017.
 */

public class ArticleListPageAdapter extends FragmentStatePagerAdapter {

    private RealmResults<RealmSource> mSourceList;

    public ArticleListPageAdapter(FragmentManager fm, RealmResults<RealmSource> rs) {
        super(fm);

        mSourceList = rs;
    }

    public RealmSource getRealmSourceFromPosition(int position)
    {
        return mSourceList.get(position);
    }

    @Override
    public Fragment getItem(int position) {

        RealmSource source = mSourceList.get(position);
        Fragment f = ArticleListFragment.newInstance(source.id);
        return f;
    }

    @Override
    public int getCount() {
        return mSourceList.size();
    }
    public RealmResults<RealmSource> swapRealmList(RealmResults<RealmSource> newList)
    {
        if (mSourceList == newList)
        {
            return null;
        }

        RealmResults<RealmSource> oldList = mSourceList;

        mSourceList = newList;
        notifyDataSetChanged();

        return oldList;
    }


}
