package roca.bajet.com.vistanews.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Arnold on 8/28/2017.
 */

public class GetArticleResponse {

    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("source")
    @Expose
    public String source;
    @SerializedName("sortBy")
    @Expose
    public String sortBy;
    @SerializedName("articles")
    @Expose
    public List<Article> articles = null;

}
