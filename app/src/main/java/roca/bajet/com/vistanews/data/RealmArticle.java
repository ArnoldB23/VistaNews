package roca.bajet.com.vistanews.data;

import io.realm.RealmObject;

/**
 * Created by Arnold on 8/30/2017.
 */

public class RealmArticle extends RealmObject {

    public String author;
    public String title;
    public String description;
    public String url;
    public String urlToImage;
    public String publishedAt;
    public int topRank;
    public int latestRank;
    public int popularRank;
    public String sourceId;
}
