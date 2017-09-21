package roca.bajet.com.vistanews.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import java.net.URI;
import java.net.URISyntaxException;

import io.realm.Realm;
import io.realm.RealmResults;
import roca.bajet.com.vistanews.R;

import static roca.bajet.com.vistanews.data.NewsService.CAT_BUSINESS;
import static roca.bajet.com.vistanews.data.NewsService.CAT_ENTERTAINMENT;
import static roca.bajet.com.vistanews.data.NewsService.CAT_GAMING;
import static roca.bajet.com.vistanews.data.NewsService.CAT_GENERAL;
import static roca.bajet.com.vistanews.data.NewsService.CAT_MUSIC;
import static roca.bajet.com.vistanews.data.NewsService.CAT_POLITICS;
import static roca.bajet.com.vistanews.data.NewsService.CAT_SCIENCE;
import static roca.bajet.com.vistanews.data.NewsService.CAT_SPORTS;
import static roca.bajet.com.vistanews.data.NewsService.CAT_TECH;

/**
 * Created by Arnold on 8/22/2017.
 */

public class ApiUtils {
    public static final String NEWS_URL = "https://newsapi.org/";
    public static final String LOGOS_URL = "https://logo.clearbit.com/";

    public static NewsService getNewsService() {
        return RetrofitClient.getClient(NEWS_URL).create(NewsService.class);
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static String getLogosUrl(String companyUrl)
    {
        String logosUrl = null;

        try{
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("https")
                    .authority("logo.clearbit.com")
                    .appendPath(getDomainName(companyUrl))
                    .appendQueryParameter("size", "288");


            logosUrl = uriBuilder.build().toString();

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return logosUrl;

    }

    public static RealmResults<RealmSource> getRealmResultsFromTab(Realm realm, String tabStr)
    {
        RealmResults<RealmSource> tabRealmResults = null;

        if (tabStr.equals("All"))
        {
            tabRealmResults = realm.where(RealmSource.class).findAll();
        }
        else if (tabStr.equals("General"))
        {
            tabRealmResults = realm.where(RealmSource.class)
                    .contains("category", "general")
                    .or()
                    .contains("category", "politics")
                    .or()
                    .contains("category", "business")
                    .findAll();

        }

        else if (tabStr.equals("Media"))
        {
            tabRealmResults = realm.where(RealmSource.class)
                    .contains("category", "entertainment")
                    .or()
                    .contains("category", "gaming")
                    .or()
                    .contains("category", "music")
                    .findAll();
        }
        else if (tabStr.equals("Technology & Science"))
        {
            tabRealmResults = realm.where(RealmSource.class)
                    .contains("category", "technology")
                    .or()
                    .contains("category", "science-and-nature")
                    .findAll();
        }
        else if (tabStr.equals("Sports"))
        {
            tabRealmResults = realm.where(RealmSource.class)
                    .contains("category", "sport")
                    .findAll();
        }

        return tabRealmResults;
    }

    public static Drawable getCategoryDrawableFromRealSource(Context c, RealmSource source)
    {
        Drawable catDrawable = null;

        switch(source.category)
        {
            case CAT_BUSINESS:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_business);
                break;
            case CAT_ENTERTAINMENT:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_entertainment);
                break;
            case CAT_GAMING:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_gaming);
                break;
            case CAT_GENERAL:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_general);
                break;
            case CAT_MUSIC:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_music);
                break;
            case CAT_POLITICS:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_politics);
                break;
            case CAT_SCIENCE:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_science);
                break;
            case CAT_SPORTS:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_sports);
                break;
            case CAT_TECH:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_tech);
                break;
            default:
                catDrawable = ContextCompat.getDrawable(c, R.drawable.cat_all);
        }

        return catDrawable;
    }


}
