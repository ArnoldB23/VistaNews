package roca.bajet.com.vistanews.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Arnold on 8/28/2017.
 */

public class Article {

    @SerializedName("author")
    @Expose
    public String author;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("urlToImage")
    @Expose
    public String urlToImage;
    @SerializedName("publishedAt")
    @Expose
    public String publishedAt;

}