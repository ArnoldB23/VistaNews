package roca.bajet.com.vistanews.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Arnold on 8/22/2017.
 */

public interface NewsService {

    //business, entertainment, gaming, general, music, politics, science-and-nature, sport, technology
    String CAT_BUSINESS = "business";
    String CAT_ENTERTAINMENT = "entertainment";
    String CAT_GAMING = "gaming";
    String CAT_GENERAL = "general";
    String CAT_MUSIC = "music";
    String CAT_POLITICS = "politics";
    String CAT_SCIENCE = "science-and-nature";
    String CAT_SPORTS = "sport";
    String CAT_TECH = "technology";

    @GET("v1/sources")
    Call<GetSourceResponse> getSource(
        @Query("language") String language,
        @Query("country") String country,
        @Query("category") String category
    );

    @GET("v1/articles")
    Call<GetArticleResponse> getArticle(
        @Query("source") String source,
        @Query("apiKey") String apiKey,
        @Query("sortBy") String sortBy
    );
}
